package cn.oyzh.easyshell.test;

import com.jcraft.jsch.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.*;
import java.security.KeyPair;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Ed25519SSHConnector {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        // 1. 生成Ed25519密钥对
        KeyPair keyPair = generateEd25519KeyPair();
        
        // 2. 生成加密私钥(PEM格式)
        String privateKey = generateEncryptedPrivateKey(keyPair.getPrivate(), "your_password");
        System.out.println("----- Encrypted Private Key -----\n" + privateKey);

        // 3. 生成OpenSSH公钥
        String publicKey = generateOpenSSHPublicKey(keyPair.getPublic());
        System.out.println("----- OpenSSH Public Key -----\n" + publicKey);

    }

    private static KeyPair generateEd25519KeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        return kpg.generateKeyPair();
    }

    private static String generateEncryptedPrivateKey(PrivateKey privateKey, String password) throws Exception {
//        StringWriter sw = new StringWriter();
//        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
//            JcePEMEncryptorBuilder encryptorBuilder = new JcePEMEncryptorBuilder("AES-256-CBC")
//                    .setSecureRandom(SecureRandom.getInstanceStrong());
//            pw.writeObject(privateKey, encryptorBuilder.build(password.toCharArray()));
//        }
//        return sw.toString();

        JcePEMEncryptorBuilder encryptorBuilder = new JcePEMEncryptorBuilder("AES-256-CBC")
                .setSecureRandom(SecureRandom.getInstanceStrong());
        PemObject pemObject = new PemObject("ENCRYPTED PRIVATE KEY", privateKey.getEncoded());
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
            pw.writeObject(pemObject, encryptorBuilder.build(password.toCharArray()));
        }
        return sw.toString();

    }

    private static String generateOpenSSHPublicKey(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        // Ed25519公钥的OpenSSH格式编码
        byte[] keyBytes = new byte[encoded.length - 12];
        System.arraycopy(encoded, 12, keyBytes, 0, keyBytes.length);
        return "ssh-ed25519 " + Base64.getEncoder().encodeToString(keyBytes) + " generated@java";
    }

}
