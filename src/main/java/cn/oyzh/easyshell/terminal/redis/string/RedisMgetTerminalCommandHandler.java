package cn.oyzh.easyshell.terminal.redis.string;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisMgetTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.MGET;
    }

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.STRING;
    }
}
