package cn.oyzh.easyshell.test;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;

public class PrivateKeyToKeyPair {
    public static KeyPair convertToKeyPair(PrivateKeyInfo privateKeyInfo) throws Exception {
        // 转换私钥
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);

        // 从私钥参数提取公钥参数（RSA示例）
        RSAPrivateCrtKeyParameters rsaParams = (RSAPrivateCrtKeyParameters) privateKeyInfo.parsePrivateKey();
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
            rsaParams.getModulus(), 
            rsaParams.getPublicExponent()
        );
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

}
