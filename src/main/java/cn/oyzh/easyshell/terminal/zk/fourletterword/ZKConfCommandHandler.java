package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * zk四字命令 conf 处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZKConfCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    /**
     * 四字命令
     */
    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKConfCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }

}
