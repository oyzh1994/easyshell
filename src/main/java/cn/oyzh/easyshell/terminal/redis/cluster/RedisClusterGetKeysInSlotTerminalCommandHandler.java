package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER GETKEYSINSLOT 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterGetKeysInSlotTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "GETKEYSINSLOT";
    }
}
