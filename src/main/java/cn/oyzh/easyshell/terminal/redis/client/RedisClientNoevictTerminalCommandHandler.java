package cn.oyzh.easyshell.terminal.redis.client;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientNoevictTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "NO-EVICT";
    }
}
