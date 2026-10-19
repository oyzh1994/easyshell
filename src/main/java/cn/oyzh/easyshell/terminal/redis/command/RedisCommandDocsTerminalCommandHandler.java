package cn.oyzh.easyshell.terminal.redis.command;

import redis.clients.jedis.Protocol;

/**
 * Redis COMMAND DOCS 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisCommandDocsTerminalCommandHandler extends RedisCommandTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.DOCS.name();
    }
}
