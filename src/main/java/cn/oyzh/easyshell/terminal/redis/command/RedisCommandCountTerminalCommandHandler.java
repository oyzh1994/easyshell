package cn.oyzh.easyshell.terminal.redis.command;

import redis.clients.jedis.Protocol;

/**
 * Redis COMMAND COUNT 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisCommandCountTerminalCommandHandler extends RedisCommandTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.COUNT.name();
    }
}
