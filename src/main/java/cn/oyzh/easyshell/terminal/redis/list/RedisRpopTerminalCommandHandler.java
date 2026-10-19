package cn.oyzh.easyshell.terminal.redis.list;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis RPOP 命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisRpopTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.RPOP;
    }

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.LIST;
    }
}
