package cn.oyzh.easyshell.terminal.redis.server.config;

import redis.clients.jedis.Protocol;

/**
 * Redis CONFIG REWRITE 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisConfigRewriteTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.REWRITE.name();
    }
}
