package cn.oyzh.easyshell.terminal.redis.client;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientListTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LIST.name();
    }
}
