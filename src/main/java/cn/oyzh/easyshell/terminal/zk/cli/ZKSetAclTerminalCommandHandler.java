package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.exception.ReadonlyOperationException;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.SetAclCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKSetAclTerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new SetAclCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "setAcl";
    }

    @Override
    public String commandArg() {
        return "[-s] [-v version] path acl";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.set", "base.acl");
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
            return TerminalExecuteResult.fail(new ReadonlyOperationException());
        }
        return super.execute(command, terminal);
    }
}
