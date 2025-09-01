package cn.oyzh.easyshell.terminal.redis.server.config;

import cn.oyzh.easyshell.terminal.redis.server.config.RedisConfigTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisConfigResetStatTerminalCommandHandler extends RedisConfigTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.RESETSTAT.name();
    }
}
