package cn.oyzh.easyshell.terminal.redis.pubsub;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis PUBSUB 命令处理器
 *
 * @author oyzh
 * @since 2023/7/27
 */

public class RedisPubsubTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.PUBSUB;
    }
}
