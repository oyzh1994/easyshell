package cn.oyzh.easyshell.mongo.terminal.basic;

import cn.oyzh.easyshell.mongo.terminal.MongoTerminalCommandHandler;
import cn.oyzh.easyshell.mongo.terminal.MongoTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class MongoShowCollectionsTerminalCommandHandler extends MongoTerminalCommandHandler<TerminalCommand> {

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
        return "show";
    }

    @Override
    public String commandSubName() {
        return "collections";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, MongoTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult(terminal.getClient().listCollectionNames(terminal.getDbName()));
        return result;
    }
}
