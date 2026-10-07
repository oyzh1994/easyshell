package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.CloseCommand;

/**
 * zk close 命令处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZKCloseTerminalCommandHandler extends ZKCliTerminalCommandHandler<TerminalCommand> {

    /**
     * cli命令
     */
    private final CliCommand cliCommand = new CloseCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public String commandName() {
        return "close";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.close", "base.connect");
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        try {
            return super.execute(command, terminal);
        } finally {
            // ShellZKEventUtil.terminalClose(terminal.getClient());
        }
    }
}
