package cn.oyzh.easyshell.test;

import cn.oyzh.common.file.FileUtil;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.*;
import java.security.*;
import java.util.Base64;

public class OpenSSHKeyGenerator {

    public static void main(String[] args) throws Exception {
        // 生成RSA密钥对（2048位）
        KeyPair keyPair = generateRSAKeyPair(2048);

        // 生成OpenSSH格式公钥
        String publicKey = generateOpenSSHPublicKey(keyPair.getPublic());
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

        String dir = "C:\\Users\\Administrator\\Desktop\\";

        File privateK = new File(dir, "k11.pri");
        File privateK1 = new File(dir, "k11.pri8");
        File publicK = new File(dir, "k11.pub");
        FileUtil.writeString(privateKeyPkcs1, privateK);
        FileUtil.writeString(privateKeyPkcs8, privateK1);
        FileUtil.writeString(publicKey, publicK);
    }

    private static KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    private static String generateOpenSSHPublicKey(PublicKey publicKey) {
        if (!(publicKey instanceof java.security.interfaces.RSAPublicKey)) {
            throw new IllegalArgumentException("Not an RSA public key");
        }

        java.security.interfaces.RSAPublicKey rsaPublicKey = (java.security.interfaces.RSAPublicKey) publicKey;

        // OpenSSH公钥格式：ssh-rsa + Base64编码的 [类型长度][类型][e长度][e][n长度][n]
        byte[] eBytes = rsaPublicKey.getPublicExponent().toByteArray();
        byte[] nBytes = rsaPublicKey.getModulus().toByteArray();

        try {
            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);

            // 写入算法类型 "ssh-rsa"
            dos.writeInt("ssh-rsa".getBytes().length);
            dos.write("ssh-rsa".getBytes());

            // 写入e
            dos.writeInt(eBytes.length);
            dos.write(eBytes);

            // 写入n
            dos.writeInt(nBytes.length);
            dos.write(nBytes);

            String encoded = Base64.getEncoder().encodeToString(byteOs.toByteArray());
            return "ssh-rsa " + encoded + " generated-by-java";
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
            pemObject = new PemObject("RSA PRIVATE KEY",
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
