package cn.oyzh.easyshell.terminal.redis.base;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/21
 */
public class RedisSelectTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.SELECT;
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, RedisTerminalPane terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            String[] args = command.getArgs();
            Integer dbIndex = Integer.valueOf(args[0]);
            Object obj = terminal.getClient().select(dbIndex);
            result.setResult(RedisTerminalUtil.formatOut(obj));
        } catch (Exception ex) {
            ex.printStackTrace();
            result.setException(ex);
        }
        return result;
    }
}
