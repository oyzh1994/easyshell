package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteInsertRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordPrimaryKey;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 生成 SQLite 人工复测夹具。默认跳过，使用 -Dsqlite.fixture=true 执行。
 *
 * @since 2026-11-22
 */
public class SQLiteComplexDataFixtureTest {

    private static final Path SOURCE = Path.of(
            "/Users/oyzh/Desktop/导出数据/easyshell.db");

    private static final Path TARGET = Path.of(
            "/Users/oyzh/Desktop/导出数据/easyshell_test2.db");

    private static final String COMPLEX_TABLE = """
            CREATE TABLE "complex_types" (
              "id" INTEGER PRIMARY KEY AUTOINCREMENT,
              "c_small" SMALLINT,
              "c_int" INTEGER,
              "c_big" BIGINT,
              "c_real" REAL,
              "c_double" DOUBLE,
              "c_float" FLOAT,
              "c_numeric" NUMERIC,
              "c_decimal" DECIMAL(30,10),
              "c_text" TEXT,
              "c_varchar" VARCHAR(200),
              "c_char" CHAR(20),
              "c_empty_text" TEXT,
              "c_nullable_text" TEXT,
              "c_blob" BLOB,
              "c_empty_blob" BLOB,
              "c_date" DATE,
              "c_time" TIME,
              "c_datetime" DATETIME,
              "c_boolean" BOOLEAN,
              "c_json" JSON,
              "c_any",
              "c_virtual" TEXT GENERATED ALWAYS AS (upper("c_varchar")) VIRTUAL,
              "c_stored" TEXT GENERATED ALWAYS AS ("c_varchar" || '-stored') STORED
            )
            """;

    private static final String DESIGN_TABLE = """
            CREATE TABLE "generated_design" (
              "id" INTEGER PRIMARY KEY AUTOINCREMENT,
              "base_text" TEXT,
              "editable_value" TEXT,
              "virtual_value" TEXT GENERATED ALWAYS AS (upper("base_text")) VIRTUAL,
              "stored_value" TEXT GENERATED ALWAYS AS ("base_text" || '-stored') STORED
            )
            """;

    @BeforeClass
    public static void initColumns() {
        ShellSqliteColumnUtil.init();
    }

    @Test
    public void regenerateManualFixtures() throws Throwable {
        Assume.assumeTrue("Set -Dsqlite.fixture=true to regenerate the manual SQLite fixtures",
                Boolean.getBoolean("sqlite.fixture"));
        try (ShellSqliteClient source = open(SOURCE);
             ShellSqliteClient target = open(TARGET)) {
            source.start();
            target.start();
            source.executeSqlSimple("DROP TABLE IF EXISTS \"complex_types\"");
            source.executeSqlSimple("DROP TABLE IF EXISTS \"generated_design\"");
            target.executeSqlSimple("DROP TABLE IF EXISTS \"complex_types\"");
            target.executeSqlSimple("DROP TABLE IF EXISTS \"generated_design\"");
            source.executeSqlSimple(COMPLEX_TABLE);
            target.executeSqlSimple(COMPLEX_TABLE);
            source.executeSqlSimple(DESIGN_TABLE);
            target.executeSqlSimple(DESIGN_TABLE);

            for (int index = 1; index <= 10; index++) {
                insert(source, "complex_types", complexValues(index));
            }
            for (int index = 1; index <= 3; index++) {
                Map<String, Object> values = new LinkedHashMap<>();
                values.put("base_text", "design-" + index + "-中文");
                values.put("editable_value", "normal-" + index);
                insert(source, "generated_design", values);
            }

            assertEquals(10, source.selectRowCount("complex_types"));
            assertEquals(0, target.selectRowCount("complex_types"));
            assertEquals(3, source.selectRowCount("generated_design"));
            assertEquals(0, target.selectRowCount("generated_design"));
        }
    }

    private static void insert(ShellSqliteClient client, String table, Map<String, Object> values) {
        List<SQLiteColumn> columns = client.selectColumns(table);
        SQLiteRecord record = new SQLiteRecord(columns);
        values.forEach(record::putValue);
        SQLiteRecordPrimaryKey primaryKey = new SQLiteRecordPrimaryKey();
        primaryKey.init(record.getColumns().column("id"), record);
        SQLiteInsertRecordParam param = new SQLiteInsertRecordParam();
        param.setTableName(table);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
    }

    private static Map<String, Object> complexValues(int index) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_small", -1234 - index);
        values.put("c_int", -987654 - index);
        values.put("c_big", -9000000000L - index);
        values.put("c_real", 123.25D + index);
        values.put("c_double", 98765.4321D + index);
        values.put("c_float", 456.5D + index);
        values.put("c_numeric", new BigDecimal("1234567890.0123"));
        values.put("c_decimal", new BigDecimal("12345678901234567890.1234567890"));
        values.put("c_text", "O'Reilly \"quoted\" line\n中文 😀 backslash\\tmp " + index);
        values.put("c_varchar", "Alpha-Value-" + index);
        values.put("c_char", "char-" + index);
        values.put("c_empty_text", "");
        values.put("c_nullable_text", null);
        values.put("c_blob", new byte[]{0, 1, 0x27, 0x22, (byte) 0xFE, (byte) 0xFF});
        values.put("c_empty_blob", new byte[0]);
        values.put("c_date", "2026-11-22");
        values.put("c_time", "12:34:56");
        values.put("c_datetime", "2026-11-22 12:34:56");
        values.put("c_boolean", index % 2 == 1);
        values.put("c_json", "{\"id\":" + index
                + ",\"name\":\"row" + index + " 中文 😀\",\"quote\":\"a\\\"b\","
                + "\"slash\":\"C:\\\\tmp\",\"array\":[1,true,null]}");
        values.put("c_any", "dynamic-'value'-" + index);
        return values;
    }

    private static ShellSqliteClient open(Path file) {
        ShellConnect connect = new ShellConnect();
        connect.setName(file.getFileName().toString());
        connect.setType(ShellPrototype.SQLITE);
        connect.setOsType(ShellPrototype.SQLITE);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setReadonly(false);
        connect.setEnvironment("journal_mode=DELETE\nforeign_keys=1\nsynchronous=NORMAL\n"
                + "createIfMissing=true\n");
        return new ShellSqliteClient(connect);
    }
}
