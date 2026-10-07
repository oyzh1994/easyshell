package cn.oyzh.easyshell.terminal.redis.list;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis RPUSH 命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisRpushTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.RPUSH;
    }

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.LIST;
    }
}
