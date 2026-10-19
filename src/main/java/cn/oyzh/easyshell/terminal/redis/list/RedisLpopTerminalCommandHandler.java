package cn.oyzh.easyshell.terminal.redis.list;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis LPOP 命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisLpopTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.LPOP;
    }

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.LIST;
    }
}
