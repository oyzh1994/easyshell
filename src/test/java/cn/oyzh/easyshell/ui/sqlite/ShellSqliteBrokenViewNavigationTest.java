package cn.oyzh.easyshell.ui.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTabPane;
import cn.oyzh.easyshell.tabs.sqlite.view.ShellSqliteViewRecordTab;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
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
import static org.junit.Assert.assertTrue;

/**
 * SQLite 坏视图导航回归测试。
 *
 * @since 2026-11-04
 */
public class ShellSqliteBrokenViewNavigationTest {

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
    public void brokenViewOpensDesignerInsteadOfRecordTab() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-broken-view-nav-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE VIEW broken_view AS SELECT * FROM missing_view_source");

            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellSqliteTreeView treeView = new ShellSqliteTreeView();
                    treeView.setClient(client);
                    RecordingTabPane tabPane = new RecordingTabPane();
                    tabPane.setClient(client);
                    tabPane.setTreeView(treeView);
                    treeView.setTabPane(tabPane);
                    treeView.root().loadChild();

                    tabPane.openViewRecord("broken_view");

                    assertEquals("broken_view", tabPane.designViewName);
                    assertFalse(tabPane.getTabs().stream()
                            .anyMatch(tab -> tab instanceof ShellSqliteViewRecordTab));
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    done.countDown();
                }
            });
            assertTrue("Broken view navigation timed out", done.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Broken view navigation failed", error.get());
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

    private static class RecordingTabPane extends ShellSqliteTabPane {

        private String designViewName;

        @Override
        public void openViewDesign(String viewName) {
            this.designViewName = viewName;
        }
    }
}
