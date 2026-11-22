package cn.oyzh.easyshell.mariadb;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbRecordUtil;
import cn.oyzh.fx.db.util.DBNodeUtil;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MariadbBooleanDisplayTest {

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
    public void tinyIntOneUsesBooleanControlAndFormatting() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                MariadbColumn column = new MariadbColumn("c_bool");
                column.setType("TINYINT(1)");

                assertTrue(column.supportBoolean());
                assertTrue(DBNodeUtil.getNode(false, column) instanceof BooleanTextFiled);
                assertTrue(DBNodeUtil.getNode(true, column) instanceof BooleanTextFiled);
                assertEquals("false", ShellMariadbRecordUtil.formatValue(false, column));
                assertEquals("true", ShellMariadbRecordUtil.formatValue(true, column));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Boolean display test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Boolean display test failed", error.get());
        }
    }
}
