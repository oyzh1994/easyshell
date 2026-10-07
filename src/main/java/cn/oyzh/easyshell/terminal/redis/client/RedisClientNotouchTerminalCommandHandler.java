package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT NO-TOUCH 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisClientNotouchTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "NO-TOUCH";
    }
}
