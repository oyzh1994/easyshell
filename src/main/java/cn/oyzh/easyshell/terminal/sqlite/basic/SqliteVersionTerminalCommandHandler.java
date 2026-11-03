package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * `.version` 命令。
 *
 * @since 2026-11-02
 */
public class SqliteVersionTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

    @Override
    public String commandName() {
        return ".version";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        return terminal.eval("SELECT sqlite_version() AS version;");
    }
}
