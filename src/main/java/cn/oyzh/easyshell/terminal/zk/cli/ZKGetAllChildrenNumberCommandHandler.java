package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.GetAllChildrenNumberCommand;

/**
 * @author oyzh
 * @since 2023/12/21
 */
public class ZKGetAllChildrenNumberCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new GetAllChildrenNumberCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "getAllChildrenNumber";
    }

    @Override
    public String commandArg() {
        return "path";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.get", "base.all", "base.childNumber");
    }

    @Override
    public String commandSupportedVersion() {
        return "3.5.0";
    }
}
