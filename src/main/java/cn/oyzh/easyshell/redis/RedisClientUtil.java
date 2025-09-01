package cn.oyzh.easyshell.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import redis.clients.jedis.DefaultJedisClientConfig;

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
    public static DefaultJedisClientConfig newConfig(String user, String password, int connectTimeout, int socketTimeout) {
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
        return builder.build();
    }

    public static RedisClient newClient(ShellConnect redisConnect) {
        return new RedisClient(redisConnect);
    }
}
