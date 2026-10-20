package cn.oyzh.easyshell.terminal.zk_v2.basic;

import cn.oyzh.easyshell.terminal.zk_v2.ZKTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk_v2.ZKTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.i18n.I18nHelper;

/**
 * 临时 ZooKeeper 终端连接命令。
 */
public class ZKConnectCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {

    @Override
    protected boolean checkArgs(String[] words) {
        return words != null && words.length >= 2 && words.length <= 6;
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        if (!terminal.isTemporary()) {
            terminal.outputByPrompt(I18nHelper.operationNotSupport());
        } else {
            if (terminal.isConnected()) {
                terminal.getClient().close();
            }
            terminal.connect(command.getContent());
        }
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setIgnoreOutput(true);
        return result;
    }

    @Override
    public String commandName() {
        return "connect";
    }

    @Override
    public String commandArg() {
        return "[-timeout timeout] [-server server] [-r]";
    }
}
