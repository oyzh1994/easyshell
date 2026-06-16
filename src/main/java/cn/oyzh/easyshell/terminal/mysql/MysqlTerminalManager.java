package cn.oyzh.easyshell.terminal.mysql;

import cn.oyzh.easyshell.terminal.mysql.basic.MysqlShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mysql.basic.MysqlShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mysql.basic.MysqlShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mysql.basic.MysqlUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class MysqlTerminalManager {

    public static void registerHandlers() {
        TerminalManager.registerHandler(MysqlTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(MysqlTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        TerminalManager.registerHandler(MysqlTerminalPane.TERMINAL_NAME, MysqlShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(MysqlTerminalPane.TERMINAL_NAME, MysqlShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(MysqlTerminalPane.TERMINAL_NAME, MysqlShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(MysqlTerminalPane.TERMINAL_NAME, MysqlUseTerminalCommandHandler.class);
    }
}
