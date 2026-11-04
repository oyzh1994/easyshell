package cn.oyzh.easyshell.terminal;

import cn.oyzh.easyshell.terminal.dameng.DamengTerminalManager;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.easyshell.terminal.dameng.basic.DamengUseTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalManager;
import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalPane;
import cn.oyzh.easyshell.terminal.mariadb.basic.MariadbUseTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.MongoTerminalManager;
import cn.oyzh.easyshell.terminal.mongo.MongoTerminalPane;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalManager;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalPane;
import cn.oyzh.easyshell.terminal.mysql.basic.MysqlUseTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalManager;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.easyshell.terminal.sqlite.basic.SqliteUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommand;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * 数据库终端内置命令测试。
 *
 * @since 2026-11-04
 */
public class DatabaseTerminalCommandTest {

    @Test
    public void testHelpAndClearCommands() throws Exception {
        this.assertBuiltInCommands(MysqlTerminalPane.TERMINAL_NAME, MysqlTerminalManager::registerHandlers);
        this.assertBuiltInCommands(MariadbTerminalPane.TERMINAL_NAME, MariadbTerminalManager::registerHandlers);
        this.assertBuiltInCommands(DamengTerminalPane.TERMINAL_NAME, DamengTerminalManager::registerHandlers);
        this.assertBuiltInCommands(MongoTerminalPane.TERMINAL_NAME, MongoTerminalManager::registerHandlers);
        this.assertBuiltInCommands(ShellSqliteTerminalPane.TERMINAL_NAME, ShellSqliteTerminalManager::registerHandlers);

        this.assertUseCommand(MysqlTerminalPane.TERMINAL_NAME, "use test;", MysqlUseTerminalCommandHandler.class);
        this.assertUseCommand(MariadbTerminalPane.TERMINAL_NAME, "use test;", MariadbUseTerminalCommandHandler.class);
        this.assertUseCommand(DamengTerminalPane.TERMINAL_NAME, "use test;", DamengUseTerminalCommandHandler.class);
        this.assertUseCommand(ShellSqliteTerminalPane.TERMINAL_NAME, "use main;", SqliteUseTerminalCommandHandler.class);
    }

    private void assertBuiltInCommands(String terminalName, Runnable register) throws Exception {
        register.run();
        assertTrue(TerminalManager.findHandler(terminalName, "help") instanceof HelpTerminalCommandHandler);
        assertTrue(TerminalManager.findHandler(terminalName, "help;") instanceof HelpTerminalCommandHandler);
        assertTrue(TerminalManager.findHandler(terminalName, "clear") instanceof ClearTerminalCommandHandler);
        assertTrue(TerminalManager.findHandler(terminalName, "clear;") instanceof ClearTerminalCommandHandler);

        ClearTerminalCommandHandler clearHis = (ClearTerminalCommandHandler) TerminalManager.findHandler(terminalName, "clear -his;");
        assertTrue(clearHis != null);
        ClearTerminalCommand clearHisCommand = clearHis.parseCommand("clear -his;");
        assertTrue(clearHisCommand.isClearHis());
    }

    private void assertUseCommand(String terminalName, String input, Class<?> handlerType) {
        assertTrue(handlerType.isInstance(TerminalManager.findHandler(terminalName, input)));
    }
}
