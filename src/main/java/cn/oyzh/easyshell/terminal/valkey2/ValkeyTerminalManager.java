package cn.oyzh.easyshell.terminal.valkey2;

import cn.oyzh.easyshell.terminal.valkey2.basic.ValkeyConnectCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * Valkey v2 终端管理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ValkeyTerminalManager {

    /**
     * 注册 Valkey v2 终端的静态命令处理器。
     */
    public static void registerHandlers() {
        TerminalManager.registerHandler(ValkeyTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);
        TerminalManager.registerHandler(ValkeyTerminalPane.TERMINAL_NAME, ValkeyHelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ValkeyTerminalPane.TERMINAL_NAME, ValkeyHelpCommandHandler.class);
        TerminalManager.registerHandler(ValkeyTerminalPane.TERMINAL_NAME, ValkeyConnectCommandHandler.class);
    }
}
