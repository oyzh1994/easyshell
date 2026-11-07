package cn.oyzh.easyshell.terminal.valkey2;

import cn.oyzh.fx.terminal.help.BaseTerminalHelpHandler;

/**
 * Valkey v2 终端帮助处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ValkeyTerminalHelpHandler extends BaseTerminalHelpHandler<ValkeyTerminalPane> {

    /** 当前实例。 */
    public static final ValkeyTerminalHelpHandler INSTANCE = new ValkeyTerminalHelpHandler();

    @Override
    public void help(String input, ValkeyTerminalPane terminal) {
        terminal.outputByPrompt(ValkeyHelpCommandHandler.render(input, terminal.registry(), terminal.lineEndingText()));
    }
}
