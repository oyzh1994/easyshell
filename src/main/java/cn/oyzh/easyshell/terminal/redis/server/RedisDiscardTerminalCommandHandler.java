package cn.oyzh.easyshell.terminal.redis.server;

import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis DISCARD 命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisDiscardTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.DISCARD;
    }
}
