package cn.oyzh.easyshell.terminal.redis.program.function;

/**
 * Redis FUNCTION RESTORE 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisFunctionRestoreTerminalCommandHandler extends RedisFunctionTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "RESTORE";
    }
}
