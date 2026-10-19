package cn.oyzh.easyshell.terminal.redis.stream.xgroup;

import redis.clients.jedis.Protocol;

/**
 * Redis XGROUP CREATE 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisXgroupCreateTerminalCommandHandler extends RedisXgroupTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.CREATE.name();
    }
}
