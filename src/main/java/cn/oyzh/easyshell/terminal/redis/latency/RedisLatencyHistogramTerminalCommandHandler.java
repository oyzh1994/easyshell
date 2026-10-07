package cn.oyzh.easyshell.terminal.redis.latency;

/**
 * Redis LATENCY HISTOGRAM 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisLatencyHistogramTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "HISTOGRAM";
    }
}
