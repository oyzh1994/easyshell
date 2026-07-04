package org.mosh4j.core.datagram;

/**
 * Decoded SSP datagram payload: sequence number, timestamps, and transport payload.
 */
public final class DatagramPayload {

    private final long seq;
    private final int timestamp;
    private final int timestampReply;
    private final byte[] payload;
    private final boolean serverToClient;

    public DatagramPayload(long seq, int timestamp, int timestampReply, byte[] payload, boolean serverToClient) {
        this.seq = seq;
        this.timestamp = timestamp;
        this.timestampReply = timestampReply;
        this.payload = payload == null ? new byte[0] : payload;
        this.serverToClient = serverToClient;
    }

    public long getSeq() {
        return seq;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getTimestampReply() {
        return timestampReply;
    }

    public byte[] getPayload() {
        return payload.clone();
    }

    public boolean isServerToClient() {
        return serverToClient;
    }
}
