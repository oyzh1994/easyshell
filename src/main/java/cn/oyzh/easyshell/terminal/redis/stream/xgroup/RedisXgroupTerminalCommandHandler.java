package cn.oyzh.easyshell.terminal.redis.stream.xgroup;

import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisKeyTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisXgroupTerminalCommandHandler extends RedisKeyTerminalCommandHandler<TerminalCommand> {

    @Override
    protected RedisKeyType getKeyType() {
        return RedisKeyType.STREAM;
    }

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.XGROUP;
    }

    @Override
    public String commandHelp(RedisTerminalPane terminal) {
        CommandObject<Object> object = RedisTerminalUtil.getCommand(this.getCommandType(), "HELP");
        Object obj = terminal.getClient().execCommand(object);
        return RedisTerminalUtil.formatOut(obj);
    }
}
