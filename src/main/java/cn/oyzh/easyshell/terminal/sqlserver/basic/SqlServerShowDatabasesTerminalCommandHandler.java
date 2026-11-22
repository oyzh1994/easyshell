package cn.oyzh.easyshell.terminal.sqlserver.basic;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer显示数据库命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class SqlServerShowDatabasesTerminalCommandHandler extends SqlServerTerminalCommandHandler<TerminalCommand> {

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
    public String commandHelp(SqlServerTerminalPane terminal) {
        return this.commandFullName() + terminal.lineEndingText()
                + "SQL: SELECT name FROM sys.databases;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, SqlServerTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = terminal.getClient().getConnManager().connection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT name FROM sys.databases WHERE state_desc = 'ONLINE' ORDER BY name");
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
