package cn.oyzh.easyshell.terminal.dameng.basic;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class DamengUseTerminalCommandHandler extends DamengTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, DamengTerminalPane terminal) {
        terminal.setDbName(command.getArgs()[1]);
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<DamengSchema> schemas = terminal.getClient().schemas();
            List<String> dbs = schemas.stream()
                    .map(DamengSchema::getName)
                    .toList();
            if (dbs.contains(terminal.getDbName())) {
                result.setResult("Database invalid");
            } else {
                result.setResult("Database changed");
            }
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
