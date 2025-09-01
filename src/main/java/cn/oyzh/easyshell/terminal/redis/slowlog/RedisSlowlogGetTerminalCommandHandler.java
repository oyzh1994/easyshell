package cn.oyzh.easyshell.terminal.redis.slowlog;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSlowlogGetTerminalCommandHandler extends RedisSlowlogTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.GET.name();
    }
}
