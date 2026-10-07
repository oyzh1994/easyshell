package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER SETSLOT 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterSetSlotTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "SETSLOT";
    }
}
