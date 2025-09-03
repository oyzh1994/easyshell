package cn.oyzh.easyshell.test;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

public class PrivateKeyInfoToKeyPair {

    // public static void main(String[] args) throws Exception {
    //     // 注册 Bouncy Castle 提供者
    //     Security.addProvider(new BouncyCastleProvider());
    //
    //     // 假设你已通过某种方式（如PEMParser）获得了 PrivateKeyInfo 对象
    //     // PrivateKeyInfo privateKeyInfo = ...;
    //
    //     // 1. 转换为 JDK PrivateKey
    //     JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    //     PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);
    //
    //     KeyPair keyPair = null;
    //
    //     // 2. 根据私钥算法获取公钥
    //     String algorithm = privateKey.getAlgorithm();
    //     if ("RSA".equals(algorithm)) {
    //         keyPair = getKeyPairFromRSA(privateKey);
    //     } else if ("EC".equals(algorithm)) {
    //         keyPair = getKeyPairFromEC(privateKey);
    //     } else {
    //         throw new NoSuchAlgorithmException("Unsupported key algorithm: " + algorithm);
    //     }
    //
    //     // 3. 此时你拥有了一个包含公钥和私钥的KeyPair
    //     // 可以使用 keyPair.getPublic() 和 keyPair.getPrivate() 进行加密、解密、签名等操作
    //     System.out.println("Public Key: " + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    //     System.out.println("Private Key: " + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
    //
    //     // 可选：验证公私钥是否匹配
    //     if (verifyKeyPair(keyPair)) {
    //         System.out.println("Key pair verification successful.");
    //     } else {
    //         System.out.println("Key pair verification failed!");
    //     }
    // }

    public static KeyPair toKeyPair(PrivateKeyInfo privateKeyInfo) throws Exception {
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);

        KeyPair keyPair = null;

        // 2. 根据私钥算法获取公钥
        String algorithm = privateKey.getAlgorithm();
        // if ("RSA".equals(algorithm)) {
            keyPair = getKeyPairFromRSA(privateKey);
        // } else if ("EC".equals(algorithm)) {
        //     keyPair = getKeyPairFromEC(privateKey);
        // } else {
        //     throw new NoSuchAlgorithmException("Unsupported key algorithm: " + algorithm);
        // }

        return keyPair;
    }

    /**
     * 处理RSA私钥，从中提取信息并生成对应的RSA公钥:cite[6]。
     */
    private static KeyPair getKeyPairFromRSA(PrivateKey privateKey) throws Exception {
        // 将JDK PrivateKey 转换回 Bouncy Castle 的 RSAPrivateKey 结构以提取参数
        // 这里假设私钥是 PKCS#8 格式，并且包含了模数(n)和公开指数(e)等信息
        // 注意：标准的PKCS#8私钥确实包含这些信息用于生成公钥
        org.bouncycastle.asn1.pkcs.RSAPrivateKey bcRSAPrivateKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(privateKey.getEncoded());

        // 从BC的RSAPrivateKey对象中获取模数(n)和公开指数(e)
        java.math.BigInteger modulus = bcRSAPrivateKey.getModulus();
        // 注意：RSAPrivateKey 结构可能不直接包含公开指数(e)。
        // 但实际上，RSA私钥通常包含e（公开指数），它通常是一个已知的小素数，如65537。
        // 如果从私钥中无法直接获取e，可能需要从其他来源获取。
        // 以下是一种常见做法，假设公开指数为65537：
        java.math.BigInteger publicExponent = bcRSAPrivateKey.getPublicExponent(); 
        // 但请注意：BC的RSAPrivateKey的getPublicExponent()方法可能返回null，
        // 如果私钥信息中确实不包含e（虽然标准PKCS#8应该包含）:cite[6]。
        // 更可靠的方式是从原始PrivateKeyInfo的AlgorithmIdentifier中获取，
        // 或者如果知道e，直接使用已知值（如65537）。

        if (publicExponent == null) {
            // 如果无法从私钥中获取公开指数，尝试使用常见值
            publicExponent = java.math.BigInteger.valueOf(65537);
            // 更健壮的做法是从PrivateKeyInfo的AlgorithmIdentifier的parameters中解析，
            // 或者确保你的私钥源包含了公开指数。
        }

        // 使用模数和公开指数创建RSAPublicKeySpec
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);

        // 生成RSAPublicKey
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

    // /**
    //  * 处理ECC私钥，使用EC参数和私钥计算推导出公钥:cite[2]:cite[5]。
    //  */
    // private static KeyPair getKeyPairFromEC(PrivateKey privateKey) throws Exception {
    //     // 对于ECC，私钥是一个标量s，公钥是基点G乘以s的点Q。
    //     // 要从私钥生成公钥，需要知道椭圆曲线参数。
    //     // 这些参数通常可以从私钥的AlgorithmIdentifier中获取，或者使用标准曲线参数。
    //
    //     // 将JDK PrivateKey 转换回 Bouncy Castle 的 ECPrivateKey 结构
    //     org.bouncycastle.asn1.sec.ECPrivateKey bcECPrivateKey = org.bouncycastle.asn1.sec.ECPrivateKey.getInstance(privateKey.getEncoded());
    //
    //     // 获取私钥值S
    //     java.math.BigInteger s = bcECPrivateKey.getKey();
    //
    //     // 获取椭圆曲线参数 - 这是一个复杂点，需要从算法标识符或已知曲线获取参数
    //     // 这里假设曲线是secp256r1（P-256），你需要根据实际情况调整
    //     AlgorithmIdentifier algId = privateKeyInfo.getPrivateKeyAlgorithm();
    //     // 实际中，你需要从algId中解析出曲线OID或参数，然后获取ECParameterSpec
    //     // 以下是一个使用已知曲线的示例：
    //     ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256r1");
    //     KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
    //     kpg.initialize(ecGenSpec);
    //     KeyPair tmpKeyPair = kpg.generateKeyPair(); // 生成一个临时密钥对以获取参数
    //     ECPublicKey tmpPubKey = (ECPublicKey) tmpKeyPair.getPublic();
    //     ECParameterSpec ecSpec = tmpPubKey.getParams(); // 获取ECParameterSpec
    //
    //     // 使用EC参数和私钥值S计算公钥点Q
    //     ECPoint g = ecSpec.getGenerator();
    //     EllipticCurve curve = ecSpec.getCurve();
    //     java.security.spec.ECField field = curve.getField();
    //
    //     // 计算公钥点 Q = s * G
    //     // 注意：这里的椭圆曲线点乘计算非常复杂，通常由密码提供者完成。
    //     // 更简单的方法是：使用已有的EC参数，用私钥s创建一个ECPrivateKeySpec，
    //     // 然后利用KeyFactory生成私钥的同时，其实我们更需要的是公钥。
    //     // 另一种思路：既然已经有了曲线参数和私钥s，可以创建一个ECPublicKeySpec，但它需要公钥点Q。
    //
    //     // 实际开发中，如果PrivateKeyInfo是从一个包含完整信息的PKCS#8数据解析而来，
    //     // 并且Bouncy Castle支持，也许可以直接用JcaPEMKeyConverter转换出KeyPair（如果它内部处理了公钥计算）。
    //     // 但根据用户问题，有时可能不直接支持:cite[1]。
    //
    //     // 一个更实用但可能不够优雅的方法是：
    //     // 根据私钥的算法参数（包含曲线信息），生成一个新的临时密钥对，但用我们的私钥值替换。
    //     // 这通常需要更底层的操作，可能涉及BC特定API。
    //
    //     // 由于ECC公钥推导相对复杂，且高度依赖BC的内部表示和曲线参数来源，
    //     // 这里提供一个概念性的方向，具体实现需根据你的私源调整。
    //     // 许多情况下，如果原始PEM文件包含的是“EC PRIVATE KEY”格式（RFC 5915），
    //     // 它可能会同时包含私钥和可选公钥点。如果你的PrivateKeyInfo来自此类源且包含公钥，
    //     // 可以尝试从中提取。
    //
    //     // 鉴于复杂性，以下代码仅示意，可能需要调整：
    //     KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
    //     ECPrivateKeySpec privKeySpec = new ECPrivateKeySpec(s, ecSpec);
    //     PrivateKey ecPrivKey = keyFactory.generatePrivate(privKeySpec);
    //
    //     // 假设我们通过某种方式得到了公钥点Q（例如从PrivateKeyInfo的额外信息，或单独计算）
    //     // 如果无法获取，可能需要其他方式。
    //     // 这里假设我们计算Q（省略具体计算，因为它依赖于椭圆曲线密码学库）。
    //     // 通常更高效的做法是确保你的私钥源本身就包含公钥信息。
    //
    //     // 对于许多应用场景，如果你最初是从一个同时包含公私钥的PEM（如PKCS#8可能不直接包含公钥于私钥结构中，但某些格式会）解析，
    //     // 或许应该尝试在解析阶段就获取KeyPair，而不是事后从PrivateKeyInfo转换:cite[1]:cite[4]。
    //     // 例如，PEMParser解析PEMEncryptedKeyPair或PEMKeyPair可能更容易得到KeyPair。
    //
    //     // 由于ECC情况复杂，且搜索结果中主要示例为RSA，此处建议：
    //     // 如果可能，优先从源头上使用能直接解析出KeyPair的方法。
    //     // 如果必须从EC PrivateKeyInfo生成KeyPair，并且无法获取公钥点，可能需要借助BC的更多内部API进行点乘计算。
    //     // 参考Bouncy Castle的ECKeyPairGenerator或EC5Util等类。
    //
    //     // 简化返回（仅示意，需要真正公钥）
    //     return new KeyPair(null, privateKey); // 这里公钥为null，需要你补充实现
    // }

    /**
     * 验证生成的KeyPair中的公钥和私钥是否匹配:cite[8]。
     * 通过使用公钥加密数据，然后用私钥解密，看是否能还原原始数据。
     */
    private static boolean verifyKeyPair(KeyPair keyPair) {
        try {
            byte[] originalData = "Test data for verification".getBytes();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            String algorithm = publicKey.getAlgorithm();

            Cipher cipher = Cipher.getInstance(algorithm);
            // 加密
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(originalData);
            // 解密
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            return java.util.Arrays.equals(originalData, decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}