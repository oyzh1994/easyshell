package cn.oyzh.easyshell.terminal.redis.memory;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.Protocol;

/**
 * Redis MEMORY 命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisMemoryTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.MEMORY;
    }

    @Override
    public String commandHelp(RedisTerminalPane terminal) {
        CommandObject<Object> object = RedisTerminalUtil.getCommand(this.getCommandType(), "HELP");
        Object obj = terminal.getClient().execCommand(object);
        return RedisTerminalUtil.formatOut(obj, terminal.lineEndingText());
    }
}
