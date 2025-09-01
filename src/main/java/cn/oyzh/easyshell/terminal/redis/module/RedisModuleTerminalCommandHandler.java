package cn.oyzh.easyshell.terminal.redis.module;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisModuleTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.MODULE;
    }

    @Override
    public String commandHelp(RedisTerminalPane terminal) {
        CommandObject<Object> object = RedisTerminalUtil.getCommand(this.getCommandType(), "HELP");
        Object obj = terminal.getClient().execCommand(object);
        return RedisTerminalUtil.formatOut(obj);
    }
}
