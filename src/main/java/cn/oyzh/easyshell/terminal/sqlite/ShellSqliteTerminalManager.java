package cn.oyzh.easyshell.terminal.sqlite;

import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteIndexesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteDatabaseTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteFunctionsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteReadTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteSchemaTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteViewsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteVersionTerminalCommandHandler;
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
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteViewsTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteIndexesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteSchemaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteDatabaseTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteFunctionsTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteVersionTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellSqliteTerminalPane.TERMINAL_NAME, SqliteReadTerminalCommandHandler.class);
    }
}
