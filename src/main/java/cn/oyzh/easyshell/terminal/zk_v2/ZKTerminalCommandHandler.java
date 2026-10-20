package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * ZooKeeper v2 终端命令处理器基类。
 */
public abstract class ZKTerminalCommandHandler<C extends TerminalCommand>
        extends BaseTerminalCommandHandler<C, ZKTerminalPane> {

    @Override
    protected C parseCommand(String line, String[] words) {
        TerminalCommand command = new TerminalCommand();
        command.setContent(line);
        command.setCommand(words.length == 0 ? "" : words[0]);
        command.setArgs(words);
        return (C) command;
    }
}
