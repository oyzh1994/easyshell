package cn.oyzh.easyshell.terminal.redis.slowlog;

import cn.oyzh.easyshell.terminal.redis.slowlog.RedisSlowlogTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSlowlogResetTerminalCommandHandler extends RedisSlowlogTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESET.name();
    }
}
