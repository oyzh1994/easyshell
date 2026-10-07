package cn.oyzh.easyshell.terminal.redis.stream.xgroup;

import redis.clients.jedis.Protocol;

/**
 * Redis XGROUP DESTROY 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXgroupDestoryTerminalCommandHandler extends RedisXgroupTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.DESTROY.name();
    }
}
