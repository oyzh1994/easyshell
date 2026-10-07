package cn.oyzh.easyshell.terminal.redis.program;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis EVAL 命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisEvalTerminalCommandHandler extends cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.EVAL;
    }
}
