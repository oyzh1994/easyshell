package cn.oyzh.easyshell.terminal.redis.latency;

import redis.clients.jedis.Protocol;

/**
 * Redis LATENCY RESET 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisLatencyResetTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESET.name();
    }
}
