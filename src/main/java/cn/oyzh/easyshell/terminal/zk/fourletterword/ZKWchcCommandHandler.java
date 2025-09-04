package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKFourLetterWordCommand;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKFourLetterWordCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKWchcCommand;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * @author oyzh
 * @since 2024/11/29
 */
public class ZKWchcCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKWchcCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }

}
