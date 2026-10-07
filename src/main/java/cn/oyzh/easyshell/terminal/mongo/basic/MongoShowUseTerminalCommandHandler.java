package cn.oyzh.easyshell.terminal.mongo.basic;

import cn.oyzh.easyshell.terminal.mongo.MongoTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.MongoTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * mongo切换数据库命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
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
