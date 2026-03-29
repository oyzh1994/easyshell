// package cn.oyzh.easyshell.test;
//
// import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
// import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
// import org.bouncycastle.cert.X509CertificateHolder;
// import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
// import org.bouncycastle.openssl.PEMDecryptorProvider;
// import org.bouncycastle.openssl.PEMParser;
// import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
// import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
// import org.bouncycastle.operator.InputDecryptorProvider;
// import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
// import org.junit.Test;
// import redis.clients.jedis.DefaultJedisClientConfig;
// import redis.clients.jedis.HostAndPort;
// import redis.clients.jedis.Jedis;
// import redis.clients.jedis.JedisClientConfig;
// import redis.clients.jedis.JedisPool;
// import redis.clients.jedis.JedisPoolConfig;
//
// import javax.net.ssl.KeyManagerFactory;
// import javax.net.ssl.SSLContext;
// import javax.net.ssl.SSLSocketFactory;
// import javax.net.ssl.TrustManager;
// import javax.net.ssl.TrustManagerFactory;
// import javax.net.ssl.X509TrustManager;
// import java.io.FileReader;
// import java.security.KeyPair;
// import java.security.KeyStore;
// import java.security.cert.X509Certificate;
//
// public class JedisSelfSignedSSLExample {
//
//     // 工具方法：加载PEM格式证书
//     private static X509Certificate loadCert(String path) throws Exception {
//         try (PEMParser parser = new PEMParser(new FileReader(path))) {
//             X509CertificateHolder certHolder = (X509CertificateHolder) parser.readObject();
//             return new JcaX509CertificateConverter().getCertificate(certHolder);
//         }
//     }
//
//     // // 工具方法：加载PEM格式私钥
//     // private static KeyPair loadKeyPair(String path) throws Exception {
//     //     try (PEMParser parser = new PEMParser(new FileReader(path))) {
//     //         PrivateKeyInfo keyInfo = (PrivateKeyInfo) parser.readObject();
//     //         return new JcaPEMKeyConverter().getKeyPair(keyInfo);
//     //     }
//     // }
//
//     // 修复的私钥加载方法：兼容普通/加密私钥
//     private static KeyPair loadKeyPair(String keyPath) throws Exception {
//         try (PEMParser parser = new PEMParser(new FileReader(keyPath))) {
//             Object obj = parser.readObject();
//             JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
//
//             // 处理加密的私钥（PKCS8格式）
//             if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
//                 PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
//                 PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder()
//                         .build(PRIVATE_KEY_PASSWORD.toCharArray());
//                 PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(decryptorProvider);
//                 return converter.getKeyPair(keyInfo);
//             }
//
//             // 处理加密的私钥（旧格式）
//             if (obj instanceof EncryptedPrivateKeyInfo) {
//                 EncryptedPrivateKeyInfo encryptedInfo = (EncryptedPrivateKeyInfo) obj;
//                 InputDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder()
//                         .build(PRIVATE_KEY_PASSWORD.toCharArray());
//                 PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(decryptorProvider);
//                 return converter.getKeyPair(keyInfo);
//             }
//
//             // 处理普通私钥
//             if (obj instanceof PrivateKeyInfo) {
//                 return converter.getKeyPair((PrivateKeyInfo) obj);
//             }
//
//             // 其他格式（如直接的私钥对象）
//             throw new IllegalArgumentException("不支持的私钥格式: " + obj.getClass().getName());
//         }
//     }
//
//
//     @Test
//     public void test1()throws Exception {
// // 1. 你的PEM文件路径（直接替换为实际路径）
//         String caCertPath = "ca.crt";       // CA根证书
//         String clientCertPath = "redis.crt"; // 客户端证书
//         String clientKeyPath = "redis.key";  // 客户端私钥
//
//         // 2. 解析CA证书（用于验证服务端）
//         X509Certificate caCert = loadCert(caCertPath);
//         TrustManagerFactory tmf = TrustManagerFactory.getInstance(
//                 TrustManagerFactory.getDefaultAlgorithm()
//         );
//         tmf.init((KeyStore) null); // 初始化空信任库
//         // 自定义信任管理器，仅信任我们的CA证书
//         X509TrustManager tm = new X509TrustManager() {
//             public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
//             public void checkClientTrusted(X509Certificate[] certs, String authType) {}
//             public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                 // 验证服务端证书是否由我们的CA签发
//                 try {
//                     certs[0].verify(caCert.getPublicKey());
//                 } catch (Exception e) {
//                     throw new RuntimeException("服务端证书验证失败", e);
//                 }
//             }
//         };
//
//         // 3. 解析客户端证书和私钥（用于服务端验证客户端）
//         X509Certificate clientCert = loadCert(clientCertPath);
//         KeyPair clientKeyPair = loadKeyPair(clientKeyPath);
//         KeyManagerFactory kmf = KeyManagerFactory.getInstance(
//                 KeyManagerFactory.getDefaultAlgorithm()
//         );
//         // 用客户端证书和私钥创建临时密钥库
//         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//         ks.load(null);
//         ks.setCertificateEntry("client-cert", clientCert);
//         ks.setKeyEntry("client-key", clientKeyPair.getPrivate(),
//                 new char[0], new X509Certificate[]{clientCert});
//         kmf.init(ks, new char[0]);
//
//         // 4. 创建SSL上下文
//         SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
//         sslContext.init(kmf.getKeyManagers(), new TrustManager[]{tm}, null);
//     }
//
//     public static void main(String[] args) throws Exception {
//         // 1. 创建SSL上下文（信任自签名证书）
//         SSLContext sslContext = SSLContext.getInstance("TLS");
//         sslContext.init(
//                 null,
//                 // 自定义信任管理器：信任所有证书（仅测试环境使用）
//                 new TrustManager[]{new X509TrustManager() {
//                     @Override
//                     public X509Certificate[] getAcceptedIssuers() {
//                         return null;
//                     }
//
//                     @Override
//                     public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                     }
//
//                     @Override
//                     public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                     }
//                 }},
//                 null
//         );
//
//         // 2. 配置Jedis连接池
//         JedisPoolConfig poolConfig = new JedisPoolConfig();
//         poolConfig.setMaxTotal(10); // 连接池最大连接数
//         SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//         JedisClientConfig clientConfig = DefaultJedisClientConfig
//                 .builder()
//                 .ssl(true)
//                 .password("123456")
//                 .sslSocketFactory(sslSocketFactory)
//                 .build();
//         JedisPool jedisPool = new JedisPool(
//                 new HostAndPort("127.0.0.1", 8380), clientConfig
//         );
//
//         // 3. 测试SSL连接
//         try (Jedis jedis = jedisPool.getResource()) {
//             jedis.set("ssl_test_key", "ssl_test_value");
//             System.out.println("获取值：" + jedis.get("ssl_test_key"));
//         } finally {
//             jedisPool.close();
//         }
//     }
// }
