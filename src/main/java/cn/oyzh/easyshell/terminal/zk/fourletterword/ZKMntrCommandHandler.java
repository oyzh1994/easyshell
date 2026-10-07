package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * zk四字命令 mntr 处理器
 *
 * @author oyzh
 * @since 2024/11/29
 */
public class ZKMntrCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    /**
     * 四字命令
     */
    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKMntrCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }

}
