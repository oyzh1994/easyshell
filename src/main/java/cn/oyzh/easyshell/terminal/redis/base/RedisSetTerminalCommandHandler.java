package cn.oyzh.easyshell.terminal.redis.base;

import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis SET 命令处理器
 *
 * @author oyzh
 * @since 2023/7/27
 */

public class RedisSetTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SET;
    }
}
