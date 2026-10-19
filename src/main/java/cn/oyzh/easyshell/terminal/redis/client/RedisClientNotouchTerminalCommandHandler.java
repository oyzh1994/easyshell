package cn.oyzh.easyshell.terminal.redis.client;

/**
 * Redis CLIENT NO-TOUCH 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisClientNotouchTerminalCommandHandler extends RedisClientTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "NO-TOUCH";
    }
}
