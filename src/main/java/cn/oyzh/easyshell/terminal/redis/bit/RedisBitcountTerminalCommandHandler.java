package cn.oyzh.easyshell.terminal.redis.bit;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis BITCOUNT 命令处理器
 *
 * @author oyzh
 * @since 2023/7/27
 */
public class RedisBitcountTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.STRING;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.BITCOUNT;
    }
}
