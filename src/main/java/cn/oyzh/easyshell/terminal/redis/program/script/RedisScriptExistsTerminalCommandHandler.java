package cn.oyzh.easyshell.terminal.redis.program.script;

import redis.clients.jedis.Protocol;

/**
 * Redis SCRIPT EXISTS 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisScriptExistsTerminalCommandHandler extends RedisScriptTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.EXISTS.name();
    }
}
