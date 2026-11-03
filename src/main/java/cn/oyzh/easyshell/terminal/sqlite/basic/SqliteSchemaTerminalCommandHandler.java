package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * `.schema` 命令。
 *
 * @since 2026-11-02
 */
public class SqliteSchemaTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

    @Override
    public String commandName() {
        return ".schema";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        if (command.getArgs() != null && command.getArgs().length == 1) {
            String name = command.getArgs()[0].replace("'", "''");
            return terminal.eval("SELECT sql FROM sqlite_master WHERE name = '" + name + "';");
        }
        return terminal.eval("SELECT name, type, sql FROM sqlite_master "
                + "WHERE sql IS NOT NULL AND name NOT LIKE 'sqlite_%' ORDER BY type, name;");
    }
}
