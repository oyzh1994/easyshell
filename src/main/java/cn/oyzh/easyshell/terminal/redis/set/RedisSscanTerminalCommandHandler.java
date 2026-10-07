package cn.oyzh.easyshell.terminal.redis.set;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis SSCAN 命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSscanTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SSCAN;
    }
}
