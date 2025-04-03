package cn.oyzh.easyshell.test;

import java.security.*;
import java.util.Base64;

public class JSchEd25519Generator1 {
    public static void main(String[] args) throws Exception {
        // 生成密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = kpg.generateKeyPair();

        // 生成OpenSSH公钥（格式不变）
        String publicKey = "ssh-ed25519 " + 
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        // 生成PKCS#8格式私钥（JSch兼容）
        String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
            Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(
                keyPair.getPrivate().getEncoded()
            ) + "\n-----END PRIVATE KEY-----";

        System.out.println("Public Key:\n" + publicKey);
        System.out.println("\nPrivate Key (PKCS#8):\n" + privateKey);
    }
}
