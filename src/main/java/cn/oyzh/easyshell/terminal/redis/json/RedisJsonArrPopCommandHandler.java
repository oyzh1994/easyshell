package cn.oyzh.easyshell.terminal.redis.json;

import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.json.JsonProtocol;

/**
 * Redis JSON.ARRPOP 命令处理器
 *
 * @author oyzh
 * @since 2025/10/24
 */
public class RedisJsonArrPopCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected ShellRedisKeyType getKeyType() {
        return ShellRedisKeyType.JSON;
    }

    @Override
    public ProtocolCommand getCommandType() {
        return JsonProtocol.JsonCommand.ARRPOP;
    }
}
