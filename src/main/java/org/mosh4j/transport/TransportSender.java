package org.mosh4j.transport;

import org.mosh4j.protocol.transport.Transportinstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * SSP transport sender matching the native C++ mosh implementation.
 *
 * Timing constants from src/network/networktransport-impl.h:
 * <ul>
 *   <li>SEND_INTERVAL_MIN = 20ms</li>
 *   <li>SEND_INTERVAL_MAX = 250ms</li>
 *   <li>ACK_DELAY = 100ms</li>
 *   <li>ACK_INTERVAL = 3000ms</li>
 *   <li>SHUTDOWN_RETRIES = 16</li>
 *   <li>ACTIVE_RETRY_TIMEOUT = 10000ms</li>
 * </ul>
 */
public class TransportSender {

    public static final long SEND_INTERVAL_MIN_MS = 20;
    public static final long SEND_INTERVAL_MAX_MS = 250;
    public static final long ACK_DELAY_MS = 100;
    public static final long ACK_INTERVAL_MS = 3000;
    public static final long ACTIVE_RETRY_TIMEOUT_MS = 10_000;
    public static final int SHUTDOWN_RETRIES = 16;
    public static final int MAX_PENDING_STATES = 32;

    private long nextStateNum = 1;
    private long knownReceiverState = 0;
    private final List<SentState> sentStates = new ArrayList<>();
    private final Supplier<byte[]> currentStateSupplier;
    private final Supplier<byte[]> diffSupplier;

    private long lastSendTimestampMs = 0;
    private long lastAckTimestampMs = 0;
    private long currentRetransmitTimeoutMs = 1000;
    private boolean shutdownInProgress = false;
    private int shutdownTries = 0;
    private boolean pendingDataAck = false;
    private long pendingDataAckDeadlineMs = 0;

    public TransportSender(Supplier<byte[]> currentStateSupplier, Supplier<byte[]> diffSupplier) {
        this.currentStateSupplier = currentStateSupplier;
        this.diffSupplier = diffSupplier;
    }

    public void setProtocolVersion(int version) {
        // protocol_version is now always set in TransportInstruction.create()
    }

    public void setKnownReceiverState(long ackNum) {
        if (ackNum > knownReceiverState) {
            knownReceiverState = ackNum;
            lastAckTimestampMs = System.currentTimeMillis();
            pruneSentStates(ackNum);
        }
    }

    /**
     * Called when we receive any packet from the remote (for ACK scheduling).
     */
    public void remoteHeard() {
        if (!pendingDataAck) {
            pendingDataAck = true;
            pendingDataAckDeadlineMs = System.currentTimeMillis() + ACK_DELAY_MS;
        }
    }

    /**
     * Called when we sent user input (schedule immediate send).
     */
    public void setNeedsSend() {
        lastSendTimestampMs = 0;
    }

    public long getKnownReceiverState() {
        return knownReceiverState;
    }

    /**
     * Compute the assumed receiver state based on sent states and RTT.
     * Uses optimistic assumption: if we sent a state and enough time has
     * passed for one RTT, assume the receiver has it.
     */
    public long getAssumedReceiverState(long rttMs) {
        long now = System.currentTimeMillis();
        long assumed = knownReceiverState;
        for (SentState ss : sentStates) {
            if (ss.stateNum > assumed && (now - ss.sentAtMs) >= rttMs) {
                assumed = ss.stateNum;
            }
        }
        return assumed;
    }

    /**
     * Determine if it's time to send, and what kind of send is needed.
     * Returns the number of milliseconds until next send is needed, or 0 if send now.
     */
    public long millisUntilNextSend(long rttMs) {
        long now = System.currentTimeMillis();

        if (shutdownInProgress) {
            return 0;
        }

        long sendInterval = calculateSendInterval(rttMs);
        long sinceLastSend = now - lastSendTimestampMs;

        if (pendingDataAck && now >= pendingDataAckDeadlineMs) {
            return 0;
        }

        long sinceAck = now - lastAckTimestampMs;
        if (sinceAck >= ACK_INTERVAL_MS && lastAckTimestampMs > 0) {
            return 0;
        }

        if (sinceLastSend >= sendInterval) {
            return 0;
        }

        long untilInterval = sendInterval - sinceLastSend;
        long untilAck = pendingDataAck ? Math.max(0, pendingDataAckDeadlineMs - now) : Long.MAX_VALUE;
        long untilAckInterval = lastAckTimestampMs > 0 ? Math.max(0, ACK_INTERVAL_MS - sinceAck) : Long.MAX_VALUE;

        return Math.min(untilInterval, Math.min(untilAck, untilAckInterval));
    }

    private long calculateSendInterval(long rttMs) {
        long interval = rttMs / 2;
        return Math.max(SEND_INTERVAL_MIN_MS, Math.min(SEND_INTERVAL_MAX_MS, interval));
    }

    private void pruneSentStates(long before) {
        sentStates.removeIf(n -> n.stateNum <= before);
    }

    /**
     * Produce the next instruction to send. Uses the assumed receiver state
     * for optimistic diff computation.
     */
    public Transportinstruction.Instruction nextInstruction(long assumedReceiverState) {
        byte[] current = currentStateSupplier.get();
        if (current == null && !shutdownInProgress) return null;

        long targetNum;
        byte[] diff;

        if (shutdownInProgress) {
            targetNum = -1;
            diff = null;
            shutdownTries++;
        } else {
            targetNum = nextStateNum++;
            diff = diffSupplier.get();
            if (diff == null) diff = new byte[0];
        }

        if (sentStates.size() >= MAX_PENDING_STATES) {
            assumedReceiverState = knownReceiverState;
        }

        Transportinstruction.Instruction inst = TransportInstruction.create(
                assumedReceiverState,
                targetNum,
                knownReceiverState,
                knownReceiverState,
                (diff != null && diff.length > 0) ? diff : null);

        long now = System.currentTimeMillis();
        if (!shutdownInProgress) {
            sentStates.add(new SentState(targetNum, now));
        }
        lastSendTimestampMs = now;
        pendingDataAck = false;
        return inst;
    }

    /**
     * Build instruction for a heartbeat/ack only (empty diff).
     */
    public Transportinstruction.Instruction createTrialInstruction() {
        lastSendTimestampMs = System.currentTimeMillis();
        pendingDataAck = false;
        return TransportInstruction.createAckOnly(knownReceiverState, knownReceiverState);
    }

    /**
     * Initiate graceful shutdown (new_num = -1 in the next instruction).
     */
    public void startShutdown() {
        shutdownInProgress = true;
        shutdownTries = 0;
    }

    public boolean isShutdownInProgress() {
        return shutdownInProgress;
    }

    public boolean isShutdownTimedOut() {
        return shutdownInProgress && shutdownTries >= SHUTDOWN_RETRIES;
    }

    /**
     * Check if connection is timed out (no ACK for ACTIVE_RETRY_TIMEOUT).
     */
    public boolean isConnectionTimedOut() {
        if (lastAckTimestampMs == 0) return false;
        return (System.currentTimeMillis() - lastAckTimestampMs) > ACTIVE_RETRY_TIMEOUT_MS;
    }

    private record SentState(long stateNum, long sentAtMs) {}
}
