package cn.oyzh.easyshell.terminal.redis.set;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis SDIFFSTORE 命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisSdiffstoreTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.SET;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SDIFFSTORE;
    }
}
