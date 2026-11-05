package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordColumn;
import cn.oyzh.easyshell.fx.sqlite.SQLiteResultTableView;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordProperty;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite记录表格列测试。
 *
 * @since 2026-11-03
 */
public class SQLiteRecordColumnTest {

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
    public void testRecordColumnHeaderShowsGraphicOnly() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/sqlite/record/ShellSqliteRecordColumn.java"));
        assertTrue(source.contains("this.setGraphic(this.initContent(showComment));"));
        assertTrue(source.contains("super.showGraphicOnlyLater();"));

        String resultTableSource = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/sqlite/SQLiteResultTableView.java"));
        assertTrue(resultTableSource.contains("new ShellSqliteRecordColumn(column, false)"));
    }

    @Test
    public void testRecordColumnHeaderContextMenu() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                SQLiteColumn column = this.column("name", "TEXT");
                ShellSqliteRecordColumn recordColumn = new ShellSqliteRecordColumn(column);
                assertNotNull(recordColumn.getGraphic());
                assertEquals(2, ((FXVBox) recordColumn.getGraphic()).getChildren().size());
                assertNotNull(recordColumn.getGraphic().getOnContextMenuRequested());
                assertEquals(2, recordColumn.getMenuItems().size());
                assertEquals(I18nHelper.columnInfo(), recordColumn.getMenuItems().get(0).getText());
                assertEquals(I18nHelper.copyColumnName(), recordColumn.getMenuItems().get(1).getText());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Column context menu test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Column context menu test failed", error.get());
        }
    }

    @Test
    public void testQueryResultUsesRecordColumnContextMenu() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                SQLiteColumn column = this.column("name", "TEXT");
                SQLiteResultTableView resultTable = new SQLiteResultTableView();
                resultTable.setResult(List.of(column), List.of());
                assertEquals(2, resultTable.getColumns().size());
                assertTrue(resultTable.getColumns().get(1) instanceof ShellSqliteRecordColumn);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Query result column test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Query result column test failed", error.get());
        }
    }

    @Test
    public void testRecordMenuContainsSqlActions() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/util/sqlite/ShellSqliteRecordUtil.java"));
        assertTrue(source.contains(
                "MenuItemHelper.copyAsInsertStatement_no_graphic(property::vCopyAsInsertSql)"));
        assertTrue(source.contains(
                "MenuItemHelper.copyAsUpdateStatement_no_graphic(property::vCopyAsUpdateSql)"));
    }

    @Test
    public void testRecordSqlGeneration() {
        SQLiteColumn id = this.column("id", "INTEGER");
        id.setPrimaryKey(true);
        SQLiteColumn name = this.column("name", "TEXT");
        id.setTableName("item");
        name.setTableName("item");
        SQLiteRecord record = new SQLiteRecord(new SQLiteColumns(java.util.List.of(id, name)));
        record.putValue(id, 1L);
        record.putValue(name, "old");

        assertEquals("INSERT INTO \"item\" (\"id\", \"name\") VALUES (1, 'old');",
                ShellSqliteDataUtil.toInsertSql(record.getColumns(), record, true));

        record.putValue(name, "new");
        assertEquals("UPDATE \"item\" SET \"name\" = 'new' WHERE \"id\" = 1;",
                ShellSqliteDataUtil.toUpdateSql(record.getColumns(), record));
    }

    @Test
    public void testClearStatusPublishesSingleStatusRefresh() {
        SQLiteColumn id = this.column("id", "INTEGER");
        id.setDbName("main");
        id.setTableName("item");
        SQLiteRecord record = new SQLiteRecord(new SQLiteColumns(java.util.List.of(id)));
        SQLiteRecordProperty property = record.putValue(id, 1L);
        AtomicInteger changedCount = new AtomicInteger();
        DBStatusListener listener = new DBStatusListener("main:item") {
            @Override
            public void changed(javafx.beans.value.ObservableValue<?> observable, Object oldValue, Object newValue) {
                changedCount.incrementAndGet();
            }
        };
        try {
            property.setChanged(true);
            property.set(2L);
            changedCount.set(0);
            record.clearStatus();

            assertEquals(1, changedCount.get());
            assertEquals(2L, property.getOriginal());
            assertFalse(property.isChanged());
            assertFalse(record.isChanged());
            assertEquals("", record.getStatus());
        } finally {
            DBStatusListenerManager.removeListener(listener);
        }
    }

    private SQLiteColumn column(String name, String type) {
        SQLiteColumn column = new SQLiteColumn();
        column.setName(name);
        column.setType(type);
        return column;
    }
}
