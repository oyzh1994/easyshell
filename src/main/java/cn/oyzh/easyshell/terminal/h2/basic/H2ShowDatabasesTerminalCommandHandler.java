package cn.oyzh.easyshell.terminal.h2.basic;

import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * H2显示数据库命令处理器
 *
 * @author oyzh
 * @since 2026-11-04
 */
public class H2ShowDatabasesTerminalCommandHandler extends ShellH2TerminalCommandHandler<TerminalCommand> {

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
    public TerminalExecuteResult execute(TerminalCommand command, ShellH2TerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<String> schemas = terminal.getClient().selectSchemas()
                    .stream()
                    .map(H2Schema::getName)
                    .toList();
            String output = schemas.stream()
                    .collect(Collectors.joining(terminal.lineEndingText()));
            result.setResult(output);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
