package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT TRACKING 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClientTrackingTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "TRACKING";
    }
}
