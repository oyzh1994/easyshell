package cn.oyzh.easyshell.terminal.redis.program.script;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisScriptKillTerminalCommandHandler extends RedisScriptTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.KILL.name();
    }
}
