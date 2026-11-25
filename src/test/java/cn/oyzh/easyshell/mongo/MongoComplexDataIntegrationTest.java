package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportCollection;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataImportFile;
import cn.oyzh.easyshell.data.mongo.handler.ShellMongoDataExportHandler;
import cn.oyzh.easyshell.data.mongo.handler.ShellMongoDataImportHandler;
import cn.oyzh.easyshell.data.mongo.handler.ShellMongoRunScriptFileHandler;
import cn.oyzh.easyshell.data.mongo.handler.ShellMongoDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mongo.collection.MongoCollectionOptions;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.index.MongoIndex;
import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordProperty;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.fx.mongo.ShellMongoCodeTextFiled;
import cn.oyzh.easyshell.util.mongo.ShellMongoColumnUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoCollectionDesignUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoRecordUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewDesignUtil;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import javafx.application.Platform;
import org.bson.BsonMaxKey;
import org.bson.BsonMinKey;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MongoDB复杂数据编辑、导入导出与传输集成测试。
 */
public class MongoComplexDataIntegrationTest {

    private static final String HOST = property("mongo.host", "MONGO_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("mongo.port", "MONGO_PORT", "27017"));
    private static final String USER = property("mongo.user", "MONGO_USER", "admin");
    private static final String PASSWORD = property("mongo.password", "MONGO_PASSWORD", "123456");
    private static final String AUTH_DB = property("mongo.authDatabase", "MONGO_AUTH_DB", "admin");

    private static final String DB = "easyshell_mongo_complex_it";
    private static final String TARGET_DB = "easyshell_mongo_complex_target";
    private static final String SOURCE = "complex_types";
    private static final String JSON_IMPORT = "complex_types_json_import";
    private static final String CSV_IMPORT = "complex_types_csv_import";
    private static final String TARGET = "complex_types_transport";

    private static final ObjectId DOC1_ID = new ObjectId("64b64c4f5d1234567890ab01");

    private static ShellMongoClient client;
    private static MongoClient rawClient;
    private static Path workDirectory;

    @BeforeClass
    public static void setUp() throws Throwable {
        Assume.assumeTrue("MongoDB endpoint is not reachable", isReachable(HOST, PORT));
        ShellMongoColumnUtil.init();
        rawClient = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(
                        List.of(new ServerAddress(HOST, PORT))))
                .credential(MongoCredential.createCredential(
                        USER, AUTH_DB, PASSWORD.toCharArray()))
                .build());
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.MONGO);
        connect.setHost(HOST + ":" + PORT);
        connect.setUser(USER);
        connect.setPassword(PASSWORD);
        connect.setMongoAuthDatabase(AUTH_DB);
        connect.setConnectTimeOut(5);
        client = new ShellMongoClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
        workDirectory = Files.createTempDirectory("easyshell-mongo-complex-");
    }

    @AfterClass
    public static void tearDown() {
        if (client != null) {
            client.close();
        }
        if (rawClient != null) {
            rawClient.close();
        }
    }

    @Before
    public void seedSource() {
        rawClient.getDatabase(DB).drop();
        rawClient.getDatabase(TARGET_DB).drop();
        List<Document> documents = List.of(
                new Document("_id", DOC1_ID)
                        .append("name", "复杂文档1")
                        .append("nested", new Document("city", "上海")
                                .append("zip", 200000)
                                .append("tags", List.of("a", "b")))
                        .append("items", List.of(
                                new Document("sku", "A1").append("qty", 2),
                                new Document("sku", "B2").append("qty", 3)))
                        .append("mixed", List.of("x", 7, true,
                                new ObjectId(), new Date(1700000000000L)))
                        .append("int32", 42)
                        .append("int64", 9223372036854770000L)
                        .append("double", 3.14159D)
                        .append("decimal", Decimal128.parse("1234567890.123456789012345678"))
                        .append("bool", true)
                        .append("nullValue", null)
                        .append("date", new Date(1700000000000L))
                        .append("oid", new ObjectId("64b64c4f5d1234567890abcd"))
                        .append("binary", new Binary((byte) 0, new byte[]{0, 1, 2, (byte) 255}))
                        .append("regex", Pattern.compile("^row.*", Pattern.CASE_INSENSITIVE))
                        .append("timestamp", new BsonTimestamp(1700000000, 5))
                        .append("minKey", new BsonMinKey())
                        .append("maxKey", new BsonMaxKey())
                        .append("code", new Code("function(){return 1;}"))
                        .append("longText", "中文😀\nline2 'quote' \"double\" \\ slash"),
                new Document("_id", "string-id-2")
                        .append("name", "字符串ID")
                        .append("int32", 7)
                        .append("nested", new Document("level", 2))
                        .append("items", List.of("x", "y")),
                new Document("_id", 3)
                        .append("name", "整数ID")
                        .append("int32", 3)
                        .append("bool", false),
                new Document("_id", new ObjectId("64b64c4f5d1234567890ab04"))
                        .append("name", "空值文档")
                        .append("nested", null)
                        .append("decimal", null)
                        .append("date", null)
                        .append("binary", null)
        );
        rawCollection(SOURCE).insertMany(documents);
        assertEquals(4, rawCollection(SOURCE).countDocuments());
    }

    @Test
    public void editsComplexDocumentFields() {
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();
        assertTrue(record._idValue() instanceof ObjectId);

        ObjectId oid = new ObjectId();
        Date date = new Date(1800000000000L);
        Binary binary = new Binary((byte) 0, new byte[]{9, 8, 7, 6});
        Pattern regex = Pattern.compile("^new.*", Pattern.CASE_INSENSITIVE);
        BsonTimestamp timestamp = new BsonTimestamp(1800000000, 7);
        Decimal128 decimal = Decimal128.parse("9876543210.012345678901234567");

        updateValue(record, "nested", new Document("city", "北京")
                .append("zip", 100000).append("tags", List.of("n", "e", "w")));
        updateValue(record, "items", List.of(
                new Document("sku", "N1").append("qty", 9),
                new Document("sku", "N2").append("qty", 8)));
        updateValue(record, "mixed", List.of("y", 99, false, new ObjectId(), date));
        updateValue(record, "int32", 123);
        updateValue(record, "int64", 1234567890123L);
        updateValue(record, "double", 1.5D);
        updateValue(record, "decimal", decimal);
        updateValue(record, "bool", false);
        updateValue(record, "nullValue", null);
        updateValue(record, "date", date);
        updateValue(record, "oid", oid);
        updateValue(record, "binary", binary);
        updateValue(record, "regex", regex);
        updateValue(record, "timestamp", timestamp);
        updateValue(record, "code", new Code("function(){return 9;}"));

        assertEquals(1, client.updateCollectionRecord(record));
        Document updated = rawCollection(SOURCE)
                .find(Filters.eq("_id", DOC1_ID)).first();
        assertNotNull(updated);
        assertEquals("北京", ((Document) updated.get("nested")).getString("city"));
        assertTrue(updated.get("items") instanceof List<?>);
        assertEquals(99, ((List<?>) updated.get("mixed")).get(1));
        assertEquals(decimal, updated.get("decimal"));
        assertEquals(date, updated.get("date"));
        assertEquals(oid, updated.get("oid"));
        assertTrue(updated.get("binary") instanceof Binary);
        assertTrue(updated.get("regex") instanceof Pattern
                || updated.get("regex") instanceof BsonRegularExpression);
        assertEquals(timestamp, updated.get("timestamp"));
        assertTrue(updated.get("code") instanceof Code);
    }

    @Test
    public void insertsNewDocumentWithDefaultsAndClearsStatus() {
        MongoColumns columns = ShellMongoRecordUtil.copyColumns(selectSourceColumns());
        MongoRecord record = new MongoRecord(columns);
        record.setCreated(true);
        for (MongoColumn column : columns) {
            record.putValue(column, column.defaultValue());
        }

        BsonValue id = client.insertCollectionRecord(record);
        if (record._idColumn().supportObjectId()) {
            record.set_id(id);
        } else {
            record.set_id(ShellMongoRecordUtil.idValue(id));
        }
        record.clearStatus();

        assertEquals(5, rawCollection(SOURCE).countDocuments());
        assertFalse(record.isCreated());
        assertFalse(record.isChanged());
        Document inserted = rawCollection(SOURCE)
                .find(Filters.eq("name", "")).first();
        assertNotNull(inserted);
        assertTrue(inserted.get("_id") instanceof ObjectId);
        assertTrue(inserted.get("regex") instanceof Pattern
                || inserted.get("regex") instanceof BsonRegularExpression);
        assertTrue(inserted.get("minKey") instanceof MinKey);
        assertTrue(inserted.get("maxKey") instanceof MaxKey);
        assertTrue(inserted.get("decimal") instanceof Decimal128);
        assertTrue(inserted.get("int32") instanceof Integer);
        assertTrue(inserted.get("int64") instanceof Long);
        assertTrue(inserted.get("items") instanceof List<?>);
        assertTrue(inserted.get("mixed") instanceof List<?>);
    }

    @Test
    public void filtersSystemCollections() {
        assertFalse(ShellMongoRecordUtil.isCollection("system.views"));
        assertFalse(ShellMongoRecordUtil.isCollection("system.js"));
        assertFalse(ShellMongoRecordUtil.isCollection("system.profile"));
        assertTrue(ShellMongoRecordUtil.isCollection("complex_types"));
        assertFalse(client.listCollectionNames("demo_complex").contains("system.views"));
        assertFalse(client.listCollectionNames("demo_complex").contains("system.js"));
    }

    @Test
    public void marksViewsInCollectionList() {
        rawClient.getDatabase(DB).createView("complex_types_view", SOURCE,
                List.of(new Document("$match", new Document("name", "复杂文档1"))));
        var view = client.listViews(DB).stream()
                .filter(item -> "complex_types_view".equals(item.getName()))
                .findFirst().orElseThrow();
        assertEquals(SOURCE, view.getViewOn());
        assertEquals(1, view.getPipeline().size());
        assertFalse(client.listCollections(DB).stream()
                .anyMatch(item -> "complex_types_view".equals(item.getName())));
        assertTrue(client.listCollections(DB).stream()
                .anyMatch(item -> SOURCE.equals(item.getName())));
    }

    @Test
    public void createsUpdatesAndDropsView() {
        MongoView view = new MongoView();
        view.setDbName(DB);
        view.setName("complex_types_view_design");
        view.setViewOn(SOURCE);
        view.setPipeline(List.of(
                new Document("$match", new Document("name", "复杂文档1")),
                new Document("$project", new Document("name", 1).append("int32", 1))));
        client.createView(view);

        MongoView created = client.selectView(DB, view.getName());
        assertNotNull(created);
        assertEquals(SOURCE, created.getViewOn());
        assertEquals(2, created.getPipeline().size());
        assertTrue(client.listViews(DB).stream()
                .anyMatch(item -> view.getName().equals(item.getName())));
        assertFalse(client.listCollections(DB).stream()
                .anyMatch(item -> view.getName().equals(item.getName())));

        created.setViewOn(SOURCE);
        created.setPipeline(List.of(
                new Document("$match", new Document("int32", new Document("$gte", 3)))));
        client.updateView(created);
        MongoView updated = client.selectView(DB, view.getName());
        assertNotNull(updated);
        assertEquals(1, updated.getPipeline().size());

        client.dropView(DB, view.getName());
        assertFalse(client.listViews(DB).stream()
                .anyMatch(item -> view.getName().equals(item.getName())));
    }

    @Test
    public void rendersViewDesignScript() {
        MongoView view = new MongoView();
        view.setName("complex_types_view");
        view.setViewOn(SOURCE);
        view.setPipeline(List.of(
                new Document("$match", new Document("int32", new Document("$gte", 3)))));
        String createScript = ShellMongoViewDesignUtil.toScript(view, true);
        assertTrue(createScript, createScript.contains("db.createView('complex_types_view'"));
        assertTrue(createScript, createScript.contains("'complex_types'"));
        assertTrue(createScript, createScript.contains("\"$match\""));

        String updateScript = ShellMongoViewDesignUtil.toScript(view, false);
        assertTrue(updateScript, updateScript.contains("db.runCommand("));
        assertTrue(updateScript, updateScript.contains("\"collMod\""));
        assertTrue(updateScript, updateScript.contains("\"viewOn\""));

        List<Document> pipeline = ShellMongoViewDesignUtil.parsePipeline(
                ShellMongoViewDesignUtil.pipelineText(view.getPipeline()));
        assertEquals(1, pipeline.size());
        assertTrue(pipeline.getFirst().containsKey("$match"));
    }

    @Test
    public void createdObjectIdUsesManualValue() throws Exception {
        initToolkit();
        MongoColumns columns = ShellMongoRecordUtil.copyColumns(selectSourceColumns());
        MongoRecord record = new MongoRecord(columns);
        record.setCreated(true);
        for (MongoColumn column : columns) {
            record.putValue(column, column.defaultValue());
        }
        MongoRecordProperty idProperty = (MongoRecordProperty) record.getProperty("_id");
        FXTextField idNode = (FXTextField) idProperty.getValue();
        ObjectId id = new ObjectId();
        idNode.setText(id.toHexString());
        idProperty.setChanged(true);

        BsonValue insertedId = client.insertCollectionRecord(record);
        assertNotNull(insertedId);
        assertTrue(insertedId instanceof BsonObjectId);
        assertEquals(id, ((BsonObjectId) insertedId).getValue());
        record.set_id(insertedId);
        record.clearStatus();

        assertEquals(5, rawCollection(SOURCE).countDocuments());
        assertFalse(record.isCreated());
        Document inserted = rawCollection(SOURCE).find(Filters.eq("_id", id)).first();
        assertNotNull(inserted);
    }

    @Test
    public void displaysCodeFieldWithoutJsonParsing() throws Exception {
        initToolkit();
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();
        MongoRecordProperty property = (MongoRecordProperty) record.getProperty("code");
        ShellMongoCodeTextFiled node = (ShellMongoCodeTextFiled) ShellMongoRecordUtil.getNode(
                property, property.get(), record.column("code"));
        assertEquals("function(){return 1;}", node.getText());
        assertTrue(node.getValue() instanceof Code);
    }

    @Test
    public void uiFieldsPreserveBsonTypes() throws Exception {
        initToolkit();
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();

        MongoRecordProperty oidProperty = (MongoRecordProperty) record.getProperty("oid");
        FXTextField oidNode = (FXTextField) oidProperty.getValue();
        ObjectId oid = new ObjectId();
        oidNode.setText(oid.toHexString());
        oidProperty.setChanged(true);
        assertEquals(oid, oidProperty.get());

        MongoRecordProperty decimalProperty = (MongoRecordProperty) record.getProperty("decimal");
        FXTextField decimalNode = (FXTextField) decimalProperty.getValue();
        decimalNode.setText("9999999999.123456789012345678");
        decimalProperty.setChanged(true);
        assertEquals(Decimal128.parse("9999999999.123456789012345678"),
                decimalProperty.get());
    }

    @Test
    public void uiNumericFieldsPreserveBsonTypes() throws Exception {
        initToolkit();
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();

        MongoRecordProperty intProperty = (MongoRecordProperty) record.getProperty("int32");
        NumberTextField intNode = (NumberTextField) intProperty.getValue();
        intNode.setText("321");
        intProperty.setChanged(true);
        assertEquals(321, intProperty.get());

        MongoRecordProperty longProperty = (MongoRecordProperty) record.getProperty("int64");
        NumberTextField longNode = (NumberTextField) longProperty.getValue();
        longNode.setText("1234567890123");
        longProperty.setChanged(true);
        assertEquals(1234567890123L, longProperty.get());

        assertEquals(1, client.updateCollectionRecord(record));
        Document updated = rawCollection(SOURCE)
                .find(Filters.eq("_id", DOC1_ID)).first();
        assertNotNull(updated);
        assertEquals(321, updated.get("int32"));
        assertTrue(updated.get("int64") instanceof Long);
        assertEquals(1234567890123L, updated.get("int64"));
    }

    @Test
    public void uiMinMaxKeyFieldsPreserveTypes() throws Exception {
        initToolkit();
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();

        MongoRecordProperty minProperty = (MongoRecordProperty) record.getProperty("minKey");
        FXTextField minNode = (FXTextField) minProperty.getValue();
        minNode.setText("MinKey");
        minProperty.setChanged(true);
        assertTrue(minProperty.get() instanceof MinKey);

        MongoRecordProperty maxProperty = (MongoRecordProperty) record.getProperty("maxKey");
        FXTextField maxNode = (FXTextField) maxProperty.getValue();
        maxNode.setText("MaxKey");
        maxProperty.setChanged(true);
        assertTrue(maxProperty.get() instanceof MaxKey);

        assertEquals(1, client.updateCollectionRecord(record));
        Document updated = rawCollection(SOURCE)
                .find(Filters.eq("_id", DOC1_ID)).first();
        assertNotNull(updated);
        assertTrue(updated.get("minKey") instanceof MinKey);
        assertTrue(updated.get("maxKey") instanceof MaxKey);
    }

    @Test
    public void uiCodeFieldUpdatePreservesType() throws Exception {
        initToolkit();
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();

        MongoRecordProperty codeProperty = (MongoRecordProperty) record.getProperty("code");
        ShellMongoCodeTextFiled codeNode = (ShellMongoCodeTextFiled) codeProperty.getValue();
        codeNode.setText("function(){return 42;}");
        codeProperty.setChanged(true);
        Code value = (Code) codeProperty.get();
        assertEquals("function(){return 42;}", value.getCode());

        assertEquals(1, client.updateCollectionRecord(record));
        Document updated = rawCollection(SOURCE)
                .find(Filters.eq("_id", DOC1_ID)).first();
        assertNotNull(updated);
        assertTrue(updated.get("code") instanceof Code);
        assertEquals("function(){return 42;}", ((Code) updated.get("code")).getCode());
    }

    @Test
    public void uiRegexFieldPreservesType() throws Exception {
        initToolkit();
        MongoRecord record = selectSource().stream()
                .filter(item -> "复杂文档1".equals(item.getValue("name")))
                .findFirst().orElseThrow();

        MongoRecordProperty regexProperty = (MongoRecordProperty) record.getProperty("regex");
        FXTextField regexNode = (FXTextField) regexProperty.getValue();
        assertEquals("/^row.*/i", regexNode.getText());
        regexNode.setText("/^new.*/i");
        regexProperty.setChanged(true);
        Object value = regexProperty.get();
        assertTrue(value instanceof BsonRegularExpression);
        assertEquals("^new.*", ((BsonRegularExpression) value).getPattern());
        assertEquals("i", ((BsonRegularExpression) value).getOptions());

        assertEquals(1, client.updateCollectionRecord(record));
        Document updated = rawCollection(SOURCE)
                .find(Filters.eq("_id", DOC1_ID)).first();
        assertNotNull(updated);
        Object regex = updated.get("regex");
        if (regex instanceof Pattern pattern) {
            assertEquals("^new.*", pattern.pattern());
            assertTrue((pattern.flags() & Pattern.CASE_INSENSITIVE) != 0);
        } else {
            assertTrue(regex instanceof BsonRegularExpression);
            assertEquals("^new.*", ((BsonRegularExpression) regex).getPattern());
        }
    }

    @Test
    public void exportsAndImportsExtendedJson() throws Exception {
        Path file = workDirectory.resolve("complex_types.json");
        export(SOURCE, "json", file, null);
        String json = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(json, json.contains("\"$oid\""));
        assertTrue(json, json.contains("\"$numberDecimal\""));
        assertTrue(json, json.contains("\"$binary\""));
        assertTrue(json, json.contains("\"$regularExpression\""));
        assertTrue(json, json.contains("\"$timestamp\""));
        assertTrue(json, json.contains("\"$minKey\""));
        assertTrue(json, json.contains("\"$maxKey\""));

        rawCollection(JSON_IMPORT).drop();
        importFile("json", file, JSON_IMPORT);
        assertEquals(4, rawCollection(JSON_IMPORT).countDocuments());
        Document imported = rawCollection(JSON_IMPORT)
                .find(Filters.eq("name", "复杂文档1")).first();
        assertNotNull(imported);
        assertTrue(imported.get("_id") instanceof ObjectId);
        assertTrue(imported.get("nested") instanceof Document);
        assertTrue(imported.get("items") instanceof List<?>);
        assertTrue(imported.get("decimal") instanceof Decimal128);
        assertTrue(imported.get("date") instanceof Date);
        assertTrue(imported.get("oid") instanceof ObjectId);
        assertTrue(imported.get("binary") instanceof Binary);
        assertTrue(imported.get("regex") instanceof Pattern
                || imported.get("regex") instanceof BsonRegularExpression);
        assertTrue(imported.get("timestamp") instanceof BsonTimestamp);
        assertTrue(imported.get("minKey") instanceof MinKey);
        assertTrue(imported.get("maxKey") instanceof MaxKey);
        assertTrue(imported.get("code") instanceof Code);
    }

    @Test
    public void exportsAndImportsCsvWithoutId() throws Exception {
        MongoSelectRecordParam selectParam = selectParam(SOURCE);
        List<MongoColumn> columns = new ArrayList<>(client.selectColumns(selectParam));
        columns.removeIf(column -> !List.of("name", "int32", "double", "bool")
                .contains(column.getName()));
        Path file = workDirectory.resolve("complex_types.csv");
        export(SOURCE, "csv", file, columns);

        rawCollection(CSV_IMPORT).drop();
        importFile("csv", file, CSV_IMPORT);
        assertEquals(4, rawCollection(CSV_IMPORT).countDocuments());
        Document imported = rawCollection(CSV_IMPORT)
                .find(Filters.eq("name", "复杂文档1")).first();
        assertNotNull(imported);
        assertTrue(imported.get("_id") instanceof ObjectId);
    }

    @Test
    public void transportsComplexCollection() throws Exception {
        ShellMongoDataTransportHandler handler = new ShellMongoDataTransportHandler();
        handler.setContinueWithErrors(false);
        handler.setSourceClient(client);
        handler.setTargetClient(client);
        handler.setSourceDatabase(DB);
        handler.setTargetDatabase(TARGET_DB);
        handler.setTables(List.of(transportObject(SOURCE)));
        handler.setDataType((byte) 0);
        handler.setSelectLimit(2);
        handler.doTransport();

        MongoCollection<Document> target = rawClient.getDatabase(TARGET_DB).getCollection(SOURCE);
        assertEquals(4, target.countDocuments());
        Document transported = target.find(Filters.eq("name", "复杂文档1")).first();
        assertNotNull(transported);
        assertTrue(transported.get("_id") instanceof ObjectId);
        assertTrue(transported.get("nested") instanceof Document);
        assertTrue(transported.get("decimal") instanceof Decimal128);
        assertTrue(transported.get("date") instanceof Date);
        assertTrue(transported.get("binary") instanceof Binary);
        assertTrue(transported.get("timestamp") instanceof BsonTimestamp);
    }

    @Test
    public void exportsComplexJavascript() throws Exception {
        Path file = workDirectory.resolve("complex_types.js");
        export(SOURCE, "js", file, null);
        String script = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(script, script.contains("ObjectId("));
        assertTrue(script, script.contains("ISODate("));
        assertTrue(script, script.contains("NumberDecimal("));
        assertTrue(script, script.contains("Binary.createFromBase64("));
        assertTrue(script, script.contains("MongoRegex("));
        assertTrue(script, script.contains("Timestamp("));
        assertTrue(script, script.contains("MinKey()"));
        assertTrue(script, script.contains("MaxKey()"));
        assertTrue(script, script.contains("nullValue: null"));
    }

    @Test
    public void runsExportedJavascriptScript() throws Exception {
        Path file = workDirectory.resolve("complex_types_run.js");
        export(SOURCE, "js", file, null);
        rawClient.getDatabase(TARGET_DB).drop();

        ShellMongoRunScriptFileHandler handler =
                new ShellMongoRunScriptFileHandler(client, TARGET_DB);
        handler.setContinueWithErrors(false);
        handler.setFile(file.toFile());
        handler.runFile();

        MongoCollection<Document> target = rawClient.getDatabase(TARGET_DB).getCollection(SOURCE);
        assertEquals(4, target.countDocuments());
        Document imported = target.find(Filters.eq("name", "复杂文档1")).first();
        assertNotNull(imported);
        assertTrue(imported.get("_id") instanceof ObjectId);
        assertTrue(imported.get("nested") instanceof Document);
        assertTrue(imported.get("items") instanceof List<?>);
        assertTrue(imported.get("decimal") instanceof Decimal128);
        assertTrue(imported.get("date") instanceof Date);
        assertTrue(imported.get("oid") instanceof ObjectId);
        assertTrue(imported.get("binary") instanceof Binary);
        assertTrue(imported.get("regex") instanceof Pattern
                || imported.get("regex") instanceof BsonRegularExpression);
        assertTrue(imported.get("timestamp") instanceof BsonTimestamp);
        assertTrue(imported.get("minKey") instanceof MinKey);
        assertTrue(imported.get("maxKey") instanceof MaxKey);
        assertTrue(imported.get("code") instanceof Code);
    }

    @Test
    public void runsLegacyRegExpScript() throws Exception {
        rawClient.getDatabase(TARGET_DB).drop();
        client.executeSingleScript(TARGET_DB,
                "db.getCollection('legacy_regex').insert({name:'legacy',regex:RegExp('^legacy.*','i')});");
        Document document = rawClient.getDatabase(TARGET_DB)
                .getCollection("legacy_regex")
                .find(Filters.eq("name", "legacy")).first();
        assertNotNull(document);
        Object regex = document.get("regex");
        if (regex instanceof Pattern pattern) {
            assertEquals("^legacy.*", pattern.pattern());
            assertTrue((pattern.flags() & Pattern.CASE_INSENSITIVE) != 0);
        } else {
            assertTrue(regex instanceof BsonRegularExpression);
            assertEquals("^legacy.*", ((BsonRegularExpression) regex).getPattern());
        }
    }

    @Test
    public void managesCollectionIndexesAndValidator() {
        MongoCollectionOptions options = client.selectCollectionOptions(DB, SOURCE);
        assertEquals(SOURCE, options.getName());
        assertFalse(options.isCapped());
        assertTrue(client.listCollectionIndexes(DB, SOURCE).stream()
                .anyMatch(MongoIndex::isIdIndex));

        MongoIndex index = new MongoIndex();
        index.setName("idx_complex_types_name");
        index.setKeys(new Document("name", 1));
        index.setUnique(true);
        index.setPartialFilterExpression(new Document("name", new Document("$exists", true)));
        client.createIndex(DB, SOURCE, index);

        MongoIndex created = client.listCollectionIndexes(DB, SOURCE).stream()
                .filter(item -> "idx_complex_types_name".equals(item.getName()))
                .findFirst().orElseThrow();
        assertEquals(1, created.getKeys().get("name"));
        assertTrue(created.isUnique());
        assertEquals(new Document("name", new Document("$exists", true)),
                created.getPartialFilterExpression());

        Document validator = new Document("$jsonSchema", new Document("bsonType", "object")
                .append("required", List.of("name"))
                .append("properties", new Document("name", new Document("bsonType", "string"))));
        options.setValidator(validator);
        options.setValidationLevel("strict");
        options.setValidationAction("error");
        client.updateCollectionOptions(DB, options);

        MongoCollectionOptions updated = client.selectCollectionOptions(DB, SOURCE);
        assertEquals(validator, updated.getValidator());
        assertEquals("strict", updated.getValidationLevel());
        assertEquals("error", updated.getValidationAction());

        client.dropIndex(DB, SOURCE, "idx_complex_types_name");
        assertFalse(client.listCollectionIndexes(DB, SOURCE).stream()
                .anyMatch(item -> "idx_complex_types_name".equals(item.getName())));
    }

    @Test
    public void createsDesignedCollectionWithIndexesAndValidator() {
        String collectionName = "complex_types_design";
        MongoCollectionOptions options = new MongoCollectionOptions();
        options.setDbName(TARGET_DB);
        options.setName(collectionName);
        options.setCapped(true);
        options.setSizeInBytes(1024L * 1024L);
        options.setMaxDocuments(100L);
        options.setValidator(new Document("$jsonSchema", new Document("bsonType", "object")
                .append("required", List.of("name"))
                .append("properties", new Document("name", new Document("bsonType", "string")))));
        options.setValidationLevel("moderate");
        options.setValidationAction("warn");
        client.createCollection(options);

        MongoIndex index = new MongoIndex();
        index.setName("idx_design_name");
        index.setKeys(new Document("name", 1));
        index.setUnique(true);
        client.createIndex(TARGET_DB, collectionName, index);

        assertTrue(client.listCollectionNames(TARGET_DB).contains(collectionName));
        MongoCollectionOptions created = client.selectCollectionOptions(TARGET_DB, collectionName);
        assertTrue(created.isCapped());
        assertEquals(1024L * 1024L, created.getSizeInBytes().longValue());
        assertEquals(100L, created.getMaxDocuments().longValue());
        assertEquals(options.getValidator(), created.getValidator());
        assertEquals("moderate", created.getValidationLevel());
        assertEquals("warn", created.getValidationAction());
        assertTrue(client.listCollectionIndexes(TARGET_DB, collectionName).stream()
                .anyMatch(item -> "idx_design_name".equals(item.getName())));
    }

    @Test
    public void rendersCollectionDesignScript() {
        MongoCollectionOptions options = new MongoCollectionOptions();
        options.setName("complex_types_design");
        options.setCapped(true);
        options.setSizeInBytes(1024L);
        options.setValidator(new Document("name", new Document("$exists", true)));
        options.setValidationLevel("strict");
        options.setValidationAction("error");

        MongoIndex index = new MongoIndex();
        index.setName("idx_design_name");
        index.setKeys(MongoIndex.parseKeys("name:1, nested.city:-1"));
        index.setUnique(true);
        index.setPartialFilterExpression(new Document("name", new Document("$type", "string")));

        String script = ShellMongoCollectionDesignUtil.toCreateCollectionScript(
                options, List.of(index));
        assertTrue(script, script.contains("db.createCollection('complex_types_design'"));
        assertTrue(script, script.contains("\"capped\": true"));
        assertTrue(script, script.contains("\"size\": 1024"));
        assertTrue(script, script.contains("\"validator\""));
        assertTrue(script, script.contains("createIndex("));
        assertTrue(script, script.contains("\"name\": \"idx_design_name\""));
        assertTrue(script, script.contains("\"nested.city\": -1"));
        assertTrue(script, script.contains("\"partialFilterExpression\""));
    }

    private static void updateValue(MongoRecord record, String columnName, Object value) {
        record.putValue(columnName, value);
        record.getProperty(columnName).setChanged(true);
    }

    private static List<MongoRecord> selectSource() {
        try {
            return client.selectCollectionRecords(selectParam(SOURCE));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static MongoColumns selectSourceColumns() {
        try {
            return new MongoColumns(new ArrayList<>(
                    client.selectColumns(selectParam(SOURCE))));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void export(String collectionName, String format, Path file,
                               List<MongoColumn> selectedColumns) throws Exception {
        List<MongoColumn> columns = selectedColumns;
        if (columns == null) {
            columns = new ArrayList<>(client.selectColumns(selectParam(collectionName)));
        }
        ShellMongoDataExportCollection table = new ShellMongoDataExportCollection();
        table.setName(collectionName);
        table.setFilePath(file.toString());
        table.columns(columns);
        ShellMongoDataExportHandler handler = new ShellMongoDataExportHandler(client, DB);
        handler.setFileType(format);
        handler.setQueryLimit(2);
        handler.setTables(List.of(table));
        handler.doExport();
    }

    private static void importFile(String format, Path file, String targetCollection) throws Exception {
        ShellMongoDataImportFile importFile = new ShellMongoDataImportFile();
        importFile.setFile(file.toFile());
        importFile.setTargetTableName(targetCollection);
        ShellMongoDataImportHandler handler = new ShellMongoDataImportHandler(client, DB);
        handler.setFileType(format);
        handler.setFiles(List.of(importFile));
        handler.importMode("2");
        handler.setContinueWithErrors(false);
        handler.doImport();
    }

    private static MongoSelectRecordParam selectParam(String collectionName) {
        MongoSelectRecordParam param = new MongoSelectRecordParam();
        param.setDbName(DB);
        param.setCollectionName(collectionName);
        param.setStart(0L);
        param.setLimit(100L);
        return param;
    }

    private static MongoCollection<Document> rawCollection(String collectionName) {
        return rawClient.getDatabase(DB).getCollection(collectionName);
    }

    private static DBDataTransportObject transportObject(String name) {
        DBDataTransportObject object = new DBDataTransportObject();
        object.setName(name);
        return object;
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

    private static void initToolkit() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit already started by another test.
        }
    }
}
