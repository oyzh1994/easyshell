package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER NODES 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClusterNodesTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "NODES";
    }
}
