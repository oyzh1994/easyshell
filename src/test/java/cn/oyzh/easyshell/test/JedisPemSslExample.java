// package cn.oyzh.easyshell.test;
//
// import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
// import org.bouncycastle.cert.X509CertificateHolder;
// import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
// import org.bouncycastle.openssl.EncryptedPrivateKeyInfo;
// import org.bouncycastle.openssl.PEMParser;
// import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
// import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
// import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
// import org.bouncycastle.util.io.pem.PemObject;
// import redis.clients.jedis.Jedis;
// import redis.clients.jedis.JedisPool;
// import redis.clients.jedis.JedisPoolConfig;
//
// import javax.net.ssl.*;
// import java.io.FileReader;
// import java.security.KeyPair;
// import java.security.KeyStore;
// import java.security.cert.X509Certificate;
// import java.security.spec.InvalidKeySpecException;
//
// public class JedisPemSslExample {
//     // 私钥密码（如果私钥有密码，填写实际密码；无密码则为null）
//     private static final String PRIVATE_KEY_PASSWORD = null;
//
//     public static void main(String[] args) {
//         try {
//             // 1. 配置证书路径和Redis信息（替换为你的实际信息）
//             String caCertPath = "ca.crt";         // CA根证书路径
//             String clientCertPath = "redis.crt";  // 客户端证书路径
//             String clientKeyPath = "redis.key";   // 客户端私钥路径
//             String redisHost = "localhost";       // Redis服务器地址
//             int redisSslPort = 6380;              // Redis SSL端口
//             String redisPassword = "your_redis_pass"; // Redis密码（无则为null）
//
//             // 2. 加载CA证书（用于验证Redis服务端）
//             X509Certificate caCert = loadCertificate(caCertPath);
//             TrustManager[] trustManagers = createTrustManagers(caCert);
//
//             // 3. 加载客户端证书和私钥（用于服务端验证客户端）
//             X509Certificate clientCert = loadCertificate(clientCertPath);
//             KeyPair clientKeyPair = loadKeyPair(clientKeyPath);
//             KeyManager[] keyManagers = createKeyManagers(clientCert, clientKeyPair);
//
//             // 4. 创建SSL上下文
//             SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
//             sslContext.init(keyManagers, trustManagers, null);
//
//             // 5. 初始化Jedis连接池并测试
//             JedisPoolConfig poolConfig = new JedisPoolConfig();
//             poolConfig.setMaxTotal(10);
//
//             try (JedisPool jedisPool = new JedisPool(
//                     poolConfig,
//                     redisHost,
//                     redisSslPort,
//                     5000,          // 连接超时时间
//                     redisPassword,
//                     true,          // 启用SSL
//                     null,
//                     sslContext,
//                     true           // 启用主机名验证
//             )) {
//                 try (Jedis jedis = jedisPool.getResource()) {
//                     String result = jedis.ping();
//                     System.out.println("Redis连接成功: " + result);
//                     jedis.set("ssl_test_key", "ssl_test_value");
//                     System.out.println("读取数据: " + jedis.get("ssl_test_key"));
//                 }
//             }
//
//         } catch (Exception e) {
//             System.err.println("连接失败: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
//
//     /**
//      * 加载PEM格式的证书
//      */
//     private static X509Certificate loadCertificate(String certPath) throws Exception {
//         try (PEMParser parser = new PEMParser(new FileReader(certPath))) {
//             X509CertificateHolder certHolder = (X509CertificateHolder) parser.readObject();
//             if (certHolder == null) {
//                 throw new IllegalArgumentException("证书文件格式错误: " + certPath);
//             }
//             return new JcaX509CertificateConverter().getCertificate(certHolder);
//         }
//     }
//
//     /**
//      * 加载PEM格式的私钥（支持普通和加密格式）
//      */
//     private static KeyPair loadKeyPair(String keyPath) throws Exception {
//         try (PEMParser parser = new PEMParser(new FileReader(keyPath))) {
//             Object obj = parser.readObject();
//             JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
//
//             // 处理PKCS8加密私钥
//             if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
//                 if (PRIVATE_KEY_PASSWORD == null || PRIVATE_KEY_PASSWORD.isEmpty()) {
//                     throw new IllegalArgumentException("私钥已加密，但未提供密码");
//                 }
//                 PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
//                 PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(
//                         new JcePEMDecryptorProviderBuilder().build(PRIVATE_KEY_PASSWORD.toCharArray())
//                 );
//                 return converter.getKeyPair(keyInfo);
//             }
//
//             // 处理旧版加密私钥
//             if (obj instanceof EncryptedPrivateKeyInfo) {
//                 if (PRIVATE_KEY_PASSWORD == null || PRIVATE_KEY_PASSWORD.isEmpty()) {
//                     throw new IllegalArgumentException("私钥已加密，但未提供密码");
//                 }
//                 EncryptedPrivateKeyInfo encryptedInfo = (EncryptedPrivateKeyInfo) obj;
//                 PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(
//                         new JcePEMDecryptorProviderBuilder().build(PRIVATE_KEY_PASSWORD.toCharArray())
//                 );
//                 return converter.getKeyPair(keyInfo);
//             }
//
//             // 处理普通私钥
//             if (obj instanceof PrivateKeyInfo) {
//                 return converter.getKeyPair((PrivateKeyInfo) obj);
//             }
//
//             // 处理PemObject格式（兼容部分旧版本）
//             if (obj instanceof PemObject) {
//                 throw new InvalidKeySpecException("不支持的PEM对象格式，请检查私钥文件");
//             }
//
//             throw new IllegalArgumentException("不支持的私钥格式: " + (obj != null ? obj.getClass().getName() : "null"));
//         }
//     }
//
//     /**
//      * 创建信任管理器（信任指定的CA证书）
//      */
//     private static TrustManager[] createTrustManagers(X509Certificate caCert) {
//         return new TrustManager[]{
//                 new X509TrustManager() {
//                     @Override
//                     public X509Certificate[] getAcceptedIssuers() {
//                         return new X509Certificate[0];
//                     }
//
//                     @Override
//                     public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                         // 双向认证时可在此验证客户端证书
//                     }
//
//                     @Override
//                     public void checkServerTrusted(X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
//                         if (certs == null || certs.length == 0) {
//                             throw new java.security.cert.CertificateException("未收到服务端证书");
//                         }
//                         try {
//                             // 验证服务端证书是否由信任的CA签发
//                             certs[0].verify(caCert.getPublicKey());
//                         } catch (Exception e) {
//                             throw new java.security.cert.CertificateException("服务端证书验证失败", e);
//                         }
//                     }
//                 }
//         };
//     }
//
//     /**
//      * 创建密钥管理器（提供客户端证书和私钥）
//      */
//     private static KeyManager[] createKeyManagers(X509Certificate clientCert, KeyPair clientKeyPair) throws Exception {
//         KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//         keyStore.load(null, null); // 初始化空密钥库
//
//         // 存储客户端证书
//         keyStore.setCertificateEntry("client-cert", clientCert);
//
//         // 存储客户端私钥
//         keyStore.setKeyEntry(
//                 "client-key",
//                 clientKeyPair.getPrivate(),
//                 new char[0], // 私钥密码（无密码）
//                 new X509Certificate[]{clientCert}
//         );
//
//         KeyManagerFactory kmf = KeyManagerFactory.getInstance(
//                 KeyManagerFactory.getDefaultAlgorithm()
//         );
//         kmf.init(keyStore, new char[0]); // 密钥库密码（无密码）
//
//         return kmf.getKeyManagers();
//     }
// }
//