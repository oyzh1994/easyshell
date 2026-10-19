package cn.oyzh.easyshell.terminal.redis.client;

import redis.clients.jedis.Protocol;

/**
 * Redis CLIENT SETNAME 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClientSetnameTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.SETNAME.name();
    }
}
