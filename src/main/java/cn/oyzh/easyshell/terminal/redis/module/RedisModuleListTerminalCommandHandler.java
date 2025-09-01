package cn.oyzh.easyshell.terminal.redis.module;

import cn.oyzh.easyshell.terminal.redis.module.RedisModuleTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisModuleListTerminalCommandHandler extends RedisModuleTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LIST.name();
    }
}
