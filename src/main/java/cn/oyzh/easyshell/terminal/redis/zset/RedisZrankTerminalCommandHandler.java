package cn.oyzh.easyshell.terminal.redis.zset;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis ZRANK 命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisZrankTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.ZRANK;
    }

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.ZSET;
    }
}
