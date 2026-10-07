package cn.oyzh.easyshell.terminal.redis.client;

import redis.clients.jedis.Protocol;

/**
 * Redis CLIENT UNPAUSE 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClientUnpauseTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.UNPAUSE.name();
    }
}
