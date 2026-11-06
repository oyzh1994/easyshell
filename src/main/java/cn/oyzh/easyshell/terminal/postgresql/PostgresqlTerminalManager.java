package cn.oyzh.easyshell.terminal.postgresql;

import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.easyshell.terminal.postgresql.basic.PostgresqlShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.postgresql.basic.PostgresqlShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.postgresql.basic.PostgresqlShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.postgresql.basic.PostgresqlUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * PostgreSQL终端管理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(PostgresqlTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(PostgresqlTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(PostgresqlTerminalPane.TERMINAL_NAME, PostgresqlShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(PostgresqlTerminalPane.TERMINAL_NAME, PostgresqlShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(PostgresqlTerminalPane.TERMINAL_NAME, PostgresqlShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(PostgresqlTerminalPane.TERMINAL_NAME, PostgresqlUseTerminalCommandHandler.class);
    }
}
