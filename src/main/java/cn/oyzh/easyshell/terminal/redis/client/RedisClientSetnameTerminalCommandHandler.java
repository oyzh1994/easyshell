package cn.oyzh.easyshell.terminal.redis.client;

import cn.oyzh.easyshell.terminal.redis.client.RedisClientTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientSetnameTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.SETNAME.name();
    }
}
