package cn.oyzh.easyshell.terminal.redis.hash;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis HINCRBYFLOAT 命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class RedisHincrbyfloatTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.HASH;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.HINCRBYFLOAT;
    }
}
