package cn.oyzh.easyshell.terminal.redis_v2;

import cn.oyzh.fx.terminal.help.BaseTerminalHelpHandler;

/**
 * Redis v2 终端帮助处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisTerminalHelpHandler extends BaseTerminalHelpHandler<RedisTerminalPane> {

    /** 当前实例。 */
    public static final RedisTerminalHelpHandler INSTANCE = new RedisTerminalHelpHandler();

    @Override
    public void help(String input, RedisTerminalPane terminal) {
        terminal.outputByPrompt(RedisHelpCommandHandler.render(input, terminal.registry(), terminal.lineEndingText()));
    }
}
