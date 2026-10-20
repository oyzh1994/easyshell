package cn.oyzh.easyshell.terminal.redis_v2;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 等宽终端中的 Redis 命令帮助处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisHelpTerminalCommandHandler extends HelpTerminalCommandHandler {

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, Terminal terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        List<String> list = new ArrayList<>();
        if (terminal instanceof RedisTerminalPane terminalPane) {
            list.addAll(terminalPane.getPrompts());
        }
        result.setResult(TextUtil.format(list));
        return result;
    }
}
