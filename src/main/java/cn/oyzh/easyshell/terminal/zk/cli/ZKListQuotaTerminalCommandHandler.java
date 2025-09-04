package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.ListQuotaCommand;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKListQuotaTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new ListQuotaCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "listquota";
    }

    @Override
    public String commandArg() {
        return "path";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.iter", "base.quota");
    }
}
