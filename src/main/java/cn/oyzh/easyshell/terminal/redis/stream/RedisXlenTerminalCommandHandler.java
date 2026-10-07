package cn.oyzh.easyshell.terminal.redis.stream;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis XLEN 命令处理器
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXlenTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.STREAM;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.XLEN;
    }
}
