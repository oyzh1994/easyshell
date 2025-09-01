package cn.oyzh.easyshell.terminal.redis.client;

import cn.oyzh.easyshell.terminal.redis.client.RedisClientTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientKillTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LIST.name();
    }
}
