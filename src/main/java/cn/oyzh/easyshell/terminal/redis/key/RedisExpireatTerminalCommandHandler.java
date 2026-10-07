package cn.oyzh.easyshell.terminal.redis.key;

import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis EXPIREAT 命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisExpireatTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.EXPIREAT;
    }
}
