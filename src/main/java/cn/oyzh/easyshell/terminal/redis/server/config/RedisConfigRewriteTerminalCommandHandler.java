package cn.oyzh.easyshell.terminal.redis.server.config;

import redis.clients.jedis.Protocol;

/**
 * Redis CONFIG REWRITE 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisConfigRewriteTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.REWRITE.name();
    }
}
