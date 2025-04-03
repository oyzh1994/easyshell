package cn.oyzh.easyshell.test;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

public class OpenSSHEd25519Generator1 {

    public static void main(String[] args) throws Exception {
        // 生成RSA密钥对（2048位）
        KeyPair keyPair = generateEd25519KeyPair();

        // 生成OpenSSH格式公钥
        String publicKey = generateSSHPublicKey(keyPair.getPublic());
        System.out.println("Public Key (OpenSSH):");
        System.out.println(publicKey);

        // 设置私钥密码
        String password = "your_strong_password_123!"; // 在此设置密码

        // 生成PKCS#1格式加密私钥
        String privateKeyPkcs1 = generatePEMPrivateKey(keyPair.getPrivate(), password, "PKCS#1");
        System.out.println("\nPrivate Key (Encrypted PKCS#1):");
        System.out.println(privateKeyPkcs1);

        // 生成PKCS#8格式加密私钥
        String privateKeyPkcs8 = generatePEMPrivateKey(keyPair.getPrivate(), password, "PKCS#8");
        System.out.println("\nPrivate Key (Encrypted PKCS#8):");
        System.out.println(privateKeyPkcs8);
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

    /**
     * 生成加密的PEM私钥
     * @param formatType PKCS#1 或 PKCS#8
     */
    private static String generatePEMPrivateKey(PrivateKey privateKey,
                                                String password,
                                                String formatType) throws Exception {
        // 构建加密器
        JcePEMEncryptorBuilder encryptorBuilder = new JcePEMEncryptorBuilder("AES-256-CBC");
        encryptorBuilder.setSecureRandom(new SecureRandom());
        PEMEncryptor encryptor = encryptorBuilder.build(password.toCharArray());

        // 根据格式类型生成不同PEM对象
        PemObject pemObject;
        if ("PKCS#1".equalsIgnoreCase(formatType)) {
            // PKCS#1格式
            PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
            pemObject = new PemObject("PRIVATE KEY",
                    pkInfo.parsePrivateKey().toASN1Primitive().getEncoded());
        } else {
            // PKCS#8格式
            pemObject = new PemObject("ENCRYPTED PRIVATE KEY", privateKey.getEncoded());
        }

        // 写入加密的PEM格式
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
            pw.writeObject(pemObject, encryptor);
        }
        return sw.toString();
    }
}
