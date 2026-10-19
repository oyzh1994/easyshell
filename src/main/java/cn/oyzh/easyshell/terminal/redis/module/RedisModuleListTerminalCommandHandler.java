package cn.oyzh.easyshell.terminal.redis.module;

import redis.clients.jedis.Protocol;

/**
 * Redis MODULE LIST 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisModuleListTerminalCommandHandler extends RedisModuleTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LIST.name();
    }
}
