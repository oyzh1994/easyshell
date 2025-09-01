package cn.oyzh.easyshell.terminal.redis.server.config;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisConfigRewriteTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.REWRITE.name();
    }
}
