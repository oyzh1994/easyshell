package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER KEYSLOT 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterKeyslotTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "KEYSLOT";
    }
}
