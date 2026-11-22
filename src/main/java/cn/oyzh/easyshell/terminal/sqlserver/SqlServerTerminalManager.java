package cn.oyzh.easyshell.terminal.sqlserver;

import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * SqlServer终端管理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(SqlServerTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(SqlServerTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(SqlServerTerminalPane.TERMINAL_NAME, SqlServerShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(SqlServerTerminalPane.TERMINAL_NAME, SqlServerShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(SqlServerTerminalPane.TERMINAL_NAME, SqlServerShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(SqlServerTerminalPane.TERMINAL_NAME, SqlServerUseTerminalCommandHandler.class);
    }
}
