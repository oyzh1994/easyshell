package cn.oyzh.easyshell.terminal.zk;

import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.terminal.key.TerminalKeyHandler;

/**
 * @author oyzh
 * @since 2023/8/28
 */
public class ZKTerminalKeyHandler implements TerminalKeyHandler<ZKTerminalPane> {

    /**
     * 当前实例
     */
    public static final ZKTerminalKeyHandler INSTANCE = new ZKTerminalKeyHandler();

//    @Override
//    public boolean onEnterKeyPressed(ZKTerminalPane terminal) throws Exception {
//        String input = terminal.getInput();
//        if (terminal.isTemporary() && !terminal.isConnected()) {
//            terminal.connect(input);
//            terminal.saveHistory(input);
//        } else if (terminal.isConnected()) {
//            if (StringUtil.isEmpty(input)) {
//                HelpTerminalCommandHandler commandHandler = SpringUtil.getBean(HelpTerminalCommandHandler.class);
//                TerminalExecuteResult result = commandHandler.execute(null, terminal);
//                terminal.appendLine((String) result.getResult());
//                terminal.outputPrompt();
//            } else {
//                TerminalKeyHandler.super.onEnterKeyPressed(terminal);
//            }
//        }
//        return false;
//    }
}
