package cn.oyzh.easyshell.terminal.sqlserver.basic;

import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SqlServer显示模式命令处理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerShowDbsTerminalCommandHandler extends SqlServerShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }

    @Override
    public String commandDesc() {
        return "list schemas";
    }

    @Override
    public String commandHelp(SqlServerTerminalPane terminal) {
        return "show dbs;" + terminal.lineEndingText()
                + "SQL: SELECT name FROM sys.schemas;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, SqlServerTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            // SQL Server Schema是数据库内的对象命名空间
            List<SqlServerSchema> schemas = terminal.getClient().selectSchemas();
            String output = schemas.stream()
                    .map(SqlServerSchema::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
