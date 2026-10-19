package cn.oyzh.easyshell.terminal.zk;

import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.BaseTerminalCompleteHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

import java.util.List;

/**
 * zk终端补全处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZKTerminalCompleteHandler extends BaseTerminalCompleteHandler<ZKTerminalPane> {

    @Override
    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(ZKTerminalPane terminal, String line) {
        if (line.contains(" /")) {
            return TerminalManager.findHandlers(terminal.terminalName(), line.split(" ")[0], 2);
        }
        return super.findCommandHandlers(terminal, line.split(" ")[0]);
    }

    /**
     * 当前实例
     */
    public static final ZKTerminalCompleteHandler INSTANCE = new ZKTerminalCompleteHandler();

}
