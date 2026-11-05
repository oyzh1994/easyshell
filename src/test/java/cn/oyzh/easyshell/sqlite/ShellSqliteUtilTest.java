package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBNodeUtil;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 公共工具测试。
 *
 * @since 2026-11-02
 */
public class ShellSqliteUtilTest {

    @Test
    public void testNormalizeJdbcType() {
        assertEquals("INTEGER", ShellSqliteColumnUtil.normalizeJdbcType(Types.BIGINT, "BIGINT"));
        assertEquals("REAL", ShellSqliteColumnUtil.normalizeJdbcType(Types.DOUBLE, "DOUBLE"));
        assertEquals("NUMERIC", ShellSqliteColumnUtil.normalizeJdbcType(Types.DECIMAL, "DECIMAL(10, 2)"));
        assertEquals("TEXT", ShellSqliteColumnUtil.normalizeJdbcType(Types.VARCHAR, "VARCHAR"));
        assertEquals("BLOB", ShellSqliteColumnUtil.normalizeJdbcType(Types.BINARY, "VARBINARY"));
        assertEquals("BOOLEAN", ShellSqliteColumnUtil.normalizeJdbcType(Types.BOOLEAN, "BOOLEAN"));
        assertEquals("DATE", ShellSqliteColumnUtil.normalizeJdbcType(Types.DATE, "DATE"));
        assertEquals("TIME", ShellSqliteColumnUtil.normalizeJdbcType(Types.TIME, "TIME"));
        assertEquals("DATETIME", ShellSqliteColumnUtil.normalizeJdbcType(Types.TIMESTAMP, "TIMESTAMP"));
        assertEquals("JSON", ShellSqliteColumnUtil.normalizeJdbcType(Types.OTHER, "JSONB"));
        assertEquals("ANY", ShellSqliteColumnUtil.normalizeJdbcType(Types.NULL, "NULL"));

        assertEquals(Integer.valueOf(20), ShellSqliteColumnUtil.normalizeJdbcSize("VARCHAR(20)"));
        assertNull(ShellSqliteColumnUtil.normalizeJdbcSize("VARCHAR"));
    }

    @Test
    public void testColumnDataAndFacadeUtilities() throws Exception {
        ShellSqliteColumnUtil.init();
        assertFalse(DBColumnFieldManager.fieldNames(DBDialect.SQLITE).isEmpty());
        assertTrue(DBColumnFieldManager.supportInteger(DBDialect.SQLITE, "INTEGER"));
        assertTrue(DBColumnFieldManager.supportString(DBDialect.SQLITE, "TEXT"));
        assertTrue(DBColumnFieldManager.supportJson(DBDialect.SQLITE, "JSONB"));

        SQLiteColumn id = this.column("id", "INTEGER");
        SQLiteColumn amount = this.column("amount", "REAL");
        SQLiteColumn generated = this.column("generated", "REAL");
        generated.setGeneratedExpression("amount * 2");
        assertTrue(id.supportInteger());
        assertTrue(amount.supportDigits());
        assertTrue(id.supportAutoIncrement());
        SQLiteColumn intColumn = this.column("int_value", "INT");
        SQLiteColumn textColumn = this.column("text_value", "TEXT");
        SQLiteColumn booleanColumn = this.column("bool_value", "BOOLEAN");
        SQLiteColumn generatedInteger = this.column("generated_integer", "INTEGER");
        generatedInteger.setGeneratedExpression("1");
        assertTrue(intColumn.supportAutoIncrement());
        assertFalse(textColumn.supportAutoIncrement());
        assertFalse(generatedInteger.supportAutoIncrement());

        assertEquals(Color.valueOf("#D7EED0"), DBNodeUtil.getNodeBackground(id));
        assertEquals(Color.valueOf("#A1C9D1"), DBNodeUtil.getNodeBackground(textColumn));
        assertEquals(Color.valueOf("#FBF0D0"),
                DBNodeUtil.getNodeBackground(this.column("blob_value", "BLOB")));
        assertEquals(Color.valueOf("#CDECFA"), DBNodeUtil.getNodeBackground(amount));
        assertFalse(booleanColumn.supportInteger());
        assertTrue(booleanColumn.supportBoolean());
        assertEquals(Color.valueOf("#FAE1E4"), DBNodeUtil.getNodeBackground(booleanColumn));
        assertEquals(Color.valueOf("#F1E1F5"),
                DBNodeUtil.getNodeBackground(this.column("date_time_value", "DATETIME")));

        Map<String, Object> record = Map.of(
                "id", "1",
                "amount", "2.5",
                "generated", 5.0D
        );
        List<String> inserts = ShellSqliteDataUtil.toInsertSql(
                "item", List.of(id, amount, generated), List.of(record));
        assertEquals(1, inserts.size());
        assertTrue(inserts.getFirst().contains("(\"id\", \"amount\") VALUES (1, 2.5)"));
        assertFalse(inserts.getFirst().contains("generated"));

        SQLiteColumn numeric = this.column("numeric_value", "NUMERIC");
        Object normalized = ShellSqliteDataUtil.normalize(List.of(numeric),
                Map.of("numeric_value", "1234567890.123456789")).get("numeric_value");
        assertEquals(new BigDecimal("1234567890.123456789"), normalized);

        String update = ShellSqliteDataUtil.toUpdateSql(
                "item", List.of(id, amount), Map.of("id", 1L, "amount", 3.5D), Map.of("id", 1L));
        assertTrue(update.contains("SET \"amount\" = 3.5"));
        assertTrue(update.endsWith("WHERE \"id\" = 1;"));

        SQLiteColumn name = this.column("name", "TEXT");
        String editableUpdate = ShellSqliteDataUtil.toUpdateSql("item", List.of(id, name),
                Map.of("id", "1", "name", "updated"), Map.of("id", 1L, "name", "old"),
                List.of("id"));
        assertTrue(editableUpdate.contains("\"name\" = 'updated'"));
        assertTrue(editableUpdate.endsWith("WHERE \"id\" = 1;"));
        String delete = ShellSqliteDataUtil.toDeleteSql("item", List.of(id, name),
                Map.of("id", 1L, "name", "old"), List.of());
        assertTrue(delete.endsWith("WHERE \"id\" = 1 AND \"name\" = 'old';"));

        assertEquals("BEGIN", ShellSqliteUtil.transactionCommand("-- comment\nBEGIN"));
        assertEquals("\"a\"\"b\"", ShellSqliteUtil.quote("a\"b"));

        ShellSqliteViewFactory.class.getMethod("exportData", ShellSqliteClient.class,
                String.class, int.class, SQLiteDataExportTable.class);
        ShellSqliteViewFactory.class.getMethod("dumpData", ShellSqliteClient.class,
                String.class, int.class, int.class);
        ShellSqliteViewFactory.class.getMethod("transportData", ShellConnect.class);
    }

    @Test
    public void testInsertColumnSelectionAndReplaceCreateObjectName() {
        SQLiteColumn id = this.column("id", "INTEGER");
        SQLiteColumn name = this.column("name", "TEXT");
        SQLiteColumn generated = this.column("generated", "TEXT");
        generated.setGeneratedExpression("name || '-generated'");
        java.util.HashMap<String, Object> record = new java.util.HashMap<>();
        record.put("name", "");
        String insert = ShellSqliteDataUtil.toInsertSql(
                "item", List.of(id, name, generated), List.of(record)).getFirst();
        assertTrue(insert.contains("(\"name\") VALUES ('');"));
        assertFalse(insert.contains("\"id\""));
        assertFalse(insert.contains("\"generated\""));

        String defaultInsert = ShellSqliteDataUtil.toInsertSql(
                "item", List.of(id, name, generated), List.of(new java.util.HashMap<>())).getFirst();
        assertEquals("INSERT INTO \"item\" DEFAULT VALUES;", defaultInsert);

        java.util.HashMap<String, Object> nullRecord = new java.util.HashMap<>();
        nullRecord.put("name", null);
        String nullInsert = ShellSqliteDataUtil.toInsertSql(
                "item", List.of(id, name, generated), List.of(nullRecord)).getFirst();
        assertTrue(nullInsert.contains("(\"name\") VALUES (NULL);"));

        assertEquals("CREATE TABLE \"copy\" (id INTEGER PRIMARY KEY);",
                SQLiteSchemaManager.replaceCreateObjectName(
                        "CREATE TABLE \"origin\" (id INTEGER PRIMARY KEY);", "copy"));
        assertEquals("CREATE UNIQUE INDEX \"copy_idx\" ON \"origin\"(name)",
                SQLiteSchemaManager.replaceCreateObjectName(
                        "CREATE UNIQUE INDEX \"origin_idx\" ON \"origin\"(name)", "copy_idx"));
        assertEquals("CREATE TRIGGER IF NOT EXISTS \"copy_trigger\" AFTER INSERT ON \"origin\" "
                        + "BEGIN SELECT 1; END",
                SQLiteSchemaManager.replaceCreateObjectName(
                        "CREATE TRIGGER IF NOT EXISTS \"origin_trigger\" AFTER INSERT ON \"origin\" "
                                + "BEGIN SELECT 1; END", "copy_trigger"));
        assertEquals("SELECT * FROM \"renamed_view_dependent\"",
                SQLiteSchemaManager.replaceTableReference(
                        "SELECT * FROM \"renamed_view_dependent\"", "renamed_view", "other_view"));
        assertEquals("SELECT * FROM \"other_view\"",
                SQLiteSchemaManager.replaceTableReference(
                        "SELECT * FROM \"renamed_view\"", "renamed_view", "other_view"));
    }

    private SQLiteColumn column(String name, String type) {
        SQLiteColumn column = new SQLiteColumn();
        column.setName(name);
        column.setType(type);
        return column;
    }
}
