package cn.oyzh.easyshell.terminal.redis.stream.xinfo;

import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXinfoGroupsTerminalCommandHandler extends RedisXinfoTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.GROUPS.name();
    }
}
