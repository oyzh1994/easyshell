package cn.oyzh.easyshell.terminal.redis.latency;

/**
 * Redis LATENCY LATEST 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisLatencyLatestTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "LATEST";
    }
}
