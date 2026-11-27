package cn.oyzh.easyshell.dameng;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.generator.table.DamengTableAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.table.DamengTableCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.table.DamengAlertTableParam;
import cn.oyzh.easyshell.dameng.table.DamengCreateTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 达梦虚列设计、DDL、记录和 UI 测试。
 */
public class DamengGeneratedColumnTest {

    @Before
    public void setUp() {
        ShellDamengColumnUtil.init();
    }

    @Test
    public void createsGeneratedColumnDdl() {
        DamengColumn value = column("VALUE", "INT", 0);
        DamengColumn generated = column("VALUE_PLUS", "INT", 0);
        generated.setGeneratedExpression("\"VALUE\" + 1");

        DamengCreateTableParam param = new DamengCreateTableParam();
        param.setTable(table());
        param.setColumns(new DamengColumns(List.of(value, generated)));
        String sql = DamengTableCreateSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains("\"VALUE_PLUS\" INT GENERATED ALWAYS AS "
                + "(\"VALUE\" + 1) VIRTUAL"));
        assertFalse(sql, sql.contains("\"VALUE_PLUS\" INT DEFAULT"));
    }

    @Test
    public void rebuildsNormalToGeneratedButModifiesGeneratedToNormal() {
        DamengColumn generated = column("COMPUTED", "INT", 0);
        generated.setGenerated(false);
        generated.setGenerated(true);
        generated.setGeneratedExpression("\"VALUE\" + 1");
        generated.setChanged(true);
        DamengAlertTableParam param = new DamengAlertTableParam();
        param.setTable(table());
        param.setColumns(new DamengColumns(List.of(generated)));
        String sql = DamengTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains("ALTER TABLE \"SYSDBA\".\"ITEM\" DROP COLUMN \"COMPUTED\""));
        assertTrue(sql, sql.contains("ADD COLUMN (\n\t\"COMPUTED\" INT GENERATED ALWAYS AS "
                + "(\"VALUE\" + 1) VIRTUAL"));

        DamengColumn normal = column("COMPUTED", "INT", 0);
        normal.setGenerated(true);
        normal.setGeneratedExpression("\"VALUE\" + 1");
        normal.setGenerated(false);
        normal.normalizeGeneratedAttributes();
        normal.setChanged(true);
        param.setColumns(new DamengColumns(List.of(normal)));
        sql = DamengTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains("DROP COLUMN \"COMPUTED\""));
        assertTrue(sql, sql.contains("\"COMPUTED\" INT NULL"));
    }

    @Test
    public void detectsGeneratedExpressionFromDamengDdl() {
        String ddl = """
                CREATE TABLE "SYSDBA"."T"
                (
                "A" INT,
                "G" INT DEFAULT 5,
                "V" VARCHAR(100) GENERATED ALWAYS AS ((A || '中文😀')) VIRTUAL )
                """;
        assertFalse(ShellDamengHelper.isGeneratedColumn(ddl, "G"));
        assertTrue(ShellDamengHelper.isGeneratedColumn(ddl, "V"));
        assertTrue(ShellDamengHelper.extractGeneratedExpression(ddl, "V").contains("中文😀"));
    }

    @Test
    public void generatedColumnIsReadonlyAndExcludedFromDml() {
        DamengColumn value = column("VALUE", "INT", 0);
        DamengColumn generated = column("COMPUTED", "INT", 0);
        generated.setGeneratedExpression("\"VALUE\" + 1");
        DamengRecord record = new DamengRecord(new DamengColumns(List.of(value, generated)));
        record.putValue("VALUE", 2);
        record.putValue("COMPUTED", 3);
        assertTrue(record.getProperty("COMPUTED").isReadonly());
        assertFalse(record.getRecordData().columns().contains("COMPUTED"));
    }

    @Test
    public void identityColumnIsReadonlyAndExcludedFromDml() {
        DamengColumn id = column("ID", "BIGINT", 0);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");
        DamengColumn value = column("VALUE", "INT", 0);
        DamengRecord record = new DamengRecord(new DamengColumns(List.of(id, value)));
        record.putValue("ID", null);
        record.putValue("VALUE", 2);
        assertTrue(record.getProperty("ID").isReadonly());
        assertFalse(record.getRecordData().columns().contains("ID"));
    }

    @Test
    public void designUiContainsGeneratedColumnControls() throws Exception {
        String fxml = Files.readString(Path.of(
                "src/main/resources/tabs/dameng/table/shellDamengTableColumnExtra.fxml"));
        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/dameng/table/ShellDamengTableColumnExtraController.java"));
        assertTrue(fxml.contains("fx:id=\"columnKind\""));
        assertTrue(fxml.contains("fx:id=\"generatedExpression\""));
        assertTrue(controller.contains("normalizeGeneratedAttributes()"));
        assertTrue(controller.contains("setGeneratedExpression("));
    }

    private static DamengTable table() {
        DamengTable table = new DamengTable();
        table.setSchema("SYSDBA");
        table.setName("ITEM");
        return table;
    }

    private static DamengColumn column(String name, String type, int size) {
        DamengColumn column = new DamengColumn(name);
        column.setType(type);
        if (size > 0) {
            column.setSize(size);
        }
        column.setSchema("SYSDBA");
        column.setTableName("ITEM");
        column.setNullable(true);
        return column;
    }
}
