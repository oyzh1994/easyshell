package cn.oyzh.easyshell.terminal.redis.memory;

import redis.clients.jedis.Protocol;

/**
 * Redis MEMORY STATS 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisMemoryStatsTerminalCommandHandler extends RedisMemoryTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.STATS.name();
    }
}
