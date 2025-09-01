package cn.oyzh.easyshell.terminal.redis;


import cn.oyzh.fx.terminal.key.TerminalKeyHandler;

/**
 * @author oyzh
 * @since 2023/8/28
 */
public class RedisTerminalKeyHandler implements TerminalKeyHandler<RedisTerminalPane> {

    /**
     * 当前实例
     */
    public static final RedisTerminalKeyHandler INSTANCE = new RedisTerminalKeyHandler();

    // @Override
    // public boolean onEnterKeyPressed(RedisTerminalTextArea terminal) throws Exception {
    //     if (terminal.isTemporary() && !terminal.isConnected()) {
    //         String input = terminal.getInput();
    //         terminal.connect(input);
    //         terminal.saveHistory(input);
    //     } else if (!terminal.isConnecting()) {
    //         TerminalKeyHandler.super.onEnterKeyPressed(terminal);
    //     }
    //     return false;
    // }
}
