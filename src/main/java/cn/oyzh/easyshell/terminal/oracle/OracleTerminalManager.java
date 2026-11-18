package cn.oyzh.easyshell.terminal.oracle;

import cn.oyzh.easyshell.terminal.oracle.OracleTerminalPane;
import cn.oyzh.easyshell.terminal.oracle.basic.OracleShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.oracle.basic.OracleShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.oracle.basic.OracleShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.oracle.basic.OracleUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * Oracle终端管理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(OracleTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(OracleTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(OracleTerminalPane.TERMINAL_NAME, OracleShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(OracleTerminalPane.TERMINAL_NAME, OracleShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(OracleTerminalPane.TERMINAL_NAME, OracleShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(OracleTerminalPane.TERMINAL_NAME, OracleUseTerminalCommandHandler.class);
    }
}
