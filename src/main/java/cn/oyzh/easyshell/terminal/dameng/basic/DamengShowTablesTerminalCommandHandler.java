package cn.oyzh.easyshell.terminal.dameng.basic;

import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class DamengShowTablesTerminalCommandHandler extends DamengTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, DamengTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<DamengTable> tables = terminal.getClient().selectTablesSimple(terminal.getDbName());
            String output = tables.stream()
                    .map(DamengTable::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
