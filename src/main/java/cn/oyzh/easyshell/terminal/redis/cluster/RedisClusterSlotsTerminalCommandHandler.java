package cn.oyzh.easyshell.terminal.redis.cluster;

import cn.oyzh.easyshell.terminal.redis.cluster.RedisClusterTerminalCommandHandler;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterSlotsTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "SLOTS";
    }
}
