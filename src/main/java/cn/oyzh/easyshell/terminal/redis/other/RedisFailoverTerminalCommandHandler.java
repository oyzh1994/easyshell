package cn.oyzh.easyshell.terminal.redis.other;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/28
 */

public class RedisFailoverTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.FAILOVER;
    }

}
