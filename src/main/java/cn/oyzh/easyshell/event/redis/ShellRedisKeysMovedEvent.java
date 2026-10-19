package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;

/**
 * redis多个键移动事件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisKeysMovedEvent extends Event<Integer>   {

    /**
     * 目标数据库
     */
    private int targetDB;

    public int getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(int targetDB) {
        this.targetDB = targetDB;
    }

    /**
     * 获取源数据库
     *
     * @return 源数据库
     */
    public int getSourceDB() {
        return this.data();
    }

    /**
     * 连接
     */
    private ShellConnect connect;

    public ShellConnect getConnect() {
        return connect;
    }

    public void setConnect(ShellConnect connect) {
        this.connect = connect;
    }
}
