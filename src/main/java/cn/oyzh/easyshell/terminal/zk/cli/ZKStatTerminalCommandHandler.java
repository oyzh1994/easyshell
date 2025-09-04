package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.StatCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKStatTerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new StatCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "stat";
    }

    @Override
    public String commandArg() {
        return "path";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.get", "base.stat");
    }

}
