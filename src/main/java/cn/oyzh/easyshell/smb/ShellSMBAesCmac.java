package cn.oyzh.easyshell.smb;

import com.hierynomus.security.Mac;

import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * smb aes-cmac实现
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class ShellSMBAesCmac implements Mac {

    private byte[] key;
    private byte[] buffer = new byte[0];
    private boolean initialized = false;

    @Override
    public void init(byte[] key) {
        this.key = Arrays.copyOf(key, key.length);
        this.buffer = new byte[0];
        this.initialized = true;
    }

    @Override
    public void update(byte b) {
        this.update(new byte[]{b}, 0, 1);
    }

    @Override
    public void update(byte[] data) {
        this.update(data, 0, data.length);
    }

    @Override
    public void update(byte[] data, int offset, int length) {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        byte[] newBuffer = new byte[this.buffer.length + length];
        System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
        System.arraycopy(data, offset, newBuffer, this.buffer.length, length);
        this.buffer = newBuffer;
    }

    @Override
    public byte[] doFinal() {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        try {
            byte[] result = ShellSMBUtil.calculateAesCmac(this.key, this.buffer);
            reset();
            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES-CMAC calculation failed", e);
        }
    }

    @Override
    public void reset() {
        this.buffer = new byte[0];
    }
}