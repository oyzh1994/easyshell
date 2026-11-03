package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 字段默认值测试。
 *
 * @since 2026-11-03
 */
public class SQLiteDefaultValueTest {

    @Test
    public void testDefaultValueEntryAndSqlGeneration() throws Exception {
        String fxml = Files.readString(Path.of(
                "src/main/resources/tabs/sqlite/table/shellSqliteTableColumnExtra.fxml"));
        assertTrue(fxml.contains("ShellSqliteDefaultValueTextFiled"));
        assertTrue(fxml.contains("fx:id=\"defaultValueBox\""));
        assertTrue(fxml.contains("fx:id=\"defaultValue\""));

        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableColumnExtraController.java"));
        assertTrue(controller.contains("this.column.supportDefaultValue()"));
        assertTrue(controller.contains("this.column.setDefaultValue(this.defaultValue.getValue())"));

        SQLiteColumn column = new SQLiteColumn();
        column.setName("name");
        column.setType("TEXT");
        column.setDefaultValue("''");
        assertEquals("\"name\" TEXT DEFAULT ''",
                SQLiteTableCreateSqlGenerator.columnDefinition(column, true));
    }
}
