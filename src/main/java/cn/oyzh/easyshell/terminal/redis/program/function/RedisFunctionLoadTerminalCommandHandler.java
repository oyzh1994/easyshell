package cn.oyzh.easyshell.terminal.redis.program.function;

import redis.clients.jedis.Protocol;

/**
 * Redis FUNCTION LOAD 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisFunctionLoadTerminalCommandHandler extends RedisFunctionTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LOAD.name();
    }
}
