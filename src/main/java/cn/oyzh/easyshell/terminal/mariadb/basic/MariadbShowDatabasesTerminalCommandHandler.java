package cn.oyzh.easyshell.terminal.mariadb.basic;

import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MariaDB显示数据库命令处理器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbShowDatabasesTerminalCommandHandler extends MariadbTerminalCommandHandler<TerminalCommand> {

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
        return "databases;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, MariadbTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<String> databases = terminal.getClient().databaseNames();
            String output = databases
                    .stream()
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
