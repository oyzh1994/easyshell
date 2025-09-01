package cn.oyzh.easyshell.terminal.redis.base;

import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.i18n.I18nHelper;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/12/13
 */
public class RedisConnectTerminalCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    protected TerminalCommand parseCommand(String line, String[] args) {
        TerminalCommand terminalCommand = new TerminalCommand();
        terminalCommand.setArgs(args);
        terminalCommand.setCommand(line);
        return terminalCommand;
    }

    @Override
    protected boolean checkArgs(String[] args) throws RuntimeException {
        return args != null;
    }

    @Override
    public String commandName() {
        return "connect";
    }

    @Override
    public Protocol.Command getCommandType() {
        return null;
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, RedisTerminalPane terminal) {
        if (terminal.isTemporary()) {
            if (terminal.isConnected()) {
                terminal.getClient().closeQuiet();
            }
            terminal.connect(command.getCommand());
        } else {
            terminal.outputByPrompt(I18nHelper.operationNotSupport());
        }
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setIgnoreOutput(true);
        return result;
    }
}
