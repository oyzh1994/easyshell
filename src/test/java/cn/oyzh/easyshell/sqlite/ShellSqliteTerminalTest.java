package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalManager;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 终端命令测试。
 *
 * @since 2026-11-04
 */
public class ShellSqliteTerminalTest {

    @Test
    public void testBasicCommands() throws Exception {
        ShellSqliteTerminalManager.registerHandlers();

        TerminalCommandHandler<?, ?> showTables = TerminalManager.findHandler(
                ShellSqliteTerminalPane.TERMINAL_NAME, "show tables;");
        assertTrue(showTables instanceof SqliteShowTablesTerminalCommandHandler);
        TerminalCommand tableCommand = showTables.parseCommand("show tables;");
        assertArrayEquals(new String[]{"show", "tables;"}, tableCommand.getArgs());

        TerminalCommandHandler<?, ?> showDatabases = TerminalManager.findHandler(
                ShellSqliteTerminalPane.TERMINAL_NAME, "show databases;");
        assertTrue(showDatabases instanceof SqliteShowDatabasesTerminalCommandHandler);

        TerminalCommandHandler<?, ?> showDbs = TerminalManager.findHandler(
                ShellSqliteTerminalPane.TERMINAL_NAME, "show dbs;");
        assertTrue(showDbs instanceof SqliteShowDbsTerminalCommandHandler);

        TerminalCommandHandler<?, ?> use = TerminalManager.findHandler(
                ShellSqliteTerminalPane.TERMINAL_NAME, "use main;");
        assertTrue(use instanceof SqliteUseTerminalCommandHandler);
        TerminalCommand useCommand = use.parseCommand("use main;");
        assertArrayEquals(new String[]{"use", "main;"}, useCommand.getArgs());
    }

    @Test
    public void testSqlAndLegacyCommandsUseSqlFallback() {
        ShellSqliteTerminalManager.registerHandlers();

        assertNull(TerminalManager.findHandler(ShellSqliteTerminalPane.TERMINAL_NAME, "SELECT 1;"));
        assertNull(TerminalManager.findHandler(ShellSqliteTerminalPane.TERMINAL_NAME, ".tables"));
    }
}
