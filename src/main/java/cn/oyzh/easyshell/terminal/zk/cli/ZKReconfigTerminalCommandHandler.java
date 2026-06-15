package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.ReconfigCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKReconfigTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new ReconfigCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "reconfig";
    }

    @Override
    public String commandArg() {
        return "[-s] [-v version] [[-file path] | [-members serverID=host:port1:port2;port3[,...]*]] | [-add serverId=host:port1:port2;port3[,...]]* [-remove serverId[,...]*]";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.re", "base.config");
    }

    @Override
    public String commandHelp(ZKTerminalPane terminal) {
        return super.commandHelp(terminal) +
                terminal.lineEndingText() + "-s stats" +
                terminal.lineEndingText() + "-v required current config version" +
                terminal.lineEndingText() + "-file path of config file to parse for membership" +
                terminal.lineEndingText() + "-members comma-separated list of config strings for non-incremental reconfig" +
                terminal.lineEndingText() + "-add comma-separated list of config strings for new servers" +
                terminal.lineEndingText() + "-remove comma-separated list of server IDs to remove";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (terminal.getClient().isReadonly()) {
            return TerminalExecuteResult.fail(new ShellReadonlyOperationException());
        }
        return super.execute(command, terminal);
    }
}
