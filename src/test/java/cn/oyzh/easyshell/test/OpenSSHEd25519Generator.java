package cn.oyzh.easyshell.test;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.*;
import java.security.*;
import java.util.Base64;

public class OpenSSHEd25519Generator {

    public static void main(String[] args) throws Exception {
        // 生成Ed25519密钥对
        KeyPair keyPair = generateEd25519KeyPair();

        // 生成OpenSSH格式公钥
        String publicKey = generateSSHPublicKey(keyPair.getPublic());
        System.out.println("Public Key (OpenSSH):");
        System.out.println(publicKey);

        // 生成加密的PEM私钥（设置密码）
        String password = "your_strong_password_123!";
        String privateKey = generateEncryptedPEMPrivateKey(keyPair.getPrivate(), password);
        System.out.println("\nEncrypted Private Key (PEM):");
        System.out.println(privateKey);
    }

    private static KeyPair generateEd25519KeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        return kpg.generateKeyPair();
    }

    private static String generateSSHPublicKey(PublicKey publicKey) {
        if (!"EdDSA".equals(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException("Not an Ed25519 public key");
        }

        // OpenSSH公钥格式结构：
        // ssh-ed25519 + Base64([类型长度][类型][密钥长度][密钥])
        byte[] pubBytes = publicKey.getEncoded();
        byte[] keyBytes = new byte[pubBytes.length - 12];
        System.arraycopy(pubBytes, 12, keyBytes, 0, keyBytes.length);

        try {
            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);

            // 写入算法标识符
            dos.writeInt("ssh-ed25519".length());
            dos.write("ssh-ed25519".getBytes());

            // 写入密钥数据
            dos.writeInt(keyBytes.length);
            dos.write(keyBytes);

            String encoded = Base64.getEncoder().encodeToString(byteOs.toByteArray());
            return "ssh-ed25519 " + encoded + " generated-by-java";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateEncryptedPEMPrivateKey(PrivateKey privateKey, String password) throws Exception {
        // 使用BC加密器构建（兼容OpenSSH格式）
        JcePEMEncryptorBuilder encryptorBuilder = new JcePEMEncryptorBuilder("AES-256-CBC");
        encryptorBuilder.setSecureRandom(SecureRandom.getInstanceStrong());
        
        // 构造PKCS#8格式的加密私钥
        PemObject pemObject = new PemObject("ENCRYPTED PRIVATE KEY", privateKey.getEncoded());

        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
            pw.writeObject(pemObject, encryptorBuilder.build(password.toCharArray()));
        }
        return sw.toString();
    }

    // 保留原有未加密生成方法（可选）
    private static String generatePEMPrivateKey(PrivateKey privateKey) throws IOException {
        PemObject pemObject = new PemObject("PRIVATE KEY", privateKey.getEncoded());
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
            pw.writeObject(pemObject);
        }
        return sw.toString();
    }
}
