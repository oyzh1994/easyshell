package cn.oyzh.easyshell.terminal.redis.command;

import cn.oyzh.easyshell.terminal.redis.command.RedisCommandTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisCommandCountTerminalCommandHandler extends RedisCommandTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.COUNT.name();
    }
}
