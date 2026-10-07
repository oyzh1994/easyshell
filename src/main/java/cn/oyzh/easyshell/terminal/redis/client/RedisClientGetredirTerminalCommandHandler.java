package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT GETREDIR 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientGetredirTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "GETREDIR";
    }
}
