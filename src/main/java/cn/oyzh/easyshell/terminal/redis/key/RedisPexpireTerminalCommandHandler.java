package cn.oyzh.easyshell.terminal.redis.key;

import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis PEXPIRE 命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisPexpireTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.PEXPIRE;
    }

}
