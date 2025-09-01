package cn.oyzh.easyshell.terminal.redis.program.function;

import cn.oyzh.easyshell.terminal.redis.program.function.RedisFunctionTerminalCommandHandler;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisFunctionRestoreTerminalCommandHandler extends RedisFunctionTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "RESTORE";
    }
}
