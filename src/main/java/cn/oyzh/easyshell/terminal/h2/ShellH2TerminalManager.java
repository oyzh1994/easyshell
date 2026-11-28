package cn.oyzh.easyshell.terminal.h2;

import cn.oyzh.easyshell.terminal.h2.basic.H2ShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.basic.H2ShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.basic.H2ShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.basic.H2UseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * H2 终端管理器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class ShellH2TerminalManager {

    private ShellH2TerminalManager() {
    }

    /**
     * 注册命令处理器。
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(ShellH2TerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellH2TerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(ShellH2TerminalPane.TERMINAL_NAME, H2ShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellH2TerminalPane.TERMINAL_NAME, H2ShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellH2TerminalPane.TERMINAL_NAME, H2ShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ShellH2TerminalPane.TERMINAL_NAME, H2UseTerminalCommandHandler.class);
    }
}
