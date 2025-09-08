package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/12
 */
public class ShellRedisKeysMovedEvent extends Event<Integer>   {

    private int targetDB;

    public int getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(int targetDB) {
        this.targetDB = targetDB;
    }

    public int getSourceDB() {
        return this.data();
    }

    private ShellConnect connect;

    public ShellConnect getConnect() {
        return connect;
    }

    public void setConnect(ShellConnect connect) {
        this.connect = connect;
    }
}
