package cn.oyzh.easyshell.terminal.redis.server.config;

import redis.clients.jedis.Protocol;

/**
 * Redis CONFIG SET 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisConfigSetTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.SET.name();
    }
}
