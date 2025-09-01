package cn.oyzh.easyshell.terminal.redis.module;

import cn.oyzh.easyshell.terminal.redis.module.RedisModuleTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisModuleLoadTerminalCommandHandler extends RedisModuleTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LOAD.name();
    }
}
