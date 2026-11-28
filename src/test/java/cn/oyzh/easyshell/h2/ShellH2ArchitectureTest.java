package cn.oyzh.easyshell.h2;

import cn.oyzh.easyshell.controller.connect.h2.ShellAddH2ConnectController;
import cn.oyzh.easyshell.controller.connect.h2.ShellUpdateH2ConnectController;
import cn.oyzh.easyshell.controller.h2.schema.ShellH2SchemaAddController;
import cn.oyzh.easyshell.controller.h2.schema.ShellH2SchemaUpdateController;
import cn.oyzh.easyshell.controller.h2.table.ShellH2TableInfoController;
import cn.oyzh.easyshell.controller.h2.view.ShellH2ViewInfoController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordPrimaryKey;
import cn.oyzh.easyshell.h2.record.H2RecordProperty;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.popups.h2.ShellH2FieldInfoPopupController;
import cn.oyzh.easyshell.popups.h2.ShellH2IndexFieldPopupController;
import cn.oyzh.easyshell.popups.h2.ShellH2RecordFilterPopupController;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeView;
import cn.oyzh.easyshell.trees.h2.function.ShellH2FunctionsTreeItem;
import cn.oyzh.easyshell.trees.h2.function.ShellH2RoutinesTreeItem;
import cn.oyzh.easyshell.trees.h2.sequence.ShellH2SequencesTreeItem;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueriesTreeItem;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueriesTreeItemValue;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueryTreeItem;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueryTreeItemValue;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItemValue;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItemValue;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TablesTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TablesTreeItemValue;
import cn.oyzh.easyshell.trees.h2.terminal.ShellH2TerminalTreeItem;
import cn.oyzh.easyshell.trees.h2.terminal.ShellH2TerminalTreeItemValue;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItemValue;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewsTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewsTreeItemValue;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.PopupAttribute;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * H2 架构与资源完整性测试。
 *
 * @since 2026-11-28
 */
public class ShellH2ArchitectureTest {

    private static final List<String> I18N_RESOURCES = List.of(
            "/i18n_en.properties",
            "/i18n_zh_CN.properties",
            "/i18n_zh_TW.properties",
            "/i18n_de.properties",
            "/i18n_fr_FR.properties",
            "/i18n_it_IT.properties",
            "/i18n_ja_JP.properties",
            "/i18n_ko_KR.properties"
    );

    private static final String[] FXML_RESOURCES = {
            "/fxml/connect/h2/shellAddH2Connect.fxml",
            "/fxml/connect/h2/shellUpdateH2Connect.fxml",
            "/tabs/h2/shellH2Tab.fxml",
            "/tabs/h2/home/shellH2HomeTab.fxml",
            "/tabs/h2/query/shellH2QueryMainTab.fxml",
            "/tabs/h2/query/shellH2QueryInfoTab.fxml",
            "/tabs/h2/query/shellH2QuerySelectTab.fxml",
            "/tabs/h2/query/shellH2QueryExplainTab.fxml",
            "/tabs/h2/terminal/shellH2TerminalTab.fxml",
            "/fxml/h2/schema/shellH2SchemaAdd.fxml",
            "/fxml/h2/schema/shellH2SchemaUpdate.fxml",
            "/tabs/h2/function/shellH2FunctionDesignTab.fxml",
            "/tabs/h2/sequence/shellH2SequenceDesignTab.fxml",
            "/tabs/h2/table/shellH2TableDesignTab.fxml",
            "/tabs/h2/table/shellH2TableColumnExtra.fxml",
            "/tabs/h2/table/shellH2TableRecordTab.fxml",
            "/tabs/h2/view/shellH2ViewDesignTab.fxml",
            "/tabs/h2/view/shellH2ViewRecordTab.fxml",
            "/popups/h2/shellH2RecordFilterPopup.fxml",
            "/popups/h2/shellH2IndexFieldPopup.fxml",
            "/popups/h2/shellH2FieldInfoPopup.fxml",
            "/fxml/h2/data/shellH2DataExport.fxml",
            "/fxml/h2/data/shellH2DataImport.fxml",
            "/fxml/h2/data/shellH2DataDump.fxml",
            "/fxml/h2/data/shellH2DataRunSqlFile.fxml",
            "/fxml/h2/data/shellH2DataTransport.fxml",
            "/fxml/h2/table/shellH2TableInfo.fxml",
            "/fxml/h2/view/shellH2ViewInfo.fxml"
    };

    @Test
    public void testClientTypeRouting() {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.H2);
        connect.setHost("sample.mv.db");
        assertTrue(connect.isH2Type());
        assertTrue(ShellClientUtil.newClient(connect) instanceof ShellH2Client);
    }

    @Test
    public void testConnectionControllerResources() {
        StageAttribute add = ShellAddH2ConnectController.class.getAnnotation(StageAttribute.class);
        StageAttribute update = ShellUpdateH2ConnectController.class.getAnnotation(StageAttribute.class);
        assertNotNull(add);
        assertNotNull(update);
        assertEquals("/fxml/connect/h2/shellAddH2Connect.fxml", add.value());
        assertEquals("/fxml/connect/h2/shellUpdateH2Connect.fxml", update.value());
        assertEquals(StageController.class, ShellAddH2ConnectController.class.getSuperclass());
        assertEquals(StageController.class, ShellUpdateH2ConnectController.class.getSuperclass());
    }

    @Test
    public void testInfoControllerResources() {
        StageAttribute table = ShellH2TableInfoController.class.getAnnotation(StageAttribute.class);
        StageAttribute view = ShellH2ViewInfoController.class.getAnnotation(StageAttribute.class);
        assertNotNull(table);
        assertNotNull(view);
        assertEquals("/fxml/h2/table/shellH2TableInfo.fxml", table.value());
        assertEquals("/fxml/h2/view/shellH2ViewInfo.fxml", view.value());
        assertEquals(StageController.class, ShellH2TableInfoController.class.getSuperclass());
        assertEquals(StageController.class, ShellH2ViewInfoController.class.getSuperclass());
        // H2 不支持 SQL 函数设计器，不提供函数信息控制器
        assertEquals(0, (long) Arrays.stream(ShellH2ArchitectureTest.class.getClasses())
                .filter(clazz -> clazz.getSimpleName().contains("FunctionInfoController"))
                .count());
    }

    @Test
    public void testEventHandlingInfrastructure() {
        assertTrue(FXEventListener.class.isAssignableFrom(cn.oyzh.easyshell.tabs.h2.ShellH2TabPane.class));
        long handlers = Arrays.stream(cn.oyzh.easyshell.tabs.h2.ShellH2TabPane.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(cn.oyzh.event.EventSubscribe.class))
                .count();
        assertTrue("H2 tab event handlers are missing", handlers >= 9);
        assertTrue(LLMTabCapability.class.isAssignableFrom(cn.oyzh.easyshell.tabs.h2.ShellH2Tab.class));
        assertNotNull(ShellH2ArchitectureTest.class.getResourceAsStream("/llm/h2_tool.txt"));
    }

    @Test
    public void testH2I18nKeysAreCompleteForAllLocales() throws Exception {
        Set<String> english = this.h2Keys("/i18n_en.properties");
        for (String resource : I18N_RESOURCES) {
            assertEquals(resource, english, this.h2Keys(resource));
        }
        assertFalse("shell.h2 i18n keys are missing", english.isEmpty());
    }

    @Test
    public void testH2I18nDoesNotContainSqliteSpecificText() throws Exception {
        for (String resource : I18N_RESOURCES) {
            Properties properties = new Properties();
            try (InputStream input = ShellH2ArchitectureTest.class.getResourceAsStream(resource)) {
                assertNotNull(input);
                properties.load(input);
            }
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("shell.h2.")) {
                    String value = properties.getProperty(key);
                    assertFalse(resource + ":" + key,
                            value.toLowerCase(java.util.Locale.ROOT).contains("sqlite"));
                }
            }
            assertFalse(properties.containsKey("shell.h2.busyTimeout"));
            assertFalse(properties.containsKey("shell.h2.journalMode"));
            assertFalse(properties.containsKey("shell.h2.withoutRowid"));
            assertFalse(properties.containsKey("shell.h2.strict"));
        }
    }

    @Test
    public void testConnectionFormsKeepTheSameLayout() throws Exception {
        String add = this.resourceText("/fxml/connect/h2/shellAddH2Connect.fxml")
                .replace("ShellAddH2ConnectController", "H2ConnectController")
                .replace("onAction=\"#add\"", "onAction=\"#submit\"");
        String update = this.resourceText("/fxml/connect/h2/shellUpdateH2Connect.fxml")
                .replace("ShellUpdateH2ConnectController", "H2ConnectController")
                .replace("onAction=\"#update\"", "onAction=\"#submit\"");
        assertEquals(add, update);
    }

    @Test
    public void testPopupControllersUseH2Resources() {
        assertEquals("/popups/h2/shellH2FieldInfoPopup.fxml",
                ShellH2FieldInfoPopupController.class.getAnnotation(PopupAttribute.class).value());
        assertEquals("/popups/h2/shellH2IndexFieldPopup.fxml",
                ShellH2IndexFieldPopupController.class.getAnnotation(PopupAttribute.class).value());
        assertEquals("/popups/h2/shellH2RecordFilterPopup.fxml",
                ShellH2RecordFilterPopupController.class.getAnnotation(PopupAttribute.class).value());
    }

    @Test
    public void testConnectionFormSupportsFileAndTcpModes() throws Exception {
        for (String resource : List.of(
                "/fxml/connect/h2/shellAddH2Connect.fxml",
                "/fxml/connect/h2/shellUpdateH2Connect.fxml")) {
            String content = this.resourceText(resource);
            assertTrue(resource, content.contains("fx:id=\"modeFile\""));
            assertFalse(resource, content.contains("fx:id=\"modeMem\""));
            assertTrue(resource, content.contains("fx:id=\"modeTcp\""));
            assertTrue(resource, content.contains("fx:id=\"filePane\""));
            assertFalse(resource, content.contains("fx:id=\"memPane\""));
            assertTrue(resource, content.contains("fx:id=\"tcpPane\""));
            assertTrue(resource, content.contains("fx:id=\"filePath\""));
            assertFalse(resource, content.contains("fx:id=\"memName\""));
            assertTrue(resource, content.contains("fx:id=\"host\""));
            assertTrue(resource, content.contains("fx:id=\"port\""));
            assertTrue(resource, content.contains("fx:id=\"schema\""));
            assertTrue(resource, content.contains("fx:id=\"createIfMissing\""));
            assertTrue(resource, content.contains("fx:id=\"readOnly\""));
        }
    }

    @Test
    public void testDatabaseRootTreeShape() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-h2-tree-shape-");
        Path file = directory.resolve("tree-shape");
        ShellConnect connect = new ShellConnect();
        connect.setId("h2-tree-shape-test");
        connect.setName("tree-shape");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start();
        CountDownLatch startup = new CountDownLatch(1);
        try {
            try {
                Platform.startup(startup::countDown);
            } catch (IllegalStateException ex) {
                startup.countDown();
            }
            assertTrue(startup.await(10, TimeUnit.SECONDS));
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch loaded = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellH2TreeView treeView = new ShellH2TreeView();
                    treeView.setClient(client);
                    ShellH2RootTreeItem root = treeView.root();
                    root.loadChild();
                    assertTrue(root.getChildren().stream()
                            .allMatch(ShellH2SchemaTreeItem.class::isInstance));
                    ShellH2SchemaTreeItem schema = root.schemaGroup("PUBLIC");
                    assertEquals("PUBLIC", schema.schema());
                    schema.loadChild();
                    assertEquals(9, schema.getChildren().size());
                    assertEquals(7, schema.getMenuItems().size());
                    assertTrue(schema.getChildren().get(0) instanceof ShellH2TablesTreeItem);
                    assertTrue(schema.getChildren().get(1) instanceof ShellH2ViewsTreeItem);
                    assertTrue(schema.getChildren().get(7) instanceof ShellH2QueriesTreeItem);
                    assertTrue(schema.getChildren().get(8) instanceof ShellH2TerminalTreeItem);
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    loaded.countDown();
                }
            });
            assertTrue(loaded.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Database root tree shape failed", error.get());
            }
        } finally {
            client.close();
            Files.walk(directory).sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    public void testFindTableDoesNotLoadOtherSchemas() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-h2-find-table-");
        Path file = directory.resolve("find-table");
        ShellConnect connect = new ShellConnect();
        connect.setId("h2-find-table-test");
        connect.setName("find-table");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start();
        try {
            for (int i = 0; i < 35; i++) {
                client.executeSqlSimple("CREATE TABLE T_" + i + "(ID INT PRIMARY KEY)");
            }
            CountDownLatch startup = new CountDownLatch(1);
            try {
                Platform.startup(startup::countDown);
            } catch (IllegalStateException ex) {
                startup.countDown();
            }
            assertTrue(startup.await(10, TimeUnit.SECONDS));
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch loaded = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellH2TreeView treeView = new ShellH2TreeView();
                    treeView.setClient(client);
                    ShellH2RootTreeItem root = treeView.root();
                    root.loadChild();
                    ShellH2SchemaTreeItem publicSchema = root.schemaGroup("PUBLIC");
                    ShellH2SchemaTreeItem systemSchema = root.schemaGroup("INFORMATION_SCHEMA");
                    publicSchema.loadChild();
                    ShellH2TablesTreeItem tables =
                            (ShellH2TablesTreeItem) publicSchema.getChildren().get(0);
                    tables.loadChild();
                    String lastTable = ((ShellH2TableTreeItem) tables.getChildren().getLast()).tableName();

                    assertNotNull(treeView.findTableItem("PUBLIC", lastTable));
                    assertFalse(systemSchema.isLoaded());
                    assertTrue(systemSchema.getChildren().isEmpty());
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    loaded.countDown();
                }
            });
            assertTrue(loaded.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Finding a table must not load other schemas", error.get());
            }
        } finally {
            client.close();
            Files.walk(directory).sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    public void testSimpleViewRecordEditsWriteThroughBaseTable() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-h2-view-editable-");
        Path file = directory.resolve("view-editable");
        ShellConnect connect = new ShellConnect();
        connect.setId("h2-view-editable-test");
        connect.setName("view-editable");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE VIEW_EDIT("
                    + "ID INT PRIMARY KEY, NAME VARCHAR(40), NOTE VARCHAR(40), "
                    + "TITLE_UPPER VARCHAR(40) GENERATED ALWAYS AS (UPPER(NAME)))");
            client.executeSqlSimple("INSERT INTO VIEW_EDIT(ID, NAME, NOTE) "
                    + "VALUES(1, 'before', 'keep')");
            client.executeSqlSimple("CREATE VIEW VIEW_EDIT_SIMPLE AS "
                    + "SELECT ID AS VIEW_ID, NAME, TITLE_UPPER FROM VIEW_EDIT");

            CountDownLatch startup = new CountDownLatch(1);
            try {
                Platform.startup(startup::countDown);
            } catch (IllegalStateException ex) {
                startup.countDown();
            }
            assertTrue(startup.await(10, TimeUnit.SECONDS));
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch loaded = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellH2TreeView treeView = new ShellH2TreeView();
                    treeView.setClient(client);
                    ShellH2RootTreeItem root = treeView.root();
                    root.loadChild();
                    ShellH2SchemaTreeItem schema = root.schemaGroup("PUBLIC");
                    schema.loadChild();
                    ShellH2ViewsTreeItem views =
                            (ShellH2ViewsTreeItem) schema.getChildren().get(1);
                    views.loadChild();
                    ShellH2ViewTreeItem view = views.richChildren().stream()
                            .filter(ShellH2ViewTreeItem.class::isInstance)
                            .map(ShellH2ViewTreeItem.class::cast)
                            .filter(item -> "VIEW_EDIT_SIMPLE".equalsIgnoreCase(item.viewName()))
                            .findFirst().orElseThrow();

                    H2Columns viewColumns = view.columns();
                    assertTrue(view.isUpdatable());
                    H2Column viewId = viewColumns.column("VIEW_ID");
                    H2Column viewName = viewColumns.column("NAME");
                    H2Column titleUpper = viewColumns.column("TITLE_UPPER");
                    assertTrue(viewId.isPrimaryKey());
                    assertTrue(titleUpper.isGenerated());

                    H2SelectRecordParam page = new H2SelectRecordParam();
                    page.setDbName("PUBLIC");
                    page.setTableName("VIEW_EDIT_SIMPLE");
                    page.setColumns(viewColumns);
                    page.setReadonly(false);
                    H2Record row = client.selectRecords(page).getFirst();
                    assertTrue(row.getProperties().get(titleUpper.getName()).isReadonly());
                    H2RecordPrimaryKey primaryKey = new H2RecordPrimaryKey();
                    primaryKey.init(viewId, row);
                    H2RecordProperty nameProperty = row.getProperties().get(viewName.getName());
                    nameProperty.set("after");
                    nameProperty.setChanged(true);
                    assertEquals(1, view.updateRecord(row.getChangedRecordData(), primaryKey));

                    H2Record created = new H2Record(viewColumns);
                    created.putValue(viewId, 2);
                    created.putValue(viewName, "inserted");
                    assertEquals(1, view.insertRecord(created.getRecordData(), null));

                    H2RecordPrimaryKey insertedKey = new H2RecordPrimaryKey();
                    insertedKey.setColumnName("VIEW_ID");
                    insertedKey.setData(2);
                    insertedKey.setOriginalData(2);
                    assertEquals(1, view.deleteRecord(insertedKey));

                    List<H2Column> baseColumns = client.selectColumns("VIEW_EDIT");
                    List<java.util.Map<String, Object>> records =
                            client.selectRecords("VIEW_EDIT", baseColumns, 0, 10);
                    assertEquals(1, records.size());
                    assertEquals("after", records.getFirst().get("NAME"));
                    assertEquals("keep", records.getFirst().get("NOTE"));
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    loaded.countDown();
                }
            });
            assertTrue(loaded.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Simple view record editing failed", error.get());
            }
        } finally {
            client.close();
            Files.walk(directory).sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    public void testGroupNodesLoadOnDoubleClick() throws Exception {
        assertNotNull(ShellH2SchemaTreeItem.class.getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellH2TablesTreeItem.class.getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellH2ViewsTreeItem.class.getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellH2QueriesTreeItem.class.getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellH2FunctionsTreeItem.class.getMethod(
                "addFunction", H2Routine.class));
        assertNotNull(ShellH2RoutinesTreeItem.class.getMethod(
                "addProcedure", H2Routine.class));
        assertNotNull(cn.oyzh.easyshell.tabs.h2.ShellH2TabPane.class.getMethod(
                "openFunctionDesign", H2Routine.class, ShellH2FunctionsTreeItem.class));
        assertNotNull(cn.oyzh.easyshell.tabs.h2.ShellH2TabPane.class.getMethod(
                "openRoutineDesign", H2Routine.class, ShellH2RoutinesTreeItem.class));
        assertNotNull(ShellH2SequencesTreeItem.class.getMethod(
                "addSequence", H2Sequence.class));
        assertNotNull(cn.oyzh.easyshell.tabs.h2.ShellH2TabPane.class.getMethod(
                "openSequenceDesign", H2Sequence.class, ShellH2SequencesTreeItem.class));
        assertNotNull(cn.oyzh.easyshell.util.h2.ShellH2ViewFactory.class.getMethod(
                "exportData", ShellH2Client.class, String.class, String.class,
                int.class, cn.oyzh.easyshell.data.h2.dto.H2DataExportTable.class));
    }

    @Test
    public void testH2QueryExportUsesSchemaInsteadOfSqliteMain() throws Exception {
        String content = this.resourceText("/fxml/h2/data/shellH2DataExport.fxml");
        assertTrue(content.contains("fx:id=\"database\""));
        assertFalse(content.contains("text=\"main\""));
        assertNotNull(ShellH2RootTreeItem.class.getMethod(
                "executeSql", String.class, String.class));
    }

    @Test
    public void testSchemaPagesAlignOracleStructure() throws Exception {
        StageAttribute add = ShellH2SchemaAddController.class.getAnnotation(StageAttribute.class);
        StageAttribute update = ShellH2SchemaUpdateController.class.getAnnotation(StageAttribute.class);
        assertNotNull(add);
        assertNotNull(update);
        assertEquals("/fxml/h2/schema/shellH2SchemaAdd.fxml", add.value());
        assertEquals("/fxml/h2/schema/shellH2SchemaUpdate.fxml", update.value());
        assertEquals(StageController.class, ShellH2SchemaAddController.class.getSuperclass());
        assertEquals(StageController.class, ShellH2SchemaUpdateController.class.getSuperclass());

        for (String resource : List.of(
                "/fxml/h2/schema/shellH2SchemaAdd.fxml",
                "/fxml/h2/schema/shellH2SchemaUpdate.fxml")) {
            String content = this.resourceText(resource);
            assertTrue(resource, content.contains("realWidth=\"750\""));
            assertTrue(resource, content.contains("realHeight=\"300\""));
            assertTrue(resource, content.contains("FXHeaderBar"));
            assertTrue(resource, content.contains("InfoLabel"));
            assertTrue(resource, content.contains("fx:id=\"name\""));
            assertTrue(resource, content.contains("fx:id=\"owner\""));
            assertTrue(resource, content.contains("SubmitButton"));
            assertTrue(resource, content.contains("CancelButton"));
            assertFalse(resource, content.contains("fx:id=\"password\""));
        }
    }

    @Test
    public void testTreePackagesAreAlignedWithSqlite() {
        assertEquals("cn.oyzh.easyshell.trees.h2.root",
                ShellH2RootTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.table",
                ShellH2TablesTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.table",
                ShellH2TableTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.view",
                ShellH2ViewsTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.view",
                ShellH2ViewTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.query",
                ShellH2QueriesTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.query",
                ShellH2QueryTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.terminal",
                ShellH2TerminalTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2",
                ShellH2TreeView.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.root",
                ShellH2RootTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.table",
                ShellH2TablesTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.table",
                ShellH2TableTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.view",
                ShellH2ViewsTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.view",
                ShellH2ViewTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.query",
                ShellH2QueriesTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.query",
                ShellH2QueryTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.h2.terminal",
                ShellH2TerminalTreeItemValue.class.getPackageName());
    }

    @Test
    public void testTreeNodesAreStructuredLikeSqlite() throws Exception {
        assertEquals(ShellH2TablesTreeItem.class,
                ShellH2TableTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellH2ViewsTreeItem.class,
                ShellH2ViewTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellH2QueriesTreeItem.class,
                ShellH2QueryTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellH2SchemaTreeItem.class,
                ShellH2TerminalTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellH2SchemaTreeItem.class,
                ShellH2QueriesTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellH2SchemaTreeItem.class,
                ShellH2TablesTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellH2SchemaTreeItem.class,
                ShellH2ViewsTreeItem.class.getMethod("parent").getReturnType());

        assertNotNull(ShellH2TableTreeItem.class
                .getConstructor(H2Table.class, cn.oyzh.fx.gui.tree.view.RichTreeView.class));
        assertNotNull(ShellH2ViewTreeItem.class
                .getConstructor(H2View.class, cn.oyzh.fx.gui.tree.view.RichTreeView.class));
        assertNotNull(ShellH2QueryTreeItem.class
                .getConstructor(cn.oyzh.easyshell.domain.ShellQuery.class,
                        cn.oyzh.fx.gui.tree.view.RichTreeView.class));

        assertNotNull(ShellH2TablesTreeItem.class.getMethod("addTable", H2Table.class));
        assertNotNull(ShellH2ViewsTreeItem.class.getMethod("addView", H2View.class));
        assertNotNull(ShellH2QueriesTreeItem.class.getMethod("addQuery",
                cn.oyzh.easyshell.domain.ShellQuery.class));
    }

    @Test
    public void testNewTableAndViewAppendToLoadedGroupsWithoutRebuildingTree() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-h2-object-add-");
        Path file = directory.resolve("object-add");
        ShellConnect connect = new ShellConnect();
        connect.setId("h2-object-add-test");
        connect.setName("object-add");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE item (id INT PRIMARY KEY)");
            client.executeSqlSimple("CREATE VIEW old_view AS SELECT \"ID\" FROM item");
            CountDownLatch startup = new CountDownLatch(1);
            try {
                Platform.startup(startup::countDown);
            } catch (IllegalStateException ex) {
                startup.countDown();
            }
            assertTrue(startup.await(10, TimeUnit.SECONDS));
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch loaded = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellH2TreeView treeView = new ShellH2TreeView();
                    treeView.setClient(client);
                    ShellH2RootTreeItem root = treeView.root();
                    root.loadChild();
                    ShellH2SchemaTreeItem schema =
                            (ShellH2SchemaTreeItem) root.unfilteredChildren().get(0);
                    schema.loadChild();
                    ShellH2TablesTreeItem tableGroup =
                            (ShellH2TablesTreeItem) schema.unfilteredChildren().get(0);
                    ShellH2ViewsTreeItem viewGroup =
                            (ShellH2ViewsTreeItem) schema.unfilteredChildren().get(1);
                    tableGroup.loadChild();
                    viewGroup.loadChild();
                    assertEquals(1, tableGroup.unfilteredChildrenSize());
                    assertEquals(1, viewGroup.unfilteredChildrenSize());

                    client.executeSqlSimple("CREATE VIEW new_view AS SELECT \"ID\" FROM item");
                    root.addView("PUBLIC", "new_view");
                    client.executeSqlSimple("CREATE TABLE new_table (id INT PRIMARY KEY)");
                    root.addTable("PUBLIC", "new_table");

                    assertSame(tableGroup, schema.unfilteredChildren().get(0));
                    assertSame(viewGroup, schema.unfilteredChildren().get(1));
                    assertEquals(2, tableGroup.unfilteredChildrenSize());
                    assertTrue(tableGroup.unfilteredChildren().stream()
                            .filter(ShellH2TableTreeItem.class::isInstance)
                            .map(ShellH2TableTreeItem.class::cast)
                            // INFORMATION_SCHEMA 返回大写名，忽略大小写匹配
                            .anyMatch(item -> "new_table".equalsIgnoreCase(item.tableName())));
                    assertEquals(2, viewGroup.unfilteredChildrenSize());
                    assertTrue(viewGroup.unfilteredChildren().stream()
                            .filter(ShellH2ViewTreeItem.class::isInstance)
                            .map(ShellH2ViewTreeItem.class::cast)
                            .anyMatch(item -> "new_view".equalsIgnoreCase(item.viewName())));

                    root.addView("PUBLIC", "new_view");
                    root.addTable("PUBLIC", "new_table");
                    assertEquals(2, tableGroup.unfilteredChildrenSize());
                    assertEquals(2, viewGroup.unfilteredChildrenSize());
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    loaded.countDown();
                }
            });
            assertTrue(loaded.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Adding a table or view must preserve the loaded tree nodes", error.get());
            }
        } finally {
            client.close();
            Files.walk(directory).sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    public void testQueryGroupUpsertsAndRemovesNodesWithoutRebuildingTree() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-h2-query-tree-");
        Path file = directory.resolve("query-tree");
        ShellConnect connect = new ShellConnect();
        connect.setId("h2-query-tree-test");
        connect.setName("query-tree");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start();
        CountDownLatch startup = new CountDownLatch(1);
        try {
            try {
                Platform.startup(startup::countDown);
            } catch (IllegalStateException ex) {
                startup.countDown();
            }
            assertTrue(startup.await(10, TimeUnit.SECONDS));
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch loaded = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    ShellH2TreeView treeView = new ShellH2TreeView();
                    treeView.setClient(client);
                    ShellH2RootTreeItem root = treeView.root();
                    root.loadChild();
                    ShellH2SchemaTreeItem schema = root.schemaGroup("PUBLIC");
                    schema.loadChild();
                    ShellH2QueriesTreeItem queryGroup =
                            (ShellH2QueriesTreeItem) schema.unfilteredChildren().get(7);

                    cn.oyzh.easyshell.domain.ShellQuery query = new cn.oyzh.easyshell.domain.ShellQuery();
                    query.setUid("query-1");
                    query.setName("first");
                    query.setDbName("PUBLIC");
                    root.upsertQuery(query);

                    assertSame(queryGroup, schema.unfilteredChildren().get(7));
                    assertEquals(1, queryGroup.unfilteredChildrenSize());
                    ShellH2QueryTreeItem item =
                            (ShellH2QueryTreeItem) queryGroup.unfilteredChildren().getFirst();
                    assertEquals("first", item.queryName());

                    cn.oyzh.easyshell.domain.ShellQuery renamed = new cn.oyzh.easyshell.domain.ShellQuery();
                    renamed.setUid("query-1");
                    renamed.setName("renamed");
                    renamed.setContent("SELECT 2");
                    renamed.setDbName("PUBLIC");
                    root.upsertQuery(renamed);
                    assertEquals(1, queryGroup.unfilteredChildrenSize());
                    assertEquals("renamed", item.queryName());
                    assertEquals("SELECT 2", item.value().getContent());

                    root.removeQuery(query);
                    assertEquals(0, queryGroup.unfilteredChildrenSize());
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    loaded.countDown();
                }
            });
            assertTrue(loaded.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Query tree updates must not rebuild the root", error.get());
            }
        } finally {
            client.close();
            Files.walk(directory).sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    public void testH2TablePagesMatchSqliteStructure() throws Exception {
        String design = this.resourceText("/tabs/h2/table/shellH2TableDesignTab.fxml");
        assertTrue(design.contains("DBStatusTableView"));
        assertTrue(design.contains("fx:id=\"columnTable\""));
        assertTrue(design.contains("fx:id=\"indexTable\""));
        assertTrue(design.contains("fx:id=\"triggerTable\""));
        assertTrue(design.contains("fx:id=\"foreignKeyTable\""));
        assertTrue(design.contains("fx:id=\"checkTable\""));
        assertTrue(design.contains("fx:id=\"preview\""));
        assertTrue(design.contains("#doMoveUp"));
        assertTrue(design.contains("#doMoveDown"));
        assertTrue(design.contains("#refresh"));
        assertTrue(design.contains("valueName=\"commentControl\""));

        String extra = this.resourceText("/tabs/h2/table/shellH2TableColumnExtra.fxml");
        assertTrue(extra.contains("fx:id=\"defaultValue\""));
        assertTrue(extra.contains("fx:id=\"autoIncrement\""));
        assertTrue(extra.contains("fx:id=\"check\""));
        assertTrue(extra.contains("fx:id=\"generatedExpression\""));
        assertFalse(extra.contains("generatedStorage"));
        assertFalse(extra.contains("fx:id=\"comment\""));

        assertFalse(design.contains("WITHOUT ROWID"));
        assertFalse(design.contains("fx:id=\"strict\""));
        assertFalse(design.contains("valueName=\"whereControl\""));
        assertFalse(design.contains("valueName=\"methodControl\""));
        assertTrue(design.contains("valueName=\"typeControl\" text=\"%base.indexType\""));
        assertTrue(design.contains("valueName=\"uniqueControl\" text=\"%shell.h2.unique\""));
        assertTrue(design.contains("valueName=\"digitsControl\""));

        String records = this.resourceText("/tabs/h2/table/shellH2TableRecordTab.fxml");
        assertTrue(records.contains("PageBox"));
        assertTrue(records.contains("ShellH2RecordTableView"));
        assertTrue(records.contains("fx:id=\"apply\""));
        assertTrue(records.contains("fx:id=\"discard\""));

        String viewDesign = this.resourceText("/tabs/h2/view/shellH2ViewDesignTab.fxml");
        assertTrue(viewDesign.contains("ShellH2QueryEditor"));
        assertTrue(viewDesign.contains("#refresh"));
        assertTrue(viewDesign.contains("fx:id=\"definitionTab\""));

        String query = this.resourceText("/tabs/h2/query/shellH2QueryMainTab.fxml");
        assertTrue(query.contains("ShellH2QueryInfoTab"));
        assertTrue(query.contains("fx:id=\"infoTab\""));
        String querySelect = this.resourceText("/tabs/h2/query/shellH2QuerySelectTab.fxml");
        assertTrue(querySelect.contains("ShellH2RecordTableView"));
        assertTrue(querySelect.contains("fx:id=\"apply\""));
        // H2 支持 EXPLAIN PLAN FOR，保留 explain 页签
        assertTrue(query.contains("ExplainSVGGlyph"));
    }

    @Test
    public void testDataToolsUseWizardLayout() throws Exception {
        for (String resource : List.of(
                "/fxml/h2/data/shellH2DataExport.fxml",
                "/fxml/h2/data/shellH2DataImport.fxml")) {
            String content = this.resourceText(resource);
            assertTrue(resource, content.contains("FXHeaderBar"));
            assertTrue(resource, content.contains("fx:id=\"step1\""));
            assertTrue(resource, content.contains("fx:id=\"step5\""));
        }
        String dump = this.resourceText("/fxml/h2/data/shellH2DataDump.fxml");
        assertTrue(dump.contains("FXHeaderBar"));
        assertTrue(dump.contains("DBDataDumpTypeComboBox"));

        String runSql = this.resourceText("/fxml/h2/data/shellH2DataRunSqlFile.fxml");
        assertTrue(runSql.contains("FXHeaderBar"));
        assertTrue(runSql.contains("ChooseFileTextField"));

        String transport = this.resourceText("/fxml/h2/data/shellH2DataTransport.fxml");
        assertTrue(transport.contains("FXHeaderBar"));
        assertTrue(transport.contains("DBDataTransportNameListView"));
        assertTrue(transport.contains("ShellConnectTextField filterMode=\"h2\" fx:id=\"sourceInfo\""));
        assertTrue(transport.contains("ShellConnectTextField filterMode=\"h2\" fx:id=\"targetInfo\""));
    }

    private String resourceText(String resource) throws Exception {
        try (InputStream input = ShellH2ArchitectureTest.class.getResourceAsStream(resource)) {
            assertNotNull("Missing resource: " + resource, input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Set<String> h2Keys(String resource) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = ShellH2ArchitectureTest.class.getResourceAsStream(resource)) {
            assertNotNull("Missing i18n resource: " + resource, input);
            properties.load(input);
        }
        return properties.stringPropertyNames().stream()
                .filter(key -> key.startsWith("shell.h2."))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    public void testFxmlResourcesAreWellFormed() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException ignored) {
            // Oracle JAXP in the runtime classpath does not expose this Xerces feature.
        }
        factory.setExpandEntityReferences(false);
        for (String resource : FXML_RESOURCES) {
            try (InputStream input = ShellH2ArchitectureTest.class.getResourceAsStream(resource)) {
                assertNotNull("Missing FXML resource: " + resource, input);
                Document document = factory.newDocumentBuilder().parse(input);
                assertNotNull(document.getDocumentElement());
            }
        }
    }

    @Test
    public void testFxmlResourcesLoadWithControllers() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(10, TimeUnit.SECONDS));
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                for (String resource : FXML_RESOURCES) {
                    URL url = ShellH2ArchitectureTest.class.getResource(resource);
                    FXMLLoader loader = new FXMLLoader(url);
                    loader.setResources(I18nResourceBundle.INSTANCE);
                    loader.load();
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("FXML load failed", error.get());
        }
    }
}
