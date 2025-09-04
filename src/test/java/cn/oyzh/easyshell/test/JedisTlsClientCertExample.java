package cn.oyzh.easyshell.test;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
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

public class JedisTlsClientCertExample {

    public static void main(String[] args) throws Exception {
        // 1. 创建 SSL Socket Factory (同时加载客户端证书和CA信任证书)
        // SSLSocketFactory sslSocketFactory = createInsecureSslSocketFactory();
        SSLSocketFactory sslSocketFactory = createSslSocketFactoryWithClientCert();
        // SSLSocketFactory sslSocketFactory = createSslSocketFactory();

        // 2. 配置连接池
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
    }

    // static String caCertPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/ca.crt";
    // static  String clientCertPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/redis.crt";
    // static   String clientKeyPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/redis.key";

    static String caCertPath = "C:\\Users\\oyzh\\Projects\\easyshell\\docker\\redis\\ssl\\ca.crt";
    static String clientCertPath = "C:\\Users\\oyzh\\Projects\\easyshell\\docker\\redis\\ssl\\redis.crt";
    static String clientKeyPath = "C:\\Users\\oyzh\\Projects\\easyshell\\docker\\redis\\ssl\\redis.key";

    private static SSLSocketFactory createSslSocketFactoryWithClientCert() throws Exception {
        // 加载 CA 证书（信任库）- 用于验证服务器证书
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate caCert;
        try (InputStream caIs = new FileInputStream(caCertPath)) { // 替换为你的 CA 证书路径
            caCert = cf.generateCertificate(caIs);
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", caCert); // 将 CA 证书添加到信任库

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // 加载客户端证书和私钥（密钥库）- 用于向服务器证明自己
        Certificate clientCert;
        try (InputStream certIs = new FileInputStream(clientCertPath)) { // 替换为你的客户端证书路径
            clientCert = cf.generateCertificate(certIs);
        }

        // 加载客户端私钥（假设是 PEM 格式的 PKCS#8 私钥）
        String privateKeyPEM = new String(Files.readAllBytes(Paths.get(clientKeyPath))); // 替换为你的客户端私钥路径
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // 移除 PEM 标记和空白字符
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // 根据你的私钥类型选择 "RSA" 或 "EC"
        PrivateKey privateKey = kf.generatePrivate(keySpec);

        // 创建密钥库并设置客户端证书条目
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        // 将客户端证书和私钥放入密钥库。这里假设私钥没有密码，如果有，请提供给它
        keyStore.setKeyEntry("client-key", privateKey, "".toCharArray(), new Certificate[]{clientCert});

        // 初始化 KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "".toCharArray()); // 如果私钥有密码，使用密码的字符数组

        // 创建并初始化 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), // 提供密钥管理器（包含客户端证书）
                tmf.getTrustManagers(), // 提供信任管理器（包含受信任的 CA）
                new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }

    private static SSLSocketFactory createInsecureSslSocketFactory() throws Exception {
        // 创建一个信任所有证书的TrustManager（仅用于测试！）
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // 创建SSLContext并使用信任所有证书的TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }

    private static SSLSocketFactory createSslSocketFactory() throws Exception {
        // 加载 CA 证书（受信任的证书）
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // 加载你的 CA 证书
        Certificate caCert;
        try (InputStream caIs = new FileInputStream(caCertPath)) {
            caCert = cf.generateCertificate(caIs);
        }

        // 创建 TrustStore 并添加 CA 证书
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", caCert);

        // 初始化 TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // 创建 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }
}