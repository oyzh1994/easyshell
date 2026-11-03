package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKey;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 外键设计组件测试。
 *
 * @since 2026-11-03
 */
public class SQLiteForeignKeyControlTest {

    @Test
    public void testForeignKeyControlsLoadDatabaseMetadata() throws Exception {
        String control = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/sqlite/foreignKey/SQLiteForeignKeyControl.java"));
        assertTrue(control.contains("new ShellSqliteIndexFieldTextFiled("));
        assertTrue(control.contains("new ShellSqliteTableComboBox("));
        assertTrue(control.contains("this.dbClient.selectColumns(this.getPrimaryKeyTable())"));
        assertTrue(control.contains("this.primaryKeyTableProperty().addListener"));

        String tableComboBox = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/fx/sqlite/table/ShellSqliteTableComboBox.java"));
        assertTrue(tableComboBox.contains("client.selectTables()"));

        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableDesignTabController.java"));
        assertTrue(controller.contains("private void initForeignKeyTable()"));
        assertTrue(controller.contains("foreignKey.setColumnList(this.columnTable.getItems())"));
        assertTrue(controller.contains("foreignKey.setDbClient(this.client)"));
    }

    @Test
    public void testReferencedTableUsesObservableValue() {
        SQLiteForeignKey foreignKey = new SQLiteForeignKey();
        foreignKey.setPrimaryKeyTable("parent");
        assertEquals("parent", foreignKey.getPrimaryKeyTable());
        assertEquals("parent", foreignKey.primaryKeyTableProperty().get());

        foreignKey.primaryKeyTableProperty().set("parent_new");
        assertEquals("parent_new", foreignKey.getPrimaryKeyTable());
    }
}
