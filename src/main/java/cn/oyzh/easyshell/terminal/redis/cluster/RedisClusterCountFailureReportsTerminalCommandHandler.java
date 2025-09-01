package cn.oyzh.easyshell.terminal.redis.cluster;

import cn.oyzh.easyshell.terminal.redis.cluster.RedisClusterTerminalCommandHandler;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterCountFailureReportsTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "COUNT-FAILURE-REPORTS";
    }
}
