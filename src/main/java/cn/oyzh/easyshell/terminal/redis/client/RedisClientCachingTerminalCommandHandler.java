package cn.oyzh.easyshell.terminal.redis.client;

import cn.oyzh.easyshell.terminal.redis.client.RedisClientTerminalCommandHandler;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientCachingTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "CACHING";
    }
}
