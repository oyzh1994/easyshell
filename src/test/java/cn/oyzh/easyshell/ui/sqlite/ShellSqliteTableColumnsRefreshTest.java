package cn.oyzh.easyshell.ui.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 表字段列表刷新回归测试。
 *
 * @since 2026-11-05
 */
public class ShellSqliteTableColumnsRefreshTest {

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void tableColumnsReflectSchemaChangesWithoutReloadingTree() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-columns-refresh-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT)");

            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellSqliteTreeView treeView = new ShellSqliteTreeView();
                    treeView.setClient(client);
                    treeView.root().loadChild();
                    ShellSqliteTableTreeItem item = treeView.findTableItem("item");
                    assertNotNull(item);
                    assertEquals(2, item.columns().size());

                    client.executeSqlSimple("ALTER TABLE item ADD COLUMN age INTEGER");
                    assertEquals(3, item.columns().size());
                    assertNotNull(item.columns().column("age"));

                    client.executeSqlSimple("ALTER TABLE item DROP COLUMN name");
                    assertEquals(2, item.columns().size());
                    assertFalse(item.columns().columnNames().contains("name"));
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    done.countDown();
                }
            });
            assertTrue("Table columns refresh test timed out", done.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Table columns refresh test failed", error.get());
            }
        } finally {
            client.close();
            Files.deleteIfExists(file);
        }
    }

    private ShellSqliteClient client(Path file) {
        ShellConnect connect = new ShellConnect();
        connect.setName(file.getFileName().toString());
        connect.setType(ShellPrototype.SQLITE);
        connect.setOsType(ShellPrototype.SQLITE);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setReadonly(false);
        connect.setEnvironment("createIfMissing=true\nforeign_keys=true\n");
        return new ShellSqliteClient(connect);
    }
}
