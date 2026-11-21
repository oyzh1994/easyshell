package cn.oyzh.easyshell.mariadb;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.table.MariadbAlertTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbCreateTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB 生成列设计与 DDL 测试。
 */
public class MariadbGeneratedColumnTest {

    @Before
    public void setUp() {
        ShellMariadbColumnUtil.init();
    }

    @Test
    public void createsVirtualAndStoredColumns() {
        MariadbColumn name = column("name", "VARCHAR", 100);
        MariadbColumn virtual = column("name_virtual", "VARCHAR", 100);
        virtual.setGeneratedExpression("CONCAT(`name`, '-v')");
        virtual.setGeneratedStorage("VIRTUAL");
        MariadbColumn stored = column("name_stored", "VARCHAR", 100);
        stored.setGeneratedExpression("CONCAT(`name`, '-s')");
        stored.setGeneratedStorage("STORED");

        MariadbCreateTableParam param = new MariadbCreateTableParam();
        param.setTable(table());
        param.setColumns(new MariadbColumns(List.of(name, virtual, stored)));
        String sql = MariadbTableCreateSqlGenerator.generateSqlSingle(param);

        assertTrue(sql, sql.contains("`name_virtual` VARCHAR(100) GENERATED ALWAYS AS "
                + "(CONCAT(`name`, '-v')) VIRTUAL"));
        assertTrue(sql, sql.contains("`name_stored` VARCHAR(100) GENERATED ALWAYS AS "
                + "(CONCAT(`name`, '-s')) STORED"));
        assertFalse(sql, sql.contains("`name_virtual` VARCHAR(100) DEFAULT"));
    }

    @Test
    public void altersGeneratedColumnsAndRejectsMissingExpression() {
        MariadbColumn generated = column("name_upper", "VARCHAR", 100);
        generated.setCreated(true);
        generated.setGeneratedExpression("UPPER(`name`)");
        generated.setGeneratedStorage("VIRTUAL");

        MariadbAlertTableParam param = new MariadbAlertTableParam();
        param.setTable(table());
        param.setColumns(new MariadbColumns(List.of(generated)));
        String sql = MariadbTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains(" ADD COLUMN `name_upper` VARCHAR(100) "
                + "GENERATED ALWAYS AS (UPPER(`name`)) VIRTUAL"));

        MariadbColumn rebuilt = column("name_upper", "VARCHAR", 100);
        rebuilt.setGenerated(false);
        rebuilt.setGeneratedStorage(null);
        rebuilt.setGenerated(true);
        rebuilt.setGeneratedStorage("VIRTUAL");
        rebuilt.setGeneratedExpression("UPPER(`name`)");
        rebuilt.setChanged(true);
        param.setColumns(new MariadbColumns(List.of(rebuilt)));
        sql = MariadbTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains(" DROP COLUMN `name_upper`,\n"
                + " ADD COLUMN `name_upper` VARCHAR(100) "
                + "GENERATED ALWAYS AS (UPPER(`name`)) VIRTUAL"));

        MariadbColumn invalid = column("invalid_generated", "VARCHAR", 100);
        invalid.setGenerated(true);
        assertTrue(invalid.isInvalid());
    }

    @Test
    public void extractsGenerationExpressionFromShowCreate() {
        String createSql = """
                CREATE TABLE `t` (
                  `name` varchar(100) DEFAULT NULL,
                  `value` varchar(300) GENERATED ALWAYS AS (concat(`name`, 'a\\'b', '中文😀')) VIRTUAL
                )
                """;
        assertEquals("concat(`name`, 'a\\'b', '中文😀')",
                ShellMariadbHelper.extractGeneratedExpression(createSql, "value"));
    }

    @Test
    public void designUiContainsGeneratedColumnControls() throws Exception {
        String fxml = Files.readString(Path.of(
                "src/main/resources/tabs/mariadb/table/shellMariadbTableColumnExtra.fxml"));
        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/mariadb/table/ShellMariadbTableColumnExtraController.java"));

        assertTrue(fxml.contains("fx:id=\"columnKind\""));
        assertTrue(fxml.contains("fx:id=\"generatedExpression\""));
        assertTrue(fxml.contains("fx:id=\"generatedStorage\""));
        assertTrue(controller.contains("normalizeGeneratedAttributes()"));
        assertTrue(controller.contains("setGeneratedExpression("));
        assertTrue(controller.contains("setGeneratedStorage("));
    }

    private static MariadbTable table() {
        MariadbTable table = new MariadbTable();
        table.setDbName("demo");
        table.setName("generated_test");
        return table;
    }

    private static MariadbColumn column(String name, String type, int size) {
        MariadbColumn column = new MariadbColumn(name);
        column.setType(type);
        column.setSize(size);
        column.setNullable(true);
        return column;
    }
}
