package cn.oyzh.easyshell.event.sqlite.table;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 表设计器打开事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTableDesignEvent extends Event<String> {

    private ShellSqliteClient client;

    public ShellSqliteClient client() {
        return this.client;
    }

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    public String tableName() {
        return this.data();
    }
}
