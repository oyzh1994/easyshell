package cn.oyzh.easyshell.h2;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.data.h2.handler.H2DataExportHandler;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser;
import cn.oyzh.easyshell.h2.check.H2Check;
import cn.oyzh.easyshell.h2.database.H2Database;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.h2.index.H2IndexControl;
import cn.oyzh.easyshell.h2.record.H2InsertRecordParam;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.query.DBQueryResults;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * H2 客户端集成测试：生命周期、元数据、SQL 执行、事务、记录与对象生命周期。
 *
 * @since 2026-11-28
 */
public class ShellH2ClientTest {

    private Path tempDir;

    @Before
    public void setUp() throws Throwable {
        this.tempDir = Files.createTempDirectory("easyshell-h2-client-test-");
    }

    @After
    public void tearDown() throws Throwable {
        if (this.tempDir != null && Files.exists(this.tempDir)) {
            Files.walk(this.tempDir).sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private ShellConnect connect(Path file, boolean readOnly, boolean createIfMissing) {
        ShellConnect connect = new ShellConnect();
        connect.setName(file.getFileName().toString());
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setReadonly(readOnly);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=" + createIfMissing + "\n");
        return connect;
    }

    @Test
    public void testSchemaScopedObjectsAndRoutines() throws Throwable {
        Path file = this.tempDir.resolve("schema-tree");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE SCHEMA S1");
        client.executeSqlSimple("CREATE TABLE S1.T1(ID INT PRIMARY KEY, NAME VARCHAR(20))");
        client.executeSqlSimple("CREATE VIEW S1.V1 AS SELECT ID, NAME FROM S1.T1");
        client.executeSqlSimple("CREATE SEQUENCE S1.SEQ1 START WITH 10");
        client.executeSqlSimple("CREATE ALIAS S1.F_SQRT FOR \"java.lang.Math.sqrt\"");
        client.executeSqlSimple("INSERT INTO S1.T1 VALUES(1, 'one')");

        List<String> schemas = client.selectSchemas().stream()
                .map(schema -> schema.getName()).toList();
        Assert.assertTrue(schemas.toString(), schemas.contains("PUBLIC"));
        Assert.assertTrue(schemas.toString(), schemas.contains("S1"));
        Assert.assertTrue(schemas.toString(), schemas.contains("INFORMATION_SCHEMA"));

        Assert.assertEquals(List.of("T1"), client.selectTablesSimple("S1").stream()
                .map(H2Table::getName).toList());
        Assert.assertEquals(List.of("V1"), client.selectViewsSimple("S1").stream()
                .map(H2View::getName).toList());
        Assert.assertEquals(2, client.selectColumns("S1", "T1").size());
        Assert.assertEquals(List.of("SEQ1"), client.selectSequences("S1").stream()
                .map(sequence -> sequence.getName()).toList());
        Assert.assertEquals(List.of("F_SQRT"), client.selectFunctions("S1").stream()
                .map(routine -> routine.getName()).toList());

        H2SelectRecordParam param = new H2SelectRecordParam();
        param.setDbName("S1");
        param.setTableName("T1");
        Assert.assertEquals(1, client.selectRecords(param).size());
        client.close();
    }

    @Test
    public void testFunctionAliasLifecycle() throws Throwable {
        Path file = this.tempDir.resolve("function-alias-lifecycle");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        try {
            client.createSchema("APP");
            H2Routine function = new H2Routine();
            function.setSchema("APP");
            function.setName("F_ADD");
            function.setType("FUNCTION");
            function.setDefinition("""
                    public class H2Functions {
                        public static int add(int a, int b) {
                            return a + b;
                        }
                    }
                    """);
            function.setDeterministic(true);
            H2Routine created = client.createFunction(function);
            Assert.assertNotNull(created);
            Assert.assertTrue(created.isDeterministic());
            Assert.assertTrue(created.getDefinition().contains("return a + b"));

            function.setDefinition("int add(int a, int b) { return a + b + 1; }");
            H2Routine altered = client.alterFunction("F_ADD", function);
            Assert.assertTrue(altered.getDefinition().contains("a + b + 1"));

            H2Routine renamed = client.renameFunction("APP", "F_ADD", "F_PLUS");
            Assert.assertNotNull(renamed);
            Assert.assertNull(client.selectFunction("APP", "F_ADD"));
            Assert.assertNotNull(client.selectFunction("APP", "F_PLUS"));

            Assert.assertNotNull(client.cloneFunction("APP", "F_PLUS", "F_COPY"));
            Assert.assertNotNull(client.selectFunction("APP", "F_COPY"));

            H2Routine external = new H2Routine();
            external.setSchema("APP");
            external.setName("F_SQRT");
            external.setType("FUNCTION");
            external.setExternalName("java.lang.Math.sqrt");
            client.createFunction(external);
            Assert.assertEquals("java.lang.Math.sqrt",
                    client.selectFunction("APP", "F_SQRT").getExternalName());

            client.dropFunction("APP", "F_PLUS");
            Assert.assertNull(client.selectFunction("APP", "F_PLUS"));
        } finally {
            client.close();
        }
    }

    @Test
    public void testProcedureAliasLifecycle() throws Throwable {
        Path file = this.tempDir.resolve("procedure-alias-lifecycle");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        try {
            client.createSchema("APP");
            H2Routine procedure = new H2Routine();
            procedure.setSchema("APP");
            procedure.setName("P_ECHO");
            procedure.setType("PROCEDURE");
            procedure.setDefinition("void echo(String value) { System.out.println(value); }");
            H2Routine created = client.createRoutine(procedure);
            Assert.assertNotNull(created);
            Assert.assertTrue(created.isProcedure());
            Assert.assertNotNull(client.selectProcedure("APP", "P_ECHO"));

            procedure.setDefinition("void echo(String value) { System.out.print(value); }");
            client.alterRoutine("P_ECHO", procedure);
            Assert.assertTrue(client.selectProcedure("APP", "P_ECHO")
                    .getDefinition().contains("System.out.print"));

            client.renameFunction("APP", "P_ECHO", "P_PRINT");
            Assert.assertNull(client.selectProcedure("APP", "P_ECHO"));
            Assert.assertNotNull(client.selectProcedure("APP", "P_PRINT"));
            client.dropFunction("APP", "P_PRINT");
        } finally {
            client.close();
        }
    }

    @Test
    public void testSequenceLifecycle() throws Throwable {
        Path file = this.tempDir.resolve("sequence-lifecycle");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        try {
            client.createSchema("APP");
            H2Sequence sequence = new H2Sequence();
            sequence.setSchema("APP");
            sequence.setName("SEQ_ORDER");
            sequence.setStartValue("100");
            sequence.setIncrement("5");
            sequence.setMinimumValue("1");
            sequence.setMaximumValue("999999");
            sequence.setCacheSize("7");
            sequence.setCycle(true);
            H2Sequence created = client.createSequence(sequence);
            Assert.assertNotNull(created);
            Assert.assertEquals("5", created.getIncrement());
            Assert.assertEquals("7", created.getCacheSize());
            Assert.assertEquals("100", created.getCurrentValue());
            Assert.assertTrue(created.isCycle());

            sequence.setStartValue("200");
            sequence.setIncrement("10");
            sequence.setCacheSize("9");
            client.alterSequence("SEQ_ORDER", sequence);
            H2Sequence altered = client.selectSequence("APP", "SEQ_ORDER");
            Assert.assertEquals("10", altered.getIncrement());
            Assert.assertEquals("9", altered.getCacheSize());
            Assert.assertEquals("200", altered.getCurrentValue());
            Object nextValue = client.executeSql("SELECT NEXT VALUE FOR APP.SEQ_ORDER")
                    .getResults().getFirst().getRecords().getFirst()
                    .getRecordData().values().iterator().next();
            Assert.assertEquals(200L, ((Number) nextValue).longValue());

            client.renameSequence("APP", "SEQ_ORDER", "SEQ_RENAMED");
            Assert.assertNull(client.selectSequence("APP", "SEQ_ORDER"));
            Assert.assertNotNull(client.selectSequence("APP", "SEQ_RENAMED"));
            client.dropSequence("APP", "SEQ_RENAMED");
            Assert.assertNull(client.selectSequence("APP", "SEQ_RENAMED"));
        } finally {
            client.close();
        }
    }

    @Test
    public void testQueryAndExportUseSelectedSchema() throws Throwable {
        Path file = this.tempDir.resolve("query-schema");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        try {
            client.createSchema("APP");
            client.executeSqlSimple("APP", "CREATE TABLE ITEM(ID INT)");
            H2ExecuteResult result = client.executeSingleSql(
                    "APP", "SELECT * FROM ITEM");
            Assert.assertEquals("APP", result.dbName());

            H2DataExportHandler export = new H2DataExportHandler(client, "APP");
            Assert.assertEquals("APP", export.getSchema());
        } finally {
            client.close();
        }
    }

    @Test
    public void testSchemaCreateRenameAndDrop() throws Throwable {
        Path file = this.tempDir.resolve("schema-lifecycle");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.createSchema("TEMP_SCHEMA");
        Assert.assertNotNull(client.findSchema("TEMP_SCHEMA"));
        client.renameSchema("TEMP_SCHEMA", "RENAMED_SCHEMA");
        Assert.assertNull(client.findSchema("TEMP_SCHEMA"));
        Assert.assertNotNull(client.findSchema("RENAMED_SCHEMA"));
        client.dropSchema("RENAMED_SCHEMA");
        Assert.assertNull(client.findSchema("RENAMED_SCHEMA"));
        client.close();
    }

    @Test
    public void testHashIndexTypeRoundTrip() throws Throwable {
        Path file = this.tempDir.resolve("index-type-round-trip");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE T1(ID INT, NAME VARCHAR(20))");
        client.executeSqlSimple("CREATE UNIQUE HASH INDEX IDX_HASH ON T1(ID)");

        H2Index index = client.selectIndexes("PUBLIC", "T1").stream()
                .filter(item -> "IDX_HASH".equalsIgnoreCase(item.getName()))
                .findFirst().orElseThrow();
        Assert.assertTrue(index.isUnique());
        H2IndexControl control = H2IndexControl.of(index);
        Assert.assertEquals("HASH", control.getIndexType());
        Assert.assertTrue(control.isUnique());
        client.close();
    }

    @Test
    public void testDeleteCheckConstraint() throws Throwable {
        Path file = this.tempDir.resolve("delete-check");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE T(ID INT PRIMARY KEY, "
                + "AMOUNT NUMERIC(10,2), CONSTRAINT CK_AMOUNT CHECK(AMOUNT >= 0))");
        H2Check check = client.selectChecks("PUBLIC", "T").getFirst();
        check.setDeleted(true);

        H2Table target = new H2Table("T");
        target.setDbName("PUBLIC");
        target.setColumns(client.selectColumns("PUBLIC", "T"));
        target.setChecks(List.of(check));
        new cn.oyzh.easyshell.h2.schema.H2SchemaManager(client)
                .updateTable("T", target);

        Assert.assertTrue(client.selectChecks("PUBLIC", "T").isEmpty());
        client.close();
    }

    @Test
    public void testTableAndColumnComments() throws Throwable {
        Path file = this.tempDir.resolve("table-comments");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        H2Column id = new H2Column("ID");
        id.setType("INTEGER");
        id.setPrimaryKey(true);
        id.setComment("primary id");
        H2Column name = new H2Column("NAME");
        name.setType("VARCHAR");
        name.setSize(40);
        H2Table table = new H2Table("COMMENT_TABLE");
        table.setDbName("PUBLIC");
        table.setComment("table comment");
        table.setColumns(List.of(id, name));
        cn.oyzh.easyshell.h2.schema.H2SchemaManager manager =
                new cn.oyzh.easyshell.h2.schema.H2SchemaManager(client);
        manager.createTable(table);

        H2Table loaded = client.selectTable("PUBLIC", "COMMENT_TABLE");
        Assert.assertEquals("table comment", loaded.getComment());
        Assert.assertEquals("primary id", loaded.getColumns().stream()
                .filter(column -> "ID".equals(column.getName()))
                .findFirst().orElseThrow().getComment());

        List<H2Column> columns = client.selectColumns("PUBLIC", "COMMENT_TABLE");
        H2Column loadedId = columns.stream()
                .filter(column -> "ID".equals(column.getName()))
                .findFirst().orElseThrow();
        H2Column loadedName = columns.stream()
                .filter(column -> "NAME".equals(column.getName()))
                .findFirst().orElseThrow();
        loadedId.setComment("new primary id");
        loadedName.setComment("display name");
        H2Table target = new H2Table("COMMENT_TABLE");
        target.setDbName("PUBLIC");
        target.setColumns(columns);
        target.setComment("updated table comment");
        manager.updateTable("COMMENT_TABLE", target);

        H2Table updated = client.selectTable("PUBLIC", "COMMENT_TABLE");
        Assert.assertEquals("updated table comment", updated.getComment());
        Assert.assertEquals("display name", updated.getColumns().stream()
                .filter(column -> "NAME".equals(column.getName()))
                .findFirst().orElseThrow().getComment());
        target.setComment(null);
        manager.updateTable("COMMENT_TABLE", target);
        Assert.assertNull(client.selectTable("PUBLIC", "COMMENT_TABLE").getComment());
        client.close();
    }

    @Test
    public void testColumnCheckRoundTripAndUpdate() throws Throwable {
        Path file = this.tempDir.resolve("column-check");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        H2Column id = new H2Column("ID");
        id.setType("INTEGER");
        id.setPrimaryKey(true);
        H2Column amount = new H2Column("AMOUNT");
        amount.setType("NUMERIC");
        amount.setSize(10);
        amount.setDigits(2);
        amount.setCheck("AMOUNT >= 0");
        H2Table table = new H2Table("COLUMN_CHECK");
        table.setDbName("PUBLIC");
        table.setColumns(List.of(id, amount));
        cn.oyzh.easyshell.h2.schema.H2SchemaManager manager =
                new cn.oyzh.easyshell.h2.schema.H2SchemaManager(client);
        manager.createTable(table);

        H2Table loaded = client.selectTable("PUBLIC", "COLUMN_CHECK");
        H2Column loadedAmount = loaded.getColumns().stream()
                .filter(column -> "AMOUNT".equals(column.getName()))
                .findFirst().orElseThrow();
        Assert.assertNotNull(loadedAmount.getCheck());
        Assert.assertNotNull(loadedAmount.getCheckName());
        Assert.assertEquals(0, H2ColumnDefinitionParser.checksExcludingColumns(
                client.showCreate("PUBLIC", "COLUMN_CHECK"), loaded.getColumns()).size());

        loadedAmount.setCheck("AMOUNT >= 10");
        H2Table target = new H2Table("COLUMN_CHECK");
        target.setDbName("PUBLIC");
        target.setColumns(loaded.getColumns());
        manager.updateTable("COLUMN_CHECK", target);
        H2Column updated = client.selectTable("PUBLIC", "COLUMN_CHECK").getColumns().stream()
                .filter(column -> "AMOUNT".equals(column.getName()))
                .findFirst().orElseThrow();
        Assert.assertTrue(updated.getCheck(), updated.getCheck().contains("10"));
        client.close();
    }

    @Test
    public void testSqlTriggerBodyCreatesH2TriggerSource() throws Throwable {
        Path file = this.tempDir.resolve("trigger-source");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE \"t1\"(\"a\" VARCHAR(10))");
        client.executeSqlSimple("CREATE TABLE \"t2\"(\"a\" VARCHAR(10))");
        H2Trigger trigger = new H2Trigger("TR_COPY");
        trigger.setTableName("t2");
        trigger.setPolicy("BEFORE INSERT");
        trigger.setDefinition("insert into \"t1\"(\"a\") values('a');");

        String sql = trigger.toCreateSql("t2");
        Assert.assertTrue(sql, sql.contains(" AS '"));
        Assert.assertFalse(sql, sql.contains("\nBEGIN\n"));
        Assert.assertTrue(sql, sql.contains("conn.setAutoCommit(false)"));
        // 模拟此前版本已保存、未带 autoCommit 防护的触发器源码
        String legacySql = sql.replace("""
                            if (conn.getAutoCommit()) {
                                conn.setAutoCommit(false);
                            }
                """, "");
        client.executeSqlSimple(legacySql);
        java.sql.Connection connection = client.getConnManager().connection();
        connection.setAutoCommit(true);
        H2Column column = client.selectColumns("PUBLIC", "t2").getFirst();
        DBRecordData record = new DBRecordData();
        record.put(column, "x");
        H2InsertRecordParam insert = new H2InsertRecordParam();
        insert.setTableName("t2");
        insert.setDbName("PUBLIC");
        insert.setRecord(record);
        Assert.assertEquals(1, client.insertRecord(insert));
        Assert.assertEquals(1, client.selectRowCount("\"t1\""));
        H2Trigger loaded = client.selectTriggers("PUBLIC", "t2").getFirst();
        Assert.assertFalse("DDL=" + loaded.getCreateDefinition()
                + "\ndefinition=" + loaded.getDefinition(), loaded.isInvalid());
        Assert.assertEquals("BEFORE INSERT", loaded.getPolicy());
        Assert.assertEquals("DDL=" + loaded.getCreateDefinition(),
                "insert into \"t1\"(\"a\") values('a');", loaded.getDefinition());
        client.close();
    }

    /**
     * 文件创建、重开、只读拒绝写入。
     */
    @Test
    public void testLifecycleAndReadonly() throws Throwable {
        Path file = this.tempDir.resolve("life");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        Assert.assertTrue(client.isConnected());
        client.executeSql("CREATE TABLE t1(id INT PRIMARY KEY, name VARCHAR(20))");
        client.executeSql("INSERT INTO t1 VALUES(1, 'a')");
        client.close();
        Assert.assertFalse(client.isConnected());

        // 重开已有文件
        client.start(5);
        Assert.assertEquals(1, client.selectTablesSimple("public").size());
        client.close();

        // 只读打开并拒绝写入
        ShellH2Client readonlyClient = new ShellH2Client(this.connect(file, true, false));
        readonlyClient.start(5);
        Assert.assertTrue(readonlyClient.isConnected());
        DBQueryResults<H2ExecuteResult> results = readonlyClient.executeSql("INSERT INTO t1 VALUES(2, 'b')");
        Assert.assertFalse(results.isSuccess());
        readonlyClient.close();
    }

    /**
     * 未开启创建且文件不存在时连接失败。
     */
    @Test
    public void testMissingFileRejected() {
        Path file = this.tempDir.resolve("missing");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, false));
        try {
            client.start(5);
            Assert.fail("should fail on missing file");
        } catch (Throwable ignore) {
        } finally {
            client.close();
        }
        Assert.assertFalse(Files.exists(file));
    }

    /**
     * 元数据：表、视图、字段、行数、数据库信息。
     */
    @Test
    public void testMetadata() throws Throwable {
        Path file = this.tempDir.resolve("meta");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSql("""
                CREATE TABLE "user"(
                    "id" INT PRIMARY KEY,
                    "name" VARCHAR(50) NOT NULL,
                    "score" DECIMAL(10,2) DEFAULT 0,
                    "remark" VARCHAR(100)
                )""");
        client.executeSql("CREATE VIEW \"user_view\" AS SELECT \"id\", \"name\" FROM \"user\"");
        client.executeSql("INSERT INTO \"user\" VALUES(1, '张三', 88.5, '备注')");
        client.executeSql("INSERT INTO \"user\" VALUES(2, '李四', NULL, NULL)");

        List<H2Table> tables = client.selectTables();
        Assert.assertEquals(1, tables.size());
        Assert.assertEquals("user", tables.getFirst().getName());

        List<H2View> views = client.selectViews();
        Assert.assertEquals(1, views.size());
        Assert.assertEquals("user_view", views.getFirst().getName());

        List<H2Column> columns = client.selectColumns("user");
        Assert.assertEquals(4, columns.size());
        Assert.assertEquals("id", columns.get(0).getName());
        Assert.assertTrue(columns.get(0).isPrimaryKey());
        H2Column score = columns.stream()
                .filter(column -> "score".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow();
        Assert.assertEquals(Integer.valueOf(10), score.getSize());
        Assert.assertEquals(Integer.valueOf(2), score.getDigits());

        Assert.assertEquals(2, client.selectRowCount("user"));

        H2Database database = client.database();
        Assert.assertNotNull(database);
        Assert.assertNotNull(database.getVersion());
        Assert.assertEquals("PUBLIC", database.getSchema());
        client.close();

        // 保留字与引号对象名
        Assert.assertEquals("user", client.selectTable("user").getName());
    }

    /**
     * 表、约束和视图定义可由 SCRIPT/INFORMATION_SCHEMA 正确还原。
     */
    @Test
    public void testShowCreatePreservesConstraintsAndViewDefinition() throws Throwable {
        Path file = this.tempDir.resolve("show-create");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE parent(id INT PRIMARY KEY)");
        client.executeSqlSimple("""
                CREATE TABLE child(
                    id INT PRIMARY KEY,
                    code VARCHAR(20) UNIQUE,
                    amount NUMERIC(10,2) CHECK(amount >= 0),
                    parent_id INT,
                    CONSTRAINT fk_child_parent FOREIGN KEY(parent_id) REFERENCES parent(id)
                )
                """);
        client.executeSqlSimple("CREATE VIEW child_view AS SELECT id, amount FROM child");

        String tableDdl = client.showCreate("child");
        Assert.assertNotNull(tableDdl);
        Assert.assertTrue(tableDdl, tableDdl.contains("NUMERIC(10, 2)"));
        Assert.assertTrue(tableDdl, tableDdl.contains("UNIQUE"));
        Assert.assertTrue(tableDdl, tableDdl.contains("CHECK"));
        Assert.assertTrue(tableDdl, tableDdl.contains("FOREIGN KEY"));
        Assert.assertEquals(1, cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser
                .checks(tableDdl).size());
        Assert.assertEquals(1, cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser
                .foreignKeys(tableDdl).size());

        String viewDdl = client.showCreate("child_view");
        Assert.assertNotNull(viewDdl);
        Assert.assertTrue(viewDdl, viewDdl.startsWith("CREATE VIEW"));

        Path targetFile = this.tempDir.resolve("show-create-target");
        ShellH2Client target = new ShellH2Client(this.connect(targetFile, false, true));
        target.start(5);
        target.executeSqlSimple("CREATE TABLE parent(id INT PRIMARY KEY)");
        target.executeSqlSimple(tableDdl);
        target.executeSqlSimple(viewDdl);
        Assert.assertNotNull(target.selectTable("child"));
        Assert.assertNotNull(target.selectView("child_view"));
        target.close();
        client.close();
    }

    /**
     * 修改小数精度和 CHECK 约束时应生成真实的 H2 ALTER 语句。
     */
    @Test
    public void testAlterPrecisionAndCheckConstraint() throws Throwable {
        Path file = this.tempDir.resolve("alter-constraints");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE t(id INT PRIMARY KEY, "
                + "amount NUMERIC(10,2), CONSTRAINT ck_amount CHECK(amount >= 0))");

        List<H2Column> columns = client.selectColumns("t");
        H2Column amount = columns.stream()
                .filter(column -> "amount".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow();
        amount.setSize(12);
        amount.setDigits(3);

        H2Check check = client.selectChecks(client.defaultDatabaseName(), "t").getFirst();
        check.setClause("amount >= 10");
        check.setChanged(true);

        H2Table target = new H2Table("T");
        target.setColumns(columns);
        target.setChecks(List.of(check));
        new cn.oyzh.easyshell.h2.schema.H2SchemaManager(client).updateTable("t", target);

        H2Column updated = client.selectColumns("t").stream()
                .filter(column -> "amount".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow();
        Assert.assertEquals(Integer.valueOf(12), updated.getSize());
        Assert.assertEquals(Integer.valueOf(3), updated.getDigits());
        String ddl = client.showCreate("t");
        Assert.assertTrue(ddl, ddl.contains("\"CK_AMOUNT\""));
        Assert.assertTrue(ddl, ddl.contains("CAST(10"));
        client.close();
    }

    /**
     * 连续保存表设计时，既有索引不能被当成新增索引重复创建。
     */
    @Test
    public void testUpdateTableDoesNotRecreateExistingIndexes() throws Throwable {
        Path file = this.tempDir.resolve("alter-existing-indexes");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE t1(a INT, b INT)");
        client.executeSqlSimple("CREATE INDEX idx_t1_a ON t1(a)");

        H2Table target = new H2Table("T1");
        target.setColumns(client.selectColumns("t1"));
        cn.oyzh.easyshell.h2.schema.H2SchemaManager manager =
                new cn.oyzh.easyshell.h2.schema.H2SchemaManager(client);
        manager.updateTable("t1", target);

        client.executeSqlSimple("CREATE INDEX idx_t1_b ON t1(b)");
        manager.updateTable("t1", target);

        List<String> indexNames = client.selectIndexes(
                        client.defaultDatabaseName(), "t1").stream()
                .map(index -> index.getName().toUpperCase(java.util.Locale.ROOT))
                .toList();
        Assert.assertTrue(indexNames.toString(), indexNames.contains("IDX_T1_A"));
        Assert.assertTrue(indexNames.toString(), indexNames.contains("IDX_T1_B"));
        client.close();
    }

    /**
     * dump 时外键应在全部表创建后恢复。
     */
    @Test
    public void testDumpRestoresDeferredForeignKeys() throws Throwable {
        Path sourceFile = this.tempDir.resolve("dump-source");
        ShellH2Client source = new ShellH2Client(this.connect(sourceFile, false, true));
        source.start(5);
        source.executeSqlSimple("CREATE TABLE parent(id INT PRIMARY KEY)");
        source.executeSqlSimple("CREATE TABLE child(id INT PRIMARY KEY, parent_id INT, "
                + "CONSTRAINT fk_parent FOREIGN KEY(parent_id) REFERENCES parent(id))");
        source.executeSqlSimple("INSERT INTO parent VALUES(1)");
        source.executeSqlSimple("INSERT INTO child VALUES(2, 1)");

        Path dumpFile = this.tempDir.resolve("foreign-key-dump.sql");
        cn.oyzh.easyshell.data.h2.handler.H2DataDumpHandler handler =
                new cn.oyzh.easyshell.data.h2.handler.H2DataDumpHandler(
                        source, source.defaultDatabaseName());
        handler.dumpFile(dumpFile.toFile()).setDumpType((byte) 1).setDataType((byte) 0);
        handler.doAction();
        source.close();

        ShellH2Client restore = new ShellH2Client(
                this.connect(this.tempDir.resolve("dump-target"), false, true));
        restore.start(5);
        restore.executeSqlSimple(Files.readString(dumpFile));
        Assert.assertEquals(1, restore.selectRowCount("child"));
        Assert.assertFalse(restore.selectForeignKeys(
                restore.defaultDatabaseName(), "child").isEmpty());
        Assert.assertNotNull(restore.selectTable("child"));
        restore.close();
    }

    /**
     * H2 视图没有主键约束；简单单表投影可推断，JOIN 视图不推断。
     */
    @Test
    public void testInferPrimaryKeyForSimpleViews() throws Throwable {
        Path file = this.tempDir.resolve("view-primary-key");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSqlSimple("CREATE TABLE dept(id BIGINT PRIMARY KEY, name VARCHAR(40))");
        client.executeSqlSimple("CREATE TABLE employee("
                + "id BIGINT PRIMARY KEY, dept_id BIGINT, name VARCHAR(40), "
                + "CONSTRAINT fk_dept FOREIGN KEY(dept_id) REFERENCES dept(id))");
        client.executeSqlSimple("CREATE VIEW employee_simple AS SELECT * FROM employee");
        client.executeSqlSimple("CREATE VIEW employee_alias AS "
                + "SELECT id AS emp_id, name, dept_id FROM employee");
        client.executeSqlSimple("CREATE VIEW employee_filtered AS "
                + "SELECT id, name FROM employee WHERE id > 0");
        client.executeSqlSimple("CREATE VIEW employee_join AS "
                + "SELECT e.id AS emp_id, e.name, d.name AS dept_name "
                + "FROM employee e JOIN dept d ON d.id = e.dept_id");

        Assert.assertTrue(client.selectColumns("employee_simple").stream()
                .filter(column -> "id".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow().isPrimaryKey());
        Assert.assertTrue(client.selectColumns("employee_alias").stream()
                .filter(column -> "emp_id".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow().isPrimaryKey());
        Assert.assertTrue(client.selectColumns("employee_filtered").stream()
                .filter(column -> "id".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow().isPrimaryKey());
        Assert.assertTrue(client.selectColumns("employee_join").stream()
                .noneMatch(H2Column::isPrimaryKey));
        client.close();
    }

    /**
     * 简单单表投影视图映射到基表后可更新，复杂视图保持只读。
     */
    @Test
    public void testSimpleViewUpdateMapping() throws Throwable {
        Path file = this.tempDir.resolve("view-update-mapping");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        try {
            client.executeSqlSimple("CREATE TABLE VIEW_SOURCE("
                    + "ID INT PRIMARY KEY, NAME VARCHAR(40), NOTE VARCHAR(40), "
                    + "NAME_UPPER VARCHAR(40) GENERATED ALWAYS AS (UPPER(NAME)))");
            client.executeSqlSimple("CREATE TABLE VIEW_JOIN_SOURCE("
                    + "ID INT PRIMARY KEY, SOURCE_ID INT)");
            client.executeSqlSimple("CREATE VIEW VIEW_EDIT_SIMPLE AS "
                    + "SELECT ID AS VIEW_ID, NAME FROM VIEW_SOURCE");
            client.executeSqlSimple("CREATE VIEW VIEW_EDIT_COMPLEX AS "
                    + "SELECT S.ID, S.NAME FROM VIEW_SOURCE S "
                    + "JOIN VIEW_JOIN_SOURCE J ON J.SOURCE_ID = S.ID");
            client.executeSqlSimple("CREATE VIEW VIEW_EDIT_EXPRESSION AS "
                    + "SELECT ID, NAME, UPPER(NAME) AS TITLE_UPPER FROM VIEW_SOURCE");
            client.executeSqlSimple("CREATE VIEW VIEW_EDIT_GENERATED AS "
                    + "SELECT ID, NAME, NOTE, NAME_UPPER FROM VIEW_SOURCE");

            H2View simple = client.selectView("PUBLIC", "VIEW_EDIT_SIMPLE");
            Assert.assertTrue(simple.isUpdatable());
            Assert.assertNotNull(simple.getUpdateMapping());
            Assert.assertEquals("VIEW_SOURCE", simple.getUpdateMapping().getTableName());
            Assert.assertEquals("ID", simple.getUpdateMapping().sourceColumn("VIEW_ID"));
            Assert.assertTrue(simple.getColumns().primaryKeys().stream()
                    .anyMatch(column -> "VIEW_ID".equalsIgnoreCase(column.getName())));

            H2View complex = client.selectView("PUBLIC", "VIEW_EDIT_COMPLEX");
            Assert.assertFalse(complex.isUpdatable());
            Assert.assertNull(complex.getUpdateMapping());
            H2View expression = client.selectView("PUBLIC", "VIEW_EDIT_EXPRESSION");
            Assert.assertTrue(expression.isUpdatable());
            Assert.assertNotNull(expression.getUpdateMapping());
            Assert.assertEquals("NAME", expression.getUpdateMapping().sourceColumn("NAME"));
            Assert.assertTrue(expression.getUpdateMapping().isVirtualColumn("TITLE_UPPER"));
            Assert.assertEquals("UPPER(\"NAME\")",
                    expression.getUpdateMapping().virtualExpression("TITLE_UPPER"));
            H2Column virtualColumn = expression.getColumns().column("TITLE_UPPER");
            Assert.assertTrue(virtualColumn.isGenerated());
            Assert.assertEquals("UPPER(\"NAME\")", virtualColumn.getGeneratedExpression());
            H2View generated = client.selectView("PUBLIC", "VIEW_EDIT_GENERATED");
            Assert.assertTrue(generated.isUpdatable());
            Assert.assertTrue(generated.getUpdateMapping().isVirtualColumn("NAME_UPPER"));
            Assert.assertNull(generated.getUpdateMapping().sourceColumn("NAME_UPPER"));
            Assert.assertTrue(generated.getColumns().column("NAME_UPPER").isGenerated());
        } finally {
            client.close();
        }
    }

    /**
     * SQL 执行：DDL、DML、更新数量、多语句事务提交与回滚。
     */
    @Test
    public void testExecuteSqlAndTransaction() throws Throwable {
        Path file = this.tempDir.resolve("tx");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);

        // 多语句单事务：全部成功
        DBQueryResults<H2ExecuteResult> results = client.executeSql(
                "CREATE TABLE t(id INT PRIMARY KEY, v VARCHAR(10));"
                        + "INSERT INTO t VALUES(1, 'a');"
                        + "INSERT INTO t VALUES(2, 'b')");
        Assert.assertTrue(results.isSuccess());
        Assert.assertEquals(3, results.getResults().size());
        Assert.assertEquals(2, client.selectRowCount("t"));

        // 多语句失败回滚
        DBQueryResults<H2ExecuteResult> failed = client.executeSql(
                "INSERT INTO t VALUES(3, 'c');INSERT INTO t VALUES(1, 'dup')");
        Assert.assertFalse(failed.isSuccess());
        Assert.assertEquals(2, client.selectRowCount("t"));

        // 错误语句记录错误信息
        DBQueryResults<H2ExecuteResult> error = client.executeSql("SELECT * FROM not_exists");
        Assert.assertFalse(error.isSuccess());
        Assert.assertNotNull(error.getErrMsg());
        client.close();
    }

    /**
     * 记录操作：插入、更新、删除、过滤与分页。
     */
    @Test
    public void testRecords() throws Throwable {
        Path file = this.tempDir.resolve("records");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSql("CREATE TABLE t(id INT PRIMARY KEY, name VARCHAR(20))");
        client.executeSql("INSERT INTO t VALUES(1, 'a'),(2, 'b'),(3, 'c'),(4, NULL)");

        List<H2Column> columns = client.selectColumns("t");
        H2SelectRecordParam param = new H2SelectRecordParam();
        param.setTableName("t");
        List<H2Record> records = client.selectRecords(param);
        Assert.assertEquals(4, records.size());

        // 分页
        param.setStart(0L);
        param.setLimit(2L);
        Assert.assertEquals(2, client.selectRecords(param).size());
        Assert.assertEquals(4, client.selectRecordCount(param));

        // 插入
        H2InsertRecordParam insert = new H2InsertRecordParam();
        insert.setTableName("t");
        DBRecordData data = new DBRecordData();
        data.put(columns.get(0), 10);
        data.put(columns.get(1), "ten");
        insert.setRecord(data);
        Assert.assertTrue(client.insertRecord(insert) > 0);
        Assert.assertEquals(5, client.selectRowCount("t"));
        client.close();
    }

    /**
     * 内存模式：连接期间可用，DB_CLOSE_DELAY 保持库存活。
     */
    @Test
    public void testMemMode() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setName("memdb");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost("mem_test");
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=mem\nschema=PUBLIC\n");
        ShellH2Client client = new ShellH2Client(connect);
        client.start(5);
        client.executeSql("CREATE TABLE m(id INT PRIMARY KEY)");
        client.executeSql("INSERT INTO m VALUES(1)");
        Assert.assertEquals(1, client.selectRowCount("m"));
        Assert.assertTrue(client.isConnected());
        client.close();
    }

    /**
     * 表生命周期：重命名、清空、截断、删除、克隆。
     */
    @Test
    public void testTableLifecycle() throws Throwable {
        Path file = this.tempDir.resolve("life2");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSql("CREATE TABLE t(id INT PRIMARY KEY, name VARCHAR(20))");
        client.executeSql("INSERT INTO t VALUES(1, 'a'),(2, 'b')");

        // 克隆结构+数据
        client.cloneTable("t", "t_copy", true);
        Assert.assertEquals(2, client.selectRowCount("t_copy"));
        // 仅结构
        client.cloneTable("t", "t_empty", false);
        Assert.assertEquals(0, client.selectRowCount("t_empty"));

        // 清空与截断
        client.clearTable("t_copy");
        Assert.assertEquals(0, client.selectRowCount("t_copy"));
        client.truncateTable("t_empty");
        Assert.assertEquals(0, client.selectRowCount("t_empty"));

        // 重命名
        client.renameTable("t", "t2");
        Assert.assertNotNull(client.selectTable("t2"));
        Assert.assertNull(client.selectTable("t"));

        // 删除
        client.dropTable("t2");
        Assert.assertNull(client.selectTable("t2"));
        client.close();
    }

    /**
     * 视图生命周期：创建、重命名、克隆、删除。
     */
    @Test
    public void testViewLifecycle() throws Throwable {
        Path file = this.tempDir.resolve("views");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSql("CREATE TABLE t(id INT PRIMARY KEY, name VARCHAR(20))");
        client.executeSql("INSERT INTO t VALUES(1, 'a')");
        client.executeSql("CREATE VIEW v AS SELECT id FROM t");

        client.renameView("v", "v2");
        Assert.assertNotNull(client.selectView("v2"));

        client.cloneView("v2", "v3");
        Assert.assertNotNull(client.selectView("v3"));

        client.dropView("v3");
        Assert.assertNull(client.selectView("v3"));
        client.close();
    }

    /**
     * IDENTITY 自增列与生成列。
     */
    @Test
    public void testIdentityAndGeneratedColumn() throws Throwable {
        Path file = this.tempDir.resolve("identity");
        ShellH2Client client = new ShellH2Client(this.connect(file, false, true));
        client.start(5);
        client.executeSql("CREATE TABLE g(id INT AUTO_INCREMENT PRIMARY KEY, price DECIMAL(10,2), "
                + "price_with_tax DECIMAL(10,2) GENERATED ALWAYS AS (price * 1.1))");
        client.executeSql("INSERT INTO g(price) VALUES(100)");
        Assert.assertEquals(1, client.selectRowCount("g"));

        List<H2Column> columns = client.selectColumns("g");
        Assert.assertTrue(columns.get(0).isAutoIncrement());
        H2Table loadedTable = client.selectTable("g");
        Assert.assertNull(loadedTable.getColumns().getFirst().getDefaultValue());
        H2Column generated = columns.stream()
                .filter(column -> "price_with_tax".equalsIgnoreCase(column.getName()))
                .findFirst().orElseThrow();
        Assert.assertTrue(generated.isGenerated());
        Assert.assertNotNull(generated.getGeneratedExpression());
        H2Column parsed = H2ColumnDefinitionParser.parseColumn(
                "\"ID\" BIGINT GENERATED BY DEFAULT AS IDENTITY COMMENT '2'");
        Assert.assertNull(parsed.getDefaultValue());
        Assert.assertEquals("2", parsed.getComment());
        H2SelectRecordParam selectParam = new H2SelectRecordParam();
        selectParam.setTableName("g");
        selectParam.setColumns(columns);
        H2Record record = client.selectRecords(selectParam).getFirst();
        Assert.assertTrue(record.getProperties().get(generated.getName()).isReadonly());
        Assert.assertFalse(record.getRecordData().columns().stream()
                .anyMatch(name -> "price_with_tax".equalsIgnoreCase(name)));
        H2ExecuteResult queryResult = client.executeSql("SELECT * FROM g")
                .getResults().getFirst();
        H2Column queryGenerated = queryResult.getColumns().column("price_with_tax");
        Assert.assertTrue(queryGenerated.isGenerated());
        Assert.assertTrue(queryResult.getRecords().getFirst()
                .getProperties().get(queryGenerated.getName()).isReadonly());
        client.close();
    }

    /**
     * H2 数据库间传输。
     */
    @Test
    public void testTransportBetweenH2Databases() throws Throwable {
        Path source = this.tempDir.resolve("src");
        Path target = this.tempDir.resolve("dst");
        ShellH2Client sourceClient = new ShellH2Client(this.connect(source, false, true));
        sourceClient.start(5);
        sourceClient.executeSql("CREATE TABLE t(id INT PRIMARY KEY, name VARCHAR(20))");
        sourceClient.executeSql("CREATE INDEX idx_t_name ON t(name)");
        sourceClient.executeSql("CREATE VIEW v AS SELECT id FROM t");
        sourceClient.executeSql("INSERT INTO t VALUES(1, 'a'),(2, 'b')");
        sourceClient.executeSql("CREATE TABLE dst_tag(id INT PRIMARY KEY)");

        ShellH2Client targetClient = new ShellH2Client(this.connect(target, false, true));
        targetClient.start(5);

        cn.oyzh.easyshell.data.h2.handler.H2DataTransportHandler handler = new cn.oyzh.easyshell.data.h2.handler.H2DataTransportHandler();
        handler.setSourceClient(sourceClient);
        handler.setTargetClient(targetClient);
        List<cn.oyzh.fx.db.data.dto.DBDataTransportObject> tables = new java.util.ArrayList<>();
        cn.oyzh.fx.db.data.dto.DBDataTransportObject table = new cn.oyzh.fx.db.data.dto.DBDataTransportObject();
        table.setName("t");
        tables.add(table);
        handler.setTables(tables);
        cn.oyzh.fx.db.data.dto.DBDataTransportObject view = new cn.oyzh.fx.db.data.dto.DBDataTransportObject();
        view.setName("v");
        handler.setViews(List.of(view));
        handler.doAction();

        Assert.assertEquals(2, targetClient.selectRowCount("t"));
        Assert.assertNotNull(targetClient.selectTable("t"));
        Assert.assertNotNull(targetClient.selectView("v"));
        sourceClient.close();
        targetClient.close();
    }
}
