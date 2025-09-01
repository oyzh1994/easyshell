package cn.oyzh.easyshell.terminal.redis.slowlog;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSlowlogLenTerminalCommandHandler extends RedisSlowlogTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LEN.name();
    }
}
