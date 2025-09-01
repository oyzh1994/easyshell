package cn.oyzh.easyshell.terminal.redis.command;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisCommandInfoTerminalCommandHandler extends RedisCommandTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.INFO.name();
    }
}
