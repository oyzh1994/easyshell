package cn.oyzh.easyshell.terminal.redis.program;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis FCALL_RO 命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisFcall_r0TerminalCommandHandler extends cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.FCALL_RO;
    }
}
