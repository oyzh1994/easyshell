package cn.oyzh.easyshell.terminal.redis.stream.xinfo;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXinfoConsumersTerminalCommandHandler extends RedisXinfoTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.CONSUMERS.name();
    }
}
