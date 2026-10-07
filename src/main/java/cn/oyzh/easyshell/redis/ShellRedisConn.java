package cn.oyzh.easyshell.redis;

import redis.clients.jedis.Jedis;

/**
 * redis连接
 *
 * @author oyzh
 * @since 2025/01/02
 */
public class ShellRedisConn {

    /**
     * jedis客户端
     */
    private Jedis jedis;

    /**
     * 是否使用中
     */
    private boolean using;

    /**
     * 构造方法
     *
     * @param jedis jedis客户端
     */
    public ShellRedisConn(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * 构造方法
     *
     * @param jedis jedis客户端
     * @param using 是否使用中
     */
    public ShellRedisConn(Jedis jedis, boolean using) {
        this.jedis = jedis;
        this.using = using;
    }

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public int getDB() {
        return this.jedis.getDB();
    }

    /**
     * 获取jedis客户端
     *
     * @return jedis客户端
     */
    public Jedis getJedis() {
        return jedis;
    }

    /**
     * 设置jedis客户端
     *
     * @param jedis jedis客户端
     */
    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * 是否使用中
     *
     * @return 结果
     */
    public boolean isUsing() {
        return using;
    }

    /**
     * 设置是否使用中
     *
     * @param using 是否使用中
     */
    public void setUsing(boolean using) {
        this.using = using;
    }
}
