package cn.oyzh.easyshell.terminal.redis.latency;

/**
 * Redis LATENCY GRAPH 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisLatencyGraphTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "GRAPH";
    }
}
