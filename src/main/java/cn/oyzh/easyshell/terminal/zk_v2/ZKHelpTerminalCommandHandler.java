package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKHelpTerminalCommandHandler extends HelpTerminalCommandHandler {

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, Terminal terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        List<String> list = new ArrayList<>();
        list.add("help");
        list.add("clear");
        list.addAll(ZKCommandRegistry.commandNames());
        result.setResult(TextUtil.format(list));
        return result;
    }
}
