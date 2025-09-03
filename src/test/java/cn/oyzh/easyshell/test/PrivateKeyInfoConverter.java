package cn.oyzh.easyshell.test;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class PrivateKeyInfoConverter {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 将PrivateKeyInfo转换为KeyPair（支持RSA算法）
     */
    public static KeyPair convertToKeyPair(PrivateKeyInfo privateKeyInfo) throws Exception {
        // 获取算法标识符
        AlgorithmIdentifier algorithmIdentifier = privateKeyInfo.getPrivateKeyAlgorithm();
        String algorithm = algorithmIdentifier.getAlgorithm().getId();
        
        if ("1.2.840.113549.1.1.1".equals(algorithm)) { // RSA算法OID
            return convertRSAPrivateKeyInfoToKeyPair(privateKeyInfo);
        } else {
            throw new UnsupportedOperationException("Unsupported algorithm: " + algorithm);
        }
    }

    /**
     * 处理RSA私钥转换
     */
    private static KeyPair convertRSAPrivateKeyInfoToKeyPair(PrivateKeyInfo privateKeyInfo) throws Exception {
        // 正确解析RSA私钥结构
        ASN1Encodable privateKey = privateKeyInfo.parsePrivateKey();
        
        if (privateKey instanceof RSAPrivateKey) {
            // 如果是RSAPrivateKey对象，直接使用
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            return createRSAKeyPair(rsaPrivateKey);
        } else if (privateKey instanceof ASN1Sequence) {
            // 如果是ASN1Sequence，尝试解析为RSAPrivateKey
            ASN1Sequence sequence = (ASN1Sequence) privateKey;
            RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(sequence);
            return createRSAKeyPair(rsaPrivateKey);
        } else {
            throw new IllegalArgumentException("Unsupported private key format: " + privateKey.getClass().getName());
        }
    }

    /**
     * 从RSAPrivateKey创建KeyPair
     */
    private static KeyPair createRSAKeyPair(RSAPrivateKey rsaPrivateKey) throws Exception {
        // 获取RSA参数
        java.math.BigInteger modulus = rsaPrivateKey.getModulus();
        java.math.BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
        
        // 获取公钥指数 - 优先从私钥结构中获取，如果不存在则使用默认值65537
        java.math.BigInteger publicExponent = rsaPrivateKey.getPublicExponent();
        if (publicExponent == null) {
            publicExponent = java.math.BigInteger.valueOf(65537); // 常见公钥指数
        }
        
        // 创建私钥
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, privateExponent);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        
        // 创建公钥
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * 使用JcaPEMKeyConverter进行转换（备选方案）
     */
    public static KeyPair convertWithJcaPEMKeyConverter(PrivateKeyInfo privateKeyInfo) throws PEMException {
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);
        
        // 注意：这种方法只能获取私钥，需要额外步骤获取公钥
        // 您需要根据私钥信息推导出公钥（如前面的示例所示）
        return new KeyPair(null, privateKey); // 需要补充公钥生成逻辑
    }
}