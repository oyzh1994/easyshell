package cn.oyzh.easyshell.mariadb;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.mariadb.dto.ShellMariadbDataExportTable;
import cn.oyzh.easyshell.data.mariadb.dto.ShellMariadbDataImportFile;
import cn.oyzh.easyshell.data.mariadb.handler.ShellMariadbDataExportHandler;
import cn.oyzh.easyshell.data.mariadb.handler.ShellMariadbDataImportHandler;
import cn.oyzh.easyshell.data.mariadb.handler.ShellMariadbDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.record.MariadbInsertRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbUpdateRecordParam;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import com.alibaba.fastjson2.JSON;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB 复杂字段记录编辑、导入导出与传输集成测试。
 */
public class MariadbComplexDataIntegrationTest {

    private static final String HOST = property("mariadb.host", "MARIADB_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("mariadb.port", "MARIADB_PORT", "33306"));
    private static final String USER = property("mariadb.user", "MARIADB_USER", "root");
    private static final String PASSWORD = property("mariadb.password", "MARIADB_PASSWORD", "123456");
    private static final String SOURCE_DB = "easyshell_mariadb_complex_it";
    private static final String TARGET_DB = "easyshell_mariadb_complex_target";
    private static final String TABLE = "complex_types";

    private static final String CREATE_TABLE = """
            CREATE TABLE `complex_types` (
              `id` BIGINT NOT NULL AUTO_INCREMENT,
              `c_tiny` TINYINT,
              `c_utiny` TINYINT UNSIGNED,
              `c_small` SMALLINT,
              `c_medium` MEDIUMINT,
              `c_int` INT,
              `c_uint` INT UNSIGNED,
              `c_big` BIGINT,
              `c_ubig` BIGINT UNSIGNED,
              `c_decimal` DECIMAL(30,10),
              `c_float` FLOAT,
              `c_double` DOUBLE,
              `c_bit` BIT(13),
              `c_bool` BOOLEAN,
              `c_date` DATE,
              `c_time` TIME(6),
              `c_datetime` DATETIME(6),
              `c_timestamp` TIMESTAMP(6) NULL,
              `c_year` YEAR,
              `c_char` CHAR(8),
              `c_varchar` VARCHAR(100),
              `c_tinytext` TINYTEXT,
              `c_text` TEXT,
              `c_mediumtext` MEDIUMTEXT,
              `c_longtext` LONGTEXT,
              `c_binary` BINARY(8),
              `c_varbinary` VARBINARY(100),
              `c_tinyblob` TINYBLOB,
              `c_blob` BLOB,
              `c_mediumblob` MEDIUMBLOB,
              `c_longblob` LONGBLOB,
              `c_enum` ENUM('alpha','beta','gamma'),
              `c_set` SET('red','green','blue'),
              `c_json` JSON,
              `c_point` POINT,
              `c_linestring` LINESTRING,
              `c_polygon` POLYGON,
              `c_multipoint` MULTIPOINT,
              `c_multilinestring` MULTILINESTRING,
              `c_multipolygon` MULTIPOLYGON,
              `c_geometry` GEOMETRY,
              `c_geomcollection` GEOMETRYCOLLECTION,
              `c_virtual` VARCHAR(120) GENERATED ALWAYS AS (CONCAT(`c_varchar`, '-virtual')) VIRTUAL,
              `c_stored` VARCHAR(120) GENERATED ALWAYS AS (CONCAT(`c_varchar`, '-stored')) STORED,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB
            """;

    private static ShellMariadbClient sourceClient;
    private static ShellMariadbClient targetClient;
    private static Path workDirectory;

    @BeforeClass
    public static void setUp() throws Throwable {
        Assume.assumeTrue("MariaDB endpoint is not reachable", isReachable(HOST, PORT));
        ShellMariadbColumnUtil.init();
        sourceClient = openClient();
        targetClient = openClient();
        recreateDatabase(sourceClient, SOURCE_DB);
        recreateDatabase(targetClient, TARGET_DB);
        workDirectory = Files.createTempDirectory("easyshell-mariadb-complex-");
    }

    @AfterClass
    public static void tearDown() throws Throwable {
        try {
            if (sourceClient != null) {
                dropDatabase(sourceClient, SOURCE_DB);
            }
            if (targetClient != null) {
                dropDatabase(targetClient, TARGET_DB);
            }
        } finally {
            if (sourceClient != null) {
                sourceClient.close();
            }
            if (targetClient != null) {
                targetClient.close();
            }
        }
    }

    @Before
    public void resetSourceTable() {
        resetTable(sourceClient, SOURCE_DB, TABLE);
    }

    @Test
    public void queryPageSkipsGeneratedColumnsOnInsert() {
        var queryResult = sourceClient.executeSql(SOURCE_DB,
                "SELECT * FROM `" + SOURCE_DB + "`.`" + TABLE + "`").getResults().getFirst();
        assertEquals(SOURCE_DB, queryResult.dbName());
        assertEquals(TABLE, queryResult.tableName());
        assertTrue(queryResult.isUpdatable());

        MariadbColumn virtual = queryResult.getColumns().column("c_virtual");
        MariadbColumn stored = queryResult.getColumns().column("c_stored");
        MariadbColumn bool = queryResult.getColumns().column("c_bool");
        assertNotNull(virtual);
        assertNotNull(stored);
        assertNotNull(bool);
        assertTrue(virtual.isGenerated());
        assertTrue(virtual.isVirtual());
        assertTrue(StringUtil.containsIgnoreCase(virtual.getGeneratedExpression(), "CONCAT"));
        assertTrue(stored.isGenerated());
        assertFalse(stored.isVirtual());
        assertEquals("TINYINT", bool.getType());
        assertEquals(1, bool.getSize().intValue());
        assertTrue(bool.supportBoolean());

        MariadbRecord record = new MariadbRecord(queryResult.getColumns());
        seedValues().forEach(record::putValue);
        assertFalse(record.getRecordData().columns().contains("c_virtual"));
        assertFalse(record.getRecordData().columns().contains("c_stored"));

        MariadbRecordPrimaryKey primaryKey = new MariadbRecordPrimaryKey();
        primaryKey.init(queryResult.getPrimaryKey(), record);
        MariadbInsertRecordParam param = new MariadbInsertRecordParam();
        param.setDbName(queryResult.dbName());
        param.setTableName(queryResult.tableName());
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, sourceClient.insertRecord(param));

        long id = ((Number) primaryKey.getReturnData()).longValue();
        assertGeneratedValues(loadSingle(sourceClient, SOURCE_DB, TABLE, id),
                "alpha-value", "alpha-value");
    }

    @Test
    public void editsEveryComplexColumnThroughClientEntry() {
        MariadbRecord inserted = insertSeed(sourceClient, SOURCE_DB, TABLE);
        long id = ((Number) inserted.getValue("id")).longValue();

        MariadbRecord selected = loadSingle(sourceClient, SOURCE_DB, TABLE, id);
        assertExpectedValues(seedValues(), selected);
        assertGeneratedValues(selected, "alpha-value", "alpha-value");

        MariadbRecord editable = loadSingle(sourceClient, SOURCE_DB, TABLE, id);
        Map<String, Object> updatedValues = updatedValues();
        for (Map.Entry<String, Object> entry : updatedValues.entrySet()) {
            MariadbColumn column = editable.getColumns().column(entry.getKey());
            assertNotNull(column);
            assertFalse("generated column must not be edited: " + entry.getKey(),
                    column.isGenerated());
            editable.putValue(column, entry.getValue());
            editable.getProperty(entry.getKey()).setChanged(true);
        }

        MariadbColumn idColumn = editable.getColumns().column("id");
        MariadbRecordPrimaryKey primaryKey = new MariadbRecordPrimaryKey();
        primaryKey.init(idColumn, editable);
        MariadbUpdateRecordParam updateParam = new MariadbUpdateRecordParam();
        updateParam.setDbName(SOURCE_DB);
        updateParam.setTableName(TABLE);
        updateParam.setRecord(editable.getOriginalRecordData());
        updateParam.setUpdateRecord(editable.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, sourceClient.updateRecord(updateParam));

        MariadbRecord updated = loadSingle(sourceClient, SOURCE_DB, TABLE, id);
        assertExpectedValues(updatedValues, updated);
        assertGeneratedValues(updated, "beta-value", "beta-value");
    }

    @Test
    public void updatesJsonEditorObjectThroughClientEntry() {
        MariadbRecord inserted = insertSeed(sourceClient, SOURCE_DB, TABLE);
        long id = ((Number) inserted.getValue("id")).longValue();
        MariadbRecord record = loadSingle(sourceClient, SOURCE_DB, TABLE, id);

        MariadbColumn jsonColumn = record.getColumns().column("c_json");
        Object edited = JSON.parseObject(
                "{\"id\":10,\"name\":\"row10 中文 😀\",\"nested\":{\"items\":[10,\"x\"],"
                        + "\"slash\":\"C:\\\\tmp\",\"quoted\":\"a\\\"b\"}}");
        record.putValue(jsonColumn, edited);
        record.getProperty("c_json").setChanged(true);

        MariadbRecordPrimaryKey primaryKey = new MariadbRecordPrimaryKey();
        primaryKey.init(record.getColumns().column("id"), record);
        MariadbUpdateRecordParam updateParam = new MariadbUpdateRecordParam();
        updateParam.setDbName(SOURCE_DB);
        updateParam.setTableName(TABLE);
        updateParam.setRecord(record.getOriginalRecordData());
        updateParam.setUpdateRecord(record.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, sourceClient.updateRecord(updateParam));

        MariadbRecord updated = loadSingle(sourceClient, SOURCE_DB, TABLE, id);
        assertEquals(JSONUtil.parseObject(edited.toString()),
                JSONUtil.parseObject(updated.getValue("c_json").toString()));
    }

    @Test
    public void exportsAndImportsEverySupportedFormat() throws Exception {
        insertSeed(sourceClient, SOURCE_DB, TABLE);

        for (String format : List.of("csv", "json", "xml", "txt", "xlsx", "sql", "html")) {
            Path file = workDirectory.resolve("complex_types." + format);
            export(SOURCE_DB, format, file);
            assertTrue("empty export: " + format, Files.size(file) > 0);

            if (List.of("csv", "json", "xml", "txt", "xlsx").contains(format)) {
                String targetTable = "import_" + format.replace("xlsx", "excel");
                createLike(targetClient, TARGET_DB, targetTable);
                importFile(TARGET_DB, targetTable, format, file);
                List<MariadbRecord> records = targetClient.selectRecords(selectParam(TARGET_DB, targetTable));
                assertEquals("imported rows for " + format, 1, records.size());
                assertExpectedValues(seedValues(), records.get(0));
                assertGeneratedValues(records.get(0), "alpha-value", "alpha-value");
            } else if ("sql".equals(format)) {
                createLike(targetClient, TARGET_DB, TABLE);
                targetClient.executeSqlSimple(TARGET_DB, Files.readString(file));
                List<MariadbRecord> records = targetClient.selectRecords(selectParam(TARGET_DB, TABLE));
                assertEquals("imported rows for SQL", 1, records.size());
                assertExpectedValues(seedValues(), records.get(0));
                assertGeneratedValues(records.get(0), "alpha-value", "alpha-value");
            } else if ("html".equals(format)) {
                String html = Files.readString(file);
                assertTrue(html.contains("<!DOCTYPE html>"));
                assertTrue(html.contains("POINT(1 2)"));
            }
        }
    }

    @Test
    public void transportsEveryComplexColumnToAnotherSchema() throws Exception {
        insertSeed(sourceClient, SOURCE_DB, TABLE);
        resetTable(targetClient, TARGET_DB, TABLE);

        DBDataTransportObject table = new DBDataTransportObject();
        table.setName(TABLE);
        ShellMariadbDataTransportHandler handler = new ShellMariadbDataTransportHandler();
        List<String> messages = new ArrayList<>();
        handler.setMessageHandler(messages::add);
        handler.setContinueWithErrors(false);
        handler.setSourceClient(sourceClient);
        handler.setTargetClient(targetClient);
        handler.setSourceDatabase(SOURCE_DB);
        handler.setTargetDatabase(TARGET_DB);
        handler.setTables(List.of(table));
        handler.setSelectLimit(2);
        handler.setInsertLimit(2);
        handler.doTransport();

        List<MariadbRecord> records = targetClient.selectRecords(selectParam(TARGET_DB, TABLE));
        assertEquals(messages.toString(), 1, records.size());
        assertExpectedValues(seedValues(), records.get(0));
        assertGeneratedValues(records.get(0), "alpha-value", "alpha-value");
    }

    private static void export(String database, String format, Path file) throws Exception {
        List<MariadbColumn> columns = sourceClient.selectColumns(new MariadbSelectColumnParam(database, TABLE));
        ShellMariadbDataExportTable table = new ShellMariadbDataExportTable();
        table.setName(TABLE);
        table.setFilePath(file.toString());
        table.columns(columns);

        ShellMariadbDataExportHandler handler = new ShellMariadbDataExportHandler(sourceClient, database);
        handler.setFileType(format);
        handler.setQueryLimit(2);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.setTables(List.of(table));
        handler.doExport();
    }

    private static void importFile(String database, String table, String format, Path file) throws Exception {
        ShellMariadbDataImportFile importFile = new ShellMariadbDataImportFile();
        importFile.setFile(file.toFile());
        importFile.setTargetTableName(table);

        ShellMariadbDataImportHandler handler = new ShellMariadbDataImportHandler(targetClient, database);
        handler.setContinueWithErrors(false);
        handler.setFileType(format.equals("xlsx") ? "excel" : format);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.setInsertLimit(2);
        handler.setBatchLimit(2);
        handler.setFiles(List.of(importFile));
        handler.getConfig().setImportMode("2");
        handler.doImport();
    }

    private static MariadbRecord insertSeed(ShellMariadbClient client, String database, String tableName) {
        List<MariadbColumn> columns = client.selectColumns(new MariadbSelectColumnParam(database, tableName));
        MariadbColumn virtual = columns.stream()
                .filter(column -> "c_virtual".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        MariadbColumn stored = columns.stream()
                .filter(column -> "c_stored".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        assertTrue(virtual.isGenerated());
        assertTrue(virtual.isVirtual());
        assertTrue(StringUtil.containsIgnoreCase(
                virtual.getGeneratedExpression(), "CONCAT"));
        assertTrue(stored.isGenerated());
        assertFalse(stored.isVirtual());
        assertTrue(StringUtil.containsIgnoreCase(
                stored.getGeneratedExpression(), "CONCAT"));
        MariadbRecord record = new MariadbRecord(columns);
        seedValues().forEach(record::putValue);
        for (String geometry : List.of("c_point", "c_linestring", "c_polygon", "c_multipoint",
                "c_multilinestring", "c_multipolygon", "c_geometry", "c_geomcollection")) {
            assertTrue(geometry + " metadata must support geometry", columns.stream()
                    .filter(column -> geometry.equals(column.getName()))
                    .findFirst()
                    .orElseThrow()
                    .supportGeometry());
            assertTrue(geometry + " record data must identify geometry",
                    record.getRecordData().isTypeGeometry(geometry));
        }

        MariadbRecordPrimaryKey primaryKey = new MariadbRecordPrimaryKey();
        primaryKey.init(new MariadbColumns(columns).column("id"), record);
        MariadbInsertRecordParam param = new MariadbInsertRecordParam();
        param.setDbName(database);
        param.setTableName(tableName);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
        return loadSingle(client, database, tableName, ((Number) primaryKey.getReturnData()).longValue());
    }

    private static MariadbRecord loadSingle(ShellMariadbClient client, String database, String tableName, long id) {
        MariadbSelectRecordParam param = selectParam(database, tableName);
        param.setColumns(client.selectColumns(new MariadbSelectColumnParam(database, tableName)));
        List<MariadbRecord> records = client.selectRecords(param);
        return records.stream()
                .filter(record -> Objects.equals(((Number) record.getValue("id")).longValue(), id))
                .findFirst()
                .orElseThrow();
    }

    private static MariadbSelectRecordParam selectParam(String database, String tableName) {
        MariadbSelectRecordParam param = new MariadbSelectRecordParam();
        param.setDbName(database);
        param.setTableName(tableName);
        param.setStart(0L);
        param.setLimit(100L);
        return param;
    }

    private static Map<String, Object> seedValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_tiny", (byte) -12);
        values.put("c_utiny", (short) 250);
        values.put("c_small", (short) -1234);
        values.put("c_medium", 123456);
        values.put("c_int", -987654);
        values.put("c_uint", 4000000000L);
        values.put("c_big", -9000000000L);
        values.put("c_ubig", new BigInteger("18446744073709551615"));
        values.put("c_decimal", new BigDecimal("12345678901234567890.1234567890"));
        values.put("c_float", 123.25F);
        values.put("c_double", 98765.4321D);
        values.put("c_bit", new byte[]{0x1A, 0x2B});
        values.put("c_bool", true);
        values.put("c_date", "2026-11-21");
        values.put("c_time", "12:34:56");
        values.put("c_datetime", "2026-11-21 12:34:56");
        values.put("c_timestamp", "2026-11-21 12:34:56");
        values.put("c_year", "2026");
        values.put("c_char", "char");
        values.put("c_varchar", "alpha-value");
        values.put("c_tinytext", "tiny text 中文");
        values.put("c_text", "text with 'quote' and \"double\"");
        values.put("c_mediumtext", "medium\nline");
        values.put("c_longtext", "long text " + "L".repeat(1024));
        values.put("c_binary", Arrays.copyOf("BINARY".getBytes(), 8));
        values.put("c_varbinary", new byte[]{0x00, 0x01, 0x02, (byte) 0xFE, (byte) 0xFF});
        values.put("c_tinyblob", "tiny blob".getBytes());
        values.put("c_blob", new byte[]{0x10, 0x20, 0x30});
        values.put("c_mediumblob", "medium blob".getBytes());
        values.put("c_longblob", "long blob".getBytes());
        values.put("c_enum", "beta");
        values.put("c_set", "red,blue");
        values.put("c_json", "{\"name\":\"张三\",\"quote\":\"a\\\"b\",\"items\":[1,\"x\"]}");
        values.put("c_point", "POINT(1 2)");
        values.put("c_linestring", "LINESTRING(0 0,1 1,2 3)");
        values.put("c_polygon", "POLYGON((0 0,5 0,5 5,0 5,0 0))");
        values.put("c_multipoint", "MULTIPOINT(0 0,1 1)");
        values.put("c_multilinestring", "MULTILINESTRING((0 0,1 1),(2 2,3 3))");
        values.put("c_multipolygon", "MULTIPOLYGON(((0 0,2 0,2 2,0 2,0 0)),((5 5,7 5,7 7,5 7,5 5)))");
        values.put("c_geometry", "POINT(9 8)");
        values.put("c_geomcollection", "GEOMETRYCOLLECTION(POINT(0 0),LINESTRING(0 0,1 1))");
        return values;
    }

    private static Map<String, Object> updatedValues() {
        Map<String, Object> values = new LinkedHashMap<>(seedValues());
        values.put("c_tiny", (byte) 12);
        values.put("c_utiny", (short) 200);
        values.put("c_small", (short) 2345);
        values.put("c_medium", 654321);
        values.put("c_int", 123456);
        values.put("c_uint", 3000000000L);
        values.put("c_big", 8000000000L);
        values.put("c_ubig", new BigInteger("9223372036854775808"));
        values.put("c_decimal", new BigDecimal("98765432109876543210.0123456789"));
        values.put("c_float", 456.5F);
        values.put("c_double", 12345.6789D);
        values.put("c_bit", new byte[]{0x0A, 0x15});
        values.put("c_bool", false);
        values.put("c_date", "2027-01-02");
        values.put("c_time", "23:59:58");
        values.put("c_datetime", "2027-01-02 23:59:58");
        values.put("c_timestamp", "2027-01-02 23:59:58");
        values.put("c_year", "2027");
        values.put("c_char", "updated");
        values.put("c_varchar", "beta-value");
        values.put("c_tinytext", "updated tiny 中文");
        values.put("c_text", "updated 'quote' \"double\"");
        values.put("c_mediumtext", "updated\nline");
        values.put("c_longtext", "updated long " + "M".repeat(1024));
        values.put("c_binary", Arrays.copyOf("UPDATED".getBytes(), 8));
        values.put("c_varbinary", new byte[]{(byte) 0xFF, (byte) 0xFE, 0x03, 0x02, 0x01});
        values.put("c_tinyblob", "updated tiny blob".getBytes());
        values.put("c_blob", new byte[]{0x30, 0x20, 0x10});
        values.put("c_mediumblob", "updated medium blob".getBytes());
        values.put("c_longblob", "updated long blob".getBytes());
        values.put("c_enum", "gamma");
        values.put("c_set", "green,blue");
        values.put("c_json", "{\"name\":\"李四\",\"quote\":\"c\\\"d\",\"items\":[2,\"y\"]}");
        values.put("c_point", "POINT(3 4)");
        values.put("c_linestring", "LINESTRING(3 3,4 4,5 6)");
        values.put("c_polygon", "POLYGON((1 1,6 1,6 6,1 6,1 1))");
        values.put("c_multipoint", "MULTIPOINT(2 2,3 3)");
        values.put("c_multilinestring", "MULTILINESTRING((1 1,2 2),(4 4,5 5))");
        values.put("c_multipolygon", "MULTIPOLYGON(((1 1,3 1,3 3,1 3,1 1)),((6 6,8 6,8 8,6 8,6 6)))");
        values.put("c_geometry", "POINT(7 6)");
        values.put("c_geomcollection", "GEOMETRYCOLLECTION(POINT(2 2),LINESTRING(2 2,3 3))");
        return values;
    }

    private static void assertExpectedValues(Map<String, Object> expected, MariadbRecord actual) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object actualValue = actual.getValue(entry.getKey());
            if (entry.getValue() instanceof byte[] expectedBytes) {
                assertArrayEquals(entry.getKey(), expectedBytes, (byte[]) actualValue);
            } else if (entry.getValue() instanceof BigDecimal expectedDecimal) {
                assertEquals(entry.getKey(), 0, expectedDecimal.compareTo((BigDecimal) actualValue));
            } else if (entry.getValue() instanceof Number expectedNumber
                    && actualValue instanceof Number actualNumber
                    && !(entry.getValue() instanceof Float || entry.getValue() instanceof Double)) {
                assertEquals(entry.getKey(), new BigDecimal(expectedNumber.toString()),
                        new BigDecimal(actualNumber.toString()));
            } else if (entry.getValue() instanceof Float expectedFloat) {
                assertEquals(entry.getKey(), expectedFloat, ((Number) actualValue).floatValue(), 0.001F);
            } else if (entry.getValue() instanceof Double expectedDouble) {
                assertEquals(entry.getKey(), expectedDouble, ((Number) actualValue).doubleValue(), 0.000001D);
            } else if (entry.getValue() instanceof CharSequence expectedJson
                    && actualValue instanceof CharSequence actualJson
                    && expectedJson.toString().stripLeading().matches("^[\\[{].*")
                    && actualJson.toString().stripLeading().matches("^[\\[{].*")) {
                assertEquals(entry.getKey(), JSONUtil.parseObject(expectedJson.toString()),
                        JSONUtil.parseObject(actualJson.toString()));
            } else if (entry.getValue() instanceof CharSequence expectedYear
                    && expectedYear.toString().matches("\\d{4}")
                    && (expectedYear + "-01-01").equals(normalize(actualValue))) {
                assertEquals(entry.getKey(), expectedYear.toString(), normalize(actualValue).substring(0, 4));
            } else {
                assertEquals(entry.getKey(), normalize(entry.getValue()), normalize(actualValue));
            }
        }
    }

    private static void assertGeneratedValues(MariadbRecord record, String virtual, String stored) {
        assertEquals(virtual + "-virtual", normalize(record.getValue("c_virtual")));
        assertEquals(stored + "-stored", normalize(record.getValue("c_stored")));
    }

    private static String normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) {
            text = text.replace('T', ' ');
        }
        if (text.matches(".*\\d{2}:\\d{2}:\\d{2}\\.0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    private static void createLike(ShellMariadbClient client, String database, String targetTable) {
        client.executeSqlSimple(database, "DROP TABLE IF EXISTS `" + targetTable + "`");
        client.executeSqlSimple(database, "CREATE TABLE `" + targetTable + "` LIKE `"
                + SOURCE_DB + "`.`" + TABLE + "`");
    }

    private static void resetTable(ShellMariadbClient client, String database, String tableName) {
        client.executeSqlSimple(database, "DROP TABLE IF EXISTS `" + tableName + "`");
        if (TABLE.equals(tableName)) {
            client.executeSqlSimple(database, CREATE_TABLE);
        }
    }

    private static ShellMariadbClient openClient() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.MARIADB);
        connect.setHost(HOST + ":" + PORT);
        connect.setUser(USER);
        connect.setPassword(PASSWORD);
        connect.setSSLMode(false);
        connect.setConnectTimeOut(5);
        ShellMariadbClient client = new ShellMariadbClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
        return client;
    }

    private static void recreateDatabase(ShellMariadbClient client, String database) {
        dropDatabase(client, database);
        MariadbDatabase mariadbDatabase = new MariadbDatabase();
        mariadbDatabase.setName(database);
        mariadbDatabase.setCharset("utf8mb4");
        client.createDatabase(mariadbDatabase);
    }

    private static void dropDatabase(ShellMariadbClient client, String database) {
        client.executeSqlSimple(null, "DROP DATABASE IF EXISTS `" + database + "`");
    }

    private static String property(String property, String environment, String defaultValue) {
        return System.getProperty(property,
                System.getenv().getOrDefault(environment, defaultValue));
    }

    private static boolean isReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
