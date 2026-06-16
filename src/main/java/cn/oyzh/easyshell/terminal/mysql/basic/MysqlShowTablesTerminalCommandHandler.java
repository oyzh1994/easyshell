package cn.oyzh.easyshell.terminal.mysql.basic;

import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class MysqlShowTablesTerminalCommandHandler extends MysqlTerminalCommandHandler<TerminalCommand> {

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
        return "tables;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, MysqlTerminalPane terminal) {
        return terminal.eval("SHOW TABLES;");
    }
}
