package cn.oyzh.easyshell.terminal.redis.program.script;

import cn.oyzh.easyshell.terminal.redis.program.script.RedisScriptTerminalCommandHandler;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisScriptDebugTerminalCommandHandler extends RedisScriptTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "DEBUG";
    }
}
