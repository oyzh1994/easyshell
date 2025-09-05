package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.SetCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKSetTerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new SetCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "set";
    }

    @Override
    public String commandArg() {
        return "[-s] [-v version] path data";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.set", "base.data");
    }

    @Override
    public String commandHelp(ZKTerminalPane terminal) {
        return super.commandHelp(terminal)  + "\n" +
                "-s stats\n" +
                "-v version";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (terminal.getClient().isReadonly()) {
            return TerminalExecuteResult.fail(new ShellReadonlyOperationException());
        }
        return super.execute(command, terminal);
    }
}
