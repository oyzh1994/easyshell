package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.DelQuotaCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKDelQuotaTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new DelQuotaCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "delquota";
    }

    @Override
    public String commandArg() {
        return "[-n|-b] path";
    }

    @Override
    public String commandDesc() {
        // return "删除配额";
        return I18nResourceBundle.i18nString("base.delete", "base.quota");
    }

    @Override
    public String commandHelp(ZKTerminalPane terminal) {
        return super.commandHelp(terminal)  + "\n" +
                "-n num quota\n" +
                "-b bytes quota";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (terminal.getClient().isReadonly()) {
            return TerminalExecuteResult.fail(new ShellReadonlyOperationException());
        }
        return super.execute(command, terminal);
    }
}
