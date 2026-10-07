package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * Redis CLUSTER LINKS 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterLinksTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "LINKS";
    }
}
