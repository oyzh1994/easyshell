package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * `.views` 命令。
 *
 * @since 2026-11-02
 */
public class SqliteViewsTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

    @Override
    public String commandName() {
        return ".views";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        return terminal.eval("SELECT name AS view_name FROM sqlite_master WHERE type='view' ORDER BY name;");
    }
}

