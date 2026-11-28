package cn.oyzh.easyshell.h2;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalManager;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalPane;
import cn.oyzh.easyshell.terminal.h2.basic.H2ShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.basic.H2ShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.basic.H2ShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.basic.H2UseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;
import javafx.application.Platform;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * H2 终端命令测试。
 *
 * @since 2026-11-28
 */
public class ShellH2TerminalTest {

    @Test
    public void testBasicCommands() throws Exception {
        ShellH2TerminalManager.registerHandlers();

        TerminalCommandHandler<?, ?> showTables = TerminalManager.findHandler(
                ShellH2TerminalPane.TERMINAL_NAME, "show tables;");
        assertTrue(showTables instanceof H2ShowTablesTerminalCommandHandler);
        TerminalCommand tableCommand = showTables.parseCommand("show tables;");
        assertArrayEquals(new String[]{"show", "tables;"}, tableCommand.getArgs());

        TerminalCommandHandler<?, ?> showDatabases = TerminalManager.findHandler(
                ShellH2TerminalPane.TERMINAL_NAME, "show databases;");
        assertTrue(showDatabases instanceof H2ShowDatabasesTerminalCommandHandler);

        TerminalCommandHandler<?, ?> showDbs = TerminalManager.findHandler(
                ShellH2TerminalPane.TERMINAL_NAME, "show dbs;");
        assertTrue(showDbs instanceof H2ShowDbsTerminalCommandHandler);

        // H2 不提供独立的 show schemas，show databases/show dbs 即 schema 列表
        assertNull(TerminalManager.findHandler(
                ShellH2TerminalPane.TERMINAL_NAME, "show schemas;"));

        TerminalCommandHandler<?, ?> use = TerminalManager.findHandler(
                ShellH2TerminalPane.TERMINAL_NAME, "use PUBLIC;");
        assertTrue(use instanceof H2UseTerminalCommandHandler);
        TerminalCommand useCommand = use.parseCommand("use PUBLIC;");
        assertArrayEquals(new String[]{"use", "PUBLIC;"}, useCommand.getArgs());
    }

    @Test
    public void testSqlAndLegacyCommandsUseSqlFallback() {
        ShellH2TerminalManager.registerHandlers();

        // 普通 SQL 与点号命令（sqlite 遗留语法）不注册专用处理器
        assertNull(TerminalManager.findHandler(ShellH2TerminalPane.TERMINAL_NAME, "SELECT 1;"));
        assertNull(TerminalManager.findHandler(ShellH2TerminalPane.TERMINAL_NAME, ".tables"));
    }

    @Test
    public void testUseHandlerSwitchesTerminalSchema() throws Exception {
        // H2 中 use 命令切换终端 schema，避免沿用 sqlite 的 ATTACH 语义
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/cn/oyzh/easyshell/terminal/h2/basic/H2UseTerminalCommandHandler.java"));
        assertTrue(source.contains("setDbName"));
        assertTrue(source.contains("selectSchemas"));

        String showSource = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/cn/oyzh/easyshell/terminal/h2/basic/H2ShowDatabasesTerminalCommandHandler.java"));
        assertTrue(showSource.contains("selectSchemas"));
        assertTrue(!showSource.contains("databaseNames"));
    }

    @Test
    public void testShowDbsListsSchemasAndUseSwitchesContext() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-h2-terminal-");
        Path file = directory.resolve("terminal");
        ShellConnect connect = new ShellConnect();
        connect.setName("terminal-test");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start(5);
        try {
            client.createSchema("APP");
            client.executeSqlSimple("APP", "CREATE TABLE ITEM(ID INT)");

            CountDownLatch startup = new CountDownLatch(1);
            try {
                Platform.startup(startup::countDown);
            } catch (IllegalStateException ex) {
                startup.countDown();
            }
            assertTrue(startup.await(20, TimeUnit.SECONDS));

            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellH2TerminalPane pane = new ShellH2TerminalPane();
                    setField(pane, "client", client);
                    setField(pane, "dbName", "PUBLIC");

                    String dbs = execute(new H2ShowDbsTerminalCommandHandler(),
                            "show dbs;", pane);
                    assertTrue(dbs, dbs.contains("PUBLIC"));
                    assertTrue(dbs, dbs.contains("APP"));
                    assertTrue(dbs, !dbs.contains(file.getFileName().toString()));

                    String use = execute(new H2UseTerminalCommandHandler(),
                            "use APP;", pane);
                    assertTrue(use.contains("Schema changed"));
                    assertTrue("APP".equals(pane.getDbName()));

                    String tables = execute(new H2ShowTablesTerminalCommandHandler(),
                            "show tables;", pane);
                    assertTrue(tables, tables.contains("ITEM"));
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    done.countDown();
                }
            });
            assertTrue(done.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError(error.get());
            }
        } finally {
            client.close();
            Files.walk(directory).sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String execute(TerminalCommandHandler handler,
                                  String input, ShellH2TerminalPane pane) throws Exception {
        TerminalExecuteResult result = handler.execute(handler.parseCommand(input), pane);
        if (result.getException() != null) {
            throw result.getException();
        }
        return String.valueOf(result.getResult());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
