package cn.oyzh.easyshell.terminal.redis.string;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis INCRBY 命令处理器
 *
 * @author oyzh
 * @since 2023/7/27
 */

public class RedisIncrbyTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.INCRBY;
    }

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.STRING;
    }
}
