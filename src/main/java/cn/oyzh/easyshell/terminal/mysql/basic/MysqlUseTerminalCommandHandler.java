package cn.oyzh.easyshell.terminal.mysql.basic;

import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class MysqlUseTerminalCommandHandler extends MysqlTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, MysqlTerminalPane terminal) {
        terminal.setDbName(command.getArgs()[1]);
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult("Database changed");
        return result;
    }
}
