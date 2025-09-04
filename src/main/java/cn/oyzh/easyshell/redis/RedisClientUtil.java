package cn.oyzh.easyshell.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSSLConfig;
import redis.clients.jedis.DefaultJedisClientConfig;

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

/**
 * redis客户端工具类
 *
 * @author oyzh
 * @since 2024/12/10
 */
public class RedisClientUtil {

    /**
     * 初始化客户端配置
     *
     * @param connect 连接
     */
    public static DefaultJedisClientConfig newConfig(ShellConnect connect) throws Exception {
        String user = connect.getUser();
        boolean ssl = connect.isSSLMode();
        String password = connect.getPassword();
        int socketTimeout = connect.getExecuteTimeOut();
        int connectTimeout = connect.getConnectTimeOut();
        // master配置处理
        DefaultJedisClientConfig.Builder builder = DefaultJedisClientConfig.builder();
        // socket超时
        builder.socketTimeoutMillis(socketTimeout);
        // 连接超时
        builder.connectionTimeoutMillis(connectTimeout);
        // 连接用户
        if (StringUtil.isNotBlank(user)) {
            builder.user(user);
        }
        // 连接密码
        if (StringUtil.isNotBlank(password)) {
            builder.password(password);
        }
        // ssl
        if (ssl) {
            builder.ssl(ssl);
            ShellSSLConfig sslConfig = connect.getSslConfig();
            SSLSocketFactory sslSocketFactory;
            if (sslConfig == null || sslConfig.isInvalid()) {
                sslSocketFactory = createSslSocketFactory();
            } else {
                sslSocketFactory = createSslSocketFactory(sslConfig.getCaCrt(), sslConfig.getClientCrt(), sslConfig.getClientKey());
            }
            builder.sslSocketFactory(sslSocketFactory);
        }
        return builder.build();
    }

    public static RedisClient newClient(ShellConnect redisConnect) {
        return new RedisClient(redisConnect);
    }

    /**
     * 创建ssl连接工厂
     *
     * @return SSLSocketFactory
     * @throws Exception 异常
     */
    public static SSLSocketFactory createSslSocketFactory() throws Exception {
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

    /**
     * 创建ssl连接工厂
     *
     * @param caCertPath     ca证书路径
     * @param clientCertPath 客户端证书路径
     * @param clientKeyPath  客户端密钥
     * @return SSLSocketFactory
     * @throws Exception 异常
     */
    public static SSLSocketFactory createSslSocketFactory(String caCertPath, String clientCertPath, String clientKeyPath) throws Exception {
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
}
