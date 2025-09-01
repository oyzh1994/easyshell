package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterCountKeysInSlotTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "COUNTKEYSINSLOT";
    }
}
