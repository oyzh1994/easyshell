package org.mosh4j.core.datagram;

import java.util.BitSet;

/**
 * Per-direction anti-replay window for received datagram sequence numbers.
 *
 * <p>AEAD (AES-OCB) prevents forgery but not replay of previously observed
 * ciphertexts: an attacker who captures a valid datagram can resend it byte for
 * byte without knowing the session key. This window tracks the highest accepted
 * datagram sequence number plus a sliding bitmap of recently accepted sequences,
 * so duplicates and stale sequences are rejected before any state-mutating
 * processing runs, while legitimate UDP reordering and retransmission within the
 * window are still accepted.
 *
 * <p>Each session guards a single inbound direction (the client receives
 * server&rarr;client, the server receives client&rarr;server), so one instance
 * per session is sufficient. Not thread-safe; call from a single receive loop.
 */
public final class ReplayWindow {

    /** Default number of sequences tracked behind the highest accepted one. */
    public static final int DEFAULT_WINDOW_SIZE = 1024;

    private final int windowSize;
    /** bit {@code k} set means sequence {@code (highest - k)} has been accepted; {@code k} in [0, windowSize). */
    private final BitSet seen;
    private long highest = -1L;

    public ReplayWindow() {
        this(DEFAULT_WINDOW_SIZE);
    }

    public ReplayWindow(int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
        this.windowSize = windowSize;
        this.seen = new BitSet(windowSize);
    }

    /**
     * Decide whether a datagram with the given sequence number is fresh.
     *
     * @param seq 63-bit datagram sequence number from the nonce
     * @return {@code true} if the sequence is new and should be processed;
     *         {@code false} if it is a duplicate or too old (drop it)
     */
    public boolean accept(long seq) {
        if (seq < 0) {
            return false;
        }
        if (highest < 0) {
            // First datagram in this direction.
            highest = seq;
            seen.clear();
            seen.set(0);
            return true;
        }
        if (seq > highest) {
            long delta = seq - highest;
            if (delta >= windowSize) {
                seen.clear();
            } else {
                shiftForward((int) delta);
            }
            highest = seq;
            seen.set(0);
            return true;
        }
        long back = highest - seq;
        if (back >= windowSize) {
            return false; // older than the window: treat as replay
        }
        int idx = (int) back;
        if (seen.get(idx)) {
            return false; // already seen
        }
        seen.set(idx);
        return true;
    }

    /** Shift accepted-bit indices up by {@code delta} as the window advances; bits past the window drop off. */
    private void shiftForward(int delta) {
        for (int dest = windowSize - 1; dest >= delta; dest--) {
            seen.set(dest, seen.get(dest - delta));
        }
        seen.clear(0, delta);
    }

    public long highestAccepted() {
        return highest;
    }
}
