package cn.oyzh.easyshell.test.rdp;

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * RDP 安全层 - 处理加密、认证和密钥管理
 */
public class RDPSecurityLayer {
    
    private static final int ENCRYPTION_METHOD_NONE = 0x00000000;
    private static final int ENCRYPTION_METHOD_40BIT = 0x00000001;
    private static final int ENCRYPTION_METHOD_128BIT = 0x00000002;
    private static final int ENCRYPTION_METHOD_56BIT = 0x00000010;
    private static final int ENCRYPTION_METHOD_FIPS = 0x00000020;
    
    private byte[] clientRandom;
    private byte[] serverRandom;
    private byte[] encryptionKey;
    private byte[] macKey;
    private int encryptionMethod;
    
    private RC4Cipher encryptionCipher;
    private RC4Cipher decryptionCipher;
    
    public RDPSecurityLayer() {
        // 生成客户端随机数 (32 字节)
        this.clientRandom = new byte[32];
        new SecureRandom().nextBytes(clientRandom);
        
        // 默认：无加密
        this.encryptionMethod = ENCRYPTION_METHOD_NONE;
    }
    
    /**
     * 派生加密密钥
     * 基于 MS-RDPBCGR 规范中的密钥衍生函数
     */
    public void deriveKeys(String username, String password, int encryptionMethod) throws Exception {
        this.encryptionMethod = encryptionMethod;
        
        // 对于简化实现，使用基础密钥派生
        // 在实际 RDP 中，这涉及复杂的 MD5/SHA1 计算
        
        if (encryptionMethod == ENCRYPTION_METHOD_NONE) {
            System.out.println("[RDP Security] Using no encryption");
            return;
        }
        
        // 简化的密钥派生（用于演示）
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        
        // 密钥输入 = clientRandom + serverRandom + password hash
        byte[] passwordBytes = password.getBytes("UTF-8");
        md5.update(clientRandom);
        md5.update(serverRandom);
        md5.update(passwordBytes);
        
        byte[] derivedKey = md5.digest();
        
        // 根据加密方法截断密钥
        if (encryptionMethod == ENCRYPTION_METHOD_40BIT) {
            this.encryptionKey = new byte[5];
            System.arraycopy(derivedKey, 0, encryptionKey, 0, 5);
            System.out.println("[RDP Security] Using 40-bit RC4 encryption");
        } else if (encryptionMethod == ENCRYPTION_METHOD_128BIT) {
            this.encryptionKey = new byte[16];
            System.arraycopy(derivedKey, 0, encryptionKey, 0, 16);
            System.out.println("[RDP Security] Using 128-bit RC4 encryption");
        }
        
        // 初始化 RC4 密码
        if (encryptionKey != null) {
            encryptionCipher = new RC4Cipher(encryptionKey);
            decryptionCipher = new RC4Cipher(encryptionKey);
        }
    }
    
    /**
     * 加密数据
     */
    public byte[] encrypt(byte[] data) {
        if (encryptionCipher == null) {
            return data;
        }
        return encryptionCipher.crypt(data);
    }
    
    /**
     * 解密数据
     */
    public byte[] decrypt(byte[] data) {
        if (decryptionCipher == null) {
            return data;
        }
        return decryptionCipher.crypt(data);
    }
    
    // Getters and Setters
    public byte[] getClientRandom() {
        return clientRandom;
    }
    
    public void setServerRandom(byte[] serverRandom) {
        this.serverRandom = serverRandom;
    }
    
    public byte[] getServerRandom() {
        return serverRandom;
    }
    
    public void setEncryptionKey(byte[] key) {
        this.encryptionKey = key;
    }
    
    public byte[] getEncryptionKey() {
        return encryptionKey;
    }
    
    public void setEncryptionMethod(int method) {
        this.encryptionMethod = method;
    }
    
    public int getEncryptionMethod() {
        return encryptionMethod;
    }
    
    public boolean isEncryptionEnabled() {
        return encryptionMethod != ENCRYPTION_METHOD_NONE;
    }
}