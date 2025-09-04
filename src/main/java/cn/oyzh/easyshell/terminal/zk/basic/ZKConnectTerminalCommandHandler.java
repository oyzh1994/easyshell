package cn.oyzh.easyshell.terminal.zk.basic;

import cn.oyzh.easyshell.terminal.zk.ZKTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKConnectTerminalCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {

    @Override
    protected TerminalCommand parseCommand(String line, String[] args) {
        TerminalCommand terminalCommand = new TerminalCommand();
        terminalCommand.setArgs(args);
        terminalCommand.setCommand(line);
        return terminalCommand;
    }

    @Override
    protected boolean checkArgs(String[] args) throws RuntimeException {
        return args != null && args.length >= 2 && args.length <= 6;
    }

    @Override
    public String commandName() {
        return "connect";
    }

    @Override
    public String commandDesc() {
        // return "开始连接";
        return I18nResourceBundle.i18nString("base.startConnect");
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (terminal.isTemporary()) {
            if (terminal.isConnected()) {
                terminal.getClient().closeQuiet();
            }
            terminal.connect(command.getCommand());
        } else {
            terminal.outputByPrompt(I18nHelper.operationNotSupport());
        }
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setIgnoreOutput(true);
        return result;
    }
}
