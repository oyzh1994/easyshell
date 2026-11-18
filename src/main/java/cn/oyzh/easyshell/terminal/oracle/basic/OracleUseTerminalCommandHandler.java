package cn.oyzh.easyshell.terminal.oracle.basic;

import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.terminal.oracle.OracleTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.oracle.OracleTerminalPane;
import cn.oyzh.fx.db.util.DBTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * Oracle切换数据库命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class OracleUseTerminalCommandHandler extends OracleTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, OracleTerminalPane terminal) {
        terminal.setSchema(DBTerminalUtil.fixArg(command.getArgs()[1]));
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<OracleSchema> schemas = terminal.getClient().selectSchemas();
            List<String> dbs = schemas.stream().map(OracleSchema::getName).toList();
            if (dbs.contains(terminal.getSchema())) {
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
