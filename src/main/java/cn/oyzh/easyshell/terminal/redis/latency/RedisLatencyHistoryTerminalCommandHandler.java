package cn.oyzh.easyshell.terminal.redis.latency;

import cn.oyzh.easyshell.terminal.redis.latency.RedisLatencyTerminalCommandHandler;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisLatencyHistoryTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "HISTORY";
    }
}
