package cn.oyzh.easyshell.terminal.redis.stream.xinfo;

import redis.clients.jedis.Protocol;

/**
 * Redis XINFO STREAM 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisXinfoStreamTerminalCommandHandler extends RedisXinfoTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.STREAM.name();
    }
}
