package cn.oyzh.easyshell.terminal.redis.memory;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisMemoryStatsTerminalCommandHandler extends RedisMemoryTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.STATS.name();
    }
}
