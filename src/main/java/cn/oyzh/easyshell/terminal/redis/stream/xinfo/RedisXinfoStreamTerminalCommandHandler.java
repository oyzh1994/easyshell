package cn.oyzh.easyshell.terminal.redis.stream.xinfo;

import cn.oyzh.easyshell.terminal.redis.stream.xinfo.RedisXinfoTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXinfoStreamTerminalCommandHandler extends RedisXinfoTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.STREAM.name();
    }
}
