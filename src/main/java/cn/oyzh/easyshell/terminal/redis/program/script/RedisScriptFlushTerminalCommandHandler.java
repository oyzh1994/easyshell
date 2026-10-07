package cn.oyzh.easyshell.terminal.redis.program.script;

import redis.clients.jedis.Protocol;

/**
 * Redis SCRIPT FLUSH 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisScriptFlushTerminalCommandHandler extends RedisScriptTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.FLUSH.name();
    }
}
