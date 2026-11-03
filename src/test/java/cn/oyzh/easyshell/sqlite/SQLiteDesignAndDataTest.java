package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataImportFile;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataDumpHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataExportHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataImportHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataRunSqlFileHandler;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlite.condition.SQLiteCondition;
import cn.oyzh.easyshell.sqlite.condition.SQLiteConditionUtil;
import cn.oyzh.easyshell.sqlite.check.SQLiteCheck;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumnDefinitionParser;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKey;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndexControl;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTriggerControl;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * SQLite 设计器与数据管理核心测试。
 *
 * @since 2026-11-02
 */
public class SQLiteDesignAndDataTest {

    @Test
    public void testSqliteRecordFilterConditions() {
        assertEquals("LIKE '%ab%'", SQLiteConditionUtil.CONTAINS.wrapCondition("name", "ab"));
        assertEquals("NOT LIKE '%ab%'", SQLiteConditionUtil.NOT_CONTAINS.wrapCondition("name", "ab"));
        assertEquals("IS NULL", SQLiteConditionUtil.NULL.wrapCondition("name", null));
        assertEquals("IS NOT NULL", SQLiteConditionUtil.NOT_NULL.wrapCondition("name", null));
        assertEquals("= ''", SQLiteConditionUtil.EMPTY.wrapCondition("name", null));
        assertEquals("<> ''", SQLiteConditionUtil.NOT_EMPTY.wrapCondition("name", null));
        assertEquals("IN ('a', 'b')", SQLiteConditionUtil.IN.wrapCondition("name", "a, b"));
        assertEquals("BETWEEN 1 AND 9", SQLiteConditionUtil.BETWEEN.wrapCondition("age", List.of(1, 9)));
    }

    @Test
    public void testSqliteRecordFilterIntegration() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-filter-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE filter_item ("
                    + "id INTEGER PRIMARY KEY, name TEXT, age INTEGER, note TEXT)");
            client.executeSqlSimple("INSERT INTO filter_item (name, age, note) VALUES "
                    + "('Alice', 30, 'red'), ('Bob', 20, NULL), ('Carol', 40, ''), "
                    + "('Dave', 25, 'blue'), ('Eve', 35, NULL)");
            List<SQLiteColumn> columns = client.selectColumns("filter_item");

            SQLiteRecordFilter contains = this.filter(columns, "name",
                    SQLiteConditionUtil.CONTAINS, "a", "AND");
            assertTrue(SQLiteConditionUtil.buildCondition(List.of(contains))
                    .contains("LIKE '%a%'"));
            assertEquals(3, client.selectRowCount("filter_item",
                    SQLiteConditionUtil.buildCondition(List.of(contains))));

            SQLiteRecordFilter isNull = this.filter(columns, "note",
                    SQLiteConditionUtil.NULL, null, "AND");
            assertEquals(2, client.selectRowCount("filter_item",
                    SQLiteConditionUtil.buildCondition(List.of(isNull))));

            SQLiteRecordFilter inList = this.filter(columns, "name",
                    SQLiteConditionUtil.IN, "Alice, Carol", "AND");
            assertEquals(2, client.selectRowCount("filter_item",
                    SQLiteConditionUtil.buildCondition(List.of(inList))));

            SQLiteRecordFilter between = this.filter(columns, "age",
                    SQLiteConditionUtil.BETWEEN, List.of(20, 30), "AND");
            assertEquals(3, client.selectRowCount("filter_item",
                    SQLiteConditionUtil.buildCondition(List.of(between))));
            assertEquals(2, client.selectRecords("filter_item", columns, 1, 2,
                    SQLiteConditionUtil.buildCondition(List.of(between))).size());

            SQLiteRecordFilter blank = this.filter(columns, "name",
                    SQLiteConditionUtil.CONTAINS, null, "AND");
            String blankCondition = SQLiteConditionUtil.buildCondition(List.of(blank, contains));
            assertFalse(blankCondition.startsWith("AND"));
            assertFalse(blankCondition.endsWith("AND"));
            assertEquals(3, client.selectRowCount("filter_item", blankCondition));

            SQLiteRecordFilter older = this.filter(columns, "age",
                    SQLiteConditionUtil.GT_EQ, 30, "OR");
            SQLiteRecordFilter nullNote = this.filter(columns, "note",
                    SQLiteConditionUtil.NULL, null, "AND");
            String mixedCondition = SQLiteConditionUtil.buildCondition(List.of(older, nullNote));
            assertTrue(mixedCondition.contains(">= 30 OR"));
            assertTrue(mixedCondition.contains("IS NULL"));
            assertEquals(4, client.selectRowCount("filter_item", mixedCondition));
        } finally {
            client.close();
        }
    }

    @Test
    public void testDesignerDdlViewFunctionAndTableRebuild() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-design-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            SQLiteColumn id = this.column("id", "INTEGER", true, false, true);
            SQLiteColumn name = this.column("name", "TEXT", false, true, false);
            SQLiteColumn price = this.column("price", "REAL", false, false, false);
            SQLiteColumn quantity = this.column("quantity", "INTEGER", false, false, false);
            SQLiteColumn amount = this.column("amount", "REAL", false, false, false);
            amount.setGeneratedExpression("price * quantity");
            amount.setGeneratedStorage("STORED");
            SQLiteTable table = new SQLiteTable();
            table.setName("person");
            table.setColumns(List.of(id, name, price, quantity, amount));
            table.setConstraints("CHECK (price >= 0)");
            SQLiteSchemaManager manager = new SQLiteSchemaManager(client);
            manager.createTable(table);
            client.executeSqlSimple("INSERT INTO person (name, price, quantity) VALUES ('Ada', 2, 3)");

            SQLiteColumn note = this.column("note", "TEXT", false, false, false);
            table.setColumns(new ArrayList<>(List.of(id, name, price, quantity, amount, note)));
            manager.updateTable("person", table);
            assertEquals(1, client.selectRowCount("person"));
            assertNotNull(client.selectColumns("person").stream()
                    .filter(column -> "amount".equals(column.getName())).findFirst().orElse(null));

            SQLiteView view = new SQLiteView();
            view.setName("person_view");
            view.setDefinition("SELECT name, amount FROM person");
            manager.saveView(null, view);
            assertNotNull(client.selectView("person_view"));

            SQLiteView dependent = new SQLiteView();
            dependent.setName("person_view_dependent");
            dependent.setDefinition("SELECT * FROM person_view");
            manager.saveView(null, dependent);
            client.executeSqlSimple("CREATE TRIGGER person_dependent_insert INSTEAD OF INSERT ON "
                    + "person_view_dependent BEGIN SELECT NEW.name; END");
            SQLiteView renamed = new SQLiteView();
            renamed.setName("person_renamed");
            renamed.setDefinition("SELECT name, amount FROM person");
            manager.saveView("person_view", renamed);
            assertNotNull(client.selectView("person_renamed"));
            assertTrue(client.showCreate("person_view_dependent").contains("person_renamed"));
            assertTrue(client.showCreate("person_dependent_insert").contains("ON person_view_dependent"));

            SQLiteFunction function = new SQLiteFunction();
            function.setKind(SQLiteFunction.KIND_TABLE_VIEW);
            function.setName("person_function");
            function.setDefinition("SELECT name FROM person");
            manager.saveFunction(null, function);
            assertNotNull(client.selectView("person_function"));

            function.setKind(SQLiteFunction.KIND_GENERATED_COLUMN);
            function.setName("discount");
            function.setTargetTable("person");
            function.setReturnType("REAL");
            function.setExpression("price * 0.9");
            manager.saveFunction(null, function);
            assertTrue(client.selectColumns("person").stream()
                    .anyMatch(column -> "discount".equals(column.getName())));
        } finally {
            client.close();
        }
    }

    @Test
    public void testFunctionSaveRejectsInvalidData() {
        SQLiteFunction function = new SQLiteFunction();
        function.setName("bad");
        function.setKind(SQLiteFunction.KIND_GENERATED_COLUMN);
        try {
            SQLiteFunction.validate(function);
            fail("Generated column must require a table");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("target table"));
        }
    }

    @Test
    public void testViewDefinitionParsing() {
        assertEquals("SELECT 'as' AS value",
                SQLiteViewCreateSqlGenerator.viewDefinition("CREATE VIEW \"as\" AS SELECT 'as' AS value;"));
        assertEquals("SELECT 1", SQLiteViewCreateSqlGenerator.viewDefinition("SELECT 1"));
        assertNull(SQLiteViewCreateSqlGenerator.viewDefinition(null));
        assertEquals("a\"b", SQLiteGeneratorUtil.unquote("\"a\"\"b\""));
        assertEquals("a`b", SQLiteGeneratorUtil.unquote("`a``b`"));
        assertEquals("a b", SQLiteGeneratorUtil.unquote("[a b]"));

        SQLiteColumn parsed = SQLiteColumnDefinitionParser.parseColumn(
                "\"amount due\" DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (\"amount due\" >= 0)");
        assertEquals("amount due", parsed.getName());
        assertEquals("DOUBLE PRECISION", parsed.getType());
        assertTrue(parsed.isNotNull());
        assertEquals("0", parsed.getDefaultValue());
        assertEquals("\"amount due\" >= 0", parsed.getCheck());

        SQLiteColumn typeless = SQLiteColumnDefinitionParser.parseColumn("\"plain\" PRIMARY KEY");
        assertEquals("plain", typeless.getName());
        assertEquals("", typeless.getType());
        assertTrue(SQLiteColumnDefinitionParser.isTableConstraint("PRIMARY KEY (left, right)"));
        assertTrue(SQLiteColumnDefinitionParser.isTableConstraint("CONSTRAINT fk FOREIGN KEY (id) REFERENCES parent(id)"));
        assertFalse(SQLiteColumnDefinitionParser.isTableConstraint("\"name\" TEXT UNIQUE"));
    }

    @Test
    public void testSqlGeneratorValidatesGeneratedAndAutoIncrementColumns() {
        SQLiteColumn generated = this.column("value", "INTEGER", false, false, false);
        generated.setGeneratedExpression("1");
        generated.setUnique(true);
        try {
            SQLiteTableCreateSqlGenerator.createTable("invalid_generated", List.of(generated), null, false, false);
            fail("Generated column cannot be unique");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Generated column"));
        }

        SQLiteColumn autoIncrement = this.column("id", "TEXT", true, false, true);
        try {
            SQLiteTableCreateSqlGenerator.createTable("invalid_autoincrement", List.of(autoIncrement), null, false, false);
            fail("AUTOINCREMENT requires INTEGER");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("INTEGER PRIMARY KEY"));
        }

        SQLiteColumn sized = this.column("name", "TEXT", false, false, false);
        sized.setSize(120);
        String sql = SQLiteTableCreateSqlGenerator.createTable("sized", List.of(sized), null, false, false);
        assertTrue(sql.contains("\"name\" TEXT(120)"));
    }

    @Test
    public void testCompositePrimaryKeyAndWithoutRowidValidation() {
        SQLiteColumn left = this.column("left", "TEXT", true, true, false);
        SQLiteColumn right = this.column("right", "TEXT", true, true, false);
        SQLiteTable table = new SQLiteTable();
        table.setName("composite");
        table.setColumns(List.of(left, right));
        table.setConstraints("PRIMARY KEY (left, right)");
        String sql = SQLiteTableCreateSqlGenerator.createTable("composite", table.getColumns(),
                table.getConstraints(), false, false);
        assertEquals(sql.indexOf("PRIMARY KEY"), sql.lastIndexOf("PRIMARY KEY"));

        try {
            SQLiteTableCreateSqlGenerator.createTable("invalid", List.of(left, right), null, false, false);
            fail("Multiple column-level primary keys must fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("table constraint"));
        }

        SQLiteColumn id = this.column("id", "INTEGER", true, false, true);
        try {
            SQLiteTableCreateSqlGenerator.createTable("invalid", List.of(id), null, true, false);
            fail("WITHOUT ROWID AUTOINCREMENT must fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("WITHOUT ROWID"));
        }
    }

    @Test
    public void testDefinitionControlNormalization() {
        SQLiteIndexControl index = new SQLiteIndexControl();
        index.setName("new_index");
        index.setTableName("old_table");
        index.setDefinition("CREATE INDEX old_index ON old_table(name)");
        String indexSql = index.normalizedDefinition("old_table", "new_table");
        assertTrue(indexSql.contains("CREATE INDEX \"new_index\" ON new_table(name)"));

        SQLiteTriggerControl trigger = new SQLiteTriggerControl();
        trigger.setName("new_trigger");
        trigger.setTableName("old_table");
        trigger.setDefinition("CREATE TRIGGER old_trigger AFTER INSERT ON old_table BEGIN SELECT 1; END");
        String triggerSql = trigger.normalizedDefinition("old_table", "new_table");
        assertTrue(triggerSql.contains("CREATE TRIGGER \"new_trigger\" AFTER INSERT ON new_table"));
    }

    @Test
    public void testForeignKeyAndTableCheckParsing() {
        String ddl = "CREATE TABLE child ("
                + "id INTEGER PRIMARY KEY, parent_id INTEGER, amount REAL CHECK (amount > 0), "
                + "CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES parent(id) "
                + "ON DELETE CASCADE ON UPDATE SET NULL, "
                + "CONSTRAINT chk_amount CHECK (amount >= 0), "
                + "UNIQUE (parent_id, amount))";
        List<SQLiteForeignKey> foreignKeys = SQLiteColumnDefinitionParser.foreignKeys(ddl);
        assertEquals(1, foreignKeys.size());
        SQLiteForeignKey foreignKey = foreignKeys.getFirst();
        assertEquals("fk_parent", foreignKey.getName());
        assertEquals(Set.of("parent_id"), foreignKey.getColumns());
        assertEquals("parent", foreignKey.getReferencedTable());
        assertEquals(Set.of("id"), foreignKey.getReferencedColumns());
        assertEquals("CASCADE", foreignKey.getDeletePolicy());
        assertEquals("SET NULL", foreignKey.getUpdatePolicy());
        assertTrue(foreignKey.toConstraintSql().contains("FOREIGN KEY (\"parent_id\") REFERENCES \"parent\" (\"id\")"));

        List<SQLiteCheck> checks = SQLiteColumnDefinitionParser.checks(ddl);
        assertEquals(1, checks.size());
        assertEquals("chk_amount", checks.getFirst().getName());
        assertEquals("amount >= 0", checks.getFirst().getClause());
        assertTrue(checks.getFirst().toConstraintSql().contains("CONSTRAINT \"chk_amount\" CHECK (amount >= 0)"));

        String base = SQLiteColumnDefinitionParser.baseTableConstraints(ddl);
        assertTrue(base.contains("UNIQUE (parent_id, amount)"));
        assertFalse(base.contains("FOREIGN KEY"));
        assertFalse(base.contains("CHECK (amount >= 0)"));
    }

    @Test
    public void testForeignKeyAndCheckConstraintCreation() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-fk-check-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE parent (id INTEGER PRIMARY KEY)");
            List<SQLiteColumn> columns = List.of(
                    this.column("id", "INTEGER", true, false, true),
                    this.column("parent_id", "INTEGER", false, false, false),
                    this.column("amount", "REAL", false, false, false));
            SQLiteForeignKey foreignKey = new SQLiteForeignKey();
            foreignKey.setName("fk_child_parent");
            foreignKey.setColumns(List.of("parent_id"));
            foreignKey.setReferencedTable("parent");
            foreignKey.setReferencedColumns(List.of("id"));
            foreignKey.setDeletePolicy("CASCADE");
            SQLiteCheck check = new SQLiteCheck();
            check.setName("chk_amount");
            check.setClause("amount >= 0");
            String constraints = foreignKey.toConstraintSql() + ",\n" + check.toConstraintSql()
                    + ",\nUNIQUE (parent_id, amount)";
            client.executeSqlSimple(SQLiteTableCreateSqlGenerator.createTable("child", columns, constraints, false, false));
            String ddl = client.showCreate("child");
            assertEquals(1, SQLiteColumnDefinitionParser.foreignKeys(ddl).size());
            assertEquals(1, SQLiteColumnDefinitionParser.checks(ddl).size());
            assertTrue(ddl.contains("FOREIGN KEY (\"parent_id\") REFERENCES \"parent\" (\"id\") ON DELETE CASCADE"));
            assertTrue(ddl.contains("CONSTRAINT \"chk_amount\" CHECK (amount >= 0)"));
        } finally {
            client.close();
        }
    }

    @Test
    public void testRenameColumnRewritesCheckAndCopiesData() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-rename-check-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE \"aa\" ("
                    + "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "\"age\" TEXT CHECK (age > 5))");
            client.executeSqlSimple("INSERT INTO \"aa\" (\"age\") VALUES ('6')");

            SQLiteTable table = client.selectTable("aa");
            List<SQLiteColumn> columns = client.selectColumns("aa");
            SQLiteColumn renamed = columns.stream()
                    .filter(column -> "age".equalsIgnoreCase(column.getName()))
                    .findFirst()
                    .orElseThrow();
            renamed.setName("aget");
            renamed.setCheck("age > 5");
            table.setColumns(columns);
            table.setConstraints("CHECK (age > 5)");

            new SQLiteSchemaManager(client).updateTable("aa", table);

            String ddl = client.showCreate("aa");
            assertTrue(ddl.contains("\"aget\" TEXT CHECK (\"aget\" > 5)"));
            List<SQLiteColumn> savedColumns = client.selectColumns("aa");
            assertEquals("6", String.valueOf(client.selectRecords("aa", savedColumns, 0, 10)
                    .getFirst().get("aget")));
        } finally {
            client.close();
        }
    }

    @Test
    public void testSchemaManagerRestoresForeignKeysState() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-pragma-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file, false);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE item (id INTEGER PRIMARY KEY)");
            SQLiteView view = new SQLiteView();
            view.setName("item_view");
            view.setDefinition("SELECT id FROM item");
            new SQLiteSchemaManager(client).saveView(null, view);
            assertFalse(client.database().isForeignKeys());
        } finally {
            client.close();
        }
    }

    @Test
    public void testSqlExportSkipsGeneratedColumns() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-generated-export-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE generated (id INTEGER PRIMARY KEY, price REAL, "
                    + "amount REAL GENERATED ALWAYS AS (price * 2) VIRTUAL)");
            client.executeSqlSimple("INSERT INTO generated (price) VALUES (3)");
            Path directory = Files.createTempDirectory("easyshell-sqlite-generated-output-");
            SQLiteDataExportTable exportTable = new SQLiteDataExportTable();
            exportTable.setName("generated");
            exportTable.columns(client.selectColumns("generated"));
            exportTable.setRecords(List.of(Map.of("id", 1, "price", 3.0, "amount", 6.0)));
            exportTable.setFilePath(directory.resolve("generated.sql").toString());
            SQLiteDataExportHandler exportHandler = new SQLiteDataExportHandler(client, "main");
            exportHandler.setFileType("sql");
            exportHandler.setTables(List.of(exportTable));
            exportHandler.doExport();
            String sql = Files.readString(directory.resolve("generated.sql"));
            assertTrue(sql.contains("INSERT INTO"));
            assertFalse(sql.contains("amount"));
        } finally {
            client.close();
        }
    }

    @Test
    public void testExportImportDumpAndRunSqlFile() throws Throwable {
        Path file = Files.createTempFile("easyshell-sqlite-data-", ".db");
        Files.delete(file);
        ShellSqliteClient client = this.client(file);
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT NOT NULL)");
            client.executeSqlSimple("CREATE INDEX idx_item_name ON item(name)");
            client.executeSqlSimple("CREATE VIEW item_view AS SELECT id, name FROM item");
            client.executeSqlSimple("CREATE TRIGGER item_view_insert INSTEAD OF INSERT ON item_view "
                    + "BEGIN INSERT INTO item(name) VALUES (NEW.name); END");
            client.executeSqlSimple("INSERT INTO item (name) VALUES ('one'), ('two')");
            Path directory = Files.createTempDirectory("easyshell-sqlite-output-");
            SQLiteDataExportTable exportTable = new SQLiteDataExportTable();
            exportTable.setName("item");
            exportTable.columns(client.selectColumns("item"));
            exportTable.setFilePath(directory.resolve("item.sql").toString());
            SQLiteDataExportHandler exportHandler = new SQLiteDataExportHandler(client, "main");
            exportHandler.setFileType("sql");
            exportHandler.setTables(List.of(exportTable));
            exportHandler.doExport();
            assertTrue(Files.readString(directory.resolve("item.sql")).contains("INSERT INTO"));

            exportTable.setFilePath(directory.resolve("item.csv").toString());
            exportHandler.setFileType("csv");
            exportHandler.doExport();
            assertTrue(Files.readString(directory.resolve("item.csv")).contains("one"));

            SQLiteDataExportTable viewExportTable = new SQLiteDataExportTable();
            viewExportTable.setName("item_view");
            viewExportTable.columns(client.selectColumns("item_view"));
            viewExportTable.setFilePath(directory.resolve("item-view.csv").toString());
            SQLiteDataExportHandler viewExportHandler = new SQLiteDataExportHandler(client, "main");
            viewExportHandler.setFileType("csv");
            viewExportHandler.setTables(List.of(viewExportTable));
            viewExportHandler.doExport();
            assertTrue(Files.readString(directory.resolve("item-view.csv")).contains("one"));

            client.executeSqlSimple("CREATE TABLE imported (id INTEGER PRIMARY KEY, name TEXT)");
            Path csv = directory.resolve("import.csv");
            Files.writeString(csv, "id,name\n3,three\n");
            SQLiteDataImportFile importFile = new SQLiteDataImportFile();
            importFile.setClient(client);
            importFile.setFile(csv.toFile());
            importFile.setTargetTableName("imported");
            SQLiteDataImportHandler importHandler = new SQLiteDataImportHandler(client, "main");
            importHandler.setFileType("csv");
            importHandler.setFiles(List.of(importFile));
            importHandler.importMode("insert");
            importHandler.columnIndex(0);
            importHandler.dataRowStarts(1);
            importHandler.fieldSeparator(",");
            importHandler.doImport();
            assertEquals(1, client.selectRowCount("imported"));

            Path dump = directory.resolve("dump.sql");
            SQLiteDataDumpHandler dumpHandler = new SQLiteDataDumpHandler(client, "main");
            dumpHandler.dumpFile(dump.toFile()).setDumpType((byte) 1).setDataType((byte) 0);
            dumpHandler.doDump();
            String dumpSql = Files.readString(dump);
            assertTrue(dumpSql.contains("CREATE TABLE"));
            assertEquals(1, occurrences(dumpSql, "CREATE INDEX idx_item_name"));
            assertTrue(dumpSql.indexOf("CREATE VIEW item_view") < dumpSql.indexOf("CREATE TRIGGER item_view_insert"));

            Path restoreFile = directory.resolve("restore.db");
            ShellSqliteClient restore = this.client(restoreFile);
            restore.start();
            try {
                restore.executeSqlSimple(dumpSql);
                assertEquals(2, restore.selectRowCount("item"));
                assertNotNull(restore.selectView("item_view"));
                assertNotNull(restore.showCreate("item_view_insert"));
            } finally {
                restore.close();
            }

            Path script = directory.resolve("run.sql");
            Files.writeString(script, "CREATE TABLE run_sql_table (id INTEGER PRIMARY KEY, name TEXT);"
                    + "INSERT INTO run_sql_table(name) VALUES ('from script');"
                    + "UPDATE item SET name='updated' WHERE id=1;");
            SQLiteDataRunSqlFileHandler runHandler = new SQLiteDataRunSqlFileHandler(client, "main");
            runHandler.file(script.toFile());
            runHandler.runFile();
            assertNotNull(client.selectTable("run_sql_table"));
            assertEquals(1, client.selectRowCount("run_sql_table"));
            assertTrue(client.selectRowCount("item") >= 2);
        } finally {
            client.close();
        }
    }

    @Test
    public void testTransportBetweenSqliteFiles() throws Throwable {
        Path sourceFile = Files.createTempFile("easyshell-sqlite-source-", ".db");
        Path targetFile = Files.createTempFile("easyshell-sqlite-target-", ".db");
        Files.delete(sourceFile);
        Files.delete(targetFile);
        ShellSqliteClient source = this.client(sourceFile);
        ShellSqliteClient target = this.client(targetFile);
        source.start();
        target.start();
        try {
            source.executeSqlSimple("CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT, "
                    + "CONSTRAINT name_not_empty CHECK (length(name) > 0))");
            source.executeSqlSimple("CREATE INDEX idx_item_name ON item(name)");
            source.executeSqlSimple("INSERT INTO item (name) VALUES ('transported')");
            source.executeSqlSimple("CREATE VIEW item_view AS SELECT id, name FROM item");
            source.executeSqlSimple("CREATE TRIGGER item_view_insert INSTEAD OF INSERT ON item_view "
                    + "BEGIN INSERT INTO item(name) VALUES (NEW.name); END");
            source.executeSqlSimple("CREATE TABLE audit (message TEXT)");
            source.executeSqlSimple("CREATE TRIGGER item_audit AFTER INSERT ON item "
                    + "BEGIN INSERT INTO audit(message) VALUES (NEW.name); END");
            source.executeSqlSimple("CREATE TABLE auto_item (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
            SQLiteDataTransportHandler handler = new SQLiteDataTransportHandler();
            handler.setSourceClient(source);
            handler.setTargetClient(target);
            handler.setSourceDatabase("main");
            handler.setTargetDatabase("main");
            DBDataTransportObject table = new DBDataTransportObject();
            table.setName("item");
            DBDataTransportObject autoTable = new DBDataTransportObject();
            autoTable.setName("auto_item");
            DBDataTransportObject auditTable = new DBDataTransportObject();
            auditTable.setName("audit");
            handler.setTables(List.of(table, autoTable, auditTable));
            DBDataTransportObject view = new DBDataTransportObject();
            view.setName("item_view");
            handler.setViews(List.of(view));
            handler.setDataType(0);
            handler.doTransport();
            assertEquals(1, target.selectRowCount("item"));
            assertFalse(target.showCreate("item").toUpperCase().contains("AUTOINCREMENT"));
            assertTrue(target.showCreate("item").contains("CONSTRAINT name_not_empty"));
            assertNotNull(target.showCreate("idx_item_name"));
            assertTrue(target.showCreate("auto_item").toUpperCase().contains("AUTOINCREMENT"));
            assertNotNull(target.showCreate("item_view_insert"));
            assertNotNull(target.showCreate("item_audit"));
            assertEquals(0, target.selectRowCount("audit"));
        } finally {
            source.close();
            target.close();
        }
    }

    private SQLiteColumn column(String name, String type, boolean primaryKey,
                                boolean notNull, boolean autoIncrement) {
        SQLiteColumn column = new SQLiteColumn();
        column.setName(name);
        column.setType(type);
        column.setPrimaryKey(primaryKey);
        column.setNotNull(notNull);
        column.setAutoIncrement(autoIncrement);
        return column;
    }

    private SQLiteRecordFilter filter(List<SQLiteColumn> columns, String column,
                                      SQLiteCondition condition, Object value, String joinSymbol) {
        SQLiteRecordFilter filter = new SQLiteRecordFilter();
        filter.setColumns(columns);
        filter.setColumn(columns.stream()
                .filter(item -> item.getName().equals(column)).findFirst().orElseThrow());
        filter.setCondition(condition);
        filter.setValue(value);
        filter.setJoinSymbol(joinSymbol);
        return filter;
    }

    private ShellSqliteClient client(Path file) {
        return this.client(file, true);
    }

    private ShellSqliteClient client(Path file, boolean foreignKeys) {
        ShellConnect connect = new ShellConnect();
        connect.setName(file.getFileName().toString());
        connect.setType(ShellPrototype.SQLITE);
        connect.setOsType(ShellPrototype.SQLITE);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setReadonly(false);
        connect.setEnvironment("createIfMissing=true\nforeign_keys=" + foreignKeys + "\n");
        return new ShellSqliteClient(connect);
    }

    private int occurrences(String value, String token) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
