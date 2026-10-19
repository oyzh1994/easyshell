package cn.oyzh.easyshell.terminal.redis.cluster;

import redis.clients.jedis.Protocol;

/**
 * Redis CLUSTER RESET 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClusterResetTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESET.name();
    }
}
