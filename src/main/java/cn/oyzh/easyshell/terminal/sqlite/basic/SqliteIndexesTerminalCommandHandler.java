package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * `.indexes` 命令。
 *
 * @since 2026-11-02
 */
public class SqliteIndexesTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

    @Override
    public String commandName() {
        return ".indexes";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        return terminal.eval("SELECT name, tbl_name, sql FROM sqlite_master "
                + "WHERE type='index' AND name NOT LIKE 'sqlite_%' ORDER BY tbl_name, name;");
    }
}
