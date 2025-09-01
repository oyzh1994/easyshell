package cn.oyzh.easyshell.terminal.redis.program.function;

import cn.oyzh.easyshell.terminal.redis.program.function.RedisFunctionTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisFunctionStatsTerminalCommandHandler extends RedisFunctionTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.STATS.name();
    }
}
