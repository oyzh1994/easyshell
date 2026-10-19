package cn.oyzh.easyshell.terminal.redis.pubsub;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis PSUBSCRIBE 命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisPsubscribeTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.PSUBSCRIBE;
    }
}
