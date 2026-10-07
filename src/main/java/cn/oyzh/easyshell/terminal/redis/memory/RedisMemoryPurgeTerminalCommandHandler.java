package cn.oyzh.easyshell.terminal.redis.memory;

import redis.clients.jedis.Protocol;

/**
 * Redis MEMORY PURGE 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisMemoryPurgeTerminalCommandHandler extends RedisMemoryTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.PURGE.name();
    }
}
