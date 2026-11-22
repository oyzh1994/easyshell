package cn.oyzh.easyshell.terminal.sqlserver.basic;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.fx.db.util.DBTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * SqlServer切换模式命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class SqlServerUseTerminalCommandHandler extends SqlServerTerminalCommandHandler<TerminalCommand> {

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
        return "change current schema";
    }

    @Override
    public String commandHelp(SqlServerTerminalPane terminal) {
        return this.commandFullName() + terminal.lineEndingText()
                + "SQL: USE <schema>;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, SqlServerTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            String inputSchema = DBTerminalUtil.fixArg(command.getArgs()[1]);
            List<SqlServerSchema> schemas = terminal.getClient().selectSchemas();
            // SqlServer模式区分大小写，此处兼容未加引号时的小写输入
            String schema = schemas.stream()
                    .map(SqlServerSchema::getName)
                    .filter(name -> StringUtil.equalsIgnoreCase(name, inputSchema))
                    .findFirst()
                    .orElse(null);
            if (schema == null) {
                result.setResult("Schema invalid");
                return result;
            }
            // SQL Server没有search_path；终端后续语句通过connection(schema)
            // 使用该Schema对应的连接执行。
            terminal.setSchema(schema);
            result.setResult("Schema changed");
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
