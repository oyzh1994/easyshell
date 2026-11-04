package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.function.SQLiteAlertFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteCreateFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableAlertSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewAlertSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.table.SQLiteAlertTableParam;
import cn.oyzh.easyshell.sqlite.table.SQLiteCreateTableParam;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteAlertViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteCreateViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SQLite SQL生成器测试。
 *
 * @since 2026-11-03
 */
public class SQLiteGeneratorSqlTest {

    @Test
    public void testTableGenerateApi() {
        SQLiteCreateTableParam createParam = this.createTableParam();
        List<String> createSqlList = SQLiteTableCreateSqlGenerator.generateSql(createParam);
        assertEquals(1, createSqlList.size());
        assertEquals(1, count(createSqlList.getFirst(), "CREATE TABLE"));

        String createSql = SQLiteTableCreateSqlGenerator.generateSqlSingle(createParam);
        assertEquals(1, count(createSql, "CREATE TABLE"));

        SQLiteAlertTableParam alertParam = new SQLiteAlertTableParam();
        alertParam.setTable(createParam.getTable());
        alertParam.setColumns(createParam.getColumns());
        List<String> alertSqlList = SQLiteTableAlertSqlGenerator.generateSql(alertParam);
        assertEquals(1, alertSqlList.size());
        assertTrue(alertSqlList.getFirst().startsWith("BEGIN;"));
        assertTrue(alertSqlList.getFirst().endsWith("COMMIT;"));
    }

    @Test
    public void testTableRenameRewritesCheckAndGeneratedReferences() {
        SQLiteColumn renamed = new SQLiteColumn();
        renamed.setName("age");
        renamed.setType("TEXT");
        renamed.setName("aget");
        renamed.setCheck("age > 5 AND note <> 'age'");

        SQLiteColumn generated = new SQLiteColumn();
        generated.setName("double_age");
        generated.setType("INTEGER");
        generated.setGeneratedExpression("age * 2");
        generated.setGeneratedStorage("VIRTUAL");

        String constraints = "CONSTRAINT \"age\" CHECK (age > 5),\nUNIQUE (age)";
        String sql = SQLiteTableCreateSqlGenerator.createTable(
                "person", List.of(renamed, generated), constraints, false, false);

        assertTrue(sql.contains("\"aget\" TEXT CHECK (\"aget\" > 5 AND note <> 'age')"));
        assertTrue(sql.contains("GENERATED ALWAYS AS (\"aget\" * 2) VIRTUAL"));
        assertTrue(sql.contains("CONSTRAINT \"age\" CHECK (\"aget\" > 5)"));
        assertTrue(sql.contains("UNIQUE (\"aget\")"));
    }

    @Test
    public void testTableAlertCopiesRenamedColumnFromOriginalName() {
        SQLiteColumn renamed = new SQLiteColumn();
        renamed.setName("age");
        renamed.setType("TEXT");
        renamed.setName("aget");
        renamed.setCheck("age > 5");

        SQLiteTable table = new SQLiteTable();
        table.setName("person");
        table.setConstraints("CHECK (age > 5)");

        SQLiteAlertTableParam alertParam = new SQLiteAlertTableParam();
        alertParam.setTable(table);
        alertParam.setColumns(new SQLiteColumns(List.of(renamed)));
        String sql = SQLiteTableAlertSqlGenerator.generateSqlSingle(alertParam);

        assertTrue(sql.contains("CHECK (\"aget\" > 5)"));
        assertTrue(sql.contains("INSERT INTO \"__easyshell_new_"));
        assertTrue(sql.contains("(\"aget\")\nSELECT \"age\" FROM \"person\""));
    }

    @Test
    public void testViewGenerateApiAndAlertOrder() {
        SQLiteView view = new SQLiteView();
        view.setName("simple_view");
        view.setDefinition("SELECT 1 AS value");

        SQLiteCreateViewParam createParam = new SQLiteCreateViewParam(view);
        List<String> createSqlList = SQLiteViewCreateSqlGenerator.generateSql(createParam);
        assertEquals(List.of(SQLiteViewCreateSqlGenerator.generateSqlSingle(createParam)), createSqlList);

        SQLiteAlertViewParam alertParam = new SQLiteAlertViewParam(view);
        List<String> alertSqlList = SQLiteViewAlertSqlGenerator.generateSql(alertParam);
        assertEquals(2, alertSqlList.size());
        assertTrue(alertSqlList.getFirst().startsWith("DROP VIEW IF EXISTS "));
        assertTrue(alertSqlList.getLast().startsWith("CREATE VIEW "));

        String alertSql = SQLiteViewAlertSqlGenerator.generateSqlSingle(alertParam);
        assertTrue(alertSql.indexOf("DROP VIEW IF EXISTS ") < alertSql.indexOf("CREATE VIEW "));
    }

    @Test
    public void testViewPreviewUsesDefaultValuesWhenRequiredInfoIsEmpty() {
        assertEquals("CREATE VIEW \"Unnamed_View\" AS\nSELECT 1;",
                SQLiteViewCreateSqlGenerator.createViewPreview(null, null));
        assertEquals("CREATE VIEW \"preview_view\" AS\nSELECT 1;",
                SQLiteViewCreateSqlGenerator.createViewPreview("preview_view", " ; "));
        assertEquals("CREATE VIEW \"preview_view\" AS\nSELECT 2;",
                SQLiteViewCreateSqlGenerator.createViewPreview("preview_view", " SELECT 2; "));
    }

    @Test
    public void testFunctionGenerateApi() {
        SQLiteFunction function = new SQLiteFunction();
        function.setName("simple_view");
        function.setKind(SQLiteFunction.KIND_TABLE_VIEW);
        function.setDefinition("SELECT 1 AS value");

        SQLiteCreateFunctionParam createParam = new SQLiteCreateFunctionParam();
        createParam.setFunction(function);
        assertEquals("CREATE VIEW \"simple_view\" AS\nSELECT 1 AS value;",
                SQLiteFunctionCreateSqlGenerator.generateSqlSingle(createParam));

        SQLiteAlertFunctionParam alertParam = new SQLiteAlertFunctionParam();
        alertParam.setFunction(function);
        List<String> alertSqlList = SQLiteFunctionAlertSqlGenerator.generateSql(alertParam);
        assertEquals(2, alertSqlList.size());
        assertTrue(alertSqlList.getFirst().startsWith("DROP VIEW IF EXISTS "));
        assertTrue(alertSqlList.getLast().startsWith("CREATE VIEW "));

        String alertSql = SQLiteFunctionAlertSqlGenerator.generateSqlSingle(alertParam);
        assertTrue(alertSql.startsWith("DROP VIEW IF EXISTS \"simple_view\";"));
        assertTrue(alertSql.indexOf("CREATE VIEW ") > alertSql.indexOf("DROP VIEW IF EXISTS "));
    }

    @Test
    public void testFunctionGenerateUsesRawValues() {
        SQLiteFunction function = new SQLiteFunction();
        function.setName(" raw_name ");
        function.setKind(SQLiteFunction.KIND_GENERATED_COLUMN);
        function.setTargetTable(" raw_table ");
        function.setReturnType("REAL");
        function.setExpression(" price * 2 ");
        function.setGeneratedStorage("invalid");

        SQLiteCreateFunctionParam param = new SQLiteCreateFunctionParam();
        param.setFunction(function);
        assertEquals("ALTER TABLE \" raw_table \" ADD COLUMN \" raw_name \" REAL "
                        + "GENERATED ALWAYS AS ( price * 2 ) invalid;",
                SQLiteFunctionCreateSqlGenerator.generateSqlSingle(param));

        SQLiteFunction invalid = new SQLiteFunction();
        invalid.setName("bad");
        invalid.setKind(SQLiteFunction.KIND_GENERATED_COLUMN);
        assertEquals("ALTER TABLE \"\" ADD COLUMN \"bad\" GENERATED ALWAYS AS () VIRTUAL;",
                SQLiteFunctionCreateSqlGenerator.createFunction(invalid));
    }

    private SQLiteCreateTableParam createTableParam() {
        SQLiteColumn id = new SQLiteColumn("id");
        id.setType("INTEGER");
        id.setPrimaryKey(true);

        SQLiteTable table = new SQLiteTable("sample");
        SQLiteCreateTableParam param = new SQLiteCreateTableParam();
        param.setTable(table);
        param.setColumns(new SQLiteColumns(List.of(id)));
        return param;
    }

    private int count(String sql, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = sql.indexOf(keyword, index)) >= 0) {
            count++;
            index += keyword.length();
        }
        return count;
    }
}
