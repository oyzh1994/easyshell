package cn.oyzh.easyshell.terminal.redis_v2;

import cn.oyzh.easyshell.terminal.redis_v2.basic.RedisConnectCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * Redis v2 终端管理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisTerminalManager {

    /**
     * 注册 Redis v2 终端的静态命令处理器。
     */
    public static void registerHandlers() {
        TerminalManager.registerHandler(RedisTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);
        TerminalManager.registerHandler(RedisTerminalPane.TERMINAL_NAME, RedisHelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(RedisTerminalPane.TERMINAL_NAME, RedisHelpCommandHandler.class);
        TerminalManager.registerHandler(RedisTerminalPane.TERMINAL_NAME, RedisConnectCommandHandler.class);
    }
}
