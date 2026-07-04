package org.mosh4j.crypto;

/**
 * 64-bit nonce for Mosh AES-OCB: 1 bit direction (0 = client→server, 1 = server→client)
 * and 63-bit sequence number. Encoded as 8 bytes in network byte order (big-endian).
 */
public final class Nonce {

    private static final int NONCE_BYTES = 8;

    /**
     * Direction flag: false = client to server, true = server to client.
     * Stored as high bit of the nonce.
     */
    public static boolean directionServerToClient(byte[] nonce) {
        if (nonce == null || nonce.length < NONCE_BYTES) {
            throw new IllegalArgumentException("Nonce must be at least " + NONCE_BYTES + " bytes");
        }
        return (nonce[0] & 0x80) != 0;
    }

    /**
     * Build nonce bytes from direction and sequence number.
     *
     * @param serverToClient true for server→client, false for client→server
     * @param seq            63-bit sequence number (must be in range 0 .. 2^63-1)
     * @return 8-byte nonce (new array)
     */
    public static byte[] create(boolean serverToClient, long seq) {
        if (seq < 0 || seq > 0x7FFF_FFFF_FFFF_FFFFL) {
            throw new IllegalArgumentException("Sequence must be 63-bit non-negative, got " + seq);
        }
        byte[] nonce = new byte[NONCE_BYTES];
        nonce[0] = (byte) ((serverToClient ? 0x80 : 0) | (int) ((seq >> 56) & 0x7F));
        nonce[1] = (byte) (seq >> 48);
        nonce[2] = (byte) (seq >> 40);
        nonce[3] = (byte) (seq >> 32);
        nonce[4] = (byte) (seq >> 24);
        nonce[5] = (byte) (seq >> 16);
        nonce[6] = (byte) (seq >> 8);
        nonce[7] = (byte) seq;
        return nonce;
    }

    /**
     * Extract 63-bit sequence number from nonce bytes.
     */
    public static long getSequence(byte[] nonce) {
        if (nonce == null || nonce.length < NONCE_BYTES) {
            throw new IllegalArgumentException("Nonce must be at least " + NONCE_BYTES + " bytes");
        }
        long seq = (nonce[0] & 0x7FL) << 56L
            | (nonce[1] & 0xFFL) << 48L
            | (nonce[2] & 0xFFL) << 40L
            | (nonce[3] & 0xFFL) << 32L
            | (nonce[4] & 0xFFL) << 24L
            | (nonce[5] & 0xFFL) << 16L
            | (nonce[6] & 0xFFL) << 8L
            | (nonce[7] & 0xFFL);
        return seq;
    }

    /**
     * Length of nonce in bytes.
     */
    public static int length() {
        return NONCE_BYTES;
    }
}
