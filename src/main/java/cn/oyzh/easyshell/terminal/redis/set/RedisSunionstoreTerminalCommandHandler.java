package cn.oyzh.easyshell.terminal.redis.set;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisSunionstoreTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.SET;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SUNIONSTORE;
    }
}
