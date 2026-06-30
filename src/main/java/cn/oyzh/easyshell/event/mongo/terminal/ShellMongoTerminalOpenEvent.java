package cn.oyzh.easyshell.event.mongo.terminal;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellMongoTerminalOpenEvent extends Event<ShellMongoClient> {

    private String dbName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
