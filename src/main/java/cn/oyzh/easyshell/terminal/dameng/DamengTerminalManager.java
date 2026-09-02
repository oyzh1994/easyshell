package cn.oyzh.easyshell.terminal.dameng;

import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.easyshell.terminal.dameng.basic.DamengShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.basic.DamengShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.basic.DamengShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.basic.DamengUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class DamengTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(DamengTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(DamengTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(DamengTerminalPane.TERMINAL_NAME, DamengShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(DamengTerminalPane.TERMINAL_NAME, DamengShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(DamengTerminalPane.TERMINAL_NAME, DamengShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(DamengTerminalPane.TERMINAL_NAME, DamengUseTerminalCommandHandler.class);
    }
}
