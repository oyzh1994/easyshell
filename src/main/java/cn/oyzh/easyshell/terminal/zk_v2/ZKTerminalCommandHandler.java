package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * ZooKeeper v2 终端命令处理器基类。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public abstract class ZKTerminalCommandHandler<C extends TerminalCommand>
        extends BaseTerminalCommandHandler<C, ZKTerminalPane> {

    /**
     * 按标准命令模型解析命令名称和参数。
     */
    @Override
    protected C parseCommand(String line, String[] words) {
        TerminalCommand command = new TerminalCommand();
        command.setContent(line);
        command.parseArgs(words);
        return (C) command;
    }
}
