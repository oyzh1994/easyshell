package cn.oyzh.easyshell.terminal.redis.hash;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/27
 */

public class RedisHsetCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.HASH;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.HSET;
    }
}
