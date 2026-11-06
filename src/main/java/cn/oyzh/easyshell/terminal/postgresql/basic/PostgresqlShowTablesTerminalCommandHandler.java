package cn.oyzh.easyshell.terminal.postgresql.basic;

import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL显示数据表命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class PostgresqlShowTablesTerminalCommandHandler extends PostgresqlTerminalCommandHandler<TerminalCommand> {

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
    public String commandHelp(PostgresqlTerminalPane terminal) {
        return this.commandFullName() + terminal.lineEndingText() + "psql: \\dt" + terminal.lineEndingText()
                + "SQL: SELECT tablename FROM pg_tables WHERE schemaname = current_schema();";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, PostgresqlTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            // 展示当前模式（search_path）下的表
            List<PostgresqlTable> tables = terminal.getClient().selectTablesSimple(terminal.getSchema());
            String output = tables.stream()
                    .map(PostgresqlTable::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
