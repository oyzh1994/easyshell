package cn.oyzh.easyshell.terminal.dameng.basic;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class DamengShowDatabasesTerminalCommandHandler extends DamengTerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, DamengTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<DamengSchema> schemas = terminal.getClient().selectSchemas();
            String output = schemas.stream()
                    .map(DamengSchema::getName)
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
