package org.mosh4j.core;

import org.mosh4j.core.datagram.DatagramChannel;
import org.mosh4j.core.datagram.DatagramPayload;
import org.mosh4j.core.datagram.FragmentCodec;
import org.mosh4j.core.datagram.ReplayWindow;
import org.mosh4j.core.datagram.RttEstimator;
import org.mosh4j.core.datagram.SspDatagramCodec;
import org.mosh4j.core.datagram.UdpDatagramChannel;
import org.mosh4j.crypto.MoshKey;
import org.mosh4j.crypto.SspCipher;
import org.mosh4j.protocol.transport.Transportinstruction;
import org.mosh4j.terminal.Framebuffer;
import org.mosh4j.terminal.SimpleFramebuffer;
import org.mosh4j.transport.TransportInstruction;
import org.mosh4j.transport.TransportReceiver;
import org.mosh4j.transport.TransportSender;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mosh server session: accepts a client (roaming), receives user input, sends host output.
 * Updated to use the fragment layer and proper SSP timing.
 */
public class MoshServerSession {

    private static final Logger LOG = Logger.getLogger(MoshServerSession.class.getName());

    private final DatagramChannel channel;
    private final SspDatagramCodec codec;
    private final Framebuffer framebuffer;
    private final TransportSender outputSender;
    private final TransportReceiver inputReceiver;
    private final FragmentCodec fragmentDecoder;
    private final ReplayWindow replayWindow = new ReplayWindow();
    private final AtomicReference<InetSocketAddress> clientAddress = new AtomicReference<>();
    private final AtomicLong sendSeq = new AtomicLong(0);
    private final AtomicLong instructionId = new AtomicLong(0);
    private final RttEstimator rtt = new RttEstimator();
    private volatile boolean running = true;
    private volatile int lastTimestampReceived = 0;

    public MoshServerSession(int port, MoshKey key, int width, int height) throws Exception {
        DatagramSocket socket = new DatagramSocket(port);
        this.channel = new UdpDatagramChannel(socket);
        SspCipher cipher = new SspCipher(key);
        this.codec = new SspDatagramCodec(cipher);
        this.framebuffer = new SimpleFramebuffer(width, height);
        this.fragmentDecoder = new FragmentCodec();

        this.inputReceiver = new TransportReceiver(
                (base, diff) -> diff,
                state -> {});

        this.outputSender = new TransportSender(
                () -> ((SimpleFramebuffer) framebuffer).toStateBytes(),
                () -> ((SimpleFramebuffer) framebuffer).toStateBytes());
    }

    /**
     * Feed host output (terminal bytes from PTY) into the framebuffer and send an update to the client.
     */
    public void feedHostOutput(byte[] hostBytes) {
        if (hostBytes == null || hostBytes.length == 0) return;
        framebuffer.feedHostBytes(hostBytes);
        InetSocketAddress client = clientAddress.get();
        if (client == null) return;
        long seq = sendSeq.getAndIncrement();
        int ts = (int) (System.currentTimeMillis() & 0xFFFF);
        Transportinstruction.Instruction inst = outputSender.nextInstruction(
                outputSender.getAssumedReceiverState(rtt.getSrttMs()));
        if (inst == null) return;
        byte[] protobufBytes = TransportInstruction.toBytes(inst);
        long fragId = instructionId.getAndIncrement();
        byte[] fragmentPayload = FragmentCodec.encodeSingle(fragId, protobufBytes);
        byte[] packet = codec.encode(true, seq, ts, lastTimestampReceived, fragmentPayload);
        channel.send(client, packet);
    }

    /**
     * Receive one datagram. Updates client address (roaming) and processes user input.
     */
    public boolean receiveOnce() {
        DatagramChannel.ReceiveResult result = channel.receive();
        if (result == null || !running) return false;
        try {
            DatagramPayload payload = codec.decode(result.packet());
            if (!payload.isServerToClient()) {
                // Drop replays/stale datagrams before updating the roaming address,
                // so a captured old packet cannot redirect the session.
                if (!replayWindow.accept(payload.getSeq())) {
                    LOG.log(Level.FINE, "Dropping replayed/stale client datagram");
                    return true;
                }
                clientAddress.set(result.source());
                int receivedTs = payload.getTimestamp();
                lastTimestampReceived = receivedTs;

                int echoedTs = payload.getTimestampReply();
                if (echoedTs != 0) {
                    int now = (int) (System.currentTimeMillis() & 0xFFFF);
                    int rttSample = (now - echoedTs) & 0xFFFF;
                    if (rttSample > 0 && rttSample <= 5000) {
                        rtt.update(rttSample);
                    }
                }

                byte[] fragmentData = payload.getPayload();
                if (fragmentData != null && fragmentData.length > 0) {
                    byte[] protobufBytes = fragmentDecoder.decode(fragmentData);
                    if (protobufBytes != null) {
                        Transportinstruction.Instruction inst = TransportInstruction.parse(protobufBytes);
                        if (!TransportInstruction.isProtocolVersionValid(inst)) {
                            LOG.log(Level.FINE, "Ignoring instruction with invalid protocol_version");
                            return true;
                        }
                        inputReceiver.receive(inst);
                        if (inst.hasAckNum()) {
                            outputSender.setKnownReceiverState(inst.getAckNum());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Decode or process failed for datagram", e);
        }
        return true;
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

    public void close() {
        running = false;
        channel.close();
    }

    public boolean isRunning() {
        return running;
    }
}
