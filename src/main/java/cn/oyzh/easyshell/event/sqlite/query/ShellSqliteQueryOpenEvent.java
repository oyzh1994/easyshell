package cn.oyzh.easyshell.event.sqlite.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 查询打开事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteQueryOpenEvent extends Event<ShellQuery> {

    private ShellSqliteClient client;

    public ShellSqliteClient client() {
        return this.client;
    }

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }
}
