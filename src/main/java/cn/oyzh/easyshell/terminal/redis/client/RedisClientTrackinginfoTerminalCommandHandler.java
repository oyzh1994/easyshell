package cn.oyzh.easyshell.terminal.redis.client;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientTrackinginfoTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "TRACKING";
    }
}
