package cn.oyzh.easyshell.terminal.redis.program.script;

import cn.oyzh.easyshell.terminal.redis.program.script.RedisScriptTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisScriptExistsTerminalCommandHandler extends RedisScriptTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.EXISTS.name();
    }
}
