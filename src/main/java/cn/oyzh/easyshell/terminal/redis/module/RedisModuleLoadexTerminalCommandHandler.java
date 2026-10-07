package cn.oyzh.easyshell.terminal.redis.module;

import redis.clients.jedis.Protocol;

/**
 * Redis MODULE LOADEX 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisModuleLoadexTerminalCommandHandler extends RedisModuleTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LOADEX.name();
    }
}
