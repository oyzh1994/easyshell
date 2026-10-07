package cn.oyzh.easyshell.terminal.redis.client;

import redis.clients.jedis.Protocol;

/**
 * Redis CLIENT GETNAME 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientGetnameTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.GETNAME.name();
    }
}
