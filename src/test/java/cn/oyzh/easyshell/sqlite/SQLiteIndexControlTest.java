package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.index.SQLiteIndexControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SQLite索引设计组件测试。
 *
 * @since 2026-11-03
 */
public class SQLiteIndexControlTest {

    @Test
    public void testIndexPartsAreParsedFromDefinition() {
        SQLiteIndexControl control = new SQLiteIndexControl();
        control.setName("idx_item");
        control.setTableName("item");
        control.setDefinition("CREATE UNIQUE INDEX \"idx(item)\" ON item "
                + "(lower(name), status COLLATE NOCASE DESC) WHERE status = 1 AND name <> '(';");

        assertEquals("lower(name), status COLLATE NOCASE DESC", control.getColumnDefinition());
        assertEquals("status = 1 AND name <> '('", control.getWhere());
        assertEquals("UNIQUE", control.getIndexType());
        assertEquals(java.util.List.of("lower(name)", "status"), control.getColumns());
    }

    @Test
    public void testNormalIndexTypeIsDefault() {
        SQLiteIndexControl control = new SQLiteIndexControl();
        control.setDefinition("CREATE INDEX IF NOT EXISTS main.idx_item ON item (id);");

        assertEquals("id", control.getColumnDefinition());
        assertEquals("", control.getWhere());
        assertEquals("NORMAL", control.getIndexType());
    }

    @Test
    public void testColumnOrderIsPreserved() {
        SQLiteIndexControl control = new SQLiteIndexControl();
        control.setDefinition("CREATE INDEX idx_item ON item (z, a, m, b, y);");

        assertEquals(java.util.List.of("z", "a", "m", "b", "y"), control.getColumns());
    }

    @Test
    public void testParenthesesInQuotedTableNameAreIgnored() {
        SQLiteIndexControl control = new SQLiteIndexControl();
        control.setDefinition("CREATE INDEX idx_item ON \"item(log)\" (id) WHERE id > 0;");

        assertEquals("id", control.getColumnDefinition());
        assertEquals("id > 0", control.getWhere());
    }

    @Test
    public void testColumnControlUsesSqliteFieldSelector() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/cn/oyzh/easyshell/sqlite/index/SQLiteIndexControl.java"));
        String controller = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableDesignTabController.java"));

        assertTrue(source.contains("new ShellSqliteIndexFieldTextFiled("));
        assertTrue(controller.contains("index.setColumnList(this.columnTable.getItems())"));
    }
}
