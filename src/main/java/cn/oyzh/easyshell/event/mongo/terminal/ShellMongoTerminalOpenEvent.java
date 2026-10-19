package cn.oyzh.easyshell.event.mongo.terminal;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.event.Event;

/**
 * mongodb终端打开事件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class ShellMongoTerminalOpenEvent extends Event<ShellMongoClient> {

    /**
     * 数据库名称
     */
    private String dbName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
