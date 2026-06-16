package cn.oyzh.easyshell.event.mysql.terminal;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellMysqlTerminalOpenEvent extends Event<ShellMysqlClient> {

    private String dbName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
