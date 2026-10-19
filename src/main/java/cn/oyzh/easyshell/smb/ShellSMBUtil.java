package cn.oyzh.easyshell.smb;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * smb工具类
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class ShellSMBUtil {

    /**
     * AES-CMAC子密钥生成常量
     */
    private static final byte[] RB = new byte[]{
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x87
    };

    /**
     * 计算AES-CMAC值
     *
     * @param key     密钥
     * @param message 消息
     * @return CMAC值
     * @throws GeneralSecurityException 异常
     */
    public static byte[] calculateAesCmac(byte[] key, byte[] message)
            throws GeneralSecurityException {
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("Key must be 16, 24, or 32 bytes");
        }

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 生成子密钥
        byte[] l = cipher.doFinal(new byte[16]);
        byte[] k1 = generateSubKey(l);
        byte[] k2 = generateSubKey(k1);

        // 计算块数
        int n = (message.length == 0) ? 1 : (int) Math.ceil(message.length / 16.0);
        boolean lastBlockComplete = (message.length % 16 == 0) && (message.length > 0);

        byte[] x = new byte[16];
        byte[] y;

        // 处理前面的完整块
        for (int i = 0; i < n - 1; i++) {
            int offset = i * 16;
            byte[] block = Arrays.copyOfRange(message, offset, offset + 16);
            y = xor(x, block);
            x = cipher.doFinal(y);
        }

        // 处理最后一个块
        byte[] lastBlock;
        if (n == 0) {
            // 空消息
            lastBlock = new byte[16];
            lastBlock[0] = (byte) 0x80;
            lastBlock = xor(lastBlock, k2);
        } else if (lastBlockComplete) {
            // 完整块
            int offset = (n - 1) * 16;
            lastBlock = Arrays.copyOfRange(message, offset, offset + 16);
            lastBlock = xor(lastBlock, k1);
        } else {
            // 不完整块，需要填充
            int offset = (n - 1) * 16;
            int remaining = message.length - offset;
            lastBlock = new byte[16];
            if (remaining > 0) {
                System.arraycopy(message, offset, lastBlock, 0, remaining);
            }
            lastBlock[remaining] = (byte) 0x80;
            lastBlock = xor(lastBlock, k2);
        }

        y = xor(x, lastBlock);
        return cipher.doFinal(y);
    }

    /**
     * 生成子密钥
     *
     * @param key 密钥
     * @return 子密钥
     */
    private static byte[] generateSubKey(byte[] key) {
        byte[] shifted = leftShiftOneBit(key);

        // 如果最高位有进位，与Rb异或
        if ((key[0] & 0x80) != 0) {
            for (int i = 0; i < 16; i++) {
                shifted[i] ^= RB[i];
            }
        }

        return shifted;
    }

    /**
     * 左移一位
     *
     * @param input 输入
     * @return 左移一位后的结果
     */
    private static byte[] leftShiftOneBit(byte[] input) {
        byte[] output = new byte[16];
        int carry = 0;

        for (int i = 15; i >= 0; i--) {
            int value = input[i] & 0xFF;
            output[i] = (byte) ((value << 1) | carry);
            carry = (value & 0x80) >>> 7;
        }

        return output;
    }

    /**
     * 按位异或
     *
     * @param a 操作数a
     * @param b 操作数b
     * @return 异或结果
     */
    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[16];
        for (int i = 0; i < 16; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
}