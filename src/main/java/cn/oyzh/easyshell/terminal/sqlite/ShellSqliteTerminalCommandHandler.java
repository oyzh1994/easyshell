package cn.oyzh.easyshell.terminal.sqlite;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * SQLite 终端命令基类。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class ShellSqliteTerminalCommandHandler<C extends TerminalCommand>
        extends BaseTerminalCommandHandler<C, ShellSqliteTerminalPane> {
}

