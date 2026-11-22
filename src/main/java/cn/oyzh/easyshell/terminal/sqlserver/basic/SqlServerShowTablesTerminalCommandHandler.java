package cn.oyzh.easyshell.terminal.sqlserver.basic;

import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SqlServer显示数据表命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class SqlServerShowTablesTerminalCommandHandler extends SqlServerTerminalCommandHandler<TerminalCommand> {

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
    public String commandDesc() {
        return "list tables in current schema";
    }

    @Override
    public String commandHelp(SqlServerTerminalPane terminal) {
        return this.commandFullName() + terminal.lineEndingText()
                + "SQL: SELECT name FROM sys.tables;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, SqlServerTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            // 展示当前Schema下的表
            List<SqlServerTable> tables = terminal.getClient().selectTablesSimple(terminal.getSchema());
            String output = tables.stream()
                    .map(SqlServerTable::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
