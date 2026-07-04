package org.mosh4j.core.datagram;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP implementation of DatagramChannel.
 */
public final class UdpDatagramChannel implements DatagramChannel {

    private final DatagramSocket socket;
    private final AtomicBoolean open = new AtomicBoolean(true);

    public UdpDatagramChannel(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Configure UDP receive timeout in milliseconds.
     * A timeout converts blocking receive() into a periodic poll loop.
     */
    public void setReceiveTimeoutMillis(int timeoutMillis) {
        try {
            socket.setSoTimeout(Math.max(0, timeoutMillis));
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure receive timeout", e);
        }
    }

    @Override
    public void send(InetSocketAddress target, byte[] data) {
        if (!open.get() || data == null) return;
        try {
            DatagramPacket p = new DatagramPacket(data, data.length, target.getAddress(), target.getPort());
            socket.send(p);
        } catch (Exception e) {
            throw new RuntimeException("Send failed", e);
        }
    }

    @Override
    public ReceiveResult receive() {
        if (!open.get()) return null;
        byte[] buf = new byte[65507];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(p);
            SocketAddress sa = p.getSocketAddress();
            InetSocketAddress source = sa instanceof InetSocketAddress ? (InetSocketAddress) sa : null;
            if (source == null) return null;
            byte[] packet = new byte[p.getLength()];
            System.arraycopy(p.getData(), p.getOffset(), packet, 0, p.getLength());
            return new ReceiveResult(source, packet);
        } catch (SocketTimeoutException timeout) {
            return null;
        } catch (Exception e) {
            if (open.get()) throw new RuntimeException("Receive failed", e);
            return null;
        }
    }

    @Override
    public void close() {
        if (open.compareAndSet(true, false)) {
            socket.close();
        }
    }

    @Override
    public boolean isOpen() {
        return open.get() && !socket.isClosed();
    }
}
