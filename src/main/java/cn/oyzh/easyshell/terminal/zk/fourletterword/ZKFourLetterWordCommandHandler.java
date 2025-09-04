package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKFourLetterWordCommand;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalUtil;

/**
 * @author oyzh
 * @since 2024/11/29
 */
public abstract class ZKFourLetterWordCommandHandler<C extends TerminalCommand> extends ZKTerminalCommandHandler<C> {

    protected abstract ZKFourLetterWordCommand furLetterWordCommand();

    @Override
    public String commandName() {
        ZKFourLetterWordCommand cmd = this.furLetterWordCommand();
        if (cmd.getAlias() != null) {
            return cmd.getAlias();
        }
        return cmd.getCmd();
    }

    @Override
    public String commandDesc() {
        return "FourLetterWord Command " + this.furLetterWordCommand().getCmd();
    }

    @Override
    public C parseCommand(String line) {
        String[] args = TerminalUtil.split(line);
        TerminalCommand command = new TerminalCommand();
        command.setArgs(args);
        return (C) command;
    }

    @Override
    public TerminalExecuteResult execute(C command, ZKTerminalPane terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            terminal.disable();
            ZKConnect connect = terminal.zkConnect();
            String execResult = this.furLetterWordCommand().exec(connect.hostIp(), connect.hostPort());
            result.setResult(execResult);
        } catch (Exception ex) {
            result.setException(ex);
        } finally {
            terminal.enable();
        }
        return result;
    }
}
