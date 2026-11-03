package cn.oyzh.easyshell.event.sqlite.function;

import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 函数设计器打开事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteFunctionDesignEvent extends Event<SQLiteFunction> {

    private ShellSqliteClient client;

    public ShellSqliteClient client() {
        return this.client;
    }

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    public String functionName() {
        return this.data() == null ? null : this.data().getName();
    }
}
