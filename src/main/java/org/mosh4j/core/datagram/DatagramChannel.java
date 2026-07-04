package org.mosh4j.core.datagram;

import java.net.InetSocketAddress;

/**
 * Abstraction for sending and receiving UDP datagrams (allows tests to mock).
 */
public interface DatagramChannel {

    void send(InetSocketAddress target, byte[] data);

    /**
     * Receive one datagram; blocks until one is available or the channel is closed.
     *
     * @return [0] = source address, [1] = packet bytes; null if closed
     */
    ReceiveResult receive();

    void close();

    boolean isOpen();

    record ReceiveResult(InetSocketAddress source, byte[] packet) {}
}
