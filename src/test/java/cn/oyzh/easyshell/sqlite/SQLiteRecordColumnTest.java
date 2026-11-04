package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordProperty;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * SQLite记录表格列测试。
 *
 * @since 2026-11-03
 */
public class SQLiteRecordColumnTest {

    @Test
    public void testRecordColumnHeaderShowsGraphicOnly() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/sqlite/record/ShellSqliteRecordColumn.java"));
        assertTrue(source.contains("this.setGraphic(this.initContent(showComment));"));
        assertTrue(source.contains("super.showGraphicOnlyLater();"));
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
