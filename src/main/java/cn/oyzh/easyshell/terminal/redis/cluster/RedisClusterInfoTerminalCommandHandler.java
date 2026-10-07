package cn.oyzh.easyshell.terminal.redis.cluster;

import redis.clients.jedis.Protocol;

/**
 * Redis CLUSTER INFO 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterInfoTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.INFO.name();
    }
}
