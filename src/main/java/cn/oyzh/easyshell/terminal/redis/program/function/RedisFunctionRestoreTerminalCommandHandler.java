package cn.oyzh.easyshell.terminal.redis.program.function;

/**
 * Redis FUNCTION RESTORE 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisFunctionRestoreTerminalCommandHandler extends RedisFunctionTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "RESTORE";
    }
}
