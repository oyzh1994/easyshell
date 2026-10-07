package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER MEET 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClusterMeetTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "MEET";
    }
}
