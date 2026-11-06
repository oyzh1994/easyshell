package cn.oyzh.easyshell.terminal.postgresql.basic;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL显示数据库命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class PostgresqlShowDatabasesTerminalCommandHandler extends PostgresqlTerminalCommandHandler<TerminalCommand> {

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
    public String commandDesc() {
        return "list databases";
    }

    @Override
    public String commandHelp(PostgresqlTerminalPane terminal) {
        return this.commandFullName() + terminal.lineEndingText() + "psql: \\l" + terminal.lineEndingText()
                + "SQL: SELECT datname FROM pg_database;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, PostgresqlTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = terminal.getClient().getConnManager().connection();
            statement = connection.createStatement();
            // 查询全部数据库，等价于psql的\l
            resultSet = statement.executeQuery("SELECT datname FROM pg_database ORDER BY datname");
            List<String> databases = new ArrayList<>();
            while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }
            result.setResult(String.join(terminal.lineEndingText(), databases));
        } catch (Exception ex) {
            result.setException(ex);
        } finally {
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        }
        return result;
    }
}
