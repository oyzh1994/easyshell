package cn.oyzh.easyshell.terminal.sqlite.basic;

/**
 * `.databases` 命令，兼容 SQLite CLI 与 `.database`。
 *
 * @since 2026-11-02
 */
public class SqliteDatabasesTerminalCommandHandler extends SqliteDatabaseTerminalCommandHandler {

    @Override
    public String commandName() {
        return ".databases";
    }
}
