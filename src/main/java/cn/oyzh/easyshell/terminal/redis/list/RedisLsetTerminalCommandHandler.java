package cn.oyzh.easyshell.terminal.redis.list;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisLsetTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.LSET;
    }

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.LIST;
    }
}
