package cn.oyzh.easyshell.terminal.mariadb;

import cn.oyzh.easyshell.terminal.mariadb.basic.MariadbShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mariadb.basic.MariadbShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mariadb.basic.MariadbShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mariadb.basic.MariadbUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * MariaDB终端管理器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        TerminalManager.registerHandler(MariadbTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(MariadbTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        TerminalManager.registerHandler(MariadbTerminalPane.TERMINAL_NAME, MariadbShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(MariadbTerminalPane.TERMINAL_NAME, MariadbShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(MariadbTerminalPane.TERMINAL_NAME, MariadbShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(MariadbTerminalPane.TERMINAL_NAME, MariadbUseTerminalCommandHandler.class);
    }
}
