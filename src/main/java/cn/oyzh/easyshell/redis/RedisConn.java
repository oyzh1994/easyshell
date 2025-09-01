package cn.oyzh.easyshell.redis;

import redis.clients.jedis.Jedis;

/**
 *
 * @author oyzh
 * @since 2025/01/02
 */
public class RedisConn {

    private Jedis jedis;

    private boolean using;

    public RedisConn(Jedis jedis) {
        this.jedis = jedis;
    }

    public RedisConn(Jedis jedis, boolean using) {
        this.jedis = jedis;
        this.using = using;
    }

    public int getDB() {
        return this.jedis.getDB();
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }
}
