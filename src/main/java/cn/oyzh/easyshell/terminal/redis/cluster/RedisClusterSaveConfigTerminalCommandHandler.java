package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER SAVECONFIG 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClusterSaveConfigTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "SAVECONFIG";
    }
}
