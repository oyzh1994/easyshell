package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataImportFile;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataExportHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataImportHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataRunSqlFileHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataTransportHandler;
import cn.oyzh.easyshell.data.sqlite.SQLiteDataImportHelper;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteInsertRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordPrimaryKey;
import cn.oyzh.easyshell.sqlite.record.SQLiteSelectRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteUpdateRecordParam;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataExcelTypeFileReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 复杂字段、数据交换和生成列真实客户端集成测试。
 *
 * @since 2026-11-22
 */
public class SQLiteComplexDataIntegrationTest {

    private static final String TABLE = "complex_types";

    private static final String CREATE_TABLE = """
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

    private Path workDirectory;

    private ShellSqliteClient sourceClient;

    private ShellSqliteClient targetClient;

    private Path sourceFile;

    private Path targetFile;

    @BeforeClass
    public static void initColumns() {
        ShellSqliteColumnUtil.init();
    }

    @Before
    public void setUp() throws Throwable {
        this.workDirectory = Files.createTempDirectory("easyshell-sqlite-complex-");
        this.sourceFile = this.workDirectory.resolve("source.db");
        this.targetFile = this.workDirectory.resolve("target.db");
        this.sourceClient = openClient(this.sourceFile);
        this.targetClient = openClient(this.targetFile);
        this.sourceClient.start();
        this.targetClient.start();
        this.sourceClient.executeSqlSimple(CREATE_TABLE);
        this.targetClient.executeSqlSimple(CREATE_TABLE);
    }

    @org.junit.After
    public void tearDown() throws Exception {
        try {
            if (this.sourceClient != null) {
                this.sourceClient.close();
            }
            if (this.targetClient != null) {
                this.targetClient.close();
            }
        } finally {
            if (this.workDirectory != null && Files.exists(this.workDirectory)) {
                try (Stream<Path> paths = Files.walk(this.workDirectory)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
                }
            }
        }
    }

    @Test
    public void editsComplexValuesThroughClientEntry() {
        SQLiteRecord inserted = insertSeed(this.sourceClient, seedValues(1));
        long id = number(inserted.getValue("id")).longValue();
        assertSeedValues(seedValues(1), inserted);
        assertGeneratedValues(inserted, "ALPHA-VALUE-1", "Alpha-Value-1-stored");

        SQLiteRecord editable = loadSingle(this.sourceClient, id);
        for (Map.Entry<String, Object> entry : updatedValues().entrySet()) {
            SQLiteColumn column = editable.getColumns().column(entry.getKey());
            assertNotNull(entry.getKey(), column);
            editable.putValue(column, entry.getValue());
            editable.getProperty(entry.getKey()).setChanged(true);
        }

        SQLiteRecordPrimaryKey primaryKey = new SQLiteRecordPrimaryKey();
        primaryKey.init(editable.getColumns().column("id"), editable);
        SQLiteUpdateRecordParam updateParam = new SQLiteUpdateRecordParam();
        updateParam.setTableName(TABLE);
        updateParam.setRecord(editable.getRecordData());
        updateParam.setUpdateRecord(editable.getRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, this.sourceClient.updateRecord(updateParam));

        SQLiteRecord reloaded = loadSingle(this.sourceClient, id);
        assertSeedValues(updatedValues(), reloaded);
        assertGeneratedValues(reloaded, "BETA-VALUE-2", "Beta-Value-2-stored");
    }

    @Test
    public void exportsAndImportsEverySupportedFormat() throws Exception {
        insertSeed(this.sourceClient, seedValues(1));
        insertSeed(this.sourceClient, seedValues(2));
        for (String format : List.of("csv", "json", "xml", "txt", "xls", "xlsx", "sql", "html")) {
            Path file = this.workDirectory.resolve(TABLE + "." + format);
            export(format, file);
            assertTrue(format + " export is empty", Files.size(file) > 0);
            if ("csv".equals(format)) {
                assertTrue(Files.readString(file).startsWith("\"id\";\"c_small\""));
            }
            if ("html".equals(format)) {
                String html = Files.readString(file);
                assertTrue(html.contains("<!DOCTYPE html>"));
                assertTrue(html.contains("O&apos;Reilly"));
                continue;
            }

            resetRows(this.targetClient);
            try {
                if ("sql".equals(format)) {
                    SQLiteDataRunSqlFileHandler runHandler =
                            new SQLiteDataRunSqlFileHandler(this.targetClient, "main");
                    runHandler.setContinueWithErrors(false);
                    runHandler.file(file.toFile());
                    runHandler.runFile();
                } else {
                    importFile(format, file);
                }
            } catch (Exception ex) {
                throw new AssertionError(format + " import failed: " + file, ex);
            }
            List<SQLiteRecord> records = selectAll(this.targetClient);
            assertEquals(format + " imported row count", 2, records.size());
            for (SQLiteRecord record : records) {
                int index = number(record.getValue("id")).intValue();
                assertImportedSeed(record, format, seedValues(index));
                assertGeneratedValues(record,
                        "ALPHA-VALUE-" + index, "Alpha-Value-" + index + "-stored");
            }
        }
    }

    @Test
    public void importsLegacyCommaCsvWithQuotedValues() throws Exception {
        this.targetClient.executeSqlSimple("DROP TABLE IF EXISTS legacy_csv");
        this.targetClient.executeSqlSimple(
                "CREATE TABLE legacy_csv (id INTEGER PRIMARY KEY, name TEXT, json TEXT, data BLOB)");
        Path file = this.workDirectory.resolve("legacy.csv");
        Files.writeString(file, """
                "id","name","json","data"
                "1","a;b","{""key"":""value;still-json""}","X'00FF27'"
                """);

        SQLiteDataImportFile importFile = new SQLiteDataImportFile();
        importFile.setClient(this.targetClient);
        importFile.setFile(file.toFile());
        importFile.setTargetTableName("legacy_csv");
        SQLiteDataImportHandler handler = new SQLiteDataImportHandler(this.targetClient, "main");
        handler.setContinueWithErrors(false);
        handler.setFileType("csv");
        handler.setFiles(List.of(importFile));
        handler.importMode("insert");
        handler.columnIndex(0);
        handler.dataRowStarts(2);
        // The CSV UI hides this option; semicolon is the configured default.
        handler.fieldSeparator(";");
        handler.doImport();

        SQLiteSelectRecordParam param = new SQLiteSelectRecordParam();
        param.setTableName("legacy_csv");
        param.setColumns(this.targetClient.selectColumns("legacy_csv"));
        param.setStart(0L);
        param.setLimit(10L);
        SQLiteRecord record = this.targetClient.selectRecords(param).getFirst();
        assertEquals("a;b", record.getValue("name"));
        assertEquals("{\"key\":\"value;still-json\"}", record.getValue("json"));
        assertArrayEquals(new byte[]{0, (byte) 0xFF, 0x27},
                (byte[]) record.getValue("data"));
    }

    @Test
    public void transportsComplexTableBetweenSqliteFiles() throws Exception {
        insertSeed(this.sourceClient, seedValues(1));
        insertSeed(this.sourceClient, seedValues(2));

        DBDataTransportObject table = new DBDataTransportObject();
        table.setName(TABLE);
        SQLiteDataTransportHandler handler = new SQLiteDataTransportHandler();
        handler.setContinueWithErrors(false);
        handler.setSourceClient(this.sourceClient);
        handler.setTargetClient(this.targetClient);
        handler.setSourceDatabase("main");
        handler.setTargetDatabase("main");
        handler.setTables(List.of(table));
        handler.setSelectLimit(1);
        handler.setInsertLimit(1);
        handler.doTransport();

        List<SQLiteRecord> records = selectAll(this.targetClient);
        assertEquals(2, records.size());
        Map<Long, SQLiteRecord> byId = new LinkedHashMap<>();
        for (SQLiteRecord record : records) {
            byId.put(number(record.getValue("id")).longValue(), record);
        }
        assertSeedValues(seedValues(1), byId.get(1L));
        assertSeedValues(seedValues(2), byId.get(2L));
        assertGeneratedValues(byId.get(1L), "ALPHA-VALUE-1", "Alpha-Value-1-stored");
        assertGeneratedValues(byId.get(2L), "ALPHA-VALUE-2", "Alpha-Value-2-stored");
    }

    @Test
    public void designsVirtualAndStoredGeneratedColumns() {
        insertSeed(this.sourceClient, seedValues(1));
        insertSeed(this.sourceClient, seedValues(2));
        SQLiteSchemaManager manager = new SQLiteSchemaManager(this.sourceClient);

        SQLiteTable table = this.sourceClient.selectTable(TABLE);
        table.setColumns(this.sourceClient.selectColumns(TABLE));
        SQLiteColumn any = table.getColumns().stream()
                .filter(column -> "c_any".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        any.setGeneratedExpression("lower(c_varchar)");
        any.setGeneratedStorage("VIRTUAL");
        any.normalizeGeneratedAttributes();
        manager.updateTable(TABLE, table);
        assertGeneratedColumn("c_any", "VIRTUAL", "lower(c_varchar)");
        assertGeneratedValues(loadSingle(this.sourceClient, 1L),
                "ALPHA-VALUE-1", "Alpha-Value-1-stored");
        assertEquals("alpha-value-1",
                loadSingle(this.sourceClient, 1L).getValue("c_any"));

        table = this.sourceClient.selectTable(TABLE);
        table.setColumns(this.sourceClient.selectColumns(TABLE));
        any = table.getColumns().stream()
                .filter(column -> "c_any".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        any.setGeneratedExpression("upper(c_varchar) || ':stored'");
        any.setGeneratedStorage("STORED");
        manager.updateTable(TABLE, table);
        assertGeneratedColumn("c_any", "STORED", "upper(c_varchar) || ':stored'");
        assertEquals("ALPHA-VALUE-1:stored",
                loadSingle(this.sourceClient, 1L).getValue("c_any"));

        table = this.sourceClient.selectTable(TABLE);
        table.setColumns(this.sourceClient.selectColumns(TABLE));
        any = table.getColumns().stream()
                .filter(column -> "c_any".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        any.setGenerated(false);
        any.normalizeGeneratedAttributes();
        manager.updateTable(TABLE, table);
        SQLiteColumn normal = this.sourceClient.selectColumns(TABLE).stream()
                .filter(column -> "c_any".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        assertFalse(normal.isGenerated());
        assertEquals("ALPHA-VALUE-1:stored",
                loadSingle(this.sourceClient, 1L).getValue("c_any"));
        assertSeedValuesExceptAny(seedValues(2), loadSingle(this.sourceClient, 2L));
    }

    private void export(String format, Path file) throws Exception {
        SQLiteDataExportTable table = new SQLiteDataExportTable();
        table.setName(TABLE);
        table.setFilePath(file.toString());
        table.columns(this.sourceClient.selectColumns(TABLE));
        SQLiteDataExportHandler handler = new SQLiteDataExportHandler(this.sourceClient, "main");
        handler.setContinueWithErrors(false);
        handler.setFileType(format);
        handler.setQueryLimit(1);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.setTables(List.of(table));
        handler.doExport();
    }

    private void importFile(String format, Path file) throws Exception {
        SQLiteDataImportFile importFile = new SQLiteDataImportFile();
        importFile.setClient(this.targetClient);
        importFile.setFile(file.toFile());
        importFile.setTargetTableName(TABLE);
        List<String> importedSql = new ArrayList<>();
        SQLiteDataImportHandler handler = new SQLiteDataImportHandler(this.targetClient, "main") {
            @Override
            public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
                importedSql.addAll(list);
                super.doBatchInsert(list, parallel);
            }
        };
        handler.setContinueWithErrors(false);
        handler.setFileType(format);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.setInsertLimit(1);
        handler.setBatchLimit(1);
        handler.setFiles(List.of(importFile));
        handler.importMode("insert");
        handler.columnIndex(0);
        handler.dataRowStarts(2);
        handler.fieldSeparator(";");
        try {
            handler.doImport();
        } catch (Exception ex) {
            if ("xls".equals(format) || "xlsx".equals(format)) {
                DBDataImportConfig config = new DBDataImportConfig();
                config.setDateFormat("yyyy-MM-dd HH:mm:ss");
                try (DBDataExcelTypeFileReader reader =
                             new DBDataExcelTypeFileReader(file.toFile(), config)) {
                    List<Map<String, Object>> records = reader.readRecords(2);
                    List<String> sqlList = SQLiteDataImportHelper.toInsertSql(
                            TABLE, this.targetClient.selectColumns(TABLE), records);
                    throw new AssertionError(format + " handled SQL: " + importedSql
                            + "\ndebug SQL: " + sqlList + "\nrecords: " + records, ex);
                }
            }
            throw ex;
        }
    }

    private static SQLiteRecord insertSeed(ShellSqliteClient client, Map<String, Object> values) {
        List<SQLiteColumn> columns = client.selectColumns(TABLE);
        SQLiteRecord record = new SQLiteRecord(columns);
        values.forEach(record::putValue);
        record.putValue("c_virtual", "ignored");
        record.putValue("c_stored", "ignored");
        assertTrue(record.getProperty("c_virtual").isReadonly());
        assertTrue(record.getProperty("c_stored").isReadonly());

        SQLiteRecordPrimaryKey primaryKey = new SQLiteRecordPrimaryKey();
        primaryKey.init(record.getColumns().column("id"), record);
        SQLiteInsertRecordParam param = new SQLiteInsertRecordParam();
        param.setTableName(TABLE);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
        return loadSingle(client, number(primaryKey.getReturnData()).longValue());
    }

    private static SQLiteRecord loadSingle(ShellSqliteClient client, long id) {
        return selectAll(client).stream()
                .filter(record -> number(record.getValue("id")).longValue() == id)
                .findFirst()
                .orElseThrow();
    }

    private static List<SQLiteRecord> selectAll(ShellSqliteClient client) {
        SQLiteSelectRecordParam param = new SQLiteSelectRecordParam();
        param.setTableName(TABLE);
        param.setColumns(client.selectColumns(TABLE));
        param.setStart(0L);
        param.setLimit(100L);
        return client.selectRecords(param);
    }

    private void resetRows(ShellSqliteClient client) {
        client.executeSqlSimple("DELETE FROM " + TABLE);
    }

    private void assertGeneratedColumn(String name, String storage, String expression) {
        SQLiteColumn column = this.sourceClient.selectColumns(TABLE).stream()
                .filter(item -> name.equals(item.getName()))
                .findFirst()
                .orElseThrow();
        assertTrue(column.isGenerated());
        assertEquals(storage, column.getGeneratedStorage());
        assertTrue(column.getGeneratedExpression(), column.getGeneratedExpression()
                .replace(" ", "").toLowerCase()
                .contains(expression.replace(" ", "").toLowerCase()));
    }

    private static Map<String, Object> seedValues(int index) {
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
        values.put("c_boolean", true);
        values.put("c_json", "{\"id\":" + index
                + ",\"name\":\"row" + index + " 中文 😀\",\"quote\":\"a\\\"b\","
                + "\"slash\":\"C:\\\\tmp\",\"array\":[1,true,null]}");
        values.put("c_any", "dynamic-'value'-" + index);
        return values;
    }

    private static Map<String, Object> updatedValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_small", 4321);
        values.put("c_int", 987654);
        values.put("c_big", 8000000000L);
        values.put("c_real", 987.5D);
        values.put("c_double", 12345.6789D);
        values.put("c_float", 654.25D);
        values.put("c_numeric", new BigDecimal("9876543210.9876"));
        values.put("c_decimal", new BigDecimal("98765432109876543210.0123456789"));
        values.put("c_text", "updated O'Reilly \"quoted\"\n中文 😀 backslash\\path");
        values.put("c_varchar", "Beta-Value-2");
        values.put("c_char", "updated");
        values.put("c_empty_text", "");
        values.put("c_nullable_text", null);
        values.put("c_blob", new byte[]{(byte) 0xFF, (byte) 0xFE, 3, 2, 1, 0});
        values.put("c_empty_blob", new byte[0]);
        values.put("c_date", "2027-01-02");
        values.put("c_time", "23:59:58");
        values.put("c_datetime", "2027-01-02 23:59:58");
        values.put("c_boolean", false);
        values.put("c_json", "{\"id\":2,\"name\":\"updated 中文 😀\","
                + "\"quote\":\"a\\\"b\",\"slash\":\"D:\\\\work\",\"array\":[false,null]}");
        values.put("c_any", "updated-dynamic-value");
        return values;
    }

    private static void assertSeedValues(Map<String, Object> expected, SQLiteRecord actual) {
        assertNotNull(actual);
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object actualValue = actual.getValue(entry.getKey());
            if (entry.getValue() instanceof byte[] bytes) {
                assertArrayEquals(entry.getKey(), bytes, (byte[]) actualValue);
            } else if (entry.getValue() instanceof BigDecimal decimal) {
                assertTrue(entry.getKey() + ": " + actualValue,
                        actualValue instanceof Number
                                && decimal.doubleValue() == ((Number) actualValue).doubleValue());
            } else if (entry.getValue() instanceof Boolean expectedBoolean) {
                assertBoolean(entry.getKey(), expectedBoolean, actualValue);
            } else if (entry.getValue() instanceof Number expectedNumber) {
                assertEquals(entry.getKey(), expectedNumber.doubleValue(),
                        ((Number) actualValue).doubleValue(), 0.000001D);
            } else {
                assertEquals(entry.getKey(), String.valueOf(entry.getValue()),
                        String.valueOf(actualValue));
            }
        }
    }

    private static void assertSeedValuesExceptAny(Map<String, Object> expected, SQLiteRecord actual) {
        Map<String, Object> values = new LinkedHashMap<>(expected);
        values.remove("c_any");
        assertSeedValues(values, actual);
    }

    private static void assertImportedSeed(SQLiteRecord actual, String format,
                                           Map<String, Object> expected) {
        assertEquals(format + " c_text", expected.get("c_text"), actual.getValue("c_text"));
        assertEquals(format + " c_varchar", expected.get("c_varchar"),
                actual.getValue("c_varchar"));
        assertEquals(format + " c_json", expected.get("c_json"), actual.getValue("c_json"));
        assertEquals(format + " c_empty_text", "", actual.getValue("c_empty_text"));
        Object nullableText = actual.getValue("c_nullable_text");
        if (List.of("json", "sql").contains(format)) {
            assertTrue(format + " c_nullable_text: " + nullableText, nullableText == null);
        } else {
            // CSV/分隔文本/Excel/XML 对空元素没有统一的 NULL 类型编码。
            assertTrue(format + " c_nullable_text: " + nullableText,
                    nullableText == null || "".equals(nullableText));
        }
        assertArrayEquals(format + " c_blob", (byte[]) expected.get("c_blob"),
                (byte[]) actual.getValue("c_blob"));
        assertArrayEquals(format + " c_empty_blob", new byte[0],
                (byte[]) actual.getValue("c_empty_blob"));
        assertEquals(format + " c_date", expected.get("c_date"),
                String.valueOf(actual.getValue("c_date")));
        assertBoolean(format + " c_boolean", true, actual.getValue("c_boolean"));
        for (String column : List.of("c_small", "c_int", "c_big", "c_real", "c_double",
                "c_float", "c_numeric", "c_decimal")) {
            assertEquals(format + " " + column,
                    ((Number) expected.get(column)).doubleValue(),
                    ((Number) actual.getValue(column)).doubleValue(), 0.001D);
        }
    }

    private static void assertGeneratedValues(SQLiteRecord record, String virtual, String stored) {
        assertEquals(virtual, String.valueOf(record.getValue("c_virtual")));
        assertEquals(stored, String.valueOf(record.getValue("c_stored")));
        assertTrue(record.getProperty("c_virtual").isReadonly());
        assertTrue(record.getProperty("c_stored").isReadonly());
    }

    private static void assertBoolean(String name, boolean expected, Object actual) {
        boolean value;
        if (actual instanceof Boolean booleanValue) {
            value = booleanValue;
        } else {
            value = number(actual).intValue() != 0;
        }
        assertEquals(name, expected, value);
    }

    private static Number number(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private static ShellSqliteClient openClient(Path file) {
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
