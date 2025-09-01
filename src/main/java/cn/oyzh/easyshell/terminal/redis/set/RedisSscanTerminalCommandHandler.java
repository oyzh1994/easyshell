package cn.oyzh.easyshell.terminal.redis.set;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSscanTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SSCAN;
    }
}
