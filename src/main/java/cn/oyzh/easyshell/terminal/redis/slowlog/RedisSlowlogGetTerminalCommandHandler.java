package cn.oyzh.easyshell.terminal.redis.slowlog;

import redis.clients.jedis.Protocol;

/**
 * Redis SLOWLOG GET 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisSlowlogGetTerminalCommandHandler extends RedisSlowlogTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.GET.name();
    }
}
