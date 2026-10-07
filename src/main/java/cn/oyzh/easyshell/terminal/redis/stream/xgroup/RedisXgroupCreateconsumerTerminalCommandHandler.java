package cn.oyzh.easyshell.terminal.redis.stream.xgroup;

import redis.clients.jedis.Protocol;

/**
 * Redis XGROUP CREATECONSUMER 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisXgroupCreateconsumerTerminalCommandHandler extends RedisXgroupTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.CREATECONSUMER.name();
    }
}
