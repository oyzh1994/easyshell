package cn.oyzh.easyshell.terminal.oracle.basic;

import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.terminal.oracle.OracleTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.oracle.OracleTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Oracle显示数据表命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class OracleShowTablesTerminalCommandHandler extends OracleTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, OracleTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<OracleTable> tables = terminal.getClient().selectTablesSimple(terminal.getSchema());
            String output = tables.stream()
                    .map(OracleTable::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
            if (terminal.getDbItem() != null && terminal.getDbItem().getTableTypeChild() != null) {
                terminal.getDbItem().getTableTypeChild().refreshChild();
            }
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
