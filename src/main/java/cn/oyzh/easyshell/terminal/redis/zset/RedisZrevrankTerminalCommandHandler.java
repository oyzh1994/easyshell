package cn.oyzh.easyshell.terminal.redis.zset;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisZrevrankTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.ZSET;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.ZREVRANK;
    }
}
