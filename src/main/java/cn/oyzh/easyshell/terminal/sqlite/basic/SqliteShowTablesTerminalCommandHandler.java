package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQLite显示数据表命令处理器
 *
 * @author oyzh
 * @since 2026-11-04
 */
public class SqliteShowTablesTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<SQLiteTable> tables = terminal.getClient().selectTablesSimple(terminal.getDbName());
            String output = tables.stream()
                    .map(SQLiteTable::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
