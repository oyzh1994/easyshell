package org.mosh4j.core.datagram;

import org.mosh4j.crypto.Nonce;
import org.mosh4j.crypto.SspCipher;

import javax.crypto.AEADBadTagException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Encode/decode SSP datagrams: 8-byte nonce (clear) + OCB ciphertext
 * (timestamp, timestamp_reply, fragment-wrapped payload).
 *
 * The payload on the wire contains a fragment header (10 bytes) followed by a
 * zlib-compressed protobuf chunk, matching the native C++ mosh wire format.
 */
public final class SspDatagramCodec {

    private static final int HEADER_PLAINTEXT_BYTES = 4; // timestamp + timestamp_reply

    private final SspCipher cipher;

    public SspDatagramCodec(SspCipher cipher) {
        this.cipher = cipher;
    }

    /**
     * Encode a datagram for sending.
     *
     * <p>{@code fragmentPayload} may be {@code null}, which is treated the same as a
     * zero-length array and produces a header-only (keepalive) datagram containing only
     * the timestamp fields. When non-null, the payload must already include the 10-byte
     * fragment header produced by {@link FragmentCodec}.
     */
    public byte[] encode(boolean serverToClient, long seq, int timestamp, int timestampReply, byte[] fragmentPayload) {
        byte[] nonce = Nonce.create(serverToClient, seq);
        int plen = fragmentPayload != null ? fragmentPayload.length : 0;
        byte[] plain = new byte[HEADER_PLAINTEXT_BYTES + plen];
        ByteBuffer buf = ByteBuffer.wrap(plain).order(ByteOrder.BIG_ENDIAN);
        buf.putShort((short) (timestamp & 0xFFFF));
        buf.putShort((short) (timestampReply & 0xFFFF));
        if (plen > 0) buf.put(fragmentPayload);
        byte[] ciphertext = cipher.encrypt(serverToClient, seq, plain);
        byte[] out = new byte[Nonce.length() + ciphertext.length];
        System.arraycopy(nonce, 0, out, 0, nonce.length);
        System.arraycopy(ciphertext, 0, out, nonce.length, ciphertext.length);
        return out;
    }

    /**
     * Decode a received datagram. The returned payload contains the raw fragment
     * data (fragment header + zlib chunk); use {@link FragmentCodec#decode} to
     * reassemble and decompress.
     */
    public DatagramPayload decode(byte[] packet) throws AEADBadTagException {
        if (packet == null || packet.length <= Nonce.length()) {
            throw new IllegalArgumentException("Packet too short");
        }
        byte[] nonce = new byte[Nonce.length()];
        System.arraycopy(packet, 0, nonce, 0, nonce.length);
        byte[] ciphertext = new byte[packet.length - nonce.length];
        System.arraycopy(packet, nonce.length, ciphertext, 0, ciphertext.length);
        byte[] plain = cipher.decrypt(nonce, ciphertext);
        if (plain.length < HEADER_PLAINTEXT_BYTES) {
            throw new IllegalArgumentException("Decrypted payload too short");
        }
        ByteBuffer buf = ByteBuffer.wrap(plain).order(ByteOrder.BIG_ENDIAN);
        int timestamp = buf.getShort() & 0xFFFF;
        int timestampReply = buf.getShort() & 0xFFFF;
        byte[] payload = new byte[plain.length - HEADER_PLAINTEXT_BYTES];
        if (payload.length > 0) buf.get(payload);
        boolean serverToClient = Nonce.directionServerToClient(nonce);
        long seq = Nonce.getSequence(nonce);
        return new DatagramPayload(seq, timestamp, timestampReply, payload, serverToClient);
    }
}
