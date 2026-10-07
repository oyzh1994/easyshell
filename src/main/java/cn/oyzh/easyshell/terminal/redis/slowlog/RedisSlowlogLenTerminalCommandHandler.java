package cn.oyzh.easyshell.terminal.redis.slowlog;

import redis.clients.jedis.Protocol;

/**
 * Redis SLOWLOG LEN 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSlowlogLenTerminalCommandHandler extends RedisSlowlogTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LEN.name();
    }
}
