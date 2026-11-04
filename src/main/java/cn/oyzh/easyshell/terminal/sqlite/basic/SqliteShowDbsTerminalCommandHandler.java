package cn.oyzh.easyshell.terminal.sqlite.basic;

/**
 * SQLite显示数据库命令处理器（简化名别名）
 *
 * @author oyzh
 * @since 2026-11-04
 */
public class SqliteShowDbsTerminalCommandHandler extends SqliteShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }
}
