package cn.oyzh.easyshell.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.json.JsonProtocol;
import redis.clients.jedis.util.SafeEncoder;

/**
 * redis的json工具类
 *
 * @author oyzh
 * @since 2025-10-24
 */
public class ShellRedisJsonUtil {

    /**
     * JSON.SET 命令：设置 JSON 数据
     *
     * @param jedis 客户端
     * @param key   键
     * @param path  路径
     * @param json  值
     * @return 结果
     */
    public static String jsonSet(Jedis jedis, String key, String path, String json) {
        return jedis.sendCommand(
                JsonProtocol.JsonCommand.SET,
                SafeEncoder.encode(key),
                SafeEncoder.encode(path),
                SafeEncoder.encode(json)
        ).toString();
    }

    /**
     * JSON.GET 命令：获取 JSON 数据
     *
     * @param jedis 客户端
     * @param key   键
     * @param path  路径
     * @return 结果
     */
    public static Object jsonGet(Jedis jedis, String key, String path) {
        return jedis.sendCommand(
                JsonProtocol.JsonCommand.GET,
                SafeEncoder.encode(key),
                SafeEncoder.encode(path)
        );
    }

}