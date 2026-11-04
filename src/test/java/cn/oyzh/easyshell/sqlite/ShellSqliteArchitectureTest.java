package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.controller.connect.sqlite.ShellAddSqliteConnectController;
import cn.oyzh.easyshell.controller.connect.sqlite.ShellUpdateSqliteConnectController;
import cn.oyzh.easyshell.controller.sqlite.function.ShellSqliteFunctionInfoController;
import cn.oyzh.easyshell.controller.sqlite.table.ShellSqliteTableInfoController;
import cn.oyzh.easyshell.controller.sqlite.view.ShellSqliteViewInfoController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.fx.sqlite.SQLiteTableSelectTextField;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumnControl;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTab;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTabPane;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionsTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.query.ShellSqliteQueriesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.query.ShellSqliteQueriesTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.query.ShellSqliteQueryTreeItem;
import cn.oyzh.easyshell.trees.sqlite.query.ShellSqliteQueryTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTablesTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.terminal.ShellSqliteTerminalTreeItem;
import cn.oyzh.easyshell.trees.sqlite.terminal.ShellSqliteTerminalTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItemValue;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewsTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewsTreeItemValue;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.db.ui.DBFiledTypeComboBox;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
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
 * SQLite 架构与资源完整性测试。
 *
 * @since 2026-11-02
 */
public class ShellSqliteArchitectureTest {

    private static final String[] FXML_RESOURCES = {
            "/fxml/connect/sqlite/shellAddSqliteConnect.fxml",
            "/fxml/connect/sqlite/shellUpdateSqliteConnect.fxml",
            "/tabs/sqlite/shellSqliteTab.fxml",
            "/tabs/sqlite/home/shellSqliteHomeTab.fxml",
            "/tabs/sqlite/query/shellSqliteQueryMainTab.fxml",
            "/tabs/sqlite/query/shellSqliteQueryInfoTab.fxml",
            "/tabs/sqlite/query/shellSqliteQuerySelectTab.fxml",
            "/tabs/sqlite/query/shellSqliteQueryExplainTab.fxml",
            "/tabs/sqlite/terminal/shellSqliteTerminalTab.fxml",
            "/tabs/sqlite/table/shellSqliteTableDesignTab.fxml",
            "/tabs/sqlite/table/shellSqliteTableColumnExtra.fxml",
            "/tabs/sqlite/table/shellSqliteTableRecordTab.fxml",
            "/tabs/sqlite/view/shellSqliteViewDesignTab.fxml",
            "/tabs/sqlite/view/shellSqliteViewRecordTab.fxml",
            "/tabs/sqlite/function/shellSqliteFunctionDesignTab.fxml",
            "/popups/sqlite/shellSqliteRecordFilterPopup.fxml",
            "/popups/sqlite/shellSqliteIndexFieldPopup.fxml",
            "/fxml/sqlite/data/shellSqliteDataExport.fxml",
            "/fxml/sqlite/data/shellSqliteDataImport.fxml",
            "/fxml/sqlite/data/shellSqliteDataDump.fxml",
            "/fxml/sqlite/data/shellSqliteDataRunSqlFile.fxml",
            "/fxml/sqlite/data/shellSqliteDataTransport.fxml",
            "/fxml/sqlite/table/shellSqliteTableInfo.fxml",
            "/fxml/sqlite/view/shellSqliteViewInfo.fxml",
            "/fxml/sqlite/function/shellSqliteFunctionInfo.fxml"
    };

    @Test
    public void testClientTypeRouting() {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.SQLITE);
        connect.setHost("sample.db");
        assertTrue(connect.isSqliteType());
        assertTrue(ShellClientUtil.newClient(connect) instanceof ShellSqliteClient);
    }

    @Test
    public void testConnectionControllerResources() {
        StageAttribute add = ShellAddSqliteConnectController.class.getAnnotation(StageAttribute.class);
        StageAttribute update = ShellUpdateSqliteConnectController.class.getAnnotation(StageAttribute.class);
        assertNotNull(add);
        assertNotNull(update);
        assertEquals("/fxml/connect/sqlite/shellAddSqliteConnect.fxml", add.value());
        assertEquals("/fxml/connect/sqlite/shellUpdateSqliteConnect.fxml", update.value());
        assertEquals(StageController.class, ShellAddSqliteConnectController.class.getSuperclass());
        assertEquals(StageController.class, ShellUpdateSqliteConnectController.class.getSuperclass());
    }

    @Test
    public void testInfoControllerResources() {
        StageAttribute table = ShellSqliteTableInfoController.class.getAnnotation(StageAttribute.class);
        StageAttribute view = ShellSqliteViewInfoController.class.getAnnotation(StageAttribute.class);
        StageAttribute function = ShellSqliteFunctionInfoController.class.getAnnotation(StageAttribute.class);
        assertNotNull(table);
        assertNotNull(view);
        assertNotNull(function);
        assertEquals("/fxml/sqlite/table/shellSqliteTableInfo.fxml", table.value());
        assertEquals("/fxml/sqlite/view/shellSqliteViewInfo.fxml", view.value());
        assertEquals("/fxml/sqlite/function/shellSqliteFunctionInfo.fxml", function.value());
        assertEquals(StageController.class, ShellSqliteTableInfoController.class.getSuperclass());
        assertEquals(StageController.class, ShellSqliteViewInfoController.class.getSuperclass());
        assertEquals(StageController.class, ShellSqliteFunctionInfoController.class.getSuperclass());
    }

    @Test
    public void testEventHandlingInfrastructure() {
        assertTrue(FXEventListener.class.isAssignableFrom(ShellSqliteTabPane.class));
        long handlers = Arrays.stream(ShellSqliteTabPane.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventSubscribe.class))
                .count();
        assertTrue("SQLite tab event handlers are missing", handlers >= 9);
        assertTrue(LLMTabCapability.class.isAssignableFrom(ShellSqliteTab.class));
        assertNotNull(ShellSqliteArchitectureTest.class.getResourceAsStream("/llm/sqlite_tool.txt"));
    }

    @Test
    public void testSqliteI18nKeysAreCompleteForAllFallbacks() throws Exception {
        Set<String> english = this.sqliteKeys("/i18n_en.properties");
        assertEquals(english, this.sqliteKeys("/i18n.properties"));
        assertEquals(english, this.sqliteKeys("/i18n_zh_CN.properties"));
    }

    @Test
    public void testConnectionFormUsesGroupedOptions() throws Exception {
        for (String resource : List.of(
                "/fxml/connect/sqlite/shellAddSqliteConnect.fxml",
                "/fxml/connect/sqlite/shellUpdateSqliteConnect.fxml")) {
            String content;
            try (InputStream input = ShellSqliteArchitectureTest.class.getResourceAsStream(resource)) {
                assertNotNull(input);
                content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertFalse(content.contains("#openDatabase"));
            assertFalse(content.contains("#createDatabase"));
            assertFalse(content.contains("%shell.sqlite.open"));
            assertFalse(content.contains("%shell.sqlite.create\""));
            assertTrue(content.contains("text=\"%base.options\""));
            assertTrue(content.contains("fx:id=\"readOnly\""));
            assertTrue(content.contains("fx:id=\"createIfMissing\""));
        }
    }

    @Test
    public void testDatabaseRootTreeShape() throws Exception {
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
                ShellSqliteTreeView treeView = new ShellSqliteTreeView();
                ShellSqliteRootTreeItem root = treeView.root();
                assertEquals("main", root.getValue().name());
                root.loadChild();
                assertEquals(5, root.getChildren().size());
                assertNotNull(treeView.getItemFilter());
                treeView.getItemFilter().setKw("item");
                assertTrue(root.getChildren().get(0) instanceof ShellSqliteTablesTreeItem);
                assertTrue(root.getChildren().get(1) instanceof ShellSqliteViewsTreeItem);
                assertTrue(root.getChildren().get(2) instanceof ShellSqliteFunctionsTreeItem);
                assertTrue(root.getChildren().get(3) instanceof ShellSqliteQueriesTreeItem);
                assertTrue(root.getChildren().get(4) instanceof ShellSqliteTerminalTreeItem);
                ShellSqliteTablesTreeItem tableGroup =
                        (ShellSqliteTablesTreeItem) root.getChildren().get(0);
                tableGroup.addChild(new ShellSqliteTerminalTreeItem(treeView));
                assertEquals(Color.GREEN, tableGroup.getValue().graphicColor());
                ShellSqliteFunctionsTreeItem functionGroup =
                        (ShellSqliteFunctionsTreeItem) root.getChildren().get(2);
                functionGroup.addChild(new ShellSqliteTerminalTreeItem(treeView));
                assertEquals(Color.GREEN, functionGroup.getValue().graphicColor());
                ShellSqliteQueriesTreeItem queryGroup =
                        (ShellSqliteQueriesTreeItem) root.getChildren().get(3);
                queryGroup.addChild(new ShellSqliteTerminalTreeItem(treeView));
                assertEquals(Color.GREEN, queryGroup.getValue().graphicColor());
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
    }

    @Test
    public void testGroupNodesLoadOnDoubleClick() throws Exception {
        assertNotNull(ShellSqliteTablesTreeItem.class
                .getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellSqliteViewsTreeItem.class
                .getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellSqliteFunctionsTreeItem.class
                .getMethod("onPrimaryDoubleClick"));
        assertNotNull(ShellSqliteQueriesTreeItem.class
                .getMethod("onPrimaryDoubleClick"));
    }

    @Test
    public void testTreePackagesAreAlignedWithMysql() {
        assertEquals("cn.oyzh.easyshell.trees.sqlite.root",
                ShellSqliteRootTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.table",
                ShellSqliteTablesTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.table",
                ShellSqliteTableTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.view",
                ShellSqliteViewsTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.view",
                ShellSqliteViewTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.function",
                ShellSqliteFunctionsTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.function",
                ShellSqliteFunctionTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.query",
                ShellSqliteQueriesTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.query",
                ShellSqliteQueryTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.terminal",
                ShellSqliteTerminalTreeItem.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite",
                ShellSqliteTreeView.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.root",
                ShellSqliteRootTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.table",
                ShellSqliteTablesTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.table",
                ShellSqliteTableTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.view",
                ShellSqliteViewsTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.view",
                ShellSqliteViewTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.function",
                ShellSqliteFunctionsTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.function",
                ShellSqliteFunctionTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.query",
                ShellSqliteQueriesTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.query",
                ShellSqliteQueryTreeItemValue.class.getPackageName());
        assertEquals("cn.oyzh.easyshell.trees.sqlite.terminal",
                ShellSqliteTerminalTreeItemValue.class.getPackageName());
    }

    @Test
    public void testTreeNodesAreStructuredLikeMysqlAndDameng() throws Exception {
        assertEquals(ShellSqliteTablesTreeItem.class,
                ShellSqliteTableTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellSqliteViewsTreeItem.class,
                ShellSqliteViewTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellSqliteFunctionsTreeItem.class,
                ShellSqliteFunctionTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellSqliteQueriesTreeItem.class,
                ShellSqliteQueryTreeItem.class.getMethod("parent").getReturnType());
        assertEquals(ShellSqliteRootTreeItem.class,
                ShellSqliteTerminalTreeItem.class.getMethod("parent").getReturnType());

        assertNotNull(ShellSqliteTableTreeItem.class
                .getConstructor(SQLiteTable.class, RichTreeView.class));
        assertNotNull(ShellSqliteViewTreeItem.class
                .getConstructor(SQLiteView.class, RichTreeView.class));
        assertNotNull(ShellSqliteFunctionTreeItem.class
                .getConstructor(SQLiteFunction.class, RichTreeView.class));
        assertNotNull(ShellSqliteQueryTreeItem.class
                .getConstructor(ShellQuery.class, RichTreeView.class));

        assertNotNull(ShellSqliteTablesTreeItem.class.getMethod("addTable", SQLiteTable.class));
        assertNotNull(ShellSqliteViewsTreeItem.class.getMethod("addView", SQLiteView.class));
        assertNotNull(ShellSqliteFunctionsTreeItem.class.getMethod("addFunction", SQLiteFunction.class));
        assertNotNull(ShellSqliteQueriesTreeItem.class.getMethod("addQuery", ShellQuery.class));
    }

    @Test
    public void testGroupCountsVisibleWhenTreeLoads() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-sqlite-count-");
        Path file = directory.resolve("counts.db");
        ShellConnect connect = new ShellConnect();
        connect.setId("sqlite-count-test");
        connect.setName("counts.db");
        connect.setType(ShellPrototype.SQLITE);
        connect.setOsType(ShellPrototype.SQLITE);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("createIfMissing=true\nforeign_keys=1\n");
        ShellSqliteClient client = new ShellSqliteClient(connect);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE item (id INTEGER PRIMARY KEY);"
                    + "CREATE VIEW item_view AS SELECT id FROM item;");
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
                    ShellSqliteTreeView treeView = new ShellSqliteTreeView();
                    treeView.setClient(client);
                    treeView.root().loadChild();
                    assertEquals(" (1)", ((ShellSqliteTreeItem<?>) treeView.root()
                            .getChildren().get(0)).getValue().extra());
                    assertEquals(" (1)", ((ShellSqliteTreeItem<?>) treeView.root()
                            .getChildren().get(1)).getValue().extra());
                    assertTrue(((ShellSqliteTreeItem<?>) treeView.root()
                            .getChildren().get(2)).getValue().extra()
                            .startsWith(" ("));
                    assertTrue(((ShellSqliteTreeItem<?>) treeView.root()
                            .getChildren().get(3)).getValue().extra()
                            .startsWith(" ("));
                } catch (Throwable ex) {
                    error.set(ex);
                } finally {
                    loaded.countDown();
                }
            });
            assertTrue(loaded.await(20, TimeUnit.SECONDS));
            if (error.get() != null) {
                throw new AssertionError("Group count initialization failed", error.get());
            }
        } finally {
            client.close();
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void testNewTableAndViewAppendToLoadedGroupsWithoutRebuildingTree() throws Throwable {
        Path directory = Files.createTempDirectory("easyshell-sqlite-object-add-");
        Path file = directory.resolve("object-add.db");
        ShellConnect connect = new ShellConnect();
        connect.setId("sqlite-object-add-test");
        connect.setName("object-add.db");
        connect.setType(ShellPrototype.SQLITE);
        connect.setOsType(ShellPrototype.SQLITE);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("createIfMissing=true\nforeign_keys=1\n");
        ShellSqliteClient client = new ShellSqliteClient(connect);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE item (id INTEGER PRIMARY KEY);"
                    + "CREATE VIEW old_view AS SELECT id FROM item;");
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
                    ShellSqliteTreeView treeView = new ShellSqliteTreeView();
                    treeView.setClient(client);
                    ShellSqliteRootTreeItem root = treeView.root();
                    root.loadChild();
                    ShellSqliteTablesTreeItem tableGroup =
                            (ShellSqliteTablesTreeItem) root.unfilteredChildren().get(0);
                    ShellSqliteViewsTreeItem viewGroup =
                            (ShellSqliteViewsTreeItem) root.unfilteredChildren().get(1);
                    tableGroup.loadChild();
                    viewGroup.loadChild();
                    assertEquals(1, tableGroup.unfilteredChildrenSize());
                    assertEquals(1, viewGroup.unfilteredChildrenSize());

                    client.executeSqlSimple("CREATE VIEW new_view AS SELECT id FROM item;");
                    root.addView("new_view");
                    client.executeSqlSimple("CREATE TABLE new_table (id INTEGER PRIMARY KEY);");
                    root.addTable("new_table");

                    assertSame(tableGroup, root.unfilteredChildren().get(0));
                    assertSame(viewGroup, root.unfilteredChildren().get(1));
                    assertEquals(2, tableGroup.unfilteredChildrenSize());
                    assertTrue(tableGroup.unfilteredChildren().stream()
                            .filter(ShellSqliteTableTreeItem.class::isInstance)
                            .map(ShellSqliteTableTreeItem.class::cast)
                            .anyMatch(item -> "new_table".equals(item.tableName())));
                    assertEquals(" (2)", tableGroup.getValue().extra());
                    assertEquals(2, viewGroup.unfilteredChildrenSize());
                    assertTrue(viewGroup.unfilteredChildren().stream()
                            .filter(ShellSqliteViewTreeItem.class::isInstance)
                            .map(ShellSqliteViewTreeItem.class::cast)
                            .anyMatch(item -> "new_view".equals(item.viewName())));
                    assertEquals(" (2)", viewGroup.getValue().extra());

                    root.addView("new_view");
                    root.addTable("new_table");
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
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void testTargetTableSearchFiltersCaseInsensitively() {
        List<String> tables = List.of("User", "USER_LOG", "orders");
        assertEquals(List.of("User", "USER_LOG"),
                SQLiteTableSelectTextField.filterTables(tables, "user"));
        assertEquals(List.of("USER_LOG"),
                SQLiteTableSelectTextField.filterTables(tables, "log"));
        assertEquals(tables, SQLiteTableSelectTextField.filterTables(tables, ""));
        assertTrue(SQLiteTableSelectTextField.filterTables(tables, "missing").isEmpty());
    }

    @Test
    public void testQueryGroupUpsertsAndRemovesNodesWithoutRebuildingTree() throws Throwable {
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
                ShellSqliteTreeView treeView = new ShellSqliteTreeView();
                ShellSqliteRootTreeItem root = treeView.root();
                root.loadChild();
                ShellSqliteTreeItem tableGroup = (ShellSqliteTreeItem) root.unfilteredChildren().get(0);
                ShellSqliteQueriesTreeItem queryGroup =
                        (ShellSqliteQueriesTreeItem) root.unfilteredChildren().get(3);

                ShellQuery query = new ShellQuery();
                query.setUid("query-1");
                query.setName("first");
                root.upsertQuery(query);

                assertSame(tableGroup, root.unfilteredChildren().get(0));
                assertSame(queryGroup, root.unfilteredChildren().get(3));
                assertEquals(1, queryGroup.unfilteredChildrenSize());
                ShellSqliteQueryTreeItem item =
                        (ShellSqliteQueryTreeItem) queryGroup.unfilteredChildren().getFirst();
                assertEquals("first", item.queryName());

                query.setName("renamed");
                root.upsertQuery(query);
                assertEquals(1, queryGroup.unfilteredChildrenSize());
                assertEquals("renamed", item.queryName());

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
    }

    @Test
    public void testColumnControlInitializationDoesNotMarkChanged() throws Exception {
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
                SQLiteColumnControl column = new SQLiteColumnControl();
                column.setName("id");
                column.setType("INTEGER");
                column.setPrimaryKey(true);
                column.setNotNull(true);
                column.setDefaultValue("1");
                column.clearStatus();
                column.getNameControl();
                DBFiledTypeComboBox typeControl = column.getTypeControl();
                column.getSizeControl();
                column.getPrimaryKeyControl();
                column.getNullableControl();
                column.getUniqueControl();
                assertFalse(column.isChanged());
                assertEquals("INTEGER", column.getType());
                FXCheckBox nullableControl = column.getNullableControl();
                assertFalse(nullableControl.isSelected());
                nullableControl.setSelected(true);
                assertTrue(column.isNullable());
                typeControl.select("TEXT");
                assertTrue(column.isChanged());
                assertEquals("TEXT", column.getType());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Column control initialization failed", error.get());
        }
    }

    @Test
    public void testSqliteTablePagesMatchMysqlStructure() throws Exception {
        String design = this.resourceText("/tabs/sqlite/table/shellSqliteTableDesignTab.fxml");
        assertTrue(design.contains("DBStatusTableView"));
        assertTrue(design.contains("fx:id=\"columnTable\""));
        assertTrue(design.contains("fx:id=\"indexTable\""));
        assertTrue(design.contains("fx:id=\"triggerTable\""));
        assertTrue(design.contains("fx:id=\"foreignKeyTable\""));
        assertTrue(design.contains("fx:id=\"checkTable\""));
        assertTrue(design.contains("fx:id=\"constraintTab\""));
        assertTrue(design.contains("fx:id=\"optionsTab\""));
        assertTrue(design.contains("fx:id=\"preview\""));
        assertTrue(design.contains("fx:include fx:id=\"tableColumnExtra\""));
        assertTrue(design.contains("valueName=\"nullableControl\" text=\"%base.nullable\""));
        assertFalse(design.contains("valueName=\"defaultValueControl\""));
        assertFalse(design.contains("fx:id=\"tableName\""));
        assertTrue(design.contains("#doMoveUp"));
        assertTrue(design.contains("#doMoveDown"));
        assertTrue(design.contains("#refresh"));
        assertFalse(design.contains("fx:id=\"columns\""));
        assertFalse(design.contains("autoIncrementControl"));

        String extra = this.resourceText("/tabs/sqlite/table/shellSqliteTableColumnExtra.fxml");
        assertTrue(extra.contains("ShellSqliteDefaultValueTextFiled"));
        assertTrue(extra.contains("fx:id=\"defaultValueBox\""));
        assertTrue(extra.contains("fx:id=\"defaultValue\""));
        assertTrue(extra.contains("fx:id=\"autoIncrementBox\""));
        assertTrue(extra.contains("fx:id=\"autoIncrement\""));
        assertTrue(extra.contains("SQLiteCollationComboBox"));
        assertTrue(extra.contains("fx:id=\"collate\""));
        assertTrue(extra.contains("fx:id=\"check\""));
        assertTrue(extra.contains("fx:id=\"checkBox\""));
        assertTrue(extra.contains("fx:id=\"generatedExpression\""));
        assertTrue(extra.contains("fx:id=\"generatedStorage\""));
        assertTrue(extra.contains("fx:id=\"generatedStorageBox\""));
        assertTrue(extra.indexOf("fx:id=\"generatedExpressionBox\"")
                < extra.indexOf("fx:id=\"generatedExpression\""));
        assertTrue(extra.indexOf("fx:id=\"generatedExpression\"")
                < extra.indexOf("fx:id=\"generatedStorageBox\""));
        assertTrue(extra.indexOf("fx:id=\"generatedStorageBox\"")
                < extra.indexOf("fx:id=\"generatedStorage\""));

        String records = this.resourceText("/tabs/sqlite/table/shellSqliteTableRecordTab.fxml");
        assertTrue(records.contains("PageBox"));
        assertTrue(records.contains("ShellSqliteRecordTableView"));
        assertTrue(records.contains("fx:id=\"apply\""));
        assertTrue(records.contains("fx:id=\"discard\""));
        assertTrue(records.contains("FilterSVGGlyph"));

        String viewRecords = this.resourceText("/tabs/sqlite/view/shellSqliteViewRecordTab.fxml");
        assertTrue(viewRecords.contains("PageBox"));
        assertTrue(viewRecords.contains("ShellSqliteRecordTableView"));
        assertTrue(viewRecords.contains("fx:id=\"apply\""));
        assertTrue(viewRecords.contains("fx:id=\"discard\""));
        assertTrue(viewRecords.contains("FilterSVGGlyph"));

        String viewDesign = this.resourceText("/tabs/sqlite/view/shellSqliteViewDesignTab.fxml");
        assertTrue(viewDesign.contains("ShellSqliteQueryEditor"));
        assertTrue(viewDesign.contains("#refresh"));
        assertFalse(viewDesign.contains("fx:id=\"viewName\""));
        assertTrue(viewDesign.contains("fx:id=\"definitionTab\""));
        assertTrue(viewDesign.contains("text=\"%base.definition\""));
        assertFalse(viewDesign.contains("%shell.sqlite.selectDefinition"));
        assertTrue(viewDesign.contains("fx:id=\"tabPane\""));
        assertTrue(viewDesign.contains("fx:id=\"previewTab\""));

        String viewInfo = this.resourceText("/fxml/sqlite/view/shellSqliteViewInfo.fxml");
        assertEquals(2, viewInfo.split("<FixedTab ", -1).length - 1);
        assertTrue(viewInfo.contains("text=\"%base.definition\""));
        assertFalse(viewInfo.contains("%shell.sqlite.selectDefinition"));
        assertFalse(viewInfo.contains("%shell.sqlite.columnCount"));
        assertFalse(viewInfo.contains("SQLiteColumnTableView"));

        String tableInfo = this.resourceText("/fxml/sqlite/table/shellSqliteTableInfo.fxml");
        assertFalse(tableInfo.contains("%shell.sqlite.columnCount"));
        assertFalse(tableInfo.contains("%shell.sqlite.columns"));
        assertFalse(tableInfo.contains("SQLiteColumnTableView"));
        assertTrue(tableInfo.contains("text=\"%base.definition\""));
        assertFalse(tableInfo.contains("text=\"%base.ddl\""));

        String functionInfo = this.resourceText("/fxml/sqlite/function/shellSqliteFunctionInfo.fxml");
        assertFalse(functionInfo.contains("%shell.sqlite.functionFlags"));
        assertEquals(1, functionInfo.split("<FixedTab ", -1).length - 1);
        assertTrue(functionInfo.contains("text=\"%base.definition\""));
        assertFalse(functionInfo.contains("text=\"%base.ddl\""));
        assertFalse(functionInfo.contains("fx:id=\"createDefinition\""));
        assertTrue(functionInfo.contains("fx:id=\"definition\""));

        String functionDesign = this.resourceText("/tabs/sqlite/function/shellSqliteFunctionDesignTab.fxml");
        assertTrue(functionDesign.contains("SQLiteFunctionKindComboBox"));
        assertTrue(functionDesign.contains("SQLiteFunctionKindComboBox fx:id=\"kind\" realHeight=\"25\""));
        assertTrue(functionDesign.contains("SQLiteTableSelectTextField fx:id=\"targetTable\" realHeight=\"25\""));
        assertTrue(functionDesign.contains("DBFiledTypeComboBox fx:id=\"returnType\""));
        assertTrue(functionDesign.contains("fx:id=\"expressionBox\""));
        assertTrue(functionDesign.contains("fx:id=\"viewDefinitionBox\""));
        assertTrue(functionDesign.contains("fx:id=\"generatedColumnBox\""));
        assertTrue(functionDesign.contains("fx:id=\"generatedExpression\""));
        assertTrue(functionDesign.contains("fx:id=\"targetTable\""));
        assertTrue(functionDesign.contains("fx:id=\"returnType\""));
        assertTrue(functionDesign.contains("fx:id=\"generatedStorage\""));
        assertTrue(functionDesign.contains("shell.sqlite.function.targetTableTip"));
        assertFalse(functionDesign.contains("fx:id=\"kindHint\""));
        assertFalse(functionDesign.contains("fx:id=\"functionName\""));
        assertFalse(functionDesign.contains("builtinFunctions"));
        assertFalse(functionDesign.contains("#refreshBuiltins"));
        assertTrue(functionDesign.contains("fx:id=\"tabPane\""));
        assertTrue(functionDesign.contains("fx:id=\"previewTab\""));
        assertTrue(functionDesign.indexOf("fx:id=\"generatedColumnSettings\"")
                < functionDesign.indexOf("fx:id=\"generatedExpressionBox\""));
        assertTrue(functionDesign.indexOf("fx:id=\"generatedExpressionBox\"")
                < functionDesign.indexOf("fx:id=\"generatedExpression\""));
        String functionController = this.sourceText(
                "cn/oyzh/easyshell/tabs/sqlite/function/ShellSqliteFunctionDesignTabController.java");
        assertTrue(functionController.contains("result.setExpression(this.generatedExpression.getText())"));

        String query = this.resourceText("/tabs/sqlite/query/shellSqliteQueryMainTab.fxml");
        assertTrue(query.contains("ShellSqliteQueryInfoTab"));
        assertTrue(query.contains("ExplainSVGGlyph"));
        assertTrue(query.contains("fx:id=\"infoTab\""));
        assertFalse(query.contains("ExportSVGGlyph"));
        String queryInfo = this.resourceText("/tabs/sqlite/query/shellSqliteQueryInfoTab.fxml");
        assertTrue(queryInfo.contains("fx:id=\"infoArea\""));
        String querySelect = this.resourceText("/tabs/sqlite/query/shellSqliteQuerySelectTab.fxml");
        assertTrue(querySelect.contains("ExportSVGGlyph"));
        assertTrue(querySelect.contains("ShellSqliteRecordTableView"));
        assertTrue(querySelect.contains("fx:id=\"add\""));
        assertTrue(querySelect.contains("fx:id=\"delete\""));
        assertTrue(querySelect.contains("fx:id=\"apply\""));
        assertTrue(querySelect.contains("fx:id=\"discard\""));
        assertTrue(querySelect.contains("#reload"));
        String queryExplain = this.resourceText("/tabs/sqlite/query/shellSqliteQueryExplainTab.fxml");
        assertTrue(queryExplain.contains("SQLiteResultTableView"));
        String queryController = this.sourceText(
                "cn/oyzh/easyshell/tabs/sqlite/query/ShellSqliteQueryMainTabController.java");
        assertTrue(queryController.contains("private void clearTabs()"));
        assertTrue(queryController.contains("initSelectTab"));
        assertTrue(queryController.contains("initExplainTab"));
    }

    @Test
    public void testSqliteQueryResultEditingMatchesMysqlAndDameng() throws Exception {
        String controller = this.sourceText(
                "cn/oyzh/easyshell/tabs/sqlite/query/ShellSqliteQuerySelectTabController.java");
        assertTrue(controller.contains("if (result.isUpdatable())"));
        assertTrue(controller.contains("if (result.isFullColumn())"));
        assertTrue(controller.contains("private void insertRecord(SQLiteRecord record)"));
        assertTrue(controller.contains("private void updateRecord(SQLiteRecord record)"));
        assertTrue(controller.contains("private void deleteRecord()"));
        assertTrue(controller.contains("DBStatusListenerManager.removeListener(this.changeListener)"));
        assertTrue(controller.contains("this.recordTable.setCtrlSAction(this::apply)"));
        String result = this.sourceText("cn/oyzh/easyshell/query/sqlite/SQLiteQueryResult.java");
        assertTrue(result.contains("StringUtil.isNotBlank(this.tableName()) && this.getPrimaryKey() != null"));
        String fxml = this.resourceText("/tabs/sqlite/query/shellSqliteQuerySelectTab.fxml");
        assertTrue(fxml.contains("ShellSqliteRecordTableView"));
        assertTrue(fxml.contains("onMousePrimaryClicked=\"#addRecord\""));
        assertTrue(fxml.contains("onMousePrimaryClicked=\"#deleteRecord\""));
        assertTrue(fxml.contains("onMousePrimaryClicked=\"#apply\""));
        assertTrue(fxml.contains("onMousePrimaryClicked=\"#discard\""));
        assertTrue(fxml.contains("onMousePrimaryClicked=\"#reload\""));
    }

    @Test
    public void testDataToolsUseMysqlWizardLayout() throws Exception {
        for (String resource : List.of(
                "/fxml/sqlite/data/shellSqliteDataExport.fxml",
                "/fxml/sqlite/data/shellSqliteDataImport.fxml")) {
            String content = this.resourceText(resource);
            assertTrue(resource, content.contains("FXHeaderBar"));
            assertTrue(resource, content.contains("fx:id=\"step1\""));
            assertTrue(resource, content.contains("fx:id=\"step2\""));
            assertTrue(resource, content.contains("fx:id=\"step3\""));
            assertTrue(resource, content.contains("fx:id=\"step4\""));
            assertTrue(resource, content.contains("fx:id=\"step5\""));
        }
        String dump = this.resourceText("/fxml/sqlite/data/shellSqliteDataDump.fxml");
        assertTrue(dump.contains("FXHeaderBar"));
        assertTrue(dump.contains("DBDataDumpTypeComboBox"));

        String runSql = this.resourceText("/fxml/sqlite/data/shellSqliteDataRunSqlFile.fxml");
        assertTrue(runSql.contains("FXHeaderBar"));
        assertTrue(runSql.contains("ChooseFileTextField"));

        String dataImport = this.resourceText("/fxml/sqlite/data/shellSqliteDataImport.fxml");
        assertFalse(dataImport.contains("%base.sqlType"));
        assertFalse(dataImport.contains("userData=\"sql\""));
        assertTrue(dataImport.contains("onAction=\"#deleteFile\""));
        String importStep4 = dataImport.substring(dataImport.indexOf("fx:id=\"step4\""),
                dataImport.indexOf("fx:id=\"step5\""));
        assertFalse(importStep4.contains("CancelButton"));

        String transport = this.resourceText("/fxml/sqlite/data/shellSqliteDataTransport.fxml");
        assertTrue(transport.contains("FXHeaderBar"));
        assertTrue(transport.contains("DBDataTransportNameListView"));
        assertTrue(transport.contains("DBDataDumpTypeComboBox"));
        assertTrue(transport.contains("fx:id=\"step4\""));
        assertTrue(transport.contains("ReadOnlyTextField fx:id=\"sourceInfo\""));
        assertTrue(transport.contains("ChooseFileTextField fx:id=\"targetInfo\""));
        assertEquals(2, transport.split("<FXTab ", -1).length - 1);

        String recordTable = this.sourceText(
                "cn/oyzh/easyshell/fx/sqlite/record/ShellSqliteRecordTableView.java");
        assertTrue(recordTable.contains("ShellSqliteRecordTableRow"));
        assertTrue(recordTable.contains("setRowFactory"));
        String tableRecord = this.sourceText(
                "cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableRecordTabController.java");
        assertTrue(tableRecord.contains("setCtrlSAction(this::apply)"));
        String viewRecord = this.sourceText(
                "cn/oyzh/easyshell/tabs/sqlite/view/ShellSqliteViewRecordTabController.java");
        assertTrue(viewRecord.contains("setCtrlSAction(this::apply)"));
    }

    @Test
    public void testSqliteTableDesignControllerMatchesMysqlAndDamengFlow() throws Exception {
        String controller = this.sourceText(
                "cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableDesignTabController.java");
        assertTrue(controller.contains("this.newData = table == null || StringUtil.isBlank(table.getName());"));
        assertTrue(controller.contains("protected void initInfo()"));
        assertTrue(controller.contains("protected void initNew()"));
        assertTrue(controller.contains("protected void initNormal()"));
        assertTrue(controller.contains("protected void resetTable() throws Exception"));
        assertTrue(controller.contains("FXUtil.runWait(this::initInfo);"));
        assertTrue(controller.contains("this.init(this.table, this.dbItem);"));
        assertTrue(controller.contains("NodeGroupUtil.display(this.getTab(), \"action2\")"));
        assertTrue(controller.contains("NodeGroupUtil.display(this.getTab(), \"action3\")"));
    }

    private String resourceText(String resource) throws Exception {
        try (InputStream input = ShellSqliteArchitectureTest.class.getResourceAsStream(resource)) {
            assertNotNull("Missing resource: " + resource, input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String sourceText(String resource) throws Exception {
        return Files.readString(Path.of("src/main/java", resource), StandardCharsets.UTF_8);
    }

    private Set<String> sqliteKeys(String resource) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = ShellSqliteArchitectureTest.class.getResourceAsStream(resource)) {
            assertNotNull("Missing i18n resource: " + resource, input);
            properties.load(input);
        }
        return properties.stringPropertyNames().stream()
                .filter(key -> key.startsWith("shell.sqlite."))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    public void testFxmlResourcesAreWellFormed() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);
        for (String resource : FXML_RESOURCES) {
            try (InputStream input = ShellSqliteArchitectureTest.class.getResourceAsStream(resource)) {
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
                    URL url = ShellSqliteArchitectureTest.class.getResource(resource);
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
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("FXML load failed", error.get());
        }
    }
}
