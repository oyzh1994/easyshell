package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.WhoAmICommand;

/**
 * zk whoami 命令处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZKWhoAmITerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    /**
     * cli命令
     */
    private final CliCommand cliCommand = new WhoAmICommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "whoami";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.get", "base.connected", "base.userInfo");
    }
}
