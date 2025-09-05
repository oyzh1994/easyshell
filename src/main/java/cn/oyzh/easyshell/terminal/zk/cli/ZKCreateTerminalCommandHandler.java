package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.CreateCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKCreateTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new CreateCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "create";
    }

    @Override
    public String commandArg() {
        return "[-s] [-e] [-c] path [data] [acl]";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.create", "base.node");
    }

    @Override
    public String commandHelp(ZKTerminalPane terminal) {
        return super.commandHelp(terminal) + "\n" +
                "-s sequential\n" +
                "-e ephemeral\n" +
                "-c container";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (terminal.getClient().isReadonly()) {
            return TerminalExecuteResult.fail(new ShellReadonlyOperationException());
        }
        return super.execute(command, terminal);
    }
}
