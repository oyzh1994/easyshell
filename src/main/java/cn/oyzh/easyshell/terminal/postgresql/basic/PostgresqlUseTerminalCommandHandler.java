package cn.oyzh.easyshell.terminal.postgresql.basic;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBTerminalUtil;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * PostgreSQL切换模式命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class PostgresqlUseTerminalCommandHandler extends PostgresqlTerminalCommandHandler<TerminalCommand> {

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
    public String commandSubName() {
        return "<schema>;";
    }

    @Override
    public String commandDesc() {
        return "change current schema (search_path)";
    }

    @Override
    public String commandHelp(PostgresqlTerminalPane terminal) {
        return this.commandFullName() + terminal.lineEndingText() + "psql: \\c <schema>" + terminal.lineEndingText()
                + "SQL: SET search_path TO \"schema\";";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, PostgresqlTerminalPane terminal) {
        terminal.setSchema(DBTerminalUtil.fixArg(command.getArgs()[1]));
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<PostgresqlSchema> schemas = terminal.getClient().selectSchemas();
            // PostgreSQL模式区分大小写，此处兼容未加引号时的小写输入
            String schema = schemas.stream()
                    .map(PostgresqlSchema::getName)
                    .filter(name -> StringUtil.equalsIgnoreCase(name, terminal.getSchema()))
                    .findFirst()
                    .orElse(null);
            if (schema != null) {
                terminal.setSchema(schema);
                // 切换当前连接的search_path
                Connection connection = terminal.getClient().getConnManager().connection(schema);
                Statement statement = null;
                try {
                    statement = connection.createStatement();
                    statement.execute("SET search_path TO " + DBUtil.wrap(schema, DBDialect.POSTGRESQL));
                } finally {
                    IOUtil.close(statement);
                }
                result.setResult("Schema changed");
            } else {
                result.setResult("Schema invalid");
            }
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
