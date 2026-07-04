package org.mosh4j.crypto;

import java.util.Base64;
import java.util.Objects;

/**
 * Mosh session key: 128-bit AES key decoded from the 22-character Base64 string
 * provided by mosh-server (e.g. via MOSH_KEY environment variable).
 * <p>
 * Do not log or persist this key. Keep it only in memory for the session.
 */
public final class MoshKey {

    private static final int KEY_BYTES = 16;
    private static final int BASE64_LENGTH = 22;

    private final byte[] keyBytes;

    private MoshKey(byte[] keyBytes) {
        this.keyBytes = Objects.requireNonNull(keyBytes, "keyBytes");
        if (keyBytes.length != KEY_BYTES) {
            throw new IllegalArgumentException("Key must be " + KEY_BYTES + " bytes, got " + keyBytes.length);
        }
    }

    /**
     * Decode a Mosh session key from the standard Base64 encoding (22 characters).
     *
     * @param base64Key the key string from mosh-server (e.g. MOSH_KEY)
     * @return decoded key
     * @throws IllegalArgumentException if the string is invalid or does not decode to 16 bytes
     */
    public static MoshKey fromBase64(String base64Key) {
        Objects.requireNonNull(base64Key, "base64Key");
        String normalized = base64Key.trim();
        if (normalized.length() != BASE64_LENGTH) {
            throw new IllegalArgumentException(
                "Mosh key must be " + BASE64_LENGTH + " characters, got " + normalized.length());
        }
        // Mosh uses Base64 without padding (22 chars for 16 bytes)
        int padding = (4 - normalized.length() % 4) % 4;
        String padded = padding == 0 ? normalized : normalized + "=".repeat(padding);
        byte[] decoded = Base64.getDecoder().decode(padded);
        if (decoded.length != KEY_BYTES) {
            throw new IllegalArgumentException(
                "Decoded key must be " + KEY_BYTES + " bytes, got " + decoded.length);
        }
        return new MoshKey(decoded);
    }

    /**
     * Create a key from raw 16-byte key material (e.g. for tests).
     */
    public static MoshKey fromBytes(byte[] keyBytes) {
        if (keyBytes.length != KEY_BYTES) {
            throw new IllegalArgumentException("Key must be " + KEY_BYTES + " bytes, got " + keyBytes.length);
        }
        return new MoshKey(keyBytes.clone());
    }

    /**
     * Returns a copy of the key bytes. Callers must not modify the returned array.
     */
    public byte[] getKeyBytes() {
        return keyBytes.clone();
    }
}
