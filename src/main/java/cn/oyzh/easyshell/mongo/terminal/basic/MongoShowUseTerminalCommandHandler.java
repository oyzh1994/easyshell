package cn.oyzh.easyshell.mongo.terminal.basic;

import cn.oyzh.easyshell.mongo.terminal.MongoTerminalCommandHandler;
import cn.oyzh.easyshell.mongo.terminal.MongoTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class MongoShowUseTerminalCommandHandler extends MongoTerminalCommandHandler<TerminalCommand> {

    @Override
    protected TerminalCommand parseCommand(String line, String[] args) {
        TerminalCommand terminalCommand = new TerminalCommand();
        terminalCommand.setArgs(args);
        terminalCommand.setCommand(line);
        return terminalCommand;
    }

    @Override
    protected boolean checkArgs(String[] args) throws RuntimeException {
        return args != null && args.length == 2;
    }

    @Override
    public String commandName() {
        return "use";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, MongoTerminalPane terminal) {
        terminal.setDbName(command.getArgs()[1]);
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult("switched to db " + terminal.getDbName());
        return result;
    }
}
