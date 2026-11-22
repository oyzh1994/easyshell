package cn.oyzh.easyshell.sqlserver;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalManager;
import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlserver.basic.SqlServerUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import javafx.application.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SQL Server终端真实实例测试。
 */
public class SqlServerTerminalIntegrationTest {

    private static ShellSqlServerClient client;

    @BeforeClass
    public static void setUp() throws Throwable {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(20, TimeUnit.SECONDS));
        SqlServerTerminalManager.registerHandlers();

        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.SQLSERVER);
        connect.setHost("127.0.0.1:14333");
        connect.setDatabase("EASYSHELL_SQLSERVER");
        connect.setUser("sa");
        connect.setPassword("easyshell@2026");
        connect.setSSLMode(false);
        connect.setConnectTimeOut(10);
        client = new ShellSqlServerClient(connect);
        client.start(10000);
        client.executeSqlSimple("EASYSHELL_SRC", """
                IF OBJECT_ID(N'[EASYSHELL_SRC].[TERMINAL_TEST]', N'U') IS NULL
                    CREATE TABLE [EASYSHELL_SRC].[TERMINAL_TEST] ([ID] INT NOT NULL PRIMARY KEY)
                """);
    }

    @AfterClass
    public static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void executesShowUseAndMetadataCommandsThroughClient() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                SqlServerTerminalPane pane = new SqlServerTerminalPane();
                setField(pane, "client", client);
                setField(pane, "schema", "EASYSHELL_SRC");

                String databases = execute(new SqlServerShowDatabasesTerminalCommandHandler(),
                        "show databases;", pane);
                assertTrue(databases.contains("EASYSHELL_SQLSERVER"));

                String schemas = execute(new SqlServerShowDbsTerminalCommandHandler(),
                        "show dbs;", pane);
                assertTrue(schemas.contains("EASYSHELL_SRC"));

                String tables = execute(new SqlServerShowTablesTerminalCommandHandler(),
                        "show tables;", pane);
                assertTrue(tables.contains("TERMINAL_TEST"));

                String use = execute(new SqlServerUseTerminalCommandHandler(),
                        "use EASYSHELL_DST;", pane);
                assertTrue(use.contains("Schema changed"));
                assertTrue("EASYSHELL_DST".equals(pane.getSchema()));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String execute(TerminalCommandHandler handler,
                                  String input, SqlServerTerminalPane pane) throws Exception {
        TerminalExecuteResult result = handler.execute(handler.parseCommand(input), pane);
        assertNull(String.valueOf(result.getException()), result.getException());
        assertFalse(result.getResult() == null);
        return String.valueOf(result.getResult());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
