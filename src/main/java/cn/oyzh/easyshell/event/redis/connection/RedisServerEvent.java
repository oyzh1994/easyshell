package cn.oyzh.easyshell.event.redis.connection;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/4/1
 */
public class RedisServerEvent extends Event<RedisClient> {

    public ShellConnect redisConnect() {
        return this.data().redisConnect();
    }
}
