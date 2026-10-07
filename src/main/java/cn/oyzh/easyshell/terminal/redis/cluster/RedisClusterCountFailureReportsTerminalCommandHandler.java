package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER COUNT-FAILURE-REPORTS 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterCountFailureReportsTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "COUNT-FAILURE-REPORTS";
    }
}
