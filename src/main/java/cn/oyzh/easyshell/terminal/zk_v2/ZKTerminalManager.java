package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.easyshell.terminal.zk_v2.basic.ZKConnectCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * ZooKeeper v2 终端管理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKTerminalManager {

    /**
     * 注册 ZooKeeper v2 终端的静态命令处理器。
     */
    public static void registerHandlers() {
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKHelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKHelpCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKConnectCommandHandler.class);
    }
}
