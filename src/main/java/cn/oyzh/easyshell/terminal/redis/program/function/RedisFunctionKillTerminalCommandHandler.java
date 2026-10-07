package cn.oyzh.easyshell.terminal.redis.program.function;

import redis.clients.jedis.Protocol;

/**
 * Redis FUNCTION KILL 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisFunctionKillTerminalCommandHandler extends RedisFunctionTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.KILL.name();
    }
}
