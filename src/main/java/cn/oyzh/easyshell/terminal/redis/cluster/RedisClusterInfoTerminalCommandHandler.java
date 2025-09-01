package cn.oyzh.easyshell.terminal.redis.cluster;

import cn.oyzh.easyshell.terminal.redis.cluster.RedisClusterTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterInfoTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.INFO.name();
    }
}
