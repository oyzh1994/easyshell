package cn.oyzh.easyshell.terminal.redis.stream.xgroup;

import redis.clients.jedis.Protocol;

/**
 * Redis XGROUP SETID 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXgroupSetidTerminalCommandHandler extends RedisXgroupTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.SETID.name();
    }
}
