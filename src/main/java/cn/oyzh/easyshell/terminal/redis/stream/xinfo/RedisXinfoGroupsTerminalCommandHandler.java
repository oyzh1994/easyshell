package cn.oyzh.easyshell.terminal.redis.stream.xinfo;

import redis.clients.jedis.Protocol;

/**
 * Redis XINFO GROUPS 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXinfoGroupsTerminalCommandHandler extends RedisXinfoTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.GROUPS.name();
    }
}
