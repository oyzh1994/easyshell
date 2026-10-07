package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.ListQuotaCommand;

/**
 * zk listquota 命令处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZKListQuotaTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    /**
     * cli命令
     */
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
