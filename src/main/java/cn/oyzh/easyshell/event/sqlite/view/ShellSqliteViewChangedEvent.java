package cn.oyzh.easyshell.event.sqlite.view;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 视图结构变更事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteViewChangedEvent extends Event<String> {

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

    public String viewName() {
        return this.data();
    }
}
