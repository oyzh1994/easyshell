package cn.oyzh.easyshell.terminal.redis.cluster;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterResetTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESET.name();
    }
}
