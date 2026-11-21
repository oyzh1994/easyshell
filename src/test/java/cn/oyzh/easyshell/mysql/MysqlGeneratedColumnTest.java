package cn.oyzh.easyshell.mysql;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.generator.table.MysqlTableAlertSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.table.MysqlTableCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.table.MysqlAlertTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlCreateTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MySQL 生成列设计与 DDL 测试。
 */
public class MysqlGeneratedColumnTest {

    @Before
    public void setUp() {
        ShellMysqlColumnUtil.init();
    }

    @Test
    public void createsVirtualAndStoredColumns() {
        MysqlColumn name = column("name", "VARCHAR", 100);
        MysqlColumn virtual = column("name_virtual", "VARCHAR", 100);
        virtual.setGeneratedExpression("CONCAT(`name`, '-v')");
        virtual.setGeneratedStorage("VIRTUAL");
        MysqlColumn stored = column("name_stored", "VARCHAR", 100);
        stored.setGeneratedExpression("CONCAT(`name`, '-s')");
        stored.setGeneratedStorage("STORED");

        MysqlCreateTableParam param = new MysqlCreateTableParam();
        param.setTable(table());
        param.setColumns(new MysqlColumns(List.of(name, virtual, stored)));
        String sql = MysqlTableCreateSqlGenerator.generateSqlSingle(param);

        assertTrue(sql, sql.contains("`name_virtual` VARCHAR(100) GENERATED ALWAYS AS "
                + "(CONCAT(`name`, '-v')) VIRTUAL"));
        assertTrue(sql, sql.contains("`name_stored` VARCHAR(100) GENERATED ALWAYS AS "
                + "(CONCAT(`name`, '-s')) STORED"));
        assertFalse(sql, sql.contains("`name_virtual` VARCHAR(100) DEFAULT"));
    }

    @Test
    public void altersGeneratedColumnsAndRejectsMissingExpression() {
        MysqlColumn generated = column("name_upper", "VARCHAR", 100);
        generated.setCreated(true);
        generated.setGeneratedExpression("UPPER(`name`)");
        generated.setGeneratedStorage("VIRTUAL");

        MysqlAlertTableParam param = new MysqlAlertTableParam();
        param.setTable(table());
        param.setColumns(new MysqlColumns(List.of(generated)));
        String sql = MysqlTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains(" ADD COLUMN `name_upper` VARCHAR(100) "
                + "GENERATED ALWAYS AS (UPPER(`name`)) VIRTUAL"));

        MysqlColumn rebuilt = column("name_upper", "VARCHAR", 100);
        rebuilt.setGenerated(false);
        rebuilt.setGeneratedStorage(null);
        rebuilt.setGenerated(true);
        rebuilt.setGeneratedStorage("VIRTUAL");
        rebuilt.setGeneratedExpression("UPPER(`name`)");
        rebuilt.setChanged(true);
        param.setColumns(new MysqlColumns(List.of(rebuilt)));
        sql = MysqlTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains(" DROP COLUMN `name_upper`,\n"
                + " ADD COLUMN `name_upper` VARCHAR(100) "
                + "GENERATED ALWAYS AS (UPPER(`name`)) VIRTUAL"));

        MysqlColumn invalid = column("invalid_generated", "VARCHAR", 100);
        invalid.setGenerated(true);
        assertTrue(invalid.isInvalid());
    }

    @Test
    public void unescapesInformationSchemaGenerationExpression() {
        String escaped = "concat(`name`,_utf8mb4\\'a\\\\\\'b\\',_utf8mb4\\'C:\\\\\\\\tmp\\')";
        String expected = "concat(`name`,_utf8mb4'a\\'b',_utf8mb4'C:\\\\tmp')";
        assertEquals(expected, ShellMysqlHelper.unescapeGenerationExpression(escaped));
    }

    @Test
    public void designUiContainsGeneratedColumnControls() throws Exception {
        String fxml = Files.readString(Path.of(
                "src/main/resources/tabs/mysql/table/shellMysqlTableColumnExtra.fxml"));
        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/mysql/table/ShellMysqlTableColumnExtraController.java"));

        assertTrue(fxml.contains("fx:id=\"columnKind\""));
        assertTrue(fxml.contains("fx:id=\"generatedExpression\""));
        assertTrue(fxml.contains("fx:id=\"generatedStorage\""));
        assertTrue(controller.contains("normalizeGeneratedAttributes()"));
        assertTrue(controller.contains("setGeneratedExpression("));
        assertTrue(controller.contains("setGeneratedStorage("));
    }

    private static MysqlTable table() {
        MysqlTable table = new MysqlTable();
        table.setDbName("demo");
        table.setName("generated_test");
        return table;
    }

    private static MysqlColumn column(String name, String type, int size) {
        MysqlColumn column = new MysqlColumn(name);
        column.setType(type);
        column.setSize(size);
        column.setNullable(true);
        return column;
    }
}
