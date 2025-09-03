package cn.oyzh.easyshell.test;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class JedisTlsExample {

    public static void main(String[] args) throws Exception {
        // 1. 创建 SSL Socket Factory
        SSLSocketFactory sslSocketFactory = createSslSocketFactory();

        // 2. 配置连接池（可选）
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);

        JedisClientConfig clientConfig = DefaultJedisClientConfig
                .builder()
                .ssl(true)
                .password("123456")
                .sslSocketFactory(sslSocketFactory)
                .build();
        JedisPool jedisPool = new JedisPool(
                new HostAndPort("127.0.0.1", 8380), clientConfig
        );
        jedisPool.getResource().set("s1", "123456");
        System.out.println(jedisPool.getResource().get("s1"));

        // 方式二：直接使用 Jedis (不推荐用于生产，仅测试)
        // JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
        //         .ssl(true)
        //         .sslSocketFactory(sslSocketFactory)
        //         .password("your-redis-password") // 可选
        //         .build();
        // try (Jedis jedis = new Jedis("your-redis-host", 6379, clientConfig)) {
        //     jedis.set("foo", "bar");
        //     String value = jedis.get("foo");
        //     System.out.println(value);
        // }
    }

     static String caCertPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/ca.crt";
    static  String clientCertPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/redis.crt";
    static   String clientKeyPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/redis.key";


    private static SSLSocketFactory createSslSocketFactory() throws Exception {
        // 加载 CA 证书（受信任的证书）
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate caCert;
        try (InputStream caIs = new FileInputStream(caCertPath)) { // 替换为你的 ca.crt 路径
            caCert = cf.generateCertificate(caIs);
        }

        // 创建 TrustStore 并添加 CA 证书
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", caCert);

        // 初始化 TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // (可选) 加载客户端证书和私钥（如果需要双向认证）
        // 注意：Redis 通常只需要服务器认证，除非你配置了 tls-auth-clients yes 并要求客户端证书
        // 如果你的环境需要，请取消注释以下代码块，并提供 redis.crt 和 redis.key 路径
        Certificate clientCert;
        try (InputStream certIs = new FileInputStream(clientCertPath)) { // 你的 redis.crt
            clientCert = cf.generateCertificate(certIs);
        }

        // 解析私钥 - 注意：你需要根据你的私钥格式（PEM PKCS#8 是常见的）进行调整
        // 这里是一个简单的示例，假设私钥是 PEM 格式的 PKCS#8
        String privateKeyPEM = new String(Files.readAllBytes(Paths.get(clientKeyPath))); // 你的 redis.key
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                                     .replace("-----END PRIVATE KEY-----", "")
                                     .replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // 或 "EC"
        PrivateKey privateKey = kf.generatePrivate(keySpec);

        // 创建 KeyStore 并设置客户端证书条目
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("client", clientCert);
        keyStore.setKeyEntry("client-key", privateKey, "".toCharArray(), new Certificate[]{clientCert}); // 无密码或用实际密码

        // 初始化 KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "".toCharArray()); // 无密码或用私钥密码

        // 创建 SSLContext 并初始化
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, // 如果没有客户端证书，kmf.getKeyManagers() 替换为 null
                       tmf.getTrustManagers(),
                       new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }
}