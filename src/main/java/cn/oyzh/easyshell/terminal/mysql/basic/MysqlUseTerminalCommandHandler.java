package cn.oyzh.easyshell.terminal.mysql.basic;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * mysql切换数据库命令处理器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class MysqlUseTerminalCommandHandler extends MysqlTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, MysqlTerminalPane terminal) {
        terminal.setDbName(command.getArgs()[1]);
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        List<MysqlDatabase> databases = terminal.getClient().databases();
        List<String> dbs = databases.stream()
                .map(MysqlDatabase::getName)
                .toList();
        if (dbs.contains(terminal.getDbName())) {
            result.setResult("Database changed");
        } else {
            result.setResult("Database invalid");
        }
        return result;
    }
}
