package cn.oyzh.easyshell.terminal.redis.stream.xgroup;

import cn.oyzh.easyshell.terminal.redis.stream.xgroup.RedisXgroupTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXgroupDestoryTerminalCommandHandler extends RedisXgroupTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.DESTROY.name();
    }
}
