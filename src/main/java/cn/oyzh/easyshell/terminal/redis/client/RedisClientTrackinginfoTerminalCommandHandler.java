package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT TRACKINGINFO 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientTrackinginfoTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "TRACKING";
    }
}
