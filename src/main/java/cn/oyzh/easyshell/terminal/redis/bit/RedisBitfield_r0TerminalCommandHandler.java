package cn.oyzh.easyshell.terminal.redis.bit;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/27
 */

public class RedisBitfield_r0TerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.STRING;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.BITFIELD_RO;
    }
}
