package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKConfCommand;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * @author oyzh
 * @since 2024/11/29
 */
public class ZKConfCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKConfCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }

}
