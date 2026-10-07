package cn.oyzh.easyshell.terminal.redis.other;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis SAVE 命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisSaveTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SAVE;
    }
}
