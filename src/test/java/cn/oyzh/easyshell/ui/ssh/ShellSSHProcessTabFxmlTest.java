package cn.oyzh.easyshell.ui.ssh;

import cn.oyzh.easyshell.fx.process.ShellProcessInfoTableView;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHProcessTabController;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SSH 进程监控 FXML 装载测试。
 */
public class ShellSSHProcessTabFxmlTest {

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
    public void processTableContainsPortColumn() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/shellSSHProcessTab.fxml");
                assertNotNull(result.getNode());
                ShellSSHProcessTabController controller =
                        (ShellSSHProcessTabController) result.getController();
                Field field = ShellSSHProcessTabController.class.getDeclaredField("processTable");
                field.setAccessible(true);
                ShellProcessInfoTableView table = (ShellProcessInfoTableView) field.get(controller);
                assertNotNull(table);
                assertTrue(table.getColumns().stream()
                        .filter(column -> column instanceof FXTableColumn)
                        .map(column -> (FXTableColumn<?, ?>) column)
                        .anyMatch(column -> "portsData".equals(column.getValueName())));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("SSH process FXML load timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("SSH process FXML load failed", error.get());
        }
    }
}
