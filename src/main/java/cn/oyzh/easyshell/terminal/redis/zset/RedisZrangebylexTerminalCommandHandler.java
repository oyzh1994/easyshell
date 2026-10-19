package cn.oyzh.easyshell.terminal.redis.zset;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis ZRANGEBYLEX 命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisZrangebylexTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.ZSET;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.ZRANGEBYLEX;
    }
}
