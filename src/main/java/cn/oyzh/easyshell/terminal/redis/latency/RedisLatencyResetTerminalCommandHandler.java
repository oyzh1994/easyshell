package cn.oyzh.easyshell.terminal.redis.latency;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisLatencyResetTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESET.name();
    }
}
