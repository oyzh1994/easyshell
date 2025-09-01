package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/12
 */
public class RedisKeysMovedEvent extends Event<Integer>   {

    private int targetDB;

    public int getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(int targetDB) {
        this.targetDB = targetDB;
    }

    public int sourceDB() {
        return this.data();
    }

    private ShellConnect connect;

    public ShellConnect redisConnect() {
        return connect;
    }
}
