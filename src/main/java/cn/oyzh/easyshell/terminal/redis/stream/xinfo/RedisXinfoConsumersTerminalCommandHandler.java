package cn.oyzh.easyshell.terminal.redis.stream.xinfo;

import redis.clients.jedis.Protocol;

/**
 * Redis XINFO CONSUMERS 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXinfoConsumersTerminalCommandHandler extends RedisXinfoTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.CONSUMERS.name();
    }
}
