package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * @author oyzh
 * @since 2024/11/29
 */
public class ZKWchpCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKWchpCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }
}
