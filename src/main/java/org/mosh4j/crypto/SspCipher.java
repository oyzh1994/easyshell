package org.mosh4j.crypto;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.OCBBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.AEADBadTagException;

/**
 * AES-128-OCB encrypt/decrypt for Mosh SSP datagrams.
 * The native C++ mosh uses a 12-byte OCB nonce: 4 zero bytes followed by
 * 8 bytes of direction|sequence. Only the 8 significant bytes are sent on
 * the wire; the 4-byte zero prefix is prepended locally before OCB init.
 */
public final class SspCipher {

    private static final int TAG_BITS = 128;
    private static final int OCB_NONCE_LEN = 12;

    private final OCBBlockCipher encryptCipher;
    private final OCBBlockCipher decryptCipher;
    private final KeyParameter keyParam;

    public SspCipher(MoshKey key) {
        byte[] keyBytes = key.getKeyBytes();
        this.keyParam = new KeyParameter(keyBytes);
        this.encryptCipher = new OCBBlockCipher(new AESEngine(), new AESEngine());
        this.decryptCipher = new OCBBlockCipher(new AESEngine(), new AESEngine());
    }

    /**
     * Pad a wire-format 8-byte nonce to the 12-byte OCB nonce used by native mosh:
     * {@code [0x00 0x00 0x00 0x00] + [8 bytes direction|seq]}.
     */
    private static byte[] toOcbNonce(byte[] wireNonce) {
        byte[] full = new byte[OCB_NONCE_LEN];
        System.arraycopy(wireNonce, 0, full, 4, wireNonce.length);
        return full;
    }

    public byte[] encrypt(boolean serverToClient, long seq, byte[] plaintext) {
        byte[] wireNonce = Nonce.create(serverToClient, seq);
        byte[] ocbNonce = toOcbNonce(wireNonce);
        AEADParameters params = new AEADParameters(keyParam, TAG_BITS, ocbNonce);
        encryptCipher.init(true, params);
        int outLen = encryptCipher.getOutputSize(plaintext.length);
        byte[] out = new byte[outLen];
        int len = encryptCipher.processBytes(plaintext, 0, plaintext.length, out, 0);
        try {
            len += encryptCipher.doFinal(out, len);
        } catch (org.bouncycastle.crypto.InvalidCipherTextException e) {
            throw new IllegalStateException("Encryption failed", e);
        }
        return len == out.length ? out : java.util.Arrays.copyOf(out, len);
    }

    public byte[] decrypt(byte[] wireNonce, byte[] ciphertext) throws AEADBadTagException {
        if (wireNonce == null || wireNonce.length < Nonce.length()) {
            throw new IllegalArgumentException("Nonce must be at least " + Nonce.length() + " bytes");
        }
        byte[] n = wireNonce.length == Nonce.length() ? wireNonce : java.util.Arrays.copyOf(wireNonce, Nonce.length());
        byte[] ocbNonce = toOcbNonce(n);
        AEADParameters params = new AEADParameters(keyParam, TAG_BITS, ocbNonce);
        decryptCipher.init(false, params);
        int outLen = decryptCipher.getOutputSize(ciphertext.length);
        byte[] out = new byte[outLen];
        int len = decryptCipher.processBytes(ciphertext, 0, ciphertext.length, out, 0);
        try {
            len += decryptCipher.doFinal(out, len);
        } catch (org.bouncycastle.crypto.InvalidCipherTextException e) {
            throw new AEADBadTagException("OCB authentication failed: " + e.getMessage());
        }
        return len == out.length ? out : java.util.Arrays.copyOf(out, len);
    }
}
