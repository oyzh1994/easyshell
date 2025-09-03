package cn.oyzh.easyshell.test;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

public class JedisPEMKeyPairExample {
    // 私钥密码（有密码则填写，无则为null）
    private static final String PRIVATE_KEY_PASSWORD = null;

    public static void main(String[] args) {
        try {
            // 在密钥操作前执行
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            // 1. 配置参数（替换为实际信息）
            String caCertPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/ca.crt";
            String clientCertPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/redis.crt";
            String clientKeyPath = "/Users/oyzh/IdeaProjects/oyzh/easyshell/docker/redis/ssl/redis.key";

            // 2. 加载CA证书
            X509Certificate caCert = loadCert(caCertPath);
            TrustManager[] trustManagers = createTrustManagers(caCert);

            // 3. 加载客户端证书和私钥（核心修复部分）
            X509Certificate clientCert = loadCert(clientCertPath);
            KeyPair clientKeyPair = loadKeyPair(clientKeyPath); // 这里返回正确类型

            // 4. 创建密钥管理器
            KeyManager[] keyManagers = createKeyManagers(clientCert, clientKeyPair);

            // 5. 初始化SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagers, trustManagers, null);

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10); // 连接池最大连接数

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 加载证书
    private static X509Certificate loadCert(String path) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(path))) {
            X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
            return new JcaX509CertificateConverter().getCertificate(holder);
        }
    }

    // 修复的私钥加载方法：返回PEMKeyPair适配高版本BouncyCastle
    private static KeyPair loadKeyPair(String keyPath) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(keyPath))) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            // // 处理加密私钥（PKCS8格式）
            // if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
            //     if (PRIVATE_KEY_PASSWORD == null) {
            //         throw new IllegalArgumentException("私钥已加密，请设置PRIVATE_KEY_PASSWORD");
            //     }
            //     PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
            //     PEMKeyPair pemKeyPair = encryptedInfo.decryptPrivateKeyInfo(
            //             new JcePEMDecryptorProviderBuilder().build(PRIVATE_KEY_PASSWORD.toCharArray())
            //     ).toPEMKeyPair();
            //     return converter.getKeyPair(pemKeyPair); // 接收PEMKeyPair参数
            // }


            // 处理普通PEM私钥（直接返回PEMKeyPair）
            if (obj instanceof PrivateKeyInfo info) {
                // PrivateKeyToKeyPair.convertToKeyPair(info);
                // return PrivateKeyInfoToKeyPair.toKeyPair(info);
                return PrivateKeyInfoConverter.convertToKeyPair(info);
            }

            // 处理普通PEM私钥（直接返回PEMKeyPair）
            if (obj instanceof PEMKeyPair) {

                return converter.getKeyPair((PEMKeyPair) obj); // 直接适配PEMKeyPair参数
            }

            throw new IllegalArgumentException("不支持的私钥格式: " + obj.getClass().getName());
        }
    }

    // 创建信任管理器
    private static TrustManager[] createTrustManagers(X509Certificate caCert) {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
                        try {
                            certs[0].verify(caCert.getPublicKey());
                        } catch (Exception e) {
                            throw new java.security.cert.CertificateException("服务端证书验证失败", e);
                        }
                    }
                }
        };
    }

    // 创建密钥管理器
    private static KeyManager[] createKeyManagers(X509Certificate clientCert, KeyPair clientKeyPair) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null);
        ks.setCertificateEntry("client-cert", clientCert);
        ks.setKeyEntry("client-key", clientKeyPair.getPrivate(), new char[0], new X509Certificate[]{clientCert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);
        return kmf.getKeyManagers();
    }
}
