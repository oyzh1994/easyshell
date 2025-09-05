package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.SyncCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKSyncTerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new SyncCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "sync";
    }

    @Override
    public String commandArg() {
        return "path";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.sync", "base.node");
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (terminal.getClient().isReadonly()) {
            return TerminalExecuteResult.fail(new ShellReadonlyOperationException());
        }
        return super.execute(command, terminal);
    }

}
