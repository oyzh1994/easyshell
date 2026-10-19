package cn.oyzh.easyshell.terminal.redis.latency;

/**
 * Redis LATENCY GRAPH 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisLatencyGraphTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "GRAPH";
    }
}
