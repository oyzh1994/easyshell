package cn.oyzh.easyshell.terminal.redis.module;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisModuleUnloadTerminalCommandHandler extends RedisModuleTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.UNLOAD.name();
    }
}
