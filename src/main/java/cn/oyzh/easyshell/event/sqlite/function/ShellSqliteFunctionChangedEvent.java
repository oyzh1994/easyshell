package cn.oyzh.easyshell.event.sqlite.function;

import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 函数设计变更事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteFunctionChangedEvent extends Event<SQLiteFunction> {

    private ShellSqliteClient client;

    private String originalName;

    public ShellSqliteClient client() {
        return this.client;
    }

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    public String originalName() {
        return this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String functionName() {
        return this.data() == null ? null : this.data().getName();
    }
}
