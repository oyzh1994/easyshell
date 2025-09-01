package cn.oyzh.easyshell.terminal.redis.program;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisEval_r0TerminalCommandHandler extends cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.EVAL_RO;
    }
}
