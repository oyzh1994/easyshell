package cn.oyzh.easyshell.terminal.redis.list;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisLrangeTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.LRANGE;
    }

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.LIST;
    }
}
