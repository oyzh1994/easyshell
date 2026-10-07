package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER SET-CONFIG-EPOCH 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterSetConfigEpochTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "SET-CONFIG-EPOCH";
    }
}
