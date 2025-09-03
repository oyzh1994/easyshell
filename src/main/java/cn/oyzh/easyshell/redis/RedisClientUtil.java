package cn.oyzh.easyshell.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import redis.clients.jedis.DefaultJedisClientConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
     * @param user     用户
     * @param password 密码
     */
    public static DefaultJedisClientConfig newConfig(String user,
                                                     String password,
                                                     int connectTimeout,
                                                     int socketTimeout,
                                                     boolean ssl) throws Exception {
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
            builder.sslSocketFactory(RedisClientUtil.createSslSocketFactory());
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
}
