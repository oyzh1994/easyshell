package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.RemoveWatchesCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKRemoveWatchesTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new RemoveWatchesCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "removewatches";
    }

    @Override
    public String commandArg() {
        return "path [-c|-d|-a] [-l]";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.remove", "base.watch");
    }

    @Override
    public String commandHelp(ZKTerminalPane terminal) {
        return super.commandHelp(terminal) + "\n" +
                "-c child watcher type\n" +
                "-d data watcher type\n" +
                "-a any watcher type\n" +
                "-l remove locally when there is no server connection";
    }
}
