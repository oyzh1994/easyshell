package cn.oyzh.easyshell.terminal.sqlite;

import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * SQLite 终端管理器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class ShellSqliteTerminalManager {

    private ShellSqliteTerminalManager() {
    }

    /**
     * 注册命令处理器。
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteUseTerminalCommandHandler.class);
    }
}
