package cn.oyzh.easyshell.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.NamedParameterSpec;
import java.util.Base64;

public class Ed25519KeyGenerator {

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // 生成密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519", "BC");
        KeyPair keyPair = kpg.generateKeyPair();

        // 设置加密参数（密码：yourpassword）
        String password = "yourpassword";
        OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC)
                .setProvider("BC")
                .setPassword(password.toCharArray())
                .build();

        String dir = "C:\\Users\\Administrator\\Desktop\\";

        // 保存加密的PKCS#8私钥
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(dir + "k10.pri"))) {
            JcaPKCS8Generator gen = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);
            pemWriter.writeObject(gen.generate());
        }

        // 生成OpenSSH公钥格式
        byte[] pubKeyBytes = keyPair.getPublic().getEncoded();
        String sshPublicKey = "ssh-ed25519 " + Base64.getEncoder().encodeToString(pubKeyBytes) + " user@host";

        try (FileWriter pubWriter = new FileWriter(dir + "k10.pub")) {
            pubWriter.write(sshPublicKey);
        }
    }
}
