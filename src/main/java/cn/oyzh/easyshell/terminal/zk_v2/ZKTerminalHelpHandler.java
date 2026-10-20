package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.fx.terminal.help.BaseTerminalHelpHandler;

/**
 * ZooKeeper v2 终端帮助处理器。
 */
public class ZKTerminalHelpHandler extends BaseTerminalHelpHandler<ZKTerminalPane> {

    public static final ZKTerminalHelpHandler INSTANCE = new ZKTerminalHelpHandler();

    @Override
    public void help(String input, ZKTerminalPane terminal) {
        terminal.outputByPrompt(ZKHelpCommandHandler.render(input));
    }
}
