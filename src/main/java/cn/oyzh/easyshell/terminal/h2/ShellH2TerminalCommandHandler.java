package cn.oyzh.easyshell.terminal.h2;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * H2 终端命令基类。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class ShellH2TerminalCommandHandler<C extends TerminalCommand>
        extends BaseTerminalCommandHandler<C, ShellH2TerminalPane> {
}
