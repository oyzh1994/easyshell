package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKFourLetterWordCommand;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKKillCommand;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * @author oyzh
 * @since 2024/11/29
 */
public class ZKKillCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKKillCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }

}
