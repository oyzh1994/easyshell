package cn.oyzh.easyshell.dameng;

import cn.oyzh.easyshell.data.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.easyshell.data.dameng.dto.ShellDamengDataImportFile;
import cn.oyzh.easyshell.data.dameng.handler.ShellDamengDataExportHandler;
import cn.oyzh.easyshell.data.dameng.handler.ShellDamengDataImportHandler;
import cn.oyzh.easyshell.data.dameng.handler.ShellDamengDataDumpHandler;
import cn.oyzh.easyshell.data.dameng.handler.ShellDamengDataRunSqlFileHandler;
import cn.oyzh.easyshell.data.dameng.handler.ShellDamengDataTransportHandler;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengUpdateRecordParam;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.type.DamengTypeDefinition;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 达梦复杂字段记录编辑集成测试。
 */
public class DamengComplexDataIntegrationTest {

    private static final String HOST = property("dameng.host", "DAMENG_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("dameng.port", "DAMENG_PORT", "15237"));
    private static final String USER = property("dameng.user", "DAMENG_USER", "SYSDBA");
    private static final String PASSWORD = property("dameng.password", "DAMENG_PASSWORD", "SYSDBA");
    private static final String SCHEMA = "EASYSHELL_DAMENG_COMPLEX_IT";
    private static final String TARGET_SCHEMA = "EASYSHELL_DAMENG_COMPLEX_TARGET";
    private static final String TABLE = "COMPLEX_TYPES";
    private static final String UDT_TABLE = "UDT_TYPES";

    private static final String CREATE_TABLE = """
            CREATE TABLE "COMPLEX_TYPES" (
              "ID" BIGINT IDENTITY(1, 1) PRIMARY KEY,
              "C_TINY" TINYINT,
              "C_SMALL" SMALLINT,
              "C_INT" INT,
              "C_BIG" BIGINT,
              "C_DECIMAL" DECIMAL(30,10),
              "C_FLOAT" FLOAT,
              "C_DOUBLE" DOUBLE,
              "C_BIT" BIT,
              "C_CHAR" CHAR(8),
              "C_VARCHAR" VARCHAR(200),
              "C_VARCHAR2" VARCHAR2(200),
              "C_CLOB" CLOB,
              "C_TEXT" TEXT,
              "C_LONGVARCHAR" LONGVARCHAR,
              "C_BINARY" BINARY(8),
              "C_VARBINARY" VARBINARY(100),
              "C_RAW" RAW(16),
              "C_BLOB" BLOB,
              "C_IMAGE" IMAGE,
              "C_LONGVARBINARY" LONGVARBINARY,
              "C_DATE" DATE,
              "C_TIME" TIME,
              "C_DATETIME" DATETIME,
              "C_TIMESTAMP" TIMESTAMP,
              "C_TIMESTAMP_TZ" TIMESTAMP WITH TIME ZONE,
              "C_INTERVAL_YM" INTERVAL YEAR TO MONTH,
              "C_INTERVAL_DS" INTERVAL DAY TO SECOND,
              "C_XML" XMLTYPE,
              "C_VIRTUAL" VARCHAR(400) GENERATED ALWAYS AS
                (CONCAT("C_VARCHAR", '-virtual')) VIRTUAL
            )
            """;

    private static final String CREATE_UDT_TABLE = """
            CREATE TABLE "UDT_TYPES" (
              "ID" BIGINT IDENTITY(1, 1) PRIMARY KEY,
              "ADDRESS_VALUE" "ADDRESS_T",
              "NUMBER_VALUES" "INT_ARRAY"
            )
            """;

    private static ShellDamengClient client;
    private static ShellDamengClient targetClient;
    private static Path workDirectory;

    @Before
    public void resetSourceTable() {
        client.executeSqlSimple(SCHEMA, "DROP TABLE IF EXISTS \"" + TABLE + "\"");
        client.executeSqlSimple(SCHEMA, CREATE_TABLE);
    }

    @BeforeClass
    public static void setUp() throws Throwable {
        Assume.assumeTrue("Dameng endpoint is not reachable", isReachable(HOST, PORT));
        ShellDamengColumnUtil.init();
        client = openClient();
        targetClient = openClient();
        if (client.existSchema(SCHEMA)) {
            client.dropSchema(SCHEMA);
        }
        if (client.existSchema(TARGET_SCHEMA)) {
            client.dropSchema(TARGET_SCHEMA);
        }
        DamengSchema schema = new DamengSchema();
        schema.setName(SCHEMA);
        client.createSchema(schema);
        schema = new DamengSchema();
        schema.setName(TARGET_SCHEMA);
        client.createSchema(schema);
        workDirectory = Files.createTempDirectory("easyshell-dameng-complex-");
    }

    @AfterClass
    public static void tearDown() {
        try {
            if (client != null && client.existSchema(SCHEMA)) {
                client.dropSchema(SCHEMA);
            }
            if (client != null && client.existSchema(TARGET_SCHEMA)) {
                client.dropSchema(TARGET_SCHEMA);
            }
        } finally {
            if (client != null) {
                client.close();
            }
            if (targetClient != null) {
                targetClient.close();
            }
        }
    }

    @Test
    public void editsEveryComplexColumnThroughClientEntry() {
        DamengRecord inserted = insertSeed();
        long id = ((Number) inserted.getValue("ID")).longValue();
        assertExpectedValues(seedValues(), inserted);
        assertEquals("alpha-value-virtual",
                inserted.getValue("C_VIRTUAL").toString());

        DamengRecord editable = loadSingle(id);
        Map<String, Object> updated = updatedValues();
        for (Map.Entry<String, Object> entry : updated.entrySet()) {
            DamengColumn column = editable.getColumns().column(entry.getKey());
            assertNotNull(column);
            assertFalse(column.isGenerated());
            editable.putValue(column, entry.getValue());
            editable.getProperty(entry.getKey()).setChanged(true);
        }
        DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
        primaryKey.init(editable.getColumns().column("ID"), editable);
        DamengUpdateRecordParam updateParam = new DamengUpdateRecordParam();
        updateParam.setSchema(SCHEMA);
        updateParam.setTableName(TABLE);
        updateParam.setRecord(editable.getOriginalRecordData());
        updateParam.setUpdateRecord(editable.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.updateRecord(updateParam));

        DamengRecord reloaded = loadSingle(id);
        assertExpectedValues(updated, reloaded);
        assertEquals("beta-value-virtual",
                reloaded.getValue("C_VIRTUAL").toString());
    }

    @Test
    public void exportsAndImportsEverySupportedFormat() throws Exception {
        insertSeed();
        for (String format : List.of("csv", "json", "xml", "txt", "xlsx", "sql", "html")) {
            Path file = workDirectory.resolve("complex_types." + format);
            export(TABLE, format, file);
            assertTrue("empty export: " + format, Files.size(file) > 0);
            if ("html".equals(format)) {
                String html = Files.readString(file);
                assertTrue(html.contains("<!DOCTYPE html>"));
                assertTrue(html.contains("alpha-value"));
                continue;
            }

            String targetTable = "SQL".equalsIgnoreCase(format)
                    ? TABLE : "IMPORT_" + format.toUpperCase();
            createTargetTable(targetTable);
            if ("sql".equalsIgnoreCase(format)) {
                targetClient.executeSqlSimple(TARGET_SCHEMA, Files.readString(file));
            } else {
                importFile(targetTable, format, file);
            }
            List<DamengRecord> records = selectTargetRecords(targetTable);
            assertEquals("imported rows for " + format, 1, records.size());
            assertExpectedValues(seedValues(), records.getFirst());
            assertEquals("alpha-value-virtual",
                    records.getFirst().getValue("C_VIRTUAL").toString());
        }
    }

    @Test
    public void exportsAndImportsUserDefinedTypes() throws Exception {
        prepareUdtSource();
        List<DamengColumn> columns = client.selectColumns(
                new DamengSelectColumnParam(SCHEMA, UDT_TABLE));
        DamengRecord record = new DamengRecord(columns);
        record.putValue("ADDRESS_VALUE", "ADDRESS_T('Imported St',24680)");
        record.putValue("NUMBER_VALUES", "INT_ARRAY(7,8,9)");
        DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
        primaryKey.init(new DamengColumns(columns).column("ID"), record);
        DamengInsertRecordParam insertParam = new DamengInsertRecordParam();
        insertParam.setSchema(SCHEMA);
        insertParam.setTableName(UDT_TABLE);
        insertParam.setRecord(record.getRecordData());
        insertParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(insertParam));

        prepareUdtTarget();
        for (String format : List.of("csv", "json", "xml", "txt", "xlsx", "sql")) {
            Path file = workDirectory.resolve("udt_types." + format);
            export(UDT_TABLE, format, file);
            String targetTable = "SQL".equalsIgnoreCase(format)
                    ? UDT_TABLE : "UDT_IMPORT_" + format.toUpperCase();
            createTargetUdtTable(targetTable);
            if ("sql".equalsIgnoreCase(format)) {
                targetClient.executeSqlSimple(TARGET_SCHEMA, Files.readString(file));
            } else {
                importFile(targetTable, format, file);
            }
            List<DamengRecord> imported = selectTargetRecords(targetTable);
            assertEquals("imported UDT rows for " + format, 1, imported.size());
            assertTrue(imported.getFirst().getValue("ADDRESS_VALUE").toString()
                    .contains("Imported St"));
            String importedNumbers = imported.getFirst().getValue("NUMBER_VALUES").toString();
            assertTrue(importedNumbers, importedNumbers.contains("7")
                    && importedNumbers.contains("8") && importedNumbers.contains("9"));
        }
    }

    @Test
    public void transportsUserDefinedTypes() throws Exception {
        prepareUdtSource();
        List<DamengColumn> columns = client.selectColumns(
                new DamengSelectColumnParam(SCHEMA, UDT_TABLE));
        DamengRecord record = new DamengRecord(columns);
        record.putValue("ADDRESS_VALUE", "ADDRESS_T('Transport St',13579)");
        record.putValue("NUMBER_VALUES", "INT_ARRAY(11,12,13)");
        DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
        primaryKey.init(new DamengColumns(columns).column("ID"), record);
        DamengInsertRecordParam insertParam = new DamengInsertRecordParam();
        insertParam.setSchema(SCHEMA);
        insertParam.setTableName(UDT_TABLE);
        insertParam.setRecord(record.getRecordData());
        insertParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(insertParam));

        prepareUdtTarget();
        DBDataTransportObject table = new DBDataTransportObject();
        table.setName(UDT_TABLE);
        ShellDamengDataTransportHandler handler = new ShellDamengDataTransportHandler();
        handler.setContinueWithErrors(false);
        handler.setSourceClient(client);
        handler.setTargetClient(targetClient);
        handler.setSourceDatabase(SCHEMA);
        handler.setTargetDatabase(TARGET_SCHEMA);
        handler.setTables(List.of(table));
        handler.setSelectLimit(2);
        handler.setInsertLimit(2);
        handler.doAction();

        List<DamengRecord> imported = selectTargetRecords(UDT_TABLE);
        assertEquals(1, imported.size());
        assertTrue(imported.getFirst().getValue("ADDRESS_VALUE").toString()
                .contains("Transport St"));
        String numbers = imported.getFirst().getValue("NUMBER_VALUES").toString();
        assertTrue(numbers, numbers.contains("11") && numbers.contains("12")
                && numbers.contains("13"));
    }

    @Test
    public void dumpsUserDefinedTypesBeforeTables() throws Exception {
        prepareUdtSource();
        List<DamengTypeDefinition> types = client.selectTypes(SCHEMA);
        assertEquals(2, types.size());
        DamengTypeDefinition address = types.stream()
                .filter(type -> "ADDRESS_T".equals(type.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("CLASS", address.getObjectType());
        assertTrue(address.getCreateDefinition().contains("ADDRESS_T"));
        DamengTypeDefinition numbers = types.stream()
                .filter(type -> "INT_ARRAY".equals(type.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("TYPE", numbers.getObjectType());
        assertTrue(numbers.getCreateDefinition().contains("VARRAY"));

        Path file = workDirectory.resolve("udt_types_dump.sql");
        ShellDamengDataDumpHandler handler = new ShellDamengDataDumpHandler(client, SCHEMA);
        handler.dumpFile(file.toFile()).setDumpType((byte) 1);
        handler.setDataType((byte) 0);
        handler.setContinueWithErrors(false);
        handler.doAction();

        String sql = Files.readString(file);
        int addressIndex = sql.indexOf("-- Type structure for ADDRESS_T");
        int numbersIndex = sql.indexOf("-- Type structure for INT_ARRAY");
        int tableIndex = sql.indexOf("-- Table structure for UDT_TYPES");
        assertTrue(sql, addressIndex >= 0);
        assertTrue(sql, numbersIndex >= 0);
        assertTrue(sql, tableIndex > addressIndex);
        assertTrue(sql, tableIndex > numbersIndex);
        String normalized = sql.replaceAll("\\s+", " ");
        assertTrue(sql, normalized.contains("CREATE OR REPLACE TYPE \"ADDRESS_T\""));
        assertTrue(sql, normalized.contains("CREATE OR REPLACE TYPE \"INT_ARRAY\""));

        dropUdtTables(targetClient, TARGET_SCHEMA);
        targetClient.executeSqlSimple(TARGET_SCHEMA, "DROP TABLE IF EXISTS \"COMPLEX_TYPES\"");
        dropTypeIfExists(targetClient, TARGET_SCHEMA, "INT_ARRAY");
        dropTypeIfExists(targetClient, TARGET_SCHEMA, "ADDRESS_T");
        ShellDamengDataRunSqlFileHandler runHandler =
                new ShellDamengDataRunSqlFileHandler(targetClient, TARGET_SCHEMA);
        runHandler.setContinueWithErrors(false);
        runHandler.setFile(file.toFile());
        runHandler.doAction();

        List<DamengTypeDefinition> targetTypes = targetClient.selectTypes(TARGET_SCHEMA);
        assertEquals(2, targetTypes.size());
        List<DamengColumn> targetColumns = targetClient.selectColumns(
                new DamengSelectColumnParam(TARGET_SCHEMA, UDT_TABLE));
        assertTrue(targetColumns.stream().anyMatch(column ->
                "ADDRESS_T".equals(column.getType())));
        assertTrue(targetColumns.stream().anyMatch(column ->
                "INT_ARRAY".equals(column.getType())));
    }

    @Test
    public void transportsEveryComplexColumnToAnotherSchema() throws Exception {
        insertSeed();
        createTargetTable(TABLE);

        DBDataTransportObject table = new DBDataTransportObject();
        table.setName(TABLE);
        ShellDamengDataTransportHandler handler = new ShellDamengDataTransportHandler();
        handler.setContinueWithErrors(false);
        handler.setSourceClient(client);
        handler.setTargetClient(targetClient);
        handler.setSourceDatabase(SCHEMA);
        handler.setTargetDatabase(TARGET_SCHEMA);
        handler.setTables(List.of(table));
        handler.setSelectLimit(2);
        handler.setInsertLimit(2);
        handler.doAction();

        List<DamengRecord> records = selectTargetRecords(TABLE);
        assertEquals(1, records.size());
        assertExpectedValues(seedValues(), records.getFirst());
        assertEquals("alpha-value-virtual",
                records.getFirst().getValue("C_VIRTUAL").toString());
    }

    @Test
    public void editsUserDefinedObjectAndArrayTypes() {
        client.executeSqlSimple(SCHEMA, "DROP TABLE IF EXISTS \"UDT_TYPES\"");
        try {
            client.executeSqlSimple(SCHEMA, "DROP TYPE \"INT_ARRAY\"");
        } catch (Exception ignored) {
        }
        try {
            client.executeSqlSimple(SCHEMA, "DROP TYPE \"ADDRESS_T\"");
        } catch (Exception ignored) {
        }
        client.executeSqlSimple(SCHEMA,
                "CREATE TYPE \"ADDRESS_T\" AS OBJECT (\"STREET\" VARCHAR(100), \"ZIP\" INT)");
        client.executeSqlSimple(SCHEMA,
                "CREATE TYPE \"INT_ARRAY\" AS VARRAY(10) OF INT");
        client.executeSqlSimple(SCHEMA, """
                CREATE TABLE "UDT_TYPES" (
                  "ID" BIGINT IDENTITY(1, 1) PRIMARY KEY,
                  "ADDRESS_VALUE" "ADDRESS_T",
                  "NUMBER_VALUES" "INT_ARRAY"
                )
                """);

        List<DamengColumn> columns = client.selectColumns(
                new DamengSelectColumnParam(SCHEMA, "UDT_TYPES"));
        assertTrue(columns.stream().anyMatch(column ->
                "ADDRESS_T".equals(column.getType())));
        assertTrue(columns.stream().anyMatch(column ->
                "INT_ARRAY".equals(column.getType())));

        DamengRecord record = new DamengRecord(columns);
        record.putValue("ADDRESS_VALUE", "ADDRESS_T('Main St',12345)");
        record.putValue("NUMBER_VALUES", "INT_ARRAY(1,2,3)");
        DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
        primaryKey.init(new DamengColumns(columns).column("ID"), record);
        DamengInsertRecordParam param = new DamengInsertRecordParam();
        param.setSchema(SCHEMA);
        param.setTableName("UDT_TYPES");
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));

        DamengSelectRecordParam selectParam = new DamengSelectRecordParam();
        selectParam.setSchema(SCHEMA);
        selectParam.setTableName("UDT_TYPES");
        selectParam.setColumns(columns);
        selectParam.setStart(0L);
        selectParam.setLimit(10L);
        DamengRecord inserted = client.selectRecords(selectParam).getFirst();
        assertTrue(inserted.getValue("ADDRESS_VALUE").toString().contains("Main St"));
        String insertedNumbers = inserted.getValue("NUMBER_VALUES").toString();
        assertTrue(insertedNumbers, insertedNumbers.contains("1")
                && insertedNumbers.contains("2") && insertedNumbers.contains("3"));

        inserted.putValue("ADDRESS_VALUE", "ADDRESS_T('Second St',54321)");
        inserted.putValue("NUMBER_VALUES", "INT_ARRAY(4,5,6)");
        inserted.getProperty("ADDRESS_VALUE").setChanged(true);
        inserted.getProperty("NUMBER_VALUES").setChanged(true);
        primaryKey = new DamengRecordPrimaryKey();
        primaryKey.init(new DamengColumns(columns).column("ID"), inserted);
        DamengUpdateRecordParam updateParam = new DamengUpdateRecordParam();
        updateParam.setSchema(SCHEMA);
        updateParam.setTableName("UDT_TYPES");
        updateParam.setRecord(inserted.getOriginalRecordData());
        updateParam.setUpdateRecord(inserted.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.updateRecord(updateParam));

        DamengRecord updated = client.selectRecords(selectParam).getFirst();
        assertTrue(updated.getValue("ADDRESS_VALUE").toString().contains("Second St"));
        String updatedNumbers = updated.getValue("NUMBER_VALUES").toString();
        assertTrue(updatedNumbers, updatedNumbers.contains("4")
                && updatedNumbers.contains("5") && updatedNumbers.contains("6"));
    }

    private static void export(String tableName, String format, Path file) throws Exception {
        List<DamengColumn> columns = client.selectColumns(
                new DamengSelectColumnParam(SCHEMA, tableName));
        ShellDamengDataExportTable table = new ShellDamengDataExportTable();
        table.setName(tableName);
        table.setFilePath(file.toString());
        table.columns(columns);

        ShellDamengDataExportHandler handler = new ShellDamengDataExportHandler(client, SCHEMA);
        handler.setFileType(format);
        handler.setQueryLimit(2);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.setTables(List.of(table));
        handler.doAction();
    }

    private static void prepareUdtSource() {
        client.executeSqlSimple(SCHEMA,
                "DROP TABLE IF EXISTS \"" + UDT_TABLE + "\"");
        dropTypeIfExists(client, SCHEMA, "INT_ARRAY");
        dropTypeIfExists(client, SCHEMA, "ADDRESS_T");
        client.executeSqlSimple(SCHEMA,
                "CREATE TYPE \"ADDRESS_T\" AS OBJECT (\"STREET\" VARCHAR(100), \"ZIP\" INT)");
        client.executeSqlSimple(SCHEMA,
                "CREATE TYPE \"INT_ARRAY\" AS VARRAY(10) OF INT");
        client.executeSqlSimple(SCHEMA, CREATE_UDT_TABLE);
    }

    private static void prepareUdtTarget() {
        dropUdtTables(targetClient, TARGET_SCHEMA);
        dropTypeIfExists(targetClient, TARGET_SCHEMA, "INT_ARRAY");
        dropTypeIfExists(targetClient, TARGET_SCHEMA, "ADDRESS_T");
        targetClient.executeSqlSimple(TARGET_SCHEMA,
                "CREATE TYPE \"ADDRESS_T\" AS OBJECT (\"STREET\" VARCHAR(100), \"ZIP\" INT)");
        targetClient.executeSqlSimple(TARGET_SCHEMA,
                "CREATE TYPE \"INT_ARRAY\" AS VARRAY(10) OF INT");
    }

    private static void createTargetUdtTable(String tableName) {
        targetClient.executeSqlSimple(TARGET_SCHEMA,
                "DROP TABLE IF EXISTS \"" + tableName + "\"");
        targetClient.executeSqlSimple(TARGET_SCHEMA,
                CREATE_UDT_TABLE.replaceFirst("\"UDT_TYPES\"", "\"" + tableName + "\""));
    }

    private static void dropTypeIfExists(ShellDamengClient client, String schema, String type) {
        try {
            client.executeSqlSimple(schema, "DROP TYPE \"" + type + "\"");
        } catch (Exception ignored) {
        }
    }

    private static void dropUdtTables(ShellDamengClient client, String schema) {
        for (String table : List.of("UDT_TYPES", "UDT_IMPORT_CSV", "UDT_IMPORT_JSON",
                "UDT_IMPORT_XML", "UDT_IMPORT_TXT", "UDT_IMPORT_XLSX")) {
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS \"" + table + "\"");
        }
    }

    private static void importFile(String table, String format, Path file) throws Exception {
        ShellDamengDataImportFile importFile = new ShellDamengDataImportFile();
        importFile.setSchema(TARGET_SCHEMA);
        importFile.setFile(file.toFile());
        importFile.setTargetTableName(table);

        ShellDamengDataImportHandler handler =
                new ShellDamengDataImportHandler(targetClient, TARGET_SCHEMA);
        handler.setContinueWithErrors(false);
        handler.setFileType("xlsx".equals(format) ? "excel" : format);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.setInsertLimit(2);
        handler.setBatchLimit(2);
        handler.setFiles(List.of(importFile));
        handler.importMode("2");
        handler.doAction();
    }

    private static void createTargetTable(String tableName) {
        targetClient.executeSqlSimple(TARGET_SCHEMA,
                "DROP TABLE IF EXISTS \"" + tableName + "\"");
        String ddl = CREATE_TABLE.replaceFirst("\"COMPLEX_TYPES\"",
                "\"" + tableName + "\"");
        targetClient.executeSqlSimple(TARGET_SCHEMA, ddl);
    }

    private static List<DamengRecord> selectTargetRecords(String tableName) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setSchema(TARGET_SCHEMA);
        param.setTableName(tableName);
        param.setColumns(targetClient.selectColumns(
                new DamengSelectColumnParam(TARGET_SCHEMA, tableName)));
        param.setStart(0L);
        param.setLimit(10L);
        return targetClient.selectRecords(param);
    }

    private static DamengRecord insertSeed() {
        List<DamengColumn> columns = selectColumns();
        DamengColumn virtual = columns.stream()
                .filter(column -> "C_VIRTUAL".equals(column.getName()))
                .findFirst()
                .orElseThrow();
        assertTrue(virtual.isGenerated());
        DamengRecord record = new DamengRecord(columns);
        seedValues().forEach(record::putValue);
        record.putValue("C_VIRTUAL", "ignored");
        assertTrue(record.getProperty("C_VIRTUAL").isReadonly());

        DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
        primaryKey.init(new DamengColumns(columns).column("ID"), record);
        DamengInsertRecordParam param = new DamengInsertRecordParam();
        param.setSchema(SCHEMA);
        param.setTableName(TABLE);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
        return loadSingle(((Number) primaryKey.getReturnData()).longValue());
    }

    private static DamengRecord loadSingle(long id) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setSchema(SCHEMA);
        param.setTableName(TABLE);
        param.setColumns(selectColumns());
        param.setStart(0L);
        param.setLimit(10L);
        List<DamengRecord> records = client.selectRecords(param);
        return records.stream()
                .filter(record -> ((Number) record.getValue("ID")).longValue() == id)
                .findFirst()
                .orElseThrow();
    }

    private static List<DamengColumn> selectColumns() {
        return client.selectColumns(new DamengSelectColumnParam(SCHEMA, TABLE));
    }

    private static Map<String, Object> seedValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("C_TINY", 12);
        values.put("C_SMALL", -1234);
        values.put("C_INT", -987654);
        values.put("C_BIG", -9000000000L);
        values.put("C_DECIMAL", new BigDecimal("12345678901234567890.1234567890"));
        values.put("C_FLOAT", 123.25F);
        values.put("C_DOUBLE", 98765.4321D);
        values.put("C_BIT", true);
        values.put("C_CHAR", "char");
        values.put("C_VARCHAR", "alpha-value");
        values.put("C_VARCHAR2", "alpha-varchar2");
        values.put("C_CLOB", "clob 中文 😀");
        values.put("C_TEXT", "text with 'quote' 中文");
        values.put("C_LONGVARCHAR", "long varchar value");
        values.put("C_BINARY", new byte[]{'B', 'I', 'N', 'A', 'R', 'Y', 0, 1});
        values.put("C_VARBINARY", new byte[]{0, 1, 2, (byte) 0xFE, (byte) 0xFF});
        values.put("C_RAW", new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        values.put("C_BLOB", new byte[]{0x10, 0x20, 0x30});
        values.put("C_IMAGE", "image data".getBytes());
        values.put("C_LONGVARBINARY", "long binary data".getBytes());
        values.put("C_DATE", java.sql.Date.valueOf("2026-11-22"));
        values.put("C_TIME", java.sql.Time.valueOf("12:34:56"));
        values.put("C_DATETIME", java.sql.Timestamp.valueOf("2026-11-22 12:34:56"));
        values.put("C_TIMESTAMP", java.sql.Timestamp.valueOf("2026-11-22 13:34:56"));
        values.put("C_TIMESTAMP_TZ", OffsetDateTime.parse("2026-11-22T14:34:56+08:00"));
        values.put("C_INTERVAL_YM", "INTERVAL '2-5' YEAR TO MONTH");
        values.put("C_INTERVAL_DS", "INTERVAL '3 12:34:56.123456' DAY TO SECOND");
        values.put("C_XML", "<root><name>张三</name></root>");
        return values;
    }

    private static Map<String, Object> updatedValues() {
        Map<String, Object> values = new LinkedHashMap<>(seedValues());
        values.put("C_INT", 123456);
        values.put("C_BIG", 8000000000L);
        values.put("C_DECIMAL", new BigDecimal("98765432109876543210.0123456789"));
        values.put("C_FLOAT", 456.5F);
        values.put("C_DOUBLE", 12345.6789D);
        values.put("C_BIT", false);
        values.put("C_CHAR", "updated");
        values.put("C_VARCHAR", "beta-value");
        values.put("C_VARCHAR2", "beta-varchar2");
        values.put("C_CLOB", "updated clob 中文 😀");
        values.put("C_TEXT", "updated text 'quote'");
        values.put("C_LONGVARCHAR", "updated long varchar");
        values.put("C_BINARY", new byte[]{'U', 'P', 'D', 'A', 'T', 'E', 'D', 2});
        values.put("C_VARBINARY", new byte[]{(byte) 0xFF, (byte) 0xFE, 3, 2, 1});
        values.put("C_RAW", new byte[]{8, 7, 6, 5, 4, 3, 2, 1});
        values.put("C_BLOB", new byte[]{0x30, 0x20, 0x10});
        values.put("C_IMAGE", "updated image".getBytes());
        values.put("C_LONGVARBINARY", "updated long binary".getBytes());
        values.put("C_DATE", java.sql.Date.valueOf("2027-01-02"));
        values.put("C_TIME", java.sql.Time.valueOf("23:59:58"));
        values.put("C_DATETIME", java.sql.Timestamp.valueOf("2027-01-02 23:59:58"));
        values.put("C_TIMESTAMP", java.sql.Timestamp.valueOf("2027-01-03 00:59:58"));
        values.put("C_TIMESTAMP_TZ", OffsetDateTime.parse("2027-01-03T01:59:58+08:00"));
        values.put("C_INTERVAL_YM", "INTERVAL '3-6' YEAR TO MONTH");
        values.put("C_INTERVAL_DS", "INTERVAL '4 01:02:03.456789' DAY TO SECOND");
        values.put("C_XML", "<root><name>李四</name></root>");
        return values;
    }

    private static void assertExpectedValues(Map<String, Object> expected, DamengRecord actual) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object actualValue = actual.getValue(entry.getKey());
            if (entry.getValue() instanceof byte[] expectedBytes) {
                assertTrue(entry.getKey(), java.util.Arrays.equals(expectedBytes, (byte[]) actualValue));
            } else if (entry.getValue() instanceof BigDecimal expectedDecimal) {
                assertEquals(entry.getKey(), 0,
                        expectedDecimal.compareTo((BigDecimal) actualValue));
            } else if (entry.getValue() instanceof Float expectedFloat) {
                assertEquals(entry.getKey(), expectedFloat,
                        ((Number) actualValue).floatValue(), 0.001F);
            } else if (entry.getValue() instanceof Double expectedDouble) {
                assertEquals(entry.getKey(), expectedDouble,
                        ((Number) actualValue).doubleValue(), 0.000001D);
            } else if (entry.getValue() instanceof OffsetDateTime) {
                assertTrue(entry.getKey(), actualValue.toString().contains("2026")
                        || actualValue.toString().contains("2027"));
            } else if (entry.getValue() instanceof String expectedString
                    && expectedString.startsWith("INTERVAL")) {
                assertTrue(entry.getKey(), normalize(actualValue).contains("INTERVAL"));
            } else {
                assertEquals(entry.getKey(), normalize(entry.getValue()), normalize(actualValue));
            }
        }
    }

    private static String normalize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date date) {
            return date.toString();
        }
        String text = value.toString().trim();
        if (text.matches(".*\\.0+")) {
            text = text.replaceFirst("\\.0+$", "");
        }
        return text;
    }

    private static ShellDamengClient openClient() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.DAMENG);
        connect.setHost(HOST + ":" + PORT);
        connect.setUser(USER);
        connect.setPassword(PASSWORD);
        connect.setConnectTimeOut(5);
        ShellDamengClient client = new ShellDamengClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
        return client;
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
