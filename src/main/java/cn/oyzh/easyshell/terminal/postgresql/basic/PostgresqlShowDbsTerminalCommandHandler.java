package cn.oyzh.easyshell.terminal.postgresql.basic;

import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL显示模式命令处理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlShowDbsTerminalCommandHandler extends PostgresqlShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }

    @Override
    public String commandDesc() {
        return "list schemas";
    }

    @Override
    public String commandHelp(PostgresqlTerminalPane terminal) {
        return "show dbs;" + terminal.lineEndingText() + "psql: \\dn" + terminal.lineEndingText()
                + "SQL: SELECT nspname FROM pg_namespace;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, PostgresqlTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            // PostgreSQL中模式即数据库对象命名空间，等价于psql的\dn
            List<PostgresqlSchema> schemas = terminal.getClient().selectSchemas();
            String output = schemas.stream()
                    .map(PostgresqlSchema::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
