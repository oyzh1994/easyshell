package cn.oyzh.easyshell.terminal.redis.server.config;

import redis.clients.jedis.Protocol;

/**
 * Redis CONFIG RESETSTAT 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisConfigResetStatTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESETSTAT.name();
    }
}
