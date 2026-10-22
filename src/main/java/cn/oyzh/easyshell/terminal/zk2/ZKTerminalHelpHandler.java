package cn.oyzh.easyshell.terminal.zk2;

import cn.oyzh.fx.terminal.help.BaseTerminalHelpHandler;

/**
 * ZooKeeper v2 终端帮助处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKTerminalHelpHandler extends BaseTerminalHelpHandler<ZKTerminalPane> {

    /** 当前实例。 */
    public static final ZKTerminalHelpHandler INSTANCE = new ZKTerminalHelpHandler();

    /**
     * 输出 ZooKeeper 命令帮助。
     */
    @Override
    public void help(String input, ZKTerminalPane terminal) {
        terminal.outputByPrompt(ZKHelpCommandHandler.render(input));
    }
}
