package cn.oyzh.easyshell.terminal.zk.fourletterword;

import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * zk四字命令 wchc 处理器
 *
 * @author oyzh
 * @since 2024/11/29
 */
public class ZKWchcCommandHandler extends ZKFourLetterWordCommandHandler<TerminalCommand> {

    /**
     * 四字命令
     */
    private final ZKFourLetterWordCommand furLetterWordCommand = new ZKWchcCommand();

    @Override
    public ZKFourLetterWordCommand furLetterWordCommand() {
        return this.furLetterWordCommand;
    }

}
