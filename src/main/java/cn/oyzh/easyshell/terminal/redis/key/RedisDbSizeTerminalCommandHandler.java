package cn.oyzh.easyshell.terminal.redis.key;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/25
 */

public class RedisDbSizeTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.DBSIZE;
    }
}
