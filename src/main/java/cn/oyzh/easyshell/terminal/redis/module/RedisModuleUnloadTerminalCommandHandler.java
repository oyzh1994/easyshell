package cn.oyzh.easyshell.terminal.redis.module;

import redis.clients.jedis.Protocol;

/**
 * Redis MODULE UNLOAD 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisModuleUnloadTerminalCommandHandler extends RedisModuleTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.UNLOAD.name();
    }
}
