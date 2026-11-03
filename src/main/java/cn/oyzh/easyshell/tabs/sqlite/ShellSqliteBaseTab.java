package cn.oyzh.easyshell.tabs.sqlite;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * SQLite 数据库标签页基类。
 *
 * @since 2026-11-02
 */
public abstract class ShellSqliteBaseTab extends RichTab {

    public abstract ShellSqliteClient client();
}

