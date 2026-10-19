package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT CACHING 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClientCachingTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "CACHING";
    }
}
