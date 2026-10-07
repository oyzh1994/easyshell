package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT TRACKING 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientTrackingTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "TRACKINGINFO";
    }
}
