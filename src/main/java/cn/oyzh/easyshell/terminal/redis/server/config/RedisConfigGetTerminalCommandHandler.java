package cn.oyzh.easyshell.terminal.redis.server.config;

import redis.clients.jedis.Protocol;

/**
 * Redis CONFIG GET 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisConfigGetTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.GET.name();
    }


}
