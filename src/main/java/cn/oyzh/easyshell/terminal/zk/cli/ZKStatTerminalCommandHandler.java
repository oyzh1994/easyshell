package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.StatCommand;

/**
 * zk stat 命令处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZKStatTerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    /**
     * cli命令
     */
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
