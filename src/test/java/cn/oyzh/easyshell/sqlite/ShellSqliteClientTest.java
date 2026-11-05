package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.database.SQLiteDatabase;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.fx.db.DBRawQueryResults;
import cn.oyzh.fx.db.query.DBQueryResults;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * SQLite 客户端、元数据、SQL 与生命周期测试。
 *
 * @since 2026-11-02
 */
public class ShellSqliteClientTest {

    private Path workDirectory;

    @After
    public void tearDown() throws Exception {
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

    @Test
    public void testCreateOpenAndReopen() throws Throwable {
        Path file = this.path("lifecycle.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        assertTrue(Files.isRegularFile(file));
        this.assertSuccess(client.executeSql("CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT)"));

        client.close();
        assertFalse(client.isConnected());
        client.start();
        assertTrue(client.isConnected());
        assertEquals(1, client.tableSize("main"));
        assertEquals(2, client.selectColumns("item").size());
        client.close();

        ShellSqliteClient readOnly = this.client(file, true, false);
        readOnly.start();
        assertTrue(readOnly.isConnected());
        assertEquals(1, readOnly.selectTables().size());
        DBQueryResults<SQLiteExecuteResult> failed = readOnly.executeSql("INSERT INTO item(name) VALUES('blocked')");
        assertFalse(failed.isSuccess());
        readOnly.close();
    }

    @Test
    public void testMetadataWithQuotedObjects() throws Throwable {
        Path file = this.path("metadata.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql(
                "CREATE TABLE \"order\" (\"select\" TEXT NOT NULL DEFAULT 'x', \"a\"\"b\" BLOB);"
                        + "CREATE VIEW \"view order\" AS SELECT \"select\" FROM \"order\";"));

        assertEquals(List.of("order"), client.selectTables().stream().map(SQLiteTable::getName).toList());
        assertEquals(List.of("view order"), client.selectViews().stream().map(SQLiteView::getName).toList());
        assertEquals(1, client.tableSize("main"));
        assertEquals(1, client.viewSize("main"));
        assertNull(client.selectTable("view order"));
        assertNull(client.selectView("order"));
        List<SQLiteColumn> columns = client.selectColumns("order");
        assertEquals(2, columns.size());
        assertEquals("select", columns.get(0).getName());
        assertTrue(columns.get(0).isNotNull());
        assertEquals("a\"b", columns.get(1).getName());
        assertNotNull(client.showCreate("order"));
        SQLiteDatabase database = client.database();
        assertNotNull(database);
        assertTrue(database.isForeignKeys());
        assertEquals(0, client.selectRowCount("order"));
        List<String> collations = client.selectCollations();
        assertTrue(collations.contains("BINARY"));
        assertTrue(collations.contains("NOCASE"));
        assertTrue(collations.contains("RTRIM"));
        assertEquals(collations.size(), collations.stream()
                .map(item -> item.toUpperCase(java.util.Locale.ROOT)).distinct().count());
        client.close();
    }

    @Test
    public void testQueryResultEditingMetadata() throws Throwable {
        Path file = this.path("query-editing.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql(
                "CREATE TABLE item (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER);"
                        + "CREATE TABLE typed_item (code TEXT PRIMARY KEY, value TEXT);"
                        + "CREATE VIEW item_view AS SELECT name FROM item;"
                        + "INSERT INTO item(name, age) VALUES('first', 18);"));

        SQLiteExecuteResult allColumns = client.executeSingleSql("SELECT * FROM item");
        assertTrue(allColumns.isUpdatable());
        assertTrue(allColumns.isFullColumn());
        assertEquals("main", allColumns.dbName());
        assertEquals("item", allColumns.tableName());
        assertNotNull(allColumns.getPrimaryKey());
        assertEquals("id", allColumns.getPrimaryKey().getName());
        assertTrue(allColumns.getPrimaryKey().isPrimaryKey());
        assertEquals("first", allColumns.getRecords().getFirst().getValue("name"));

        SQLiteExecuteResult withoutPrimaryKey = client.executeSingleSql("SELECT name FROM item");
        assertFalse(withoutPrimaryKey.isUpdatable());
        assertFalse(withoutPrimaryKey.isFullColumn());

        SQLiteExecuteResult partialColumns = client.executeSingleSql("SELECT id, name FROM item");
        assertTrue(partialColumns.isUpdatable());
        assertFalse(partialColumns.isFullColumn());

        SQLiteExecuteResult nonAutoIncrementKey = client.executeSingleSql("SELECT * FROM typed_item");
        assertTrue(nonAutoIncrementKey.isUpdatable());
        assertNotNull(nonAutoIncrementKey.getPrimaryKey());
        assertEquals("code", nonAutoIncrementKey.getPrimaryKey().getName());
        assertFalse(nonAutoIncrementKey.getPrimaryKey().isAutoIncrement());

        SQLiteExecuteResult viewResult = client.executeSingleSql("SELECT * FROM item_view");
        assertFalse(viewResult.isUpdatable());
        assertTrue(viewResult.isFullColumn());
        client.close();
    }

    @Test
    public void testViewRecordsAreReadonly() throws Throwable {
        Path file = this.path("view-readonly.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        try {
            this.assertSuccess(client.executeSql(
                    "CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT);"
                            + "INSERT INTO item(name) VALUES('view row'), (NULL), ('NULL');"
                            + "CREATE VIEW item_view AS SELECT id, name FROM item;"));

            List<SQLiteRecord> records = client.viewRecords("main", "item_view", 0L, 10L, null);
            assertEquals(3, records.size());
            SQLiteRecord record = records.stream()
                    .filter(item -> "view row".equals(item.getProperty("name").getValue()))
                    .findFirst()
                    .orElseThrow();
            assertTrue(record.getProperty("id").isReadonly());
            assertTrue(record.getProperty("name").isReadonly());
            assertEquals("view row", record.getProperty("name").getValue());
            assertFalse(record.getProperty("name").getValue() instanceof javafx.scene.Node);
            assertEquals(1, records.stream()
                    .filter(item -> item.getProperty("name").getValue() == null)
                    .count());
            assertEquals(1, records.stream()
                    .filter(item -> "NULL".equals(item.getProperty("name").getValue()))
                    .count());
        } finally {
            client.close();
        }
    }

    @Test
    public void testTableAndViewInfoMetadata() throws Throwable {
        Path file = this.path("object-info.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql(
                "CREATE TABLE report (id INTEGER PRIMARY KEY, amount REAL, "
                        + "total REAL GENERATED ALWAYS AS (amount * 2) VIRTUAL, "
                        + "CONSTRAINT amount_positive CHECK (amount >= 0)) WITHOUT ROWID; "
                        + "CREATE VIEW report_view AS SELECT id, total FROM report;"));

        SQLiteTable table = client.selectTable("report");
        assertNotNull(table);
        assertEquals("report", table.getName());
        assertEquals("table", table.getType());
        assertTrue(table.isWithoutRowid());
        assertFalse(table.isStrict());
        assertEquals(3, table.getColumns().size());
        assertTrue(table.getConstraints().contains("CONSTRAINT amount_positive CHECK"));
        assertEquals(3, client.selectColumns(table.getName()).size());
        assertEquals(0, client.selectRowCount(table.getName()));
        assertNotNull(client.showCreate(table.getName()));

        SQLiteView view = client.selectView("report_view");
        assertNotNull(view);
        assertEquals("report_view", view.getName());
        assertEquals("SELECT id, total FROM report", view.getDefinition());
        assertEquals(2, client.selectColumns(view.getName()).size());
        assertEquals("CREATE VIEW report_view AS SELECT id, total FROM report", client.showCreate(view.getName()));

        List<SQLiteFunction> functions = client.selectFunctions();
        assertFalse(functions.isEmpty());
        SQLiteFunction function = functions.stream()
                .filter(item -> "abs".equals(item.getName()))
                .findFirst()
                .orElse(functions.getFirst());
        assertNotNull(function.getName());
        assertEquals(SQLiteFunction.KIND_BUILTIN, function.getKind());
        assertEquals(1, function.getArgumentCount());
        assertTrue(function.isDeterministic());
        assertFalse(function.isDirectOnly());
        assertFalse(function.isFinalFunction());
        client.close();
    }

    @Test
    public void testSqlTransactionAndResults() throws Throwable {
        Path file = this.path("sql.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql("CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT, data BLOB)"));
        DBQueryResults<SQLiteExecuteResult> explain = client.explainSql("SELECT * FROM item WHERE id = 1");
        assertTrue(explain.getErrMsg(), explain.isSuccess());
        assertFalse(explain.getResults().isEmpty());
        assertFalse(explain.getResults().getFirst().getColumns().isEmpty());

        DBQueryResults<SQLiteExecuteResult> rolledBack = client.executeSql(
                "INSERT INTO item(name) VALUES('rollback'); INSERT INTO missing VALUES(1)");
        assertFalse(rolledBack.isSuccess());
        assertEquals(0, client.selectRowCount("item"));

        DBQueryResults<SQLiteExecuteResult> committed = client.executeSql(
                "INSERT INTO item(name,data) VALUES('中文', X'00FF');"
                        + "INSERT INTO item(name,data) VALUES(NULL, NULL);"
                        + "SELECT * FROM item ORDER BY id");
        assertTrue(committed.isSuccess());
        assertEquals(3, committed.getResults().size());
        SQLiteExecuteResult result = committed.getResults().get(2);
        assertEquals(2, result.getCount());
        assertEquals("中文", result.getRecords().getFirst().getValue("name"));
        assertArrayEquals(new byte[]{0, (byte) 0xFF}, (byte[]) result.getRecords().getFirst().getValue("data"));
        assertTrue(LLMDBToolsUtil.format(committed).contains("name"));
        assertTrue(LLMDBToolsUtil.format(committed).contains("中文"));
        ShellSqliteLLMTools llmTools = new ShellSqliteLLMTools();
        llmTools.setClient(client);
        assertTrue(llmTools.executeSql("SELECT name FROM item WHERE id = 1").contains("中文"));
        assertEquals(1, llmTools.executeSqlRaw("SELECT name FROM item WHERE id = 1").size());
        llmTools.close();
        assertEquals("NULL", ShellSqliteHelper.displayValue(result.getRecords().get(1).getValue("name")));

        DBQueryResults<SQLiteExecuteResult> explicit = client.executeSql(
                "BEGIN; INSERT INTO item(name) VALUES('explicit'); COMMIT;");
        assertTrue(explicit.isSuccess());
        assertEquals(3, client.selectRowCount("item"));

        DBQueryResults<SQLiteExecuteResult> savepoint = client.executeSql(
                "SAVEPOINT s1; INSERT INTO item(name) VALUES('savepoint rollback'); "
                        + "ROLLBACK TO s1; INSERT INTO item(name) VALUES('savepoint commit'); RELEASE s1; COMMIT;");
        assertTrue(savepoint.isSuccess());
        assertEquals(4, client.selectRowCount("item"));
        client.close();
    }

    @Test
    public void testRawResultsAndErrorPaths() throws Throwable {
        Path file = this.path("raw.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql("CREATE TABLE item (id INTEGER PRIMARY KEY)"));
        try (DBRawQueryResults raw = client.executeSqlRaw("main", "SELECT 1 AS value")) {
            assertEquals(1, raw.getResultSets().size());
            ResultSet resultSet = raw.getResultSets().getFirst();
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("value"));
        }

        DBQueryResults<SQLiteExecuteResult> invalid = client.executeSql("SELECT * FROM");
        assertFalse(invalid.isSuccess());
        assertNotNull(invalid.getErrMsg());
        client.close();

        try {
            this.client(this.path("missing.db"), false, false).start();
            fail("Missing database must fail");
        } catch (ShellException expected) {
            assertTrue(expected.getMessage().contains("does not exist"));
        }

        Path corrupt = this.path("corrupt.db");
        Files.writeString(corrupt, "not a sqlite database");
        try {
            ShellSqliteClient corruptClient = this.client(corrupt, true, false);
            corruptClient.start();
            fail("Corrupt database must fail");
        } catch (ShellException expected) {
            assertTrue(expected.getMessage().contains("not a database"));
        }
    }

    @Test
    public void testEnvironmentValidationAndTransactionControl() {
        ShellSqliteConnConfig config = new ShellSqliteConnConfig();
        config.setHost(Path.of("data", "sample.db").toString());
        config.setReadOnly(true);
        config.setCreateIfMissing(false);
        config.setBusyTimeout(2500);
        assertEquals(Path.of("data", "sample.db").toAbsolutePath().normalize(), config.resolveFilePath());
        assertTrue(config.isReadOnly());
        assertEquals(2500, config.getBusyTimeout());

        assertEquals("NORMAL", ShellSqliteHelper.parseEnvironment("synchronous=NORMAL").getProperty("synchronous"));
        assertTrue(ShellSqliteHelper.environmentBoolean("createIfMissing=1", "createIfMissing", false));
        assertFalse(ShellSqliteHelper.environmentBoolean(
                ShellSqliteHelper.setEnvironmentValue("createIfMissing=1", "createIfMissing", "false"),
                "createIfMissing", true));
        assertTrue(ShellSqliteHelper.isTransactionControl("BEGIN"));
        assertEquals("BEGIN", ShellSqliteHelper.transactionCommand("-- comment\nBEGIN"));
        assertEquals("ROLLBACK_TO", ShellSqliteHelper.transactionCommand("ROLLBACK TO savepoint_1"));
        assertFalse(ShellSqliteHelper.isTransactionControl("BEGINNING"));
        assertTrue(ShellSqliteHelper.isTransactionControl("SAVEPOINT s1"));
        assertFalse(ShellSqliteHelper.isTransactionControl("SELECT 1"));
        assertTrue(ShellSqliteHelper.isForeignKeysPragma("PRAGMA foreign_keys=OFF"));
        assertTrue(ShellSqliteHelper.isForeignKeysPragma("-- comment\nPRAGMA foreign_keys = ON"));
        assertFalse(ShellSqliteHelper.isForeignKeysPragma("PRAGMA foreign_keys_enable=OFF"));
        assertFalse(ShellSqliteHelper.isForeignKeysPragma("SELECT 1"));
        try {
            ShellSqliteHelper.parseEnvironment("invalid");
            fail("Invalid environment must fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Invalid SQLite environment"));
        }
    }

    @Test
    public void testExplicitTransactionLifecycleRestoresConnectionState() throws Throwable {
        Path file = this.path("explicit-state.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql("CREATE TABLE item (id INTEGER PRIMARY KEY)"));

        this.assertSuccess(client.executeSql("-- comment\nBEGIN; INSERT INTO item DEFAULT VALUES;"));
        assertFalse(client.getConnManager().getServerConnection().getAutoCommit());
        this.assertSuccess(client.executeSql("COMMIT"));
        assertTrue(client.getConnManager().getServerConnection().getAutoCommit());

        this.assertSuccess(client.executeSql("BEGIN; INSERT INTO item DEFAULT VALUES;"));
        assertFalse(client.getConnManager().getServerConnection().getAutoCommit());
        DBQueryResults<SQLiteExecuteResult> failed = client.executeSql("INSERT INTO missing VALUES(1)");
        assertFalse(failed.isSuccess());
        assertTrue(client.getConnManager().getServerConnection().getAutoCommit());
        assertEquals(1, client.selectRowCount("item"));
        client.close();
    }

    @Test
    public void testTableLifecycleOperationsAndClone() throws Throwable {
        Path file = this.path("table-lifecycle.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql(
                "CREATE TABLE item (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT DEFAULT 'default', data BLOB);"
                        + "CREATE INDEX idx_item_name ON item(name);"
                        + "INSERT INTO item(name) VALUES ('one');"));

        client.renameTable("item", "renamed");
        assertNotNull(client.selectTable("renamed"));
        assertTrue(client.showCreate("idx_item_name").contains("renamed"));

        client.clearTable("renamed");
        assertEquals(0, client.selectRowCount("renamed"));
        client.executeSqlSimple("DELETE FROM sqlite_sequence WHERE name = 'renamed'");
        client.executeSqlSimple("INSERT INTO renamed(name) VALUES ('before truncate')");
        client.truncateTable("renamed");
        assertEquals(0, client.selectRowCount("renamed"));
        client.executeSqlSimple("INSERT INTO renamed(name) VALUES ('after truncate')");
        List<Map<String, Object>> records = client.selectRecords(
                "renamed", client.selectColumns("renamed"), 0, 10);
        assertEquals(1, records.size());
        assertEquals(1L, ((Number) records.getFirst().get("id")).longValue());
        assertEquals(1, client.selectRowCount("renamed", "name LIKE 'after%'"));
        assertEquals(1, client.selectRecords("renamed", client.selectColumns("renamed"),
                0, 10, "name LIKE 'after%'").size());

        client.cloneTable("renamed", "renamed_empty", false);
        assertEquals(0, client.selectRowCount("renamed_empty"));
        client.cloneTable("renamed", "renamed_copy", true);
        assertEquals(1, client.selectRowCount("renamed_copy"));
        assertEquals(1, client.selectSchemaObjects("index").stream()
                .filter(index -> "renamed_copy".equals(index.getTableName()))
                .count());

        client.dropTable("renamed_copy");
        assertNull(client.selectTable("renamed_copy"));
        client.dropTable("renamed_empty");
        assertNull(client.selectTable("renamed_empty"));
        client.close();
    }

    @Test
    public void testViewLifecyclePreservesTriggerAndDependencies() throws Throwable {
        Path file = this.path("view-lifecycle.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql(
                "CREATE TABLE item (id INTEGER PRIMARY KEY, name TEXT);"
                        + "CREATE VIEW item_view AS SELECT id, name FROM item;"
                        + "CREATE VIEW item_view_dependent AS SELECT name FROM item_view;"
                        + "CREATE TRIGGER item_view_insert INSTEAD OF INSERT ON item_view "
                        + "BEGIN INSERT INTO item(name) VALUES (NEW.name); END;"
                        + "CREATE TRIGGER item_view_dependent_insert INSTEAD OF INSERT ON item_view_dependent "
                        + "BEGIN INSERT INTO item(name) VALUES (NEW.name); END;"));

        client.renameView("item_view", "renamed_view");
        assertNull(client.selectView("item_view"));
        assertNotNull(client.selectView("renamed_view"));
        assertTrue(client.showCreate("item_view_dependent").contains("renamed_view"));
        assertTrue(client.showCreate("item_view_insert").contains("ON renamed_view"));
        assertTrue(client.showCreate("item_view_dependent_insert").contains("ON item_view_dependent"));
        client.executeSqlSimple("INSERT INTO renamed_view(name) VALUES ('through trigger')");
        assertEquals(1, client.selectRowCount("item"));

        client.cloneView("renamed_view", "renamed_view_copy");
        assertNotNull(client.selectView("renamed_view_copy"));
        client.dropView("renamed_view_copy");
        assertNull(client.selectView("renamed_view_copy"));
        client.close();
    }

    @Test
    public void testEmptyStringNullBlobAndDefaultsRemainDistinct() throws Throwable {
        Path file = this.path("record-values.db");
        ShellSqliteClient client = this.client(file, false, true);
        client.start();
        this.assertSuccess(client.executeSql(
                "CREATE TABLE value_item (id INTEGER PRIMARY KEY, name TEXT DEFAULT 'fallback', "
                        + "empty_text TEXT, nullable_text TEXT, data BLOB)"));
        List<SQLiteColumn> columns = client.selectColumns("value_item");
        String insert = ShellSqliteDataUtil.toInsertSql("value_item", columns, List.of(Map.of(
                "name", "",
                "empty_text", "",
                "nullable_text", "NULL",
                "data", new byte[]{0, (byte) 0xFF}
        ))).getFirst();
        assertTrue(insert.contains("\"name\""));
        client.executeSqlSimple(insert);
        Map<String, Object> record = client.selectRecords("value_item", columns, 0, 1).getFirst();
        assertEquals("", record.get("name"));
        assertEquals("", record.get("empty_text"));
        assertEquals("NULL", record.get("nullable_text"));
        assertArrayEquals(new byte[]{0, (byte) 0xFF}, (byte[]) record.get("data"));

        String defaults = ShellSqliteDataUtil.toInsertSql(
                "value_item", columns, List.of(new java.util.HashMap<>())).getFirst();
        assertEquals("INSERT INTO \"value_item\" DEFAULT VALUES;", defaults);
        client.executeSqlSimple(defaults);
        List<Map<String, Object>> rows = client.selectRecords("value_item", columns, 0, 10);
        assertEquals("fallback", rows.get(1).get("name"));
        assertNotNull(rows.get(1).get("id"));
        client.close();
    }

    private ShellSqliteClient client(Path file, boolean readOnly, boolean createIfMissing) {
        ShellConnect connect = new ShellConnect();
        connect.setName(file.getFileName().toString());
        connect.setType(ShellPrototype.SQLITE);
        connect.setOsType(ShellPrototype.SQLITE);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setReadonly(readOnly);
        connect.setEnvironment("journal_mode=DELETE\nforeign_keys=1\nsynchronous=NORMAL\ncreateIfMissing="
                + createIfMissing + "\n");
        return new ShellSqliteClient(connect);
    }

    private Path path(String name) throws Exception {
        if (this.workDirectory == null) {
            this.workDirectory = Files.createTempDirectory("easyshell-sqlite-test-");
        }
        return this.workDirectory.resolve(name);
    }

    private void assertSuccess(DBQueryResults<SQLiteExecuteResult> results) {
        assertNotNull(results);
        assertTrue(results.getErrMsg(), results.isSuccess());
    }
}
