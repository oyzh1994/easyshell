package cn.oyzh.easyshell.terminal.redis.client;

import redis.clients.jedis.Protocol;

/**
 * Redis CLIENT UNPAUSE 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientUnpuaseTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.UNPAUSE.name();
    }
}
