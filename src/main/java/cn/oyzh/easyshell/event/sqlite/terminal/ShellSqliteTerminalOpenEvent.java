package cn.oyzh.easyshell.event.sqlite.terminal;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.Event;

/**
 * SQLite 终端打开事件。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTerminalOpenEvent extends Event<ShellSqliteClient> {

    public ShellSqliteClient client() {
        return this.data();
    }
}
