package cn.oyzh.easyshell.oracle;

import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataExportTable;
import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataImportFile;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataExportHandler;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataImportHandler;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataRunSqlFileHandler;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.oracle.OracleColumnKindComboBox;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.record.OracleInsertRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleRecordPrimaryKey;
import cn.oyzh.easyshell.oracle.record.OracleRecordProperty;
import cn.oyzh.easyshell.oracle.record.OracleSelectRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleUpdateRecordParam;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.easyshell.util.oracle.OracleComplexValueUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleNodeUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleRecordUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataCsvTypeFileReader;
import cn.oyzh.fx.editor.incubator.control.XmlTextFiled;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Oracle 11g/23c 复杂类型、数据交换和生成列真实客户端集成测试。
 *
 * @since 2026-11-22
 */
@RunWith(Parameterized.class)
public class OracleComplexDataIntegrationTest {

    private static final String SYSTEM_USER = "SYSTEM";

    private static final String SYSTEM_PASSWORD = "easyshell@2026";

    private static final String SCHEMA_PASSWORD = "Easyshell2026";

    private static final String TABLE = "COMPLEX_TYPES";

    private static final String UDT_TABLE = "UDT_TYPES";

    private final String mode;

    private final int port;

    private final String service;

    private String sourceSchema;

    private String targetSchema;

    private ShellOracleClient sourceClient;

    private ShellOracleClient targetClient;

    private Path workDirectory;

    public OracleComplexDataIntegrationTest(String mode, int port, String service) {
        this.mode = mode;
        this.port = port;
        this.service = service;
    }

    @Parameterized.Parameters(name = "{0}:{1}/{2}")
    public static Collection<Object[]> endpoints() {
        return List.of(
                new Object[]{"11g", 21521, "XE"},
                new Object[]{"23c", 31521, "FREEPDB1"}
        );
    }

    @BeforeClass
    public static void initColumns() throws Exception {
        ShellOracleColumnUtil.init();
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
    }

    @Before
    public void setUp() throws Throwable {
        String selectedMode = System.getProperty("oracle.test.mode");
        Assume.assumeTrue("Oracle test mode filtered out: " + this.mode,
                selectedMode == null || selectedMode.isBlank()
                        || List.of(selectedMode.split(",")).contains(this.mode));
        ShellOracleColumnUtil.init();
        String suffix = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        String prefix = "11g".equals(this.mode) ? "E11" : "E23";
        this.sourceSchema = prefix + "S" + suffix;
        this.targetSchema = prefix + "T" + suffix;
        this.createSchema(this.sourceSchema);
        this.createSchema(this.targetSchema);
        this.sourceClient = this.openClient(this.sourceSchema);
        this.targetClient = this.openClient(this.targetSchema);
        this.workDirectory = Files.createTempDirectory("easyshell-oracle-complex-");
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (this.sourceClient != null) {
                this.sourceClient.close();
            }
            if (this.targetClient != null) {
                this.targetClient.close();
            }
        } finally {
            this.dropSchema(this.sourceSchema);
            this.dropSchema(this.targetSchema);
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
    public void editsComplexColumnsThroughClientEntry() {
        this.createComplexTable(this.sourceClient, this.sourceSchema, TABLE);
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "CREATE VIEW \"" + this.sourceSchema + "\".\"COMPLEX_TYPES_VIEW\" AS "
                        + "SELECT * FROM \"" + this.sourceSchema + "\".\"" + TABLE + "\"");
        OracleColumns viewColumns = new OracleColumns(
                this.sourceClient.viewColumns(this.sourceSchema, "COMPLEX_TYPES_VIEW"));
        assertTrue(viewColumns.column("C_JSON").supportJson());
        assertTrue(viewColumns.column("C_VIRTUAL").isGenerated());
        assertFalse(viewColumns.column("C_VIRTUAL").isDmlWritable());
        OracleRecord viewDraft = new OracleRecord(viewColumns);
        for (OracleColumn column : viewColumns) {
            viewDraft.putValue(column, null);
        }
        assertTrue(viewDraft.getProperty("C_VIRTUAL").isReadonly());
        Node xmlNode = ShellOracleRecordUtil.getNode(
                (OracleRecordProperty) viewDraft.getProperty("C_XML"),
                "<row/>", viewColumns.column("C_XML"));
        assertTrue(xmlNode instanceof XmlTextFiled);

        OracleRecord inserted = this.insertComplexRecord(this.sourceClient, this.sourceSchema, TABLE, 1);
        this.assertComplexValues(this.seedValues(1), inserted);
        assertGeneratedValue(inserted);
        OracleRecord viewRecord = this.sourceClient.viewRecords(
                this.sourceSchema, "COMPLEX_TYPES_VIEW", 0L, 10L, null, viewColumns).getFirst();
        assertTrue(viewRecord.getColumns().column("C_XML").isXmlType());
        assertTrue(viewRecord.getColumns().column("C_VIRTUAL").isGenerated());
        assertTrue(viewRecord.getProperty("C_VIRTUAL").isReadonly());
        Node viewXmlNode = ShellOracleRecordUtil.getNode(
                (OracleRecordProperty) viewRecord.getProperty("C_XML"),
                viewRecord.getProperty("C_XML").get(),
                viewRecord.getColumns().column("C_XML"));
        assertTrue(viewXmlNode instanceof XmlTextFiled);

        OracleRecord editable = this.loadSingle(this.sourceClient, this.sourceSchema, TABLE, 1);
        Map<String, Object> updated = this.updatedValues(2);
        for (Map.Entry<String, Object> entry : updated.entrySet()) {
            OracleColumn column = editable.getColumns().column(entry.getKey());
            if (column == null || !column.isDmlWritable()) {
                continue;
            }
            editable.putValue(column, this.recordControlValue(column, entry.getValue()));
            editable.getProperty(entry.getKey()).setChanged(true);
        }
        OracleRecordPrimaryKey primaryKey = new OracleRecordPrimaryKey();
        primaryKey.init(editable.getColumns().column("ID"), editable);
        OracleUpdateRecordParam updateParam = new OracleUpdateRecordParam();
        updateParam.setSchema(this.sourceSchema);
        updateParam.setTableName(TABLE);
        updateParam.setUpdateRecord(editable.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, this.sourceClient.updateRecord(updateParam));

        OracleRecord reloaded = this.loadSingle(this.sourceClient, this.sourceSchema, TABLE, 1);
        this.assertComplexValues(updated, reloaded);
        assertGeneratedValue(reloaded);
        assertTrue(reloaded.getProperty("C_VIRTUAL").isReadonly());
        assertTrue(reloaded.getProperty("C_BFILE").isReadonly());
    }

    @Test
    public void exportsAndImportsComplexTableInEverySupportedFormat() throws Exception {
        this.createComplexTable(this.sourceClient, this.sourceSchema, TABLE);
        this.insertComplexRecord(this.sourceClient, this.sourceSchema, TABLE, 1);
        this.insertComplexRecord(this.sourceClient, this.sourceSchema, TABLE, 2);

        for (String format : List.of("csv", "json", "xml", "txt", "xls", "xlsx", "sql", "html")) {
            Path file = this.workDirectory.resolve(TABLE + "." + format);
            this.exportComplexTable(format, file);
            assertTrue(format + " export is empty", Files.size(file) > 0);
            if ("csv".equals(format)) {
                this.assertExportedCsvJson(file);
            }
            if ("html".equals(format)) {
                String html = Files.readString(file);
                assertTrue(html.contains("<!DOCTYPE html>"));
                assertTrue(html.contains("alpha-value"));
                continue;
            }

            this.resetTable(this.targetClient, this.targetSchema, TABLE);
            try {
                if ("sql".equals(format)) {
                    ShellOracleDataRunSqlFileHandler handler =
                            new ShellOracleDataRunSqlFileHandler(this.targetClient, this.targetSchema);
                    handler.setContinueWithErrors(false);
                    handler.file(file.toFile());
                    handler.doAction();
                } else {
                    this.importComplexTable(format, file);
                }
            } catch (Exception ex) {
                throw new AssertionError(format + " import failed: " + file, ex);
            }
            List<OracleRecord> records = this.selectRows(this.targetClient, this.targetSchema, TABLE);
            assertEquals(format + " imported row count", 2, records.size());
            for (OracleRecord record : records) {
                int id = ((Number) record.getValue("ID")).intValue();
                this.assertImportedValues(id, record, format);
                assertGeneratedValue(record);
            }
        }
    }

    @Test
    public void editsExportsAndImportsUserDefinedTypes() throws Exception {
        this.createUdtModel(this.sourceClient, this.sourceSchema, UDT_TABLE);
        this.insertUdtRecord(this.sourceClient, this.sourceSchema, UDT_TABLE);
        List<OracleRecord> records = this.selectRows(this.sourceClient, this.sourceSchema, UDT_TABLE);
        assertEquals(1, records.size());
        assertUdtValues(records.getFirst(), "Main St", "Shanghai", List.of(1, 2, 3),
                List.of("tag1", "tag2"));

        OracleRecord editable = records.getFirst();
        editable.putValue("C_OBJECT", this.recordControlValue(
                editable.getColumns().column("C_OBJECT"),
                "{\"STREET\":\"Updated St\",\"CITY\":\"Beijing\"}"));
        editable.putValue("C_VARRAY", this.recordControlValue(
                editable.getColumns().column("C_VARRAY"), "[4,5,6]"));
        editable.putValue("C_NESTED_TAB", this.recordControlValue(
                editable.getColumns().column("C_NESTED_TAB"), "[\"tag3\",\"tag4\"]"));
        editable.getProperty("C_OBJECT").setChanged(true);
        editable.getProperty("C_VARRAY").setChanged(true);
        editable.getProperty("C_NESTED_TAB").setChanged(true);
        OracleUpdateRecordParam updateParam = new OracleUpdateRecordParam();
        updateParam.setSchema(this.sourceSchema);
        updateParam.setTableName(UDT_TABLE);
        updateParam.setRecord(editable.getOriginalRecordData());
        updateParam.setUpdateRecord(editable.getChangedRecordData());
        OracleRecordPrimaryKey primaryKey = new OracleRecordPrimaryKey();
        primaryKey.init(editable.getColumns().column("ID"), editable);
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, this.sourceClient.updateRecord(updateParam));
        assertUdtValues(this.selectRows(this.sourceClient, this.sourceSchema, UDT_TABLE).getFirst(),
                "Updated St", "Beijing", List.of(4, 5, 6), List.of("tag3", "tag4"));

        this.createUdtTypes(this.targetClient, this.targetSchema);
        for (String format : List.of("csv", "json", "xml", "txt", "xls", "xlsx", "sql", "html")) {
            Path file = this.workDirectory.resolve(UDT_TABLE + "." + format);
            this.exportTable(this.sourceClient, this.sourceSchema, UDT_TABLE, format, file);
            assertTrue(format + " UDT export is empty", Files.size(file) > 0);
            if ("html".equals(format)) {
                assertTrue(Files.readString(file).contains("<!DOCTYPE html>"));
                continue;
            }

            this.resetUdtTable(this.targetClient, this.targetSchema, UDT_TABLE);
            try {
                if ("sql".equals(format)) {
                    ShellOracleDataRunSqlFileHandler handler =
                            new ShellOracleDataRunSqlFileHandler(this.targetClient, this.targetSchema);
                    handler.setContinueWithErrors(false);
                    handler.file(file.toFile());
                    handler.doAction();
                } else {
                    this.importTable(this.targetClient, this.targetSchema, UDT_TABLE, format, file);
                }
            } catch (Exception ex) {
                throw new AssertionError(format + " UDT import failed: " + file, ex);
            }
            List<OracleRecord> imported = this.selectRows(this.targetClient, this.targetSchema, UDT_TABLE);
            assertEquals(format + " UDT row count", 1, imported.size());
            assertUdtValues(imported.getFirst(), "Updated St", "Beijing",
                    List.of(4, 5, 6), List.of("tag3", "tag4"));
        }
    }

    @Test
    public void loadsAndInsertsUserDefinedTypeColumnsFromViews() {
        this.createUdtModel(this.sourceClient, this.sourceSchema, UDT_TABLE);
        this.insertUdtRecord(this.sourceClient, this.sourceSchema, UDT_TABLE);
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "CREATE VIEW \"" + this.sourceSchema + "\".\"UDT_TYPES_VIEW\" AS "
                        + "SELECT \"ID\", \"C_OBJECT\", \"C_VARRAY\", \"C_NESTED_TAB\" FROM \""
                        + this.sourceSchema + "\".\"" + UDT_TABLE + "\"");

        OracleColumns viewColumns = new OracleColumns(
                this.sourceClient.viewColumns(this.sourceSchema, "UDT_TYPES_VIEW"));
        List<OracleRecord> records = this.sourceClient.viewRecords(
                this.sourceSchema, "UDT_TYPES_VIEW", 0L, 10L, null, viewColumns);
        assertEquals(1, records.size());
        assertUdtValues(records.getFirst(), "Main St", "Shanghai",
                List.of(1, 2, 3), List.of("tag1", "tag2"));

        assertTrue(viewColumns.column("C_OBJECT").isObjectType());
        assertTrue(viewColumns.column("C_VARRAY").isCollectionType());
        assertTrue(viewColumns.column("C_NESTED_TAB").isCollectionType());

        OracleRecord inserted = new OracleRecord(viewColumns);
        inserted.putValue("C_OBJECT", "{\"STREET\":\"View St\",\"CITY\":\"Hangzhou\"}");
        inserted.putValue("C_VARRAY", "[4,5,6]");
        inserted.putValue("C_NESTED_TAB", "[\"view-tag\",\"second\"]");
        DBRecordData recordData = inserted.getRecordData();
        recordData.put(viewColumns.column("ID"), 2);
        OracleInsertRecordParam insertParam = new OracleInsertRecordParam();
        insertParam.setSchema(this.sourceSchema);
        insertParam.setTableName("UDT_TYPES_VIEW");
        insertParam.setRecord(recordData);
        assertEquals(1, this.sourceClient.insertRecord(insertParam));

        OracleRecord insertedViewRecord = this.sourceClient.viewRecords(
                        this.sourceSchema, "UDT_TYPES_VIEW", 0L, 10L, null, viewColumns).stream()
                .filter(record -> number(record.getValue("ID")).intValue() == 2)
                .findFirst().orElseThrow();
        assertUdtValues(insertedViewRecord, "View St", "Hangzhou",
                List.of(4, 5, 6), List.of("view-tag", "second"));
    }

    @Test
    public void transportsComplexAndUserDefinedTypeTablesBetweenSchemas() throws Exception {
        this.createComplexTable(this.sourceClient, this.sourceSchema, TABLE);
        this.insertComplexRecord(this.sourceClient, this.sourceSchema, TABLE, 1);
        this.insertComplexRecord(this.sourceClient, this.sourceSchema, TABLE, 2);
        this.createUdtModel(this.sourceClient, this.sourceSchema, UDT_TABLE);
        this.insertUdtRecord(this.sourceClient, this.sourceSchema, UDT_TABLE);

        ShellOracleDataTransportHandler handler = new ShellOracleDataTransportHandler();
        handler.setContinueWithErrors(false);
        handler.setSourceClient(this.sourceClient);
        handler.setTargetClient(this.targetClient);
        handler.setSourceDatabase(this.sourceSchema);
        handler.setTargetDatabase(this.targetSchema);
        handler.setSelectLimit(1);
        handler.setInsertLimit(1);
        handler.setDataType(0);
        List<String> messages = new ArrayList<>();
        handler.setMessageHandler(messages::add);
        DBDataTransportObject complex = new DBDataTransportObject();
        complex.setName(TABLE);
        DBDataTransportObject udt = new DBDataTransportObject();
        udt.setName(UDT_TABLE);
        handler.setTables(List.of(complex, udt));
        handler.doAction();
        assertTrue(messages.toString(), messages.stream()
                .noneMatch(message -> message != null && message.contains("ORA-")));

        List<OracleRecord> complexRecords =
                this.selectRows(this.targetClient, this.targetSchema, TABLE);
        assertEquals(2, complexRecords.size());
        assertGeneratedValue(complexRecords.getFirst());
        List<OracleRecord> udtRecords =
                this.selectRows(this.targetClient, this.targetSchema, UDT_TABLE);
        assertEquals(1, udtRecords.size());
        assertUdtValues(udtRecords.getFirst(), "Main St", "Shanghai",
                List.of(1, 2, 3), List.of("tag1", "tag2"));
    }

    @Test
    public void designsOracleVirtualColumnsThroughTableEntry() {
        String tableName = "VC_DESIGN";
        OracleTable table = new OracleTable();
        table.setSchema(this.sourceSchema);
        table.setName(tableName);

        OracleColumn id = column("ID", "NUMBER", this.sourceSchema, tableName);
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        OracleColumn base = column("BASE_TEXT", "VARCHAR2", this.sourceSchema, tableName);
        base.setSize(200);
        base.setNullable(true);
        OracleColumn virtual = column("VIRTUAL_TEXT", "VARCHAR2", this.sourceSchema, tableName);
        virtual.setSize(200);
        virtual.setNullable(true);
        virtual.setGenerated(true);
        virtual.setGeneratedExpression("UPPER(\"BASE_TEXT\")");

        OracleCreateTableParam createParam = new OracleCreateTableParam();
        createParam.setTable(table);
        createParam.setColumns(new OracleColumns(List.of(id, base, virtual)));
        createParam.setTriggers(new DBObjects<>());
        this.sourceClient.createTable(createParam);

        OracleColumns columns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        OracleColumn createdVirtual = columns.column("VIRTUAL_TEXT");
        assertTrue(createdVirtual.isGenerated());
        assertEquals("UPPER(\"BASE_TEXT\")",
                createdVirtual.getGeneratedExpression().replace("\"", "\""));
        assertFalse(createdVirtual.isDmlWritable());

        OracleRecord record = new OracleRecord(columns);
        record.putValue("ID", 1);
        record.putValue("BASE_TEXT", "alpha");
        DBRecordData data = record.getRecordData();
        data.put(columns.column("ID"), 1);
        OracleRecordPrimaryKey primaryKey = new OracleRecordPrimaryKey();
        primaryKey.init(columns.column("ID"), record);
        OracleInsertRecordParam insertParam = new OracleInsertRecordParam();
        insertParam.setSchema(this.sourceSchema);
        insertParam.setTableName(tableName);
        insertParam.setRecord(data);
        insertParam.setPrimaryKey(primaryKey);
        assertEquals(1, this.sourceClient.insertRecord(insertParam));
        assertEquals("ALPHA", String.valueOf(
                this.loadSingle(this.sourceClient, this.sourceSchema, tableName, 1)
                        .getValue("VIRTUAL_TEXT")));

        columns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        createdVirtual = columns.column("VIRTUAL_TEXT");
        createdVirtual.setSize(300);
        createdVirtual.setGeneratedExpression("UPPER(\"BASE_TEXT\") || '-V2'");
        OracleAlertTableParam alertParam = new OracleAlertTableParam();
        alertParam.setTable(table);
        alertParam.setColumns(new OracleColumns(columns));
        this.sourceClient.alertTable(alertParam);
        OracleColumn updatedVirtual = this.columns(this.sourceClient, this.sourceSchema, tableName)
                .column("VIRTUAL_TEXT");
        assertTrue(updatedVirtual.getGeneratedExpression().contains("-V2"));
        assertEquals("ALPHA-V2", String.valueOf(
                this.loadSingle(this.sourceClient, this.sourceSchema, tableName, 1)
                        .getValue("VIRTUAL_TEXT")));

        columns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        OracleColumn normal = columns.column("VIRTUAL_TEXT");
        normal.setGenerated(false);
        alertParam.setColumns(new OracleColumns(columns));
        this.sourceClient.alertTable(alertParam);
        OracleColumn normalColumn = this.columns(this.sourceClient, this.sourceSchema, tableName)
                .column("VIRTUAL_TEXT");
        assertFalse(normalColumn.isGenerated());
        assertEquals("ALPHA-V2", String.valueOf(
                this.loadSingle(this.sourceClient, this.sourceSchema, tableName, 1)
                        .getValue("VIRTUAL_TEXT")));

        columns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        OracleColumn virtualAgain = columns.column("VIRTUAL_TEXT");
        virtualAgain.setGenerated(true);
        virtualAgain.setGeneratedExpression("UPPER(\"BASE_TEXT\") || '-V3'");
        alertParam.setColumns(new OracleColumns(columns));
        this.sourceClient.alertTable(alertParam);
        OracleColumn convertedVirtual = this.columns(this.sourceClient, this.sourceSchema, tableName)
                .column("VIRTUAL_TEXT");
        assertTrue(convertedVirtual.isGenerated());
        assertEquals("ALPHA-V3", String.valueOf(
                this.loadSingle(this.sourceClient, this.sourceSchema, tableName, 1)
                        .getValue("VIRTUAL_TEXT")));
    }

    @Test
    public void loadsSharedGeneratedColumnKindControl() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(
                        "/tabs/oracle/table/shellOracleTableColumnExtra.fxml");
                assertNotNull(result.getNode());
                assertNotNull(findNode((Parent) result.getNode(), OracleColumnKindComboBox.class));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Oracle column design UI load timed out",
                done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void convertsExistingColumnToAutoIncrementThroughTableDesignEntry() {
        String tableName = "AUTO_INCREMENT_DESIGN";
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "CREATE TABLE \"" + this.sourceSchema + "\".\"" + tableName + "\" ("
                        + "\"ID\" NUMBER(19) PRIMARY KEY,"
                        + "\"NAME\" VARCHAR2(100))");
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "INSERT INTO \"" + this.sourceSchema + "\".\"" + tableName + "\" VALUES (1, 'one')");
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "INSERT INTO \"" + this.sourceSchema + "\".\"" + tableName + "\" VALUES (10, 'ten')");

        OracleTable table = new OracleTable();
        table.setSchema(this.sourceSchema);
        table.setName(tableName);
        OracleColumns columns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        OracleColumn id = columns.column("ID");
        id.setType("INT");
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");

        OracleAlertTableParam alertParam = new OracleAlertTableParam();
        alertParam.setTable(table);
        alertParam.setColumns(new OracleColumns(columns));
        this.sourceClient.alertTable(alertParam);

        OracleColumns updatedColumns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        OracleColumn updatedId = updatedColumns.column("ID");
        assertTrue(updatedId.isAutoIncrement());
        assertEquals("SEQUENCE_TRIGGER", updatedId.getAutoIncrementType());
        assertNotNull(updatedId.getAutoIncrementSequenceName());
        assertNotNull(updatedId.getAutoIncrementTriggerName());

        OracleRecord record = new OracleRecord(updatedColumns);
        record.putValue("NAME", "automatic");
        DBRecordData data = record.getRecordData();
        data.put(updatedColumns.column("NAME"), "automatic");
        OracleInsertRecordParam insertParam = new OracleInsertRecordParam();
        insertParam.setSchema(this.sourceSchema);
        insertParam.setTableName(tableName);
        insertParam.setRecord(data);
        assertEquals(1, this.sourceClient.insertRecord(insertParam));

        List<OracleRecord> records = this.selectRows(
                this.sourceClient, this.sourceSchema, tableName);
        OracleRecord automatic = records.stream()
                .filter(item -> "automatic".equals(item.getValue("NAME")))
                .findFirst()
                .orElseThrow();
        assertEquals(11, ((Number) automatic.getValue("ID")).intValue());
    }

    @Test
    public void modifiesColumnsReferencedByVirtualColumnsThroughTableDesignEntry() {
        String tableName = "VIRTUAL_DEPENDENCY_DESIGN";
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "CREATE TABLE \"" + this.sourceSchema + "\".\"" + tableName + "\" ("
                        + "\"ID\" NUMBER(19) PRIMARY KEY,"
                        + "\"BASE_NUMBER\" NUMBER(20,2),"
                        + "\"DOUBLED\" NUMBER GENERATED ALWAYS AS (\"BASE_NUMBER\" * 2) VIRTUAL)");
        this.sourceClient.executeSqlSimple(this.sourceSchema,
                "INSERT INTO \"" + this.sourceSchema + "\".\"" + tableName
                        + "\" (\"ID\", \"BASE_NUMBER\") VALUES (1, 12.0)");

        OracleTable table = new OracleTable();
        table.setSchema(this.sourceSchema);
        table.setName(tableName);
        OracleColumns columns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        columns.column("BASE_NUMBER").setType("INT");
        OracleAlertTableParam alertParam = new OracleAlertTableParam();
        alertParam.setTable(table);
        alertParam.setColumns(new OracleColumns(columns));
        this.sourceClient.alertTable(alertParam);

        OracleColumns updatedColumns = this.columns(this.sourceClient, this.sourceSchema, tableName);
        OracleColumn doubled = updatedColumns.column("DOUBLED");
        assertTrue(doubled.isGenerated());
        assertTrue(doubled.getGeneratedExpression().contains("BASE_NUMBER"));
        OracleRecord record = this.loadSingle(
                this.sourceClient, this.sourceSchema, tableName, 1);
        assertEquals(0, new BigDecimal("24")
                .compareTo(new BigDecimal(String.valueOf(record.getValue("DOUBLED")))));
    }

    @Test
    public void editsSequenceCurrentValueThroughDesignEntry() throws Exception {
        String sequenceName = "CURRENT_VALUE_DESIGN_SEQ";
        OracleSequence sequence = new OracleSequence(sequenceName);
        sequence.setSchema(this.sourceSchema);
        sequence.setMinVal(BigInteger.ONE);
        sequence.setMaxVal(new BigInteger("9999999999999999999999999999"));
        sequence.setCurrentVal(BigInteger.ONE);
        sequence.setIncrementBy(BigInteger.ONE);
        sequence.setCacheSize(BigInteger.TWO);
        sequence.setCycle(false);
        sequence.setOrder(false);
        boolean created = false;
        try {
            OracleCreateSequenceParam createParam = new OracleCreateSequenceParam();
            createParam.setSchema(this.sourceSchema);
            createParam.setSequence(sequence);
            this.sourceClient.createSequence(createParam);
            created = true;

            OracleSequence selected = this.sourceClient.selectSequence(this.sourceSchema, sequenceName);
            selected.setCurrentVal(BigInteger.valueOf(1000));
            OracleAlertSequenceParam alertParam = new OracleAlertSequenceParam();
            alertParam.setSchema(this.sourceSchema);
            alertParam.setSequence(selected);
            this.sourceClient.alertSequence(alertParam);

            try (Connection connection = this.schemaConnection(this.sourceSchema);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT \"" + sequenceName + "\".NEXTVAL FROM DUAL")) {
                assertTrue(resultSet.next());
                assertEquals(1000, resultSet.getLong(1));
            }

            OracleSequence lowered = this.sourceClient.selectSequence(this.sourceSchema, sequenceName);
            lowered.setCurrentVal(BigInteger.TEN);
            OracleAlertSequenceParam lowerParam = new OracleAlertSequenceParam();
            lowerParam.setSchema(this.sourceSchema);
            lowerParam.setSequence(lowered);
            this.sourceClient.alertSequence(lowerParam);
            try (Connection connection = this.schemaConnection(this.sourceSchema);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT \"" + sequenceName + "\".NEXTVAL FROM DUAL")) {
                assertTrue(resultSet.next());
                assertEquals(10, resultSet.getLong(1));
            }
        } finally {
            if (created) {
                this.sourceClient.dropSequence(sequence);
            }
        }
    }

    @Test
    public void clonesTableWithNestedAndUserDefinedTypesThroughClientEntry() {
        this.createUdtModel(this.sourceClient, this.sourceSchema, UDT_TABLE);
        this.insertUdtRecord(this.sourceClient, this.sourceSchema, UDT_TABLE);
        String cloneTable = UDT_TABLE + "_CLONE";
        try {
            this.sourceClient.cloneTable(
                    this.sourceSchema, UDT_TABLE, cloneTable, true);

            OracleColumns columns = this.columns(
                    this.sourceClient, this.sourceSchema, cloneTable);
            OracleColumn objectColumn = columns.column("C_OBJECT");
            OracleColumn varrayColumn = columns.column("C_VARRAY");
            OracleColumn nestedColumn = columns.column("C_NESTED_TAB");
            assertTrue(objectColumn.isObjectType());
            assertTrue(varrayColumn.isCollectionType());
            assertTrue(nestedColumn.isCollectionType());

            List<OracleRecord> records = this.selectRows(
                    this.sourceClient, this.sourceSchema, cloneTable);
            assertEquals(1, records.size());
            assertUdtValues(records.getFirst(), "Main St", "Shanghai",
                    List.of(1, 2, 3), List.of("tag1", "tag2"));
        } finally {
            if (this.sourceClient.selectTables(this.sourceSchema).stream()
                    .anyMatch(table -> cloneTable.equals(table.getName()))) {
                this.sourceClient.executeSqlSimple(this.sourceSchema,
                        "DROP TABLE \"" + this.sourceSchema + "\".\""
                                + cloneTable + "\" CASCADE CONSTRAINTS PURGE");
            }
        }
    }

    @Test
    public void clonesComplexTableWithLongLobsAndVirtualColumnThroughClientEntry() {
        String sourceTable = "COMPLEX_TYPES_LONG_SOURCE";
        this.createComplexTable(this.sourceClient, this.sourceSchema, sourceTable);
        this.insertComplexRecord(this.sourceClient, this.sourceSchema, sourceTable, 1);
        this.insertComplexRecord(this.sourceClient, this.sourceSchema, sourceTable, 2);
        String cloneTable = this.sourceClient.cloneTableName(sourceTable);
        assertTrue(cloneTable.length() <= ("11g".equals(this.mode) ? 30 : 128));
        try {
            this.sourceClient.cloneTable(
                    this.sourceSchema, sourceTable, cloneTable, true);

            List<OracleRecord> records = this.selectRows(
                    this.sourceClient, this.sourceSchema, cloneTable);
            assertEquals(2, records.size());
            records.sort(Comparator.comparingLong(record ->
                    number(record.getValue("ID")).longValue()));
            for (int i = 0; i < records.size(); i++) {
                int id = i + 1;
                OracleRecord record = records.get(i);
                assertEquals(id, number(record.getValue("ID")).intValue());
                this.assertComplexValues(this.seedValues(id), record);
                assertGeneratedValue(record);
            }
        } finally {
            this.dropTableIfExists(this.sourceClient, this.sourceSchema, cloneTable);
        }
    }

    private void createComplexTable(ShellOracleClient client, String schema, String tableName) {
        String extra = "23c".equals(this.mode)
                ? "\"C_BOOLEAN\" BOOLEAN,\n"
                + "\"C_JSON\" JSON,\n"
                + "\"C_VECTOR\" VECTOR,\n"
                : "";
        client.executeSqlSimple(schema, "CREATE TABLE \"" + schema + "\".\"" + tableName + "\" (\n"
                + "\"ID\" NUMBER(19) PRIMARY KEY,\n"
                + "\"C_NUMBER\" NUMBER(20,6),\n"
                + "\"C_NUMBER_PS\" NUMBER(10,2),\n"
                + "\"C_INTEGER\" INTEGER,\n"
                + "\"C_FLOAT\" FLOAT(126),\n"
                + "\"C_BINARY_FLOAT\" BINARY_FLOAT,\n"
                + "\"C_BINARY_DOUBLE\" BINARY_DOUBLE,\n"
                + extra
                + "\"C_CHAR\" CHAR(20),\n"
                + "\"C_VARCHAR2\" VARCHAR2(200),\n"
                + "\"C_VARCHAR2_CHAR\" VARCHAR2(200 CHAR),\n"
                + "\"C_NCHAR\" NCHAR(20),\n"
                + "\"C_NVARCHAR2\" NVARCHAR2(200),\n"
                + "\"C_RAW\" RAW(64),\n"
                + "\"C_LONG\" LONG,\n"
                + "\"C_DATE\" DATE,\n"
                + "\"C_TIMESTAMP\" TIMESTAMP(6),\n"
                + "\"C_TS_TZ\" TIMESTAMP(6) WITH TIME ZONE,\n"
                + "\"C_TS_LTZ\" TIMESTAMP(6) WITH LOCAL TIME ZONE,\n"
                + "\"C_INTERVAL_YM\" INTERVAL YEAR TO MONTH,\n"
                + "\"C_INTERVAL_DS\" INTERVAL DAY TO SECOND,\n"
                + "\"C_CLOB\" CLOB,\n"
                + "\"C_NCLOB\" NCLOB,\n"
                + "\"C_BLOB\" BLOB,\n"
                + "\"C_BFILE\" BFILE,\n"
                + "\"C_ROWID\" ROWID,\n"
                + "\"C_UROWID\" UROWID,\n"
                + "\"C_XML\" XMLTYPE,\n"
                + "\"C_DEFAULT_STR\" VARCHAR2(100) DEFAULT 'N/A',\n"
                + "\"C_VIRTUAL\" NUMBER GENERATED ALWAYS AS (\"C_NUMBER\" * 2) VIRTUAL\n"
                + ")");
    }

    private void createUdtTypes(ShellOracleClient client, String schema) {
        client.executeSqlSimple(schema,
                "CREATE TYPE \"" + schema + "\".\"T_ADDR\" AS OBJECT"
                        + " (\"STREET\" VARCHAR2(100), \"CITY\" VARCHAR2(100))");
        client.executeSqlSimple(schema,
                "CREATE TYPE \"" + schema + "\".\"T_NUM_ARRAY\" AS VARRAY(10) OF NUMBER");
        client.executeSqlSimple(schema,
                "CREATE TYPE \"" + schema + "\".\"T_TAG_LIST\" AS TABLE OF VARCHAR2(50)");
    }

    private void createUdtModel(ShellOracleClient client, String schema, String tableName) {
        this.createUdtTypes(client, schema);
        client.executeSqlSimple(schema, "CREATE TABLE \"" + schema + "\".\"" + tableName + "\" ("
                + "\"ID\" NUMBER PRIMARY KEY,"
                + "\"C_OBJECT\" \"" + schema + "\".\"T_ADDR\","
                + "\"C_VARRAY\" \"" + schema + "\".\"T_NUM_ARRAY\","
                + "\"C_NESTED_TAB\" \"" + schema + "\".\"T_TAG_LIST\""
                + ") NESTED TABLE \"C_NESTED_TAB\" STORE AS \"NESTED_TAG_STORE\"");
    }

    private OracleRecord insertComplexRecord(ShellOracleClient client, String schema,
                                             String tableName, int suffix) {
        OracleColumns columns = this.columns(client, schema, tableName);
        OracleColumn idColumn = columns.column("ID");
        assertTrue(columns.column("C_CLOB").supportText());
        assertTrue(columns.column("C_BLOB").supportBinary());
        if ("23c".equals(this.mode)) {
            assertTrue(columns.column("C_JSON").supportJson());
        }
        Map<String, Object> values = this.seedValues(suffix);
        OracleRecord record = new OracleRecord(columns);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            OracleColumn column = columns.column(entry.getKey());
            record.putValue(column, this.recordControlValue(column, entry.getValue()));
        }
        record.putValue(idColumn, suffix);
        DBRecordData data = record.getRecordData();
        data.put(idColumn, suffix);
        OracleRecordPrimaryKey primaryKey = new OracleRecordPrimaryKey();
        primaryKey.init(idColumn, record);
        OracleInsertRecordParam param = new OracleInsertRecordParam();
        param.setSchema(schema);
        param.setTableName(tableName);
        param.setRecord(data);
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        return this.loadSingle(client, schema, tableName, suffix);
    }

    private void insertUdtRecord(ShellOracleClient client, String schema, String tableName) {
        OracleColumns columns = this.columns(client, schema, tableName);
        OracleColumn objectColumn = columns.column("C_OBJECT");
        assertEquals(2, objectColumn.getTypeMeta().attributes().size());
        try {
            Object jdbcObject = OracleComplexValueUtil.toJdbcValue(
                    client.getConnManager().connection(schema), objectColumn,
                    "{\"STREET\":\"Main St\",\"CITY\":\"Shanghai\"}");
            assertTrue(jdbcObject instanceof java.sql.Struct);
            Object[] attributes = ((java.sql.Struct) jdbcObject).getAttributes();
            assertEquals(2, attributes.length);
            assertEquals("Main St", attributes[0]);
            assertEquals("Shanghai", attributes[1]);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
        OracleRecord record = new OracleRecord(columns);
        record.putValue("ID", 1);
        record.putValue("C_OBJECT", this.recordControlValue(
                columns.column("C_OBJECT"), "{\"STREET\":\"Main St\",\"CITY\":\"Shanghai\"}"));
        record.putValue("C_VARRAY", this.recordControlValue(
                columns.column("C_VARRAY"), "[1,2,3]"));
        record.putValue("C_NESTED_TAB", this.recordControlValue(
                columns.column("C_NESTED_TAB"), "[\"tag1\",\"tag2\"]"));
        OracleInsertRecordParam param = new OracleInsertRecordParam();
        param.setSchema(schema);
        param.setTableName(tableName);
        DBRecordData data = record.getRecordData();
        data.put(columns.column("ID"), 1);
        assertEquals("UDT insert columns: " + data.entries(), 4, data.columnSize());
        assertTrue(data.value("C_OBJECT") instanceof Map<?, ?>);
        assertTrue(data.value("C_VARRAY") instanceof List<?>);
        assertTrue(data.value("C_NESTED_TAB") instanceof List<?>);
        param.setRecord(data);
        assertEquals(1, client.insertRecord(param));
        try (Connection connection = this.schemaConnection(schema);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT \"C_OBJECT\" FROM \"" + schema + "\".\"" + tableName + "\"")) {
            assertTrue(resultSet.next());
            Object stored = resultSet.getObject(1);
            assertTrue("stored C_OBJECT=" + stored + ", type="
                            + (stored == null ? null : stored.getClass()),
                    stored instanceof java.sql.Struct);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    private void exportComplexTable(String format, Path file) throws Exception {
        this.exportTable(this.sourceClient, this.sourceSchema, TABLE, format, file);
    }

    private void assertExportedCsvJson(Path file) throws Exception {
        if (!"23c".equals(this.mode)) {
            return;
        }
        DBDataImportConfig config = new DBDataImportConfig();
        config.setFieldSeparator(";");
        config.setTxtIdentifier("\"");
        config.setRecordSeparator("\n");
        config.setDataRowStarts(2);
        try (DBDataCsvTypeFileReader reader =
                     new DBDataCsvTypeFileReader(file.toFile(), config)) {
            Map<String, Object> record = reader.readRecords(1).getFirst();
            String json = String.valueOf(record.get("C_JSON"));
            assertTrue("Invalid C_JSON in exported CSV: " + json + "\n" + Files.readString(file),
                    JSONUtil.isJson(json));
        }
    }

    private void exportTable(ShellOracleClient client, String schema, String tableName,
                             String format, Path file) throws Exception {
        List<OracleColumn> columns = client.selectColumns(
                new OracleSelectColumnParam(schema, tableName));
        ShellOracleDataExportTable table = new ShellOracleDataExportTable();
        table.setName(tableName);
        table.setFilePath(file.toString());
        table.columns(columns);
        ShellOracleDataExportHandler handler =
                new ShellOracleDataExportHandler(client, schema);
        handler.setContinueWithErrors(false);
        handler.setFileType(format);
        handler.setQueryLimit(1);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.fieldSeparator(";");
        handler.recordSeparator("\n");
        handler.txtIdentifier("\"");
        handler.setTables(List.of(table));
        handler.doAction();
    }

    private void importComplexTable(String format, Path file) throws Exception {
        this.importTable(this.targetClient, this.targetSchema, TABLE, format, file);
    }

    private void importTable(ShellOracleClient client, String schema, String tableName,
                             String format, Path file) throws Exception {
        ShellOracleDataImportFile importFile = new ShellOracleDataImportFile();
        importFile.setSchema(schema);
        importFile.setDbClient(client);
        importFile.setFile(file.toFile());
        importFile.setTargetTableName(tableName);
        ShellOracleDataImportHandler handler = new ShellOracleDataImportHandler(client, schema);
        handler.setContinueWithErrors(false);
        handler.setFileType("xls".equals(format) || "xlsx".equals(format) ? "excel" : format);
        handler.dateFormat("yyyy-MM-dd HH:mm:ss");
        handler.recordSeparator("\n");
        handler.txtIdentifier("\"");
        handler.fieldSeparator(";");
        handler.importMode("2");
        handler.columnIndex(0);
        handler.dataRowStarts(2);
        handler.setInsertLimit(1);
        handler.setBatchLimit(1);
        handler.setFiles(List.of(importFile));
        handler.doAction();
    }

    private void resetTable(ShellOracleClient client, String schema, String tableName) {
        this.dropTableIfExists(client, schema, tableName);
        this.createComplexTable(client, schema, tableName);
    }

    private void resetUdtTable(ShellOracleClient client, String schema, String tableName) {
        this.dropTableIfExists(client, schema, tableName);
        client.executeSqlSimple(schema, "CREATE TABLE \"" + schema + "\".\"" + tableName + "\" ("
                + "\"ID\" NUMBER PRIMARY KEY,"
                + "\"C_OBJECT\" \"" + schema + "\".\"T_ADDR\","
                + "\"C_VARRAY\" \"" + schema + "\".\"T_NUM_ARRAY\","
                + "\"C_NESTED_TAB\" \"" + schema + "\".\"T_TAG_LIST\""
                + ") NESTED TABLE \"C_NESTED_TAB\" STORE AS \"NESTED_TAG_STORE_2\"");
    }

    private void dropTableIfExists(ShellOracleClient client, String schema, String tableName) {
        try {
            client.executeSqlSimple(schema, "DROP TABLE \"" + schema + "\".\"" + tableName
                    + "\" CASCADE CONSTRAINTS PURGE");
        } catch (Exception ignored) {
        }
    }

    private OracleColumns columns(ShellOracleClient client, String schema, String tableName) {
        return new OracleColumns(client.selectColumns(
                new OracleSelectColumnParam(schema, tableName)));
    }

    private OracleRecord loadSingle(ShellOracleClient client, String schema,
                                    String tableName, long id) {
        return this.selectRows(client, schema, tableName).stream()
                .filter(record -> number(record.getValue("ID")).longValue() == id)
                .findFirst()
                .orElseThrow();
    }

    private List<OracleRecord> selectRows(ShellOracleClient client, String schema,
                                          String tableName) {
        OracleSelectRecordParam param = new OracleSelectRecordParam();
        param.setSchema(schema);
        param.setTableName(tableName);
        param.setColumns(client.selectColumns(new OracleSelectColumnParam(schema, tableName)));
        param.setStart(0L);
        param.setLimit(100L);
        return client.selectRecords(param);
    }

    private Map<String, Object> seedValues(int suffix) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("C_NUMBER", new BigDecimal("123.25").add(BigDecimal.valueOf(suffix)));
        values.put("C_NUMBER_PS", new BigDecimal("456.50").add(BigDecimal.valueOf(suffix)));
        values.put("C_INTEGER", 789 + suffix);
        values.put("C_FLOAT", 12.5D + suffix);
        values.put("C_BINARY_FLOAT", 3.5F + suffix);
        values.put("C_BINARY_DOUBLE", 9.25D + suffix);
        values.put("C_CHAR", "char-" + suffix);
        values.put("C_VARCHAR2", "alpha-value-" + suffix + " O'Reilly 中文 😀");
        values.put("C_VARCHAR2_CHAR", "char-length-" + suffix);
        values.put("C_NCHAR", "nchar-" + suffix);
        values.put("C_NVARCHAR2", "nvarchar 中文 😀 " + suffix);
        values.put("C_RAW", new byte[]{0, 1, 2, (byte) 0xFE, (byte) 0xFF});
        values.put("C_LONG", "L".repeat(5000) + suffix);
        values.put("C_DATE", java.sql.Timestamp.valueOf("2026-11-22 12:34:56"));
        values.put("C_TIMESTAMP", LocalDateTime.parse("2026-11-22T12:34:56.123456"));
        values.put("C_TS_TZ", OffsetDateTime.parse("2026-11-22T12:34:56.123456+08:00"));
        values.put("C_TS_LTZ", java.sql.Timestamp.valueOf("2026-11-22 12:34:56.123"));
        values.put("C_INTERVAL_YM", "INTERVAL '2-5' YEAR TO MONTH");
        values.put("C_INTERVAL_DS", "INTERVAL '3 12:34:56.123456' DAY TO SECOND");
        values.put("C_CLOB", "C".repeat(7000) + suffix);
        values.put("C_NCLOB", "中".repeat(3000) + suffix);
        values.put("C_BLOB", bytes(1000, suffix));
        values.put("C_XML", "<root><id>" + suffix + "</id><name>张三</name></root>");
        values.put("C_DEFAULT_STR", "custom-" + suffix);
        if ("23c".equals(this.mode)) {
            values.put("C_BOOLEAN", suffix % 2 == 1);
            values.put("C_JSON", "{\"id\":" + suffix
                    + ",\"name\":\"row" + suffix + " 中文 😀\","
                    + "\"quote\":\"a\\\"b\",\"array\":[1,true,null]}");
            values.put("C_VECTOR", "[1.0,2.0," + suffix + ".0]");
        }
        return values;
    }

    private Map<String, Object> updatedValues(int suffix) {
        Map<String, Object> values = this.seedValues(suffix);
        values.put("C_NUMBER", new BigDecimal("987.75"));
        values.put("C_INTEGER", 321);
        values.put("C_VARCHAR2", "updated-value O'Reilly 中文 😀");
        values.put("C_CLOB", "updated-clob-" + "C".repeat(6000));
        values.put("C_NCLOB", "更新" + "中".repeat(2500));
        values.put("C_BLOB", bytes(1200, suffix));
        values.put("C_XML", "<root><id>" + suffix + "</id><name>李四</name></root>");
        if ("23c".equals(this.mode)) {
            values.put("C_JSON", "{\"id\":" + suffix + ",\"name\":\"updated 中文 😀\","
                    + "\"array\":[false,null]}");
            values.put("C_VECTOR", "[3.0,4.0," + suffix + ".0]");
        }
        return values;
    }

    private void assertImportedValues(int id, OracleRecord record, String format) {
        Map<String, Object> expected = this.seedValues(id);
        assertEquals(format + " ID", id, ((Number) record.getValue("ID")).intValue());
        assertEquals(format + " NUMBER", 0, ((BigDecimal) expected.get("C_NUMBER"))
                .compareTo(new BigDecimal(String.valueOf(record.getValue("C_NUMBER")))));
        assertEquals(format + " VARCHAR2", expected.get("C_VARCHAR2"),
                String.valueOf(record.getValue("C_VARCHAR2")));
        assertTrue(format + " CLOB", String.valueOf(record.getValue("C_CLOB"))
                .startsWith("C".repeat(100)));
        assertArrayEquals(format + " BLOB", (byte[]) expected.get("C_BLOB"),
                (byte[]) record.getValue("C_BLOB"));
        assertTrue(format + " XML", String.valueOf(record.getValue("C_XML"))
                .contains("<name>张三</name>"));
        if ("23c".equals(this.mode)) {
            assertBoolean(format + " BOOLEAN", (Boolean) expected.get("C_BOOLEAN"),
                    record.getValue("C_BOOLEAN"));
            assertTrue(format + " JSON", String.valueOf(record.getValue("C_JSON"))
                    .contains("\"id\""));
            assertNotNull(format + " VECTOR", record.getValue("C_VECTOR"));
        }
    }

    private void assertComplexValues(Map<String, Object> expected, OracleRecord actual) {
        assertNotNull(actual);
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object value = actual.getValue(entry.getKey());
            if (entry.getValue() instanceof byte[] bytes) {
                assertArrayEquals(entry.getKey(), bytes, (byte[]) value);
            } else if (entry.getValue() instanceof BigDecimal decimal) {
                assertEquals(entry.getKey(), 0,
                        decimal.compareTo(new BigDecimal(String.valueOf(value))));
            } else if (entry.getValue() instanceof Float number) {
                assertEquals(entry.getKey(), number.floatValue(),
                        ((Number) value).floatValue(), 0.001F);
            } else if (entry.getValue() instanceof Double number) {
                assertEquals(entry.getKey(), number, ((Number) value).doubleValue(), 0.000001D);
            } else if (entry.getValue() instanceof Boolean bool) {
                assertBoolean(entry.getKey(), bool, value);
            } else if (entry.getValue() instanceof java.sql.Timestamp
                    || entry.getValue() instanceof LocalDateTime
                    || entry.getValue() instanceof OffsetDateTime) {
                assertNotNull(entry.getKey(), value);
            } else if ("C_INTERVAL_YM".equals(entry.getKey())
                    || "C_INTERVAL_DS".equals(entry.getKey())) {
                assertNotNull(entry.getKey(), value);
            } else if ("C_JSON".equals(entry.getKey())) {
                assertTrue(String.valueOf(value).contains("\"name\""));
                assertTrue(String.valueOf(value), JSONUtil.isJson(String.valueOf(value)));
            } else if ("C_VECTOR".equals(entry.getKey())) {
                assertNotNull(entry.getKey(), value);
            } else if ("C_XML".equals(entry.getKey())) {
                assertEquals(entry.getKey(),
                        String.valueOf(entry.getValue()).replaceAll("\\s+", ""),
                        String.valueOf(value).replaceAll("\\s+", ""));
            } else if ("C_CHAR".equals(entry.getKey())
                    || "C_NCHAR".equals(entry.getKey())) {
                assertEquals(entry.getKey(), String.valueOf(entry.getValue()).trim(),
                        String.valueOf(value).trim());
            } else {
                assertEquals(entry.getKey(), String.valueOf(entry.getValue()),
                        String.valueOf(value));
            }
        }
    }

    private static void assertGeneratedValue(OracleRecord record) {
        BigDecimal number = new BigDecimal(String.valueOf(record.getValue("C_NUMBER")));
        BigDecimal virtual = new BigDecimal(String.valueOf(record.getValue("C_VIRTUAL")));
        assertEquals(0, number.multiply(BigDecimal.valueOf(2)).compareTo(virtual));
    }

    private static void assertUdtValues(OracleRecord record, String street,
                                        String city, List<?> array, List<?> nested) {
        Object objectValue = record.getValue("C_OBJECT");
        assertTrue("C_OBJECT=" + objectValue
                + ", type=" + (objectValue == null ? null : objectValue.getClass())
                + ", record=" + record.toMap(),
                objectValue instanceof Map<?, ?>);
        Map<?, ?> object = (Map<?, ?>) objectValue;
        assertEquals(street, object.get("STREET"));
        assertEquals(city, object.get("CITY"));
        Object arrayValue = record.getValue("C_VARRAY");
        assertTrue("C_VARRAY=" + arrayValue
                + ", type=" + (arrayValue == null ? null : arrayValue.getClass()),
                arrayValue instanceof List<?>);
        assertEquals(array.size(), ((List<?>) arrayValue).size());
        Object nestedValue = record.getValue("C_NESTED_TAB");
        assertTrue("C_NESTED_TAB=" + nestedValue
                + ", type=" + (nestedValue == null ? null : nestedValue.getClass()),
                nestedValue instanceof List<?>);
        assertEquals(nested.size(), ((List<?>) nestedValue).size());
    }

    private static void assertBoolean(String name, boolean expected, Object actual) {
        if (actual instanceof Boolean value) {
            assertEquals(name, expected, value);
        } else {
            assertEquals(name, expected, number(actual).intValue() != 0);
        }
    }

    private static Number number(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private static byte[] bytes(int size, int suffix) {
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) suffix);
        bytes[0] = 0;
        bytes[1] = (byte) 0xFF;
        return bytes;
    }

    private static OracleColumn column(String name, String type, String schema, String table) {
        OracleColumn column = new OracleColumn(name);
        column.setSchema(schema);
        column.setTableName(table);
        column.setType(type);
        return column;
    }

    private static <T extends Node> T findNode(Parent parent, Class<T> type) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
            if (child instanceof Parent childParent) {
                T result = findNode(childParent, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private Object recordControlValue(OracleColumn column, Object value) {
        if (column == null || value == null) {
            return value;
        }
        if (column.supportJson() || column.supportJsonArray()
                || column.isObjectType() || column.isCollectionType()) {
            AtomicReference<Object> result = new AtomicReference<>();
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Runnable task = () -> {
                try {
                    result.set(ShellOracleNodeUtil.getNodeVal(
                            ShellOracleRecordUtil.getNode(null, value, column)));
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    done.countDown();
                }
            };
            if (Platform.isFxApplicationThread()) {
                task.run();
            } else {
                Platform.runLater(task);
            }
            try {
                assertTrue("Record control initialization timed out",
                        done.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AssertionError(ex);
            }
            if (error.get() != null) {
                throw new AssertionError(error.get());
            }
            return result.get();
        }
        return value;
    }

    private ShellOracleClient openClient(String schema) throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.ORACLE);
        connect.setHost("127.0.0.1:" + this.port);
        connect.setDatabase(this.service);
        connect.setUser(schema);
        connect.setPassword(SCHEMA_PASSWORD);
        connect.setConnectTimeOut(10);
        ShellOracleClient client = new ShellOracleClient(connect);
        client.start(10000);
        return client;
    }

    private void createSchema(String schema) throws Exception {
        try (Connection connection = this.systemConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE USER \"" + schema + "\" IDENTIFIED BY \""
                    + SCHEMA_PASSWORD + "\" DEFAULT TABLESPACE USERS"
                    + " TEMPORARY TABLESPACE TEMP QUOTA UNLIMITED ON USERS");
            statement.execute("GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE,"
                    + " CREATE PROCEDURE, CREATE TRIGGER, CREATE TYPE TO \"" + schema + "\"");
        }
    }

    private void dropSchema(String schema) {
        if (schema == null) {
            return;
        }
        try (Connection connection = this.systemConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP USER \"" + schema + "\" CASCADE");
        } catch (Exception ignored) {
        }
    }

    private Connection systemConnection() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", SYSTEM_USER);
        properties.setProperty("password", SYSTEM_PASSWORD);
        properties.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        return DriverManager.getConnection(
                "jdbc:oracle:thin:@tcp://127.0.0.1:" + this.port + "/" + this.service,
                properties);
    }

    private Connection schemaConnection(String schema) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", schema);
        properties.setProperty("password", SCHEMA_PASSWORD);
        properties.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        return DriverManager.getConnection(
                "jdbc:oracle:thin:@tcp://127.0.0.1:" + this.port + "/" + this.service,
                properties);
    }
}
