package org.mosh4j.core;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import org.mosh4j.core.datagram.DatagramChannel;
import org.mosh4j.core.datagram.DatagramPayload;
import org.mosh4j.core.datagram.FragmentCodec;
import org.mosh4j.core.datagram.ReplayWindow;
import org.mosh4j.core.datagram.SspDatagramCodec;
import org.mosh4j.core.datagram.UdpDatagramChannel;
import org.mosh4j.crypto.MoshKey;
import org.mosh4j.crypto.SspCipher;
import org.mosh4j.protocol.client.Userinput;
import org.mosh4j.protocol.host.Hostinput;
import org.mosh4j.protocol.transport.Transportinstruction;
import org.mosh4j.terminal.Framebuffer;
import org.mosh4j.terminal.SimpleFramebuffer;
import org.mosh4j.transport.TransportInstruction;
import org.mosh4j.transport.TransportReceiver;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mosh client session: connects to a mosh-server, sends user input, receives
 * and displays host output. Uses the native C++ mosh wire format:
 * <ul>
 *   <li>12-byte OCB nonce (4 zero prefix + 8 wire bytes)</li>
 *   <li>Fragment layer with zlib compression</li>
 *   <li>ClientBuffers.UserMessage for sending keystrokes</li>
 *   <li>HostBuffers.HostMessage for receiving terminal output</li>
 *   <li>protocol_version=2, random chaff bytes</li>
 * </ul>
 */
public class MoshClientSession {
    private static final Logger LOG = Logger.getLogger(MoshClientSession.class.getName());
    private static final int DEFAULT_UDP_RECEIVE_TIMEOUT_MS = 250;
    private static final boolean DEBUG = Boolean.parseBoolean(System.getenv("KORTTY_MOSH_DEBUG"));

    private final InetSocketAddress serverAddress;
    private final DatagramChannel channel;
    private final SspDatagramCodec codec;
    private final Framebuffer framebuffer;
    private final TransportReceiver outputReceiver;
    private final FragmentCodec fragmentDecoder;
    private final ReplayWindow replayWindow = new ReplayWindow();
    private final AtomicLong sendSeq = new AtomicLong(0);
    private final AtomicLong clientStateSeq = new AtomicLong(1);
    private final AtomicLong instructionId = new AtomicLong(0);
    private final LinkedBlockingQueue<byte[]> hostBytesQueue = new LinkedBlockingQueue<>(2048);
    private volatile boolean running = true;
    private volatile int lastTimestampReceived = 0;
    private volatile long lastReceivedServerSeq = 0;
    private volatile long lastAckedClientSeq = 0;
    private volatile long lastSentClientState = 0;

    private final ExtensionRegistry hostExtensionRegistry;

    public MoshClientSession(InetSocketAddress serverAddress, MoshKey key, int width, int height) throws Exception {
        this.serverAddress = serverAddress;
        DatagramSocket socket = new DatagramSocket();
        try {
            UdpDatagramChannel udpChannel = new UdpDatagramChannel(socket);
            udpChannel.setReceiveTimeoutMillis(DEFAULT_UDP_RECEIVE_TIMEOUT_MS);
            this.channel = udpChannel;
            SspCipher cipher = new SspCipher(key);
            this.codec = new SspDatagramCodec(cipher);
            this.framebuffer = new SimpleFramebuffer(width, height);
            this.fragmentDecoder = new FragmentCodec();

            hostExtensionRegistry = ExtensionRegistry.newInstance();
            Hostinput.registerAllExtensions(hostExtensionRegistry);

            this.outputReceiver = new TransportReceiver(
                    (base, diff) -> applyHostDiff(diff),
                    state -> {});
        } catch (Exception e) {
            socket.close();
            throw e;
        }
    }

    /**
     * Apply a diff received from the server. The diff contains a serialized
     * HostBuffers.HostMessage protobuf with HostBytes, ResizeMessage, EchoAck.
     */
    private byte[] applyHostDiff(byte[] diff) {
        if (diff == null || diff.length == 0) {
            return ((SimpleFramebuffer) framebuffer).toStateBytes();
        }
        try {
            Hostinput.HostMessage hostMsg = Hostinput.HostMessage.parseFrom(diff, hostExtensionRegistry);
            SimpleFramebuffer buf = (SimpleFramebuffer) framebuffer;
            for (Hostinput.Instruction instr : hostMsg.getInstructionList()) {
                if (instr.hasExtension(Hostinput.hostbytes)) {
                    Hostinput.HostBytes hb = instr.getExtension(Hostinput.hostbytes);
                    if (hb.hasHoststring()) {
                        byte[] hostBytes = hb.getHoststring().toByteArray();
                        buf.feedHostBytes(hostBytes);
                        enqueueHostBytes(hostBytes);
                    }
                }
                if (instr.hasExtension(Hostinput.resize)) {
                    Hostinput.ResizeMessage rm = instr.getExtension(Hostinput.resize);
                    if (rm.hasWidth() && rm.hasHeight()) {
                        buf.resize(rm.getWidth(), rm.getHeight());
                    }
                }
            }
            return buf.toStateBytes();
        } catch (InvalidProtocolBufferException e) {
            LOG.log(Level.WARNING, "Failed to parse HostMessage protobuf", e);
            if (framebuffer instanceof SimpleFramebuffer buf) {
                return buf.toStateBytes();
            }
            return new byte[0];
        }
    }

    /**
     * Send one harmless wake-up packet so servers that wait for first client input
     * start emitting framebuffer updates immediately.
     */
    public void sendInitialWakeUp() {
        sendUserInput(new byte[]{0});
    }

    /**
     * Send user input (keystrokes) to the server. Wraps in ClientBuffers.UserMessage,
     * then in a TransportBuffers.Instruction, fragments with zlib, and sends.
     */
    public void sendUserInput(byte[] keys) {
        if (keys == null || keys.length == 0) return;

        Userinput.Keystroke keystroke = Userinput.Keystroke.newBuilder()
                .setKeys(ByteString.copyFrom(keys))
                .build();
        Userinput.Instruction clientInstr = Userinput.Instruction.newBuilder()
                .setExtension(Userinput.keystroke, keystroke)
                .build();
        Userinput.UserMessage userMsg = Userinput.UserMessage.newBuilder()
                .addInstruction(clientInstr)
                .build();
        sendWrappedUserMessage(userMsg.toByteArray());
    }

    /**
     * Send a resize notification to the server.
     */
    public void sendResize(int width, int height) {
        Userinput.ResizeMessage resizeMsg = Userinput.ResizeMessage.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .build();
        Userinput.Instruction clientInstr = Userinput.Instruction.newBuilder()
                .setExtension(Userinput.resize, resizeMsg)
                .build();
        Userinput.UserMessage userMsg = Userinput.UserMessage.newBuilder()
                .addInstruction(clientInstr)
                .build();
        sendWrappedUserMessage(userMsg.toByteArray());
    }

    private void sendWrappedUserMessage(byte[] userMsgBytes) {
        long seq = sendSeq.getAndIncrement();
        long newClientState = clientStateSeq.getAndIncrement();
        long oldClientState = Math.max(0, Math.max(lastSentClientState, lastAckedClientSeq));
        if (oldClientState >= newClientState) {
            oldClientState = Math.max(0, newClientState - 1);
        }
        long clientThrowaway = Math.max(0, lastAckedClientSeq);
        int ts = currentTimestamp();
        int tsReply = lastTimestampReceived;

        Transportinstruction.Instruction inst = TransportInstruction.create(
                oldClientState, newClientState, lastReceivedServerSeq, clientThrowaway, userMsgBytes);
        byte[] protobufBytes = TransportInstruction.toBytes(inst);

        long fragId = instructionId.getAndIncrement();
        byte[] fragmentPayload = FragmentCodec.encodeSingle(fragId, protobufBytes);

        try {
            byte[] packet = codec.encode(false, seq, ts, tsReply, fragmentPayload);
            channel.send(serverAddress, packet);
            lastSentClientState = newClientState;
            if (DEBUG) {
                LOG.log(Level.INFO, "MoshClientSession tx userinput datagramSeq={0} old={1} new={2} ack={3} throwaway={4} ts={5} tsReply={6} bytes={7}",
                        new Object[]{seq, oldClientState, newClientState, lastReceivedServerSeq, clientThrowaway, ts, tsReply, userMsgBytes.length});
            }
        } catch (RuntimeException e) {
            // Transient UDP send failures can happen while network is interrupted.
            // Keep session alive so roaming/recovery can continue.
            LOG.log(Level.FINE, "Ignoring transient send failure in userinput", e);
        }
    }

    /**
     * Receive one datagram, decrypt, reassemble fragments, decode protobuf,
     * and update the framebuffer. Call in a loop or from a thread.
     */
    public boolean receiveOnce() {
        DatagramChannel.ReceiveResult result;
        try {
            result = channel.receive();
        } catch (RuntimeException e) {
            // Transient receive failures (e.g. network down) should not terminate the loop.
            LOG.log(Level.FINE, "Ignoring transient receive failure", e);
            return false;
        }
        if (result == null || !running) return false;
        try {
            DatagramPayload payload = codec.decode(result.packet());
            if (payload.isServerToClient()) {
                if (!replayWindow.accept(payload.getSeq())) {
                    if (DEBUG) {
                        LOG.log(Level.FINE, "Dropping replayed/stale server datagram seq={0}", payload.getSeq());
                    }
                    return true;
                }
                lastTimestampReceived = payload.getTimestamp();
                byte[] fragmentData = payload.getPayload();
                if (fragmentData != null && fragmentData.length > 0) {
                    byte[] protobufBytes = fragmentDecoder.decode(fragmentData);
                    if (protobufBytes != null) {
                        Transportinstruction.Instruction inst = TransportInstruction.parse(protobufBytes);
                        if (!TransportInstruction.isProtocolVersionValid(inst)) {
                            LOG.log(Level.FINE, "Ignoring instruction with invalid protocol_version");
                            return true;
                        }
                        long ack = outputReceiver.receive(inst);
                        lastReceivedServerSeq = ack;
                        if (inst.hasAckNum()) {
                            lastAckedClientSeq = inst.getAckNum();
                        }
                        if (DEBUG) {
                            LOG.log(Level.INFO, "MoshClientSession rx server datagramSeq={0} old={1} new={2} ack={3} throwaway={4} diff={5} acceptedServerState={6}",
                                    new Object[]{
                                            payload.getSeq(),
                                            inst.hasOldNum() ? inst.getOldNum() : -1,
                                            inst.hasNewNum() ? inst.getNewNum() : -1,
                                            inst.hasAckNum() ? inst.getAckNum() : -1,
                                            inst.hasThrowawayNum() ? inst.getThrowawayNum() : -1,
                                            inst.hasDiff() ? inst.getDiff().size() : 0,
                                            ack
                                    });
                        }
                        sendAckOnly();
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Decode or process failed for datagram (auth failure or bad packet)", e);
        }
        return true;
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

    /**
     * Returns next raw host byte chunk if available, otherwise null.
     */
    public byte[] pollHostBytes() {
        return hostBytesQueue.poll();
    }

    /**
     * Waits for next raw host byte chunk up to timeout.
     *
     * @param timeoutMs timeout in milliseconds
     * @return host bytes or null if timeout elapsed
     */
    public byte[] takeHostBytes(long timeoutMs) throws InterruptedException {
        return hostBytesQueue.poll(Math.max(1, timeoutMs), TimeUnit.MILLISECONDS);
    }

    /**
     * Send a protocol-level heartbeat/ack packet without injecting user input bytes.
     * This keeps the session active during idle periods and network recovery.
     */
    public void sendHeartbeat() {
        sendAckOnly();
    }

    public void close() {
        running = false;
        channel.close();
    }

    public boolean isRunning() {
        return running;
    }

    private static int currentTimestamp() {
        return (int) (System.currentTimeMillis() & 0xFFFF);
    }

    private void enqueueHostBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        byte[] copy = bytes.clone();
        if (!hostBytesQueue.offer(copy)) {
            hostBytesQueue.poll();
            hostBytesQueue.offer(copy);
        }
    }

    private void sendAckOnly() {
        try {
            long seq = sendSeq.getAndIncrement();
            int ts = currentTimestamp();
            int tsReply = lastTimestampReceived;
            long state = Math.max(lastSentClientState, lastAckedClientSeq);
            long clientThrowaway = Math.max(0, lastAckedClientSeq);
            Transportinstruction.Instruction ackOnly = TransportInstruction.createAckOnly(
                    state, state, lastReceivedServerSeq, clientThrowaway);
            byte[] protobuf = TransportInstruction.toBytes(ackOnly);
            long fragId = instructionId.getAndIncrement();
            byte[] fragmentPayload = FragmentCodec.encodeSingle(fragId, protobuf);
            byte[] packet = codec.encode(false, seq, ts, tsReply, fragmentPayload);
            channel.send(serverAddress, packet);
            if (DEBUG) {
                LOG.log(Level.INFO, "MoshClientSession tx ack-only datagramSeq={0} old={1} new={2} ack={3} throwaway={4} ts={5} tsReply={6}",
                        new Object[]{seq, state, state, lastReceivedServerSeq, clientThrowaway, ts, tsReply});
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to send ack-only packet", e);
        }
    }
}
