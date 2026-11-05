package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumnDefinitionParser;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * SQLite 普通列/生成列切换测试。
 *
 * @since 2026-11-05
 */
public class SQLiteColumnKindTest {

    @Test
    public void testNormalColumnDropsGeneratedAttributes() {
        SQLiteColumn column = new SQLiteColumn("name");
        column.setType("TEXT");
        column.setGeneratedExpression("upper(name)");
        column.setGeneratedStorage("STORED");
        column.setGenerated(false);
        column.setDefaultValue("'unknown'");
        column.setAutoIncrement(true);

        column.normalizeGeneratedAttributes();

        assertFalse(column.isGenerated());
        assertNull(column.getGeneratedExpression());
        assertNull(column.getGeneratedStorage());
        assertTrue(column.isAutoIncrement());
        assertTrue("'unknown'".equals(column.getDefaultValue()));
        String sql = SQLiteTableCreateSqlGenerator.columnDefinition(column, true);
        assertFalse(sql.contains("GENERATED"));
        assertTrue(sql.contains("DEFAULT 'unknown'"));
    }

    @Test
    public void testGeneratedColumnDropsNormalAttributes() {
        SQLiteColumn column = new SQLiteColumn("generated_name");
        column.setType("TEXT");
        column.setGenerated(true);
        column.setGeneratedExpression("upper(name)");
        column.setGeneratedStorage("STORED");
        column.setDefaultValue("'unknown'");
        column.setAutoIncrement(true);

        column.normalizeGeneratedAttributes();

        assertTrue(column.isGenerated());
        assertFalse(column.isAutoIncrement());
        assertNull(column.getDefaultValue());
        assertTrue("upper(name)".equals(column.getGeneratedExpression()));
        assertTrue("STORED".equals(column.getGeneratedStorage()));
        String sql = SQLiteTableCreateSqlGenerator.columnDefinition(column, true);
        assertTrue(sql.contains("GENERATED ALWAYS AS (upper(name)) STORED"));
        assertFalse(sql.contains("DEFAULT"));
        assertFalse(sql.contains("AUTOINCREMENT"));
    }

    @Test
    public void testCreateTableSanitizesKindAndRequiresGeneratedExpression() {
        SQLiteColumn normal = new SQLiteColumn("normal");
        normal.setType("TEXT");
        normal.setGeneratedExpression("upper(name)");
        normal.setGenerated(false);

        SQLiteColumn generated = new SQLiteColumn("generated_value");
        generated.setType("TEXT");
        generated.setGenerated(true);
        generated.setGeneratedExpression("upper(name)");
        generated.setDefaultValue("''");

        String sql = SQLiteTableCreateSqlGenerator.createTable(
                "kind_test", List.of(normal, generated), null, false, false);
        assertFalse(sql.contains("\"normal\" TEXT GENERATED"));
        assertTrue(sql.contains("\"generated_value\" TEXT GENERATED ALWAYS AS (upper(name)) VIRTUAL"));
        assertFalse(sql.contains("DEFAULT"));

        SQLiteColumn invalid = new SQLiteColumn("invalid_generated");
        invalid.setType("TEXT");
        invalid.setGenerated(true);
        try {
            SQLiteTableCreateSqlGenerator.createTable(
                    "invalid_kind_test", List.of(invalid), null, false, false);
            fail("Generated column should require an expression");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("expression"));
        }
    }

    @Test
    public void testColumnKindUiWiringAndEchoState() throws Exception {
        SQLiteColumn parsedGenerated = SQLiteColumnDefinitionParser.parseColumn(
                "amount REAL GENERATED ALWAYS AS (price * 2) STORED");
        assertTrue(parsedGenerated.isGenerated());
        assertTrue("STORED".equals(parsedGenerated.getGeneratedStorage()));
        SQLiteColumn parsedNormal = SQLiteColumnDefinitionParser.parseColumn("name TEXT DEFAULT 'normal'");
        assertFalse(parsedNormal.isGenerated());

        String fxml = Files.readString(Path.of(
                "src/main/resources/tabs/sqlite/table/shellSqliteTableColumnExtra.fxml"));
        assertTrue(fxml.contains("SQLiteColumnKindComboBox"));
        assertTrue(fxml.contains("fx:id=\"columnKindBox\""));
        assertTrue(fxml.contains("fx:id=\"columnKind\""));

        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableColumnExtraController.java"));
        assertTrue(controller.contains("generated ? SQLiteColumn.KIND_GENERATED : SQLiteColumn.KIND_NORMAL"));
        assertTrue(controller.contains("this.updateKindLayout(generated)"));
        assertTrue(controller.contains("this.column.normalizeGeneratedAttributes()"));
    }
}
