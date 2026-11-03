package cn.oyzh.easyshell.event.sqlite.view;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 视图设计器打开事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteViewDesignEvent extends Event<String> {

    private ShellSqliteClient client;

    public ShellSqliteClient client() {
        return this.client;
    }

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    public String viewName() {
        return this.data();
    }
}
