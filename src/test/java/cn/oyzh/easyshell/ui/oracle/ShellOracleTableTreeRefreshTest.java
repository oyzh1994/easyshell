package cn.oyzh.easyshell.ui.oracle;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeView;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTablesTreeItem;
import javafx.application.Platform;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShellOracleTableTreeRefreshTest {

    private static String mode;

    private static String url;

    private static String user;

    private static String password;

    private static String host;

    private static int port;

    @BeforeClass
    public static void setUp() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));

        mode = value("oracle.docker.mode", "ORACLE_DOCKER_MODE", "23c").toLowerCase(java.util.Locale.ROOT);
        url = value("oracle.docker.url", "ORACLE_DOCKER_URL", null);
        user = value("oracle.docker.user", "ORACLE_DOCKER_USER", "DEMO");
        password = value("oracle.docker.password", "ORACLE_DOCKER_PASSWORD", "Demo2026");
        Assume.assumeTrue("Set oracle.docker.url to run Oracle Docker tests", url != null && !url.isBlank());
        Assume.assumeTrue("oracle.docker.mode must be 11g or 23c", mode.equals("11g") || mode.equals("23c"));

        var matcher = java.util.regex.Pattern
                .compile("@tcp://([^:/]+):(\\d+)")
                .matcher(url);
        Assume.assumeTrue("oracle.docker.url must be a TCP thin URL", matcher.find());
        host = matcher.group(1);
        port = Integer.parseInt(matcher.group(2));
    }

    @Test
    public void refreshesTableTreeAfterExternalTableCreation() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.ORACLE);
        connect.setHost(host + ":" + port);
        connect.setDatabase(mode.equals("11g") ? "XE" : "FREEPDB1");
        connect.setUser(user);
        connect.setPassword(password);
        connect.setConnectTimeOut(10);

        ShellOracleClient client = new ShellOracleClient(connect);
        String tableName = "EASYSHELL_TREE_REFRESH_TEST";
        try {
            try {
                client.start(10000);
            } catch (Exception ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            if (client.selectTablesSimple("DEMO").stream()
                    .anyMatch(table -> tableName.equals(table.getName()))) {
                client.dropTable("DEMO", tableName);
            }

            ShellOracleTreeView treeView = new ShellOracleTreeView();
            treeView.setClient(client);
            OracleSchema schema = new OracleSchema();
            schema.setName("DEMO");
            ShellOracleSchemaTreeItem schemaItem = new ShellOracleSchemaTreeItem(schema, treeView);
            treeView.root().addChild(schemaItem);
            schemaItem.loadChild();
            await(() -> schemaItem.isLoaded() && !schemaItem.isLoading());

            ShellOracleTablesTreeItem tablesItem = schemaItem.getTableTypeChild();
            tablesItem.loadChild();
            await(() -> tablesItem.isLoaded() && !tablesItem.isLoading());
            int initialSize = tablesItem.unfilteredChildrenSize();
            assertTrue(initialSize > 0);

            try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
                statement.executeUpdate("CREATE TABLE \"DEMO\".\"" + tableName
                        + "\" (\"ID\" NUMBER(10) PRIMARY KEY)");
            }

            tablesItem.tableSize();
            await(() -> tablesItem.unfilteredChildrenSize() == client.tableSize("DEMO"));
            assertTrue(tablesItem.unfilteredChildren().stream()
                    .anyMatch(item -> item instanceof ShellOracleTableTreeItem table
                            && tableName.equals(table.tableName())));
            assertEquals(client.tableSize("DEMO"), tablesItem.unfilteredChildrenSize());
        } finally {
            if (client.selectTablesSimple("DEMO").stream()
                    .anyMatch(table -> tableName.equals(table.getName()))) {
                client.dropTable("DEMO", tableName);
            }
            client.close();
        }
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 20000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertTrue("Timed out waiting for Oracle table tree refresh", condition.getAsBoolean());
    }

    private static String value(String property, String environment, String defaultValue) {
        String result = System.getProperty(property);
        if (result == null || result.isBlank()) {
            result = System.getenv(environment);
        }
        return result == null || result.isBlank() ? defaultValue : result;
    }
}
