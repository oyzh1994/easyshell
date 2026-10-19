package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER FLUSHSLOTS 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClusterFlushSlotsTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "FLUSHSLOTS";
    }
}
