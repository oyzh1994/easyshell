package cn.oyzh.easyshell.test;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class OpenSSHKeyGenerator {

    public static void main(String[] args) throws Exception {
        // 生成RSA密钥对（2048位）
        KeyPair keyPair = generateRSAKeyPair(2048);

        // 生成OpenSSH格式公钥
        String publicKey = generateOpenSSHPublicKey(keyPair.getPublic());
        System.out.println("Public Key (OpenSSH):");
        System.out.println(publicKey);

        // 生成PEM格式的私钥（PKCS#1）
        String privateKey = generatePEMPrivateKey(keyPair.getPrivate());
        System.out.println("\nPrivate Key (PEM PKCS#1):");
        System.out.println(privateKey);
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

    private static String generatePEMPrivateKey(PrivateKey privateKey) throws Exception {
        // 转换私钥到PKCS#1格式
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        PemObject pemObject = new PemObject("RSA PRIVATE KEY", pkInfo.parsePrivateKey().toASN1Primitive().getEncoded());

        // 写入PEM格式
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
            pw.writeObject(pemObject);
        }
        return sw.toString();
    }
}
