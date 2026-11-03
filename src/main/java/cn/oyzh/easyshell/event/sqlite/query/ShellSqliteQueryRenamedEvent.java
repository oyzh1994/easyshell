package cn.oyzh.easyshell.event.sqlite.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 查询重命名事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteQueryRenamedEvent extends Event<ShellQuery> {

    private ShellSqliteClient client;

    private String oldName;

    public ShellSqliteClient client() {
        return this.client;
    }

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    public String oldName() {
        return this.oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String queryId() {
        return this.data().getUid();
    }

    public String queryName() {
        return this.data().getName();
    }
}
