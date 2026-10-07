package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER COUNT-FAILURE-REPORTS 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClusterCountFailureReportsTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "COUNT-FAILURE-REPORTS";
    }
}
