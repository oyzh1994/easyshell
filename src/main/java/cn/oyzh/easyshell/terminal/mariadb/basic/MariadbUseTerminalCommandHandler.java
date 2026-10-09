package cn.oyzh.easyshell.terminal.mariadb.basic;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * MariaDB切换数据库命令处理器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbUseTerminalCommandHandler extends MariadbTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, MariadbTerminalPane terminal) {
        terminal.setDbName(command.getArgs()[1]);
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        List<MariadbDatabase> databases = terminal.getClient().databases();
        List<String> dbs = databases.stream()
                .map(MariadbDatabase::getName)
                .toList();
        if (dbs.contains(terminal.getDbName())) {
            result.setResult("Database changed");
        } else {
            result.setResult("Database invalid");
        }
        return result;
    }
}
