package cn.oyzh.easyshell.terminal.redis.json;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.json.JsonProtocol;

/**
 * @author oyzh
 * @since 2025/10/24
 */
public class RedisJsonStrLenCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.JSON;
    }

    @Override
    public ProtocolCommand getCommandType() {
        return JsonProtocol.JsonCommand.STRLEN;
    }
}
