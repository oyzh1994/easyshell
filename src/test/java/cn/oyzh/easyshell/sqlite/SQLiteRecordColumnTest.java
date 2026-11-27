package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordColumn;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellRecordHeaderUtil;
import cn.oyzh.easyshell.fx.sqlite.SQLiteResultTableView;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordProperty;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.fx.db.util.DBNodeUtil;
import cn.oyzh.fx.editor.incubator.control.LongTextFiled;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.i18n.I18nHelper;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
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
import static org.junit.Assert.assertNull;
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
        String base = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/record/ShellRecordColumn.java"));
        // 列头只显示 graphic，避免名称重复显示
        assertTrue(base.contains("super.showGraphicOnlyLater();"));
        // 列头由 名称 + 类型 + 注释 三行组成
        assertTrue(base.contains("ShellRecordHeaderUtil.showType()"));
        assertTrue(base.contains("ShellRecordHeaderUtil.showComment()"));

        String source = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/sqlite/record/ShellSqliteRecordColumn.java"));
        assertTrue(source.contains("extends ShellRecordColumn<SQLiteRecord>"));

        // 查询结果表同样使用记录列
        String resultTableSource = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/sqlite/SQLiteResultTableView.java"));
        assertTrue(resultTableSource.contains("new ShellSqliteRecordColumn(column)"));
    }

    @Test
    public void testRecordColumnHeaderContextMenu() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                this.resetHeaderSetting();
                SQLiteColumn column = this.column("name", "TEXT");
                ShellSqliteRecordColumn recordColumn = new ShellSqliteRecordColumn(column);
                assertNotNull(recordColumn.getGraphic());
                // SQLite 无注释：只显示名称与类型两行，不显示占位符
                assertEquals(2, ((FXVBox) recordColumn.getGraphic()).getChildren().size());
                assertNull(ShellRecordHeaderUtil.commentText(null, false));
                assertEquals(ShellRecordHeaderUtil.PRIMARY_KEY_TAG, ShellRecordHeaderUtil.commentText(null, true));
                assertEquals("名称", ShellRecordHeaderUtil.commentText("名称", false));
                assertNull(ShellRecordHeaderUtil.typeText(null, null, false));
                assertNotNull(recordColumn.getGraphic().getOnContextMenuRequested());
                // 字段信息、复制字段名称、分隔符、显示字段类型、显示注释
                assertEquals(5, recordColumn.getMenuItems().size());
                assertEquals(I18nHelper.columnInfo(), recordColumn.getMenuItems().get(0).getText());
                assertEquals(I18nHelper.copyColumnName(), recordColumn.getMenuItems().get(1).getText());
                assertTrue(recordColumn.getMenuItems().get(3) instanceof CheckMenuItem);
                assertTrue(recordColumn.getMenuItems().get(4) instanceof CheckMenuItem);
                assertTrue(((CheckMenuItem) recordColumn.getMenuItems().get(3)).isSelected());
                assertTrue(((CheckMenuItem) recordColumn.getMenuItems().get(4)).isSelected());
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
    public void testColumnHeaderSwitchesPersisted() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                this.resetHeaderSetting();
                ShellSetting setting = ShellSettingStore.SETTING;
                SQLiteColumn column = this.column("name", "TEXT");
                ShellSqliteRecordColumn recordColumn = new ShellSqliteRecordColumn(column);
                // 列需要加入表格后才能在切换时刷新
                FXTableView<SQLiteRecord> tableView = new FXTableView<>();
                tableView.getColumns().add(recordColumn);
                // 名称 + 类型（无注释）
                assertEquals(2, ((FXVBox) recordColumn.getGraphic()).getChildren().size());

                // 关闭类型：仅剩名称（通过右键菜单触发，模拟真实操作）
                ((CheckMenuItem) recordColumn.getMenuItems().get(3)).fire();
                assertFalse(setting.isShowColumnType());
                assertEquals(1, ((FXVBox) recordColumn.getGraphic()).getChildren().size());
                assertFalse(((CheckMenuItem) recordColumn.getMenuItems().get(3)).isSelected());

                // 再关闭注释：仍只有名称，不会出现占位行
                ((CheckMenuItem) recordColumn.getMenuItems().get(4)).fire();
                assertFalse(setting.isShowColumnComment());
                assertEquals(1, ((FXVBox) recordColumn.getGraphic()).getChildren().size());

                // 恢复默认，避免影响其它用例
                this.resetHeaderSetting();
                recordColumn.refreshHeader();
                assertEquals(2, ((FXVBox) recordColumn.getGraphic()).getChildren().size());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Column header switch test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Column header switch test failed", error.get());
        }
    }

    /**
     * 重置列头显示设置
     */
    private void resetHeaderSetting() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setShowColumnType(true);
        setting.setShowColumnComment(true);
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
    public void testSQLiteUsesTextEditorsExceptPrimaryKey() throws Exception {
        ShellSqliteColumnUtil.init();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                SQLiteColumn id = this.column("id", "INTEGER");
                id.setPrimaryKey(true);
                SQLiteColumn intValue = this.column("int_value", "INTEGER");
                SQLiteColumn realValue = this.column("real_value", "REAL");
                SQLiteColumn dateTime = this.column("date_time", "DATETIME");
                SQLiteColumn data = this.column("data", "BLOB");

                assertTrue(DBNodeUtil.getNode(1L, id) instanceof NumberTextField);
                assertTrue(DBNodeUtil.getNode("001", intValue) instanceof LongTextFiled);
                assertTrue(DBNodeUtil.getNode("1.25", realValue) instanceof LongTextFiled);
                assertTrue(DBNodeUtil.getNode("2026-11-24", dateTime) instanceof LongTextFiled);
                assertTrue(DBNodeUtil.getNode(new byte[]{1}, data) instanceof BinaryTextFiled);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("SQLite editor capability test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("SQLite editor capability test failed", error.get());
        }
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
