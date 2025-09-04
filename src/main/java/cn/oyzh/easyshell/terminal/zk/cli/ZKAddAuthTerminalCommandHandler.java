package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.AddAuthCommand;
import org.apache.zookeeper.cli.CliCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKAddAuthTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new AddAuthCommand();

    @Override
    public String commandName() {
        return "addauth";
    }

    @Override
    public String commandArg() {
        return "scheme auth";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.add", "base.auth");
    }

    @Override
    protected CliCommand cliCommand() {
        return this.cliCommand;
    }
}
