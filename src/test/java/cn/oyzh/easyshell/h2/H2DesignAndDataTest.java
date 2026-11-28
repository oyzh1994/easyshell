package cn.oyzh.easyshell.h2;

import cn.oyzh.easyshell.data.h2.dto.H2DataExportTable;
import cn.oyzh.easyshell.data.h2.dto.H2DataImportFile;
import cn.oyzh.easyshell.data.h2.handler.H2DataDumpHandler;
import cn.oyzh.easyshell.data.h2.handler.H2DataExportHandler;
import cn.oyzh.easyshell.data.h2.handler.H2DataImportHandler;
import cn.oyzh.easyshell.data.h2.handler.H2DataRunSqlFileHandler;
import cn.oyzh.easyshell.data.h2.handler.H2DataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * H2 设计器与数据工具集成测试：ALTER 序列、克隆、导出、导入、dump、SQL 文件执行。
 *
 * @since 2026-11-28
 */
public class H2DesignAndDataTest {

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        this.tempDir = Files.createTempDirectory("easyshell-h2-data-test-");
    }

    @After
    public void tearDown() throws Exception {
        if (this.tempDir != null && Files.exists(this.tempDir)) {
            Files.walk(this.tempDir).sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private ShellConnect connect(Path file) {
        ShellConnect connect = new ShellConnect();
        connect.setName(file.getFileName().toString());
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment("urlMode=file\nschema=PUBLIC\ncreateIfMissing=true\n");
        return connect;
    }

    /**
     * 表设计：建表 DDL、ALTER 序列（加列/改列/删列/重命名列）。
     */
    @Test
    public void testTableDesignAlter() throws Throwable {
        ShellH2Client client = new ShellH2Client(this.connect(this.tempDir.resolve("design")));
        client.start();
        try {
            client.executeSql("CREATE TABLE t(id INT PRIMARY KEY, name VARCHAR(20))");
            client.executeSql("ALTER TABLE t ADD COLUMN age INT");
            client.executeSql("ALTER TABLE t ADD COLUMN score DECIMAL(10,2) DEFAULT 0");
            List<H2Column> columns = client.selectColumns("t");
            Assert.assertEquals(4, columns.size());

            client.executeSql("ALTER TABLE t DROP COLUMN age");
            Assert.assertEquals(3, client.selectColumns("t").size());

            client.executeSql("ALTER TABLE t RENAME COLUMN name TO full_name");
            List<String> names = client.selectColumns("t").stream().map(H2Column::getName).toList();
            // H2 未加引号标识符自动转为大写
            Assert.assertTrue(names.contains("FULL_NAME"));

            // CHECK 约束表
            client.executeSql("CREATE TABLE ck(id INT PRIMARY KEY, v INT CHECK (v >= 0))");
            client.executeSql("INSERT INTO ck VALUES(1, 1)");
            DBQueryResults<H2ExecuteResult> violation = client.executeSql("INSERT INTO ck VALUES(2, -1)");
            Assert.assertFalse(violation.isSuccess());
        } finally {
            client.close();
        }
    }

    /**
     * 导出、导入、dump、SQL 文件执行端到端。
     */
    @Test
    public void testExportImportDumpAndRunSqlFile() throws Throwable {
        ShellH2Client client = new ShellH2Client(this.connect(this.tempDir.resolve("data")));
        client.start();
        try {
            client.executeSql("CREATE TABLE item (id INT PRIMARY KEY, name VARCHAR(20) NOT NULL)");
            client.executeSql("CREATE INDEX idx_item_name ON item(name)");
            client.executeSql("CREATE VIEW \"item_view\" AS SELECT \"ID\", \"NAME\" FROM item");
            client.executeSql("INSERT INTO item VALUES(1, 'one'),(2, 'two')");
            Path directory = this.tempDir.resolve("out");
            Files.createDirectories(directory);

            // SQL 导出
            H2DataExportTable exportTable = new H2DataExportTable();
            exportTable.setName("item");
            exportTable.columns(client.selectColumns("item"));
            exportTable.setFilePath(directory.resolve("item.sql").toString());
            H2DataExportHandler exportHandler = new H2DataExportHandler(client, client.defaultDatabaseName());
            exportHandler.setFileType("sql");
            exportHandler.setTables(List.of(exportTable));
            exportHandler.doAction();
            Assert.assertTrue(Files.readString(directory.resolve("item.sql")).contains("INSERT INTO"));

            // CSV 导出
            exportTable.setFilePath(directory.resolve("item.csv").toString());
            exportHandler.setFileType("csv");
            exportHandler.doAction();
            Assert.assertTrue(Files.readString(directory.resolve("item.csv")).contains("one"));

            // 视图 CSV 导出
            H2DataExportTable viewExportTable = new H2DataExportTable();
            viewExportTable.setName("item_view");
            viewExportTable.columns(client.selectColumns("item_view"));
            viewExportTable.setFilePath(directory.resolve("item-view.csv").toString());
            H2DataExportHandler viewExportHandler = new H2DataExportHandler(client, client.defaultDatabaseName());
            viewExportHandler.setFileType("csv");
            viewExportHandler.setTables(List.of(viewExportTable));
            viewExportHandler.doAction();
            Assert.assertTrue(Files.readString(directory.resolve("item-view.csv")).contains("one"));

            // CSV 导入
            client.executeSql("CREATE TABLE imported (id INT PRIMARY KEY, name VARCHAR(20))");
            Path csv = directory.resolve("import.csv");
            Files.writeString(csv, "id,name\n3,three\n");
            H2DataImportFile importFile = new H2DataImportFile();
            importFile.setClient(client);
            importFile.setFile(csv.toFile());
            importFile.setTargetTableName("imported");
            H2DataImportHandler importHandler = new H2DataImportHandler(client, client.defaultDatabaseName());
            importHandler.setContinueWithErrors(false);
            importHandler.setFileType("csv");
            importHandler.setFiles(List.of(importFile));
            importHandler.importMode("insert");
            importHandler.columnIndex(0);
            importHandler.dataRowStarts(1);
            importHandler.fieldSeparator(",");
            importHandler.doAction();
            Assert.assertEquals(1, client.selectRowCount("imported"));

            // dump 全库并恢复
            Path dump = directory.resolve("dump.sql");
            H2DataDumpHandler dumpHandler = new H2DataDumpHandler(client, client.defaultDatabaseName());
            dumpHandler.dumpFile(dump.toFile()).setDumpType((byte) 1).setDataType((byte) 0);
            dumpHandler.doAction();
            String dumpSql = Files.readString(dump);
            Assert.assertTrue(dumpSql.contains("CREATE "));
            Assert.assertTrue(dumpSql.contains(" TABLE "));

            Path restoreFile = this.tempDir.resolve("restore");
            ShellH2Client restore = new ShellH2Client(this.connect(restoreFile));
            restore.start();
            try {
                DBQueryResults<H2ExecuteResult> restoreResults = restore.executeSql(dumpSql);
                Assert.assertTrue("restore dump failed: " + restoreResults.getErrMsg(),
                        restoreResults.isSuccess());
                Assert.assertEquals(2, restore.selectRowCount("item"));
                Assert.assertNotNull(restore.selectView("item_view"));
            } finally {
                restore.close();
            }

            // SQL 文件执行（IDENTITY 主键在省略 id 时自动生成）
            Path script = directory.resolve("run.sql");
            Files.writeString(script, "CREATE TABLE run_sql_table (id IDENTITY PRIMARY KEY, name VARCHAR(20));"
                    + "INSERT INTO run_sql_table(name) VALUES ('from script');"
                    + "INSERT INTO run_sql_table(name) VALUES ('from script 2');");
            H2DataRunSqlFileHandler runHandler = new H2DataRunSqlFileHandler(client, client.defaultDatabaseName());
            List<String> messages = new ArrayList<>();
            runHandler.setMessageHandler(messages::add);
            runHandler.file(script.toFile());
            runHandler.doAction();
            Assert.assertNotNull(client.selectTable("run_sql_table"));
            Assert.assertEquals(2, client.selectRowCount("run_sql_table"));
        } finally {
            client.close();
        }
    }

    @Test
    public void testJsonDateAndTimeRoundTrip() throws Throwable {
        ShellH2Client client = new ShellH2Client(
                this.connect(this.tempDir.resolve("json-date-time")));
        client.start();
        try {
            client.executeSqlSimple("""
                    CREATE TABLE JSON_SOURCE(
                        ID INT PRIMARY KEY,
                        EFFECTIVE_DATE DATE,
                        EFFECTIVE_TIME TIME,
                        CREATED_AT TIMESTAMP,
                        TIMEZONE_AT TIMESTAMP WITH TIME ZONE,
                        ENABLED BOOLEAN,
                        JSON_DATA JSON)
                    """);
            client.executeSqlSimple("""
                    INSERT INTO JSON_SOURCE VALUES(
                        1, DATE '2026-01-15', TIME '09:30:00',
                        TIMESTAMP '2026-11-28 19:11:29',
                        TIMESTAMP WITH TIME ZONE '2026-11-28 19:11:29+08:00',
                        TRUE, JSON '{"a":1}'),
                    (
                        2, NULL, NULL, NULL, NULL, FALSE, NULL)
                    """);
            Path json = this.tempDir.resolve("JSON_SOURCE.json");
            H2DataExportTable exportTable = new H2DataExportTable();
            exportTable.setName("JSON_SOURCE");
            exportTable.columns(client.selectColumns("JSON_SOURCE"));
            exportTable.setFilePath(json.toString());
            H2DataExportHandler export = new H2DataExportHandler(client, "PUBLIC");
            export.setFileType("json");
            export.setTables(List.of(exportTable));
            export.doAction();
            String content = Files.readString(json);
            Assert.assertTrue(content, content.contains("\"2026-01-15\""));
            Assert.assertTrue(content, content.contains("\"09:30:00\""));
            Assert.assertTrue(content, content.contains("\"ENABLED\" : true"));
            Assert.assertTrue(content, content.contains("\"JSON_DATA\" : null"));

            client.executeSqlSimple("""
                    CREATE TABLE JSON_TARGET(
                        ID INT PRIMARY KEY,
                        EFFECTIVE_DATE DATE,
                        EFFECTIVE_TIME TIME,
                        CREATED_AT TIMESTAMP,
                        TIMEZONE_AT TIMESTAMP WITH TIME ZONE,
                        ENABLED BOOLEAN,
                        JSON_DATA JSON)
                    """);
            H2DataImportFile importFile = new H2DataImportFile();
            importFile.setFile(json.toFile());
            importFile.setTargetTableName("JSON_TARGET");
            H2DataImportHandler importHandler = new H2DataImportHandler(client, "PUBLIC");
            importHandler.setFileType("json");
            importHandler.setFiles(List.of(importFile));
            importHandler.importMode("2");
            importHandler.doAction();

            H2SelectRecordParam param = new H2SelectRecordParam();
            param.setDbName("PUBLIC");
            param.setTableName("JSON_TARGET");
            Assert.assertEquals(2, client.selectRecords(param).size());

            Path xml = this.tempDir.resolve("JSON_SOURCE.xml");
            H2DataExportTable xmlExportTable = new H2DataExportTable();
            xmlExportTable.setName("JSON_SOURCE");
            xmlExportTable.columns(client.selectColumns("JSON_SOURCE"));
            xmlExportTable.setFilePath(xml.toString());
            H2DataExportHandler xmlExport = new H2DataExportHandler(client, "PUBLIC");
            xmlExport.setFileType("xml");
            xmlExport.setTables(List.of(xmlExportTable));
            xmlExport.doAction();

            client.executeSqlSimple("""
                    CREATE TABLE XML_TARGET(
                        ID INT PRIMARY KEY,
                        EFFECTIVE_DATE DATE,
                        EFFECTIVE_TIME TIME,
                        CREATED_AT TIMESTAMP,
                        TIMEZONE_AT TIMESTAMP WITH TIME ZONE,
                        ENABLED BOOLEAN,
                        JSON_DATA JSON)
                    """);
            H2DataImportFile xmlImportFile = new H2DataImportFile();
            xmlImportFile.setFile(xml.toFile());
            xmlImportFile.setTargetTableName("XML_TARGET");
            H2DataImportHandler xmlImport = new H2DataImportHandler(client, "PUBLIC");
            xmlImport.setFileType("xml");
            xmlImport.setFiles(List.of(xmlImportFile));
            xmlImport.importMode("2");
            xmlImport.doAction();
            param.setTableName("XML_TARGET");
            Assert.assertEquals(2, client.selectRecords(param).size());
        } finally {
            client.close();
        }
    }

    /**
     * 模式菜单对应的转储、SQL 文件执行与数据传输必须保持模式作用域。
     */
    @Test
    public void testSchemaScopedDumpRunAndTransport() throws Throwable {
        ShellH2Client source = new ShellH2Client(this.connect(this.tempDir.resolve("schema-source")));
        source.start();
        try {
            source.executeSqlSimple("CREATE SCHEMA SRC");
            source.executeSqlSimple("CREATE TABLE SRC.ITEM("
                    + "ID INT PRIMARY KEY, NAME VARCHAR(20) NOT NULL, QUANTITY BIGINT)");
            source.executeSqlSimple("CREATE INDEX SRC.IDX_ITEM_NAME ON SRC.ITEM(NAME)");
            source.executeSqlSimple("CREATE VIEW SRC.ITEM_VIEW AS "
                    + "SELECT \"E\".\"ID\", \"E\".\"NAME\" FROM SRC.ITEM \"E\"");
            source.executeSqlSimple("INSERT INTO SRC.ITEM VALUES(1, 'one', 10)");
            H2Trigger trigger = new H2Trigger("TRG_SRC_ITEM");
            trigger.setPolicy("BEFORE INSERT");
            trigger.setDefinition("SELECT 1;");
            source.executeSqlSimple("SRC", trigger.toCreateSql("ITEM"));

            Path dumpFile = this.tempDir.resolve("schema-dump.sql");
            H2DataDumpHandler dumpHandler = new H2DataDumpHandler(source, "SRC");
            dumpHandler.dumpFile(dumpFile.toFile()).setDumpType((byte) 1).setDataType((byte) 0);
            dumpHandler.doAction();
            String dumpSql = Files.readString(dumpFile);
            Assert.assertTrue(dumpSql, dumpSql.contains("CREATE SCHEMA IF NOT EXISTS \"SRC\""));
            Assert.assertTrue(dumpSql, dumpSql.contains("\"SRC\".\"ITEM\""));
            Assert.assertTrue(dumpSql, dumpSql.contains("INSERT INTO \"SRC\".\"ITEM\""));

            Path restoreFile = this.tempDir.resolve("schema-restore");
            ShellH2Client restore = new ShellH2Client(this.connect(restoreFile));
            restore.start();
            try {
                DBQueryResults<H2ExecuteResult> restored = restore.executeSql(dumpSql);
                Assert.assertTrue(restored.getErrMsg(), restored.isSuccess());
                cn.oyzh.easyshell.h2.record.H2SelectRecordParam restoreParam =
                        new cn.oyzh.easyshell.h2.record.H2SelectRecordParam();
                restoreParam.setDbName("SRC");
                restoreParam.setTableName("ITEM");
                Assert.assertEquals(1, restore.selectRecords(restoreParam).size());
                Assert.assertNotNull(restore.selectView("SRC", "ITEM_VIEW"));
            } finally {
                restore.close();
            }

            Path script = this.tempDir.resolve("schema-run.sql");
            Files.writeString(script, "CREATE TABLE FROM_SCRIPT(ID INT PRIMARY KEY);"
                    + "INSERT INTO FROM_SCRIPT VALUES(2);");
            H2DataRunSqlFileHandler runHandler = new H2DataRunSqlFileHandler(source, "SRC");
            runHandler.file(script.toFile());
            runHandler.doAction();
            Assert.assertNotNull(source.selectTable("SRC", "FROM_SCRIPT"));
            Assert.assertNull(source.selectTable("PUBLIC", "FROM_SCRIPT"));

            Path targetFile = this.tempDir.resolve("schema-target");
            ShellH2Client target = new ShellH2Client(this.connect(targetFile));
            target.start();
            try {
                target.executeSqlSimple("CREATE SCHEMA TGT");
                H2DataTransportHandler transport = new H2DataTransportHandler();
                transport.setSourceClient(source);
                transport.setTargetClient(target);
                transport.setSourceDatabase("SRC");
                transport.setTargetDatabase("TGT");
                DBDataTransportObject table = new DBDataTransportObject();
                table.setName("ITEM");
                transport.setTables(List.of(table));
                DBDataTransportObject view = new DBDataTransportObject();
                view.setName("ITEM_VIEW");
                transport.setViews(List.of(view));
                transport.doAction();
                cn.oyzh.easyshell.h2.record.H2SelectRecordParam targetParam =
                        new cn.oyzh.easyshell.h2.record.H2SelectRecordParam();
                targetParam.setDbName("TGT");
                targetParam.setTableName("ITEM");
                Assert.assertEquals(1, target.selectRecords(targetParam).size());
                Assert.assertNotNull(target.selectView("TGT", "ITEM_VIEW"));
                Assert.assertEquals(1, target.selectTriggers("TGT", "ITEM").size());
                DBQueryResults<H2ExecuteResult> viewResult =
                        target.executeSql("SELECT * FROM TGT.ITEM_VIEW");
                Assert.assertTrue(viewResult.getErrMsg(), viewResult.isSuccess());
                Assert.assertEquals(1, viewResult.getResults().getFirst()
                        .getRecords().size());
            } finally {
                target.close();
            }
        } finally {
            source.close();
        }
    }

    @Test
    public void testRunDumpIntoTargetSchemaRewritesSourceSchema() throws Throwable {
        ShellH2Client client = new ShellH2Client(
                this.connect(this.tempDir.resolve("schema-rewrite")));
        client.start();
        try {
            client.executeSqlSimple("CREATE SCHEMA SRC");
            client.executeSqlSimple(
                    "CREATE TABLE SRC.ITEM(ID INT PRIMARY KEY, NAME VARCHAR(20))");
            client.executeSqlSimple("INSERT INTO SRC.ITEM VALUES(1, 'public-value')");
            client.executeSqlSimple(
                    "CREATE VIEW SRC.ITEM_VIEW AS SELECT ID, NAME FROM SRC.ITEM");
            H2Trigger sourceTrigger = new H2Trigger("TRG_SRC_ITEM");
            sourceTrigger.setPolicy("BEFORE INSERT");
            sourceTrigger.setDefinition("SELECT 1;");
            client.executeSqlSimple("SRC", sourceTrigger.toCreateSql("ITEM"));

            Path dumpFile = this.tempDir.resolve("source-schema.sql");
            H2DataDumpHandler dump = new H2DataDumpHandler(client, "SRC");
            dump.dumpFile(dumpFile.toFile()).setDumpType((byte) 1).setDataType((byte) 0);
            dump.doAction();
            String dumpSql = Files.readString(dumpFile);
            Assert.assertTrue(dumpSql, dumpSql.contains(
                    "-- EasyShell H2 source schema: \"SRC\""));
            Assert.assertTrue(dumpSql, dumpSql.contains("\"SRC\".\"ITEM\""));

            H2DataRunSqlFileHandler run = new H2DataRunSqlFileHandler(client, "TGT");
            run.file(dumpFile.toFile());
            run.doAction();

            H2SelectRecordParam param = new H2SelectRecordParam();
            param.setDbName("TGT");
            param.setTableName("ITEM");
            Assert.assertEquals(1, client.selectRecords(param).size());
            Assert.assertNotNull(client.selectView("TGT", "ITEM_VIEW"));
            Assert.assertEquals(1, client.selectTriggers("TGT", "ITEM").size());
            Assert.assertNull(client.selectTable("PUBLIC", "ITEM"));
        } finally {
            client.close();
        }
    }

    @Test
    public void testRunSqlFileReplacesExistingTrigger() throws Throwable {
        ShellH2Client client = new ShellH2Client(
                this.connect(this.tempDir.resolve("trigger-rerun")));
        client.start();
        try {
            client.executeSqlSimple("CREATE SCHEMA TGT");
            client.executeSqlSimple(
                    "CREATE TABLE TGT.ITEM(ID INT PRIMARY KEY)");
            H2Trigger trigger = new H2Trigger("TRG_ITEM");
            trigger.setPolicy("BEFORE INSERT");
            trigger.setDefinition("SELECT 1;");
            String triggerSql = trigger.toCreateSql("ITEM");
            client.executeSqlSimple("TGT", triggerSql);

            Path script = this.tempDir.resolve("trigger.sql");
            Files.writeString(script, triggerSql);
            H2DataRunSqlFileHandler run = new H2DataRunSqlFileHandler(client, "TGT");
            List<String> messages = new ArrayList<>();
            run.setMessageHandler(messages::add);
            run.file(script.toFile());
            run.doAction();

            Assert.assertEquals(1, client.selectTriggers("TGT", "ITEM").size());
            Assert.assertFalse(messages.toString(),
                    messages.stream().anyMatch(item -> item.contains("already exists")));
        } finally {
            client.close();
        }
    }

    @Test
    public void testImportFailureIncrementsFailureCount() throws Throwable {
        ShellH2Client client = new ShellH2Client(
                this.connect(this.tempDir.resolve("import-failure-count")));
        client.start();
        try {
            client.executeSqlSimple("CREATE TABLE IMPORT_TARGET(ID INT PRIMARY KEY)");
            client.executeSqlSimple("INSERT INTO IMPORT_TARGET VALUES(1)");
            Path json = this.tempDir.resolve("IMPORT_TARGET.json");
            Files.writeString(json, "[{\"ID\":1}]");
            H2DataImportFile importFile = new H2DataImportFile();
            importFile.setFile(json.toFile());
            importFile.setTargetTableName("IMPORT_TARGET");

            List<Integer> counts = java.util.Collections.synchronizedList(new ArrayList<>());
            H2DataImportHandler importHandler = new H2DataImportHandler(client, "PUBLIC") {
                @Override
                public void processed(int processed) {
                    counts.add(processed);
                }
            };
            importHandler.setFileType("json");
            importHandler.setFiles(List.of(importFile));
            importHandler.importMode("1");
            importHandler.setContinueWithErrors(true);
            importHandler.doAction();

            Assert.assertTrue(counts.toString(), counts.contains(-1));
        } finally {
            client.close();
        }
    }

    @Test
    public void testTransportTriggerBetweenSchemasInSameDatabase() throws Throwable {
        ShellH2Client client = new ShellH2Client(
                this.connect(this.tempDir.resolve("same-database-transport")));
        client.start();
        try {
            client.executeSqlSimple("CREATE SCHEMA SRC");
            client.executeSqlSimple("CREATE SCHEMA TGT");
            client.executeSqlSimple(
                    "CREATE TABLE SRC.ITEM(ID INT PRIMARY KEY, NAME VARCHAR(20))");
            client.executeSqlSimple("INSERT INTO SRC.ITEM VALUES(1, 'one')");
            H2Trigger trigger = new H2Trigger("TRG_ITEM");
            trigger.setPolicy("BEFORE INSERT");
            trigger.setDefinition("SELECT 1;");
            client.executeSqlSimple("SRC", trigger.toCreateSql("ITEM"));

            H2DataTransportHandler transport = new H2DataTransportHandler();
            transport.setSourceClient(client);
            transport.setTargetClient(client);
            transport.setSourceDatabase("SRC");
            transport.setTargetDatabase("TGT");
            DBDataTransportObject table = new DBDataTransportObject();
            table.setName("ITEM");
            transport.setTables(List.of(table));
            transport.doAction();

            H2SelectRecordParam param = new H2SelectRecordParam();
            param.setDbName("TGT");
            param.setTableName("ITEM");
            Assert.assertEquals(1, client.selectRecords(param).size());
            Assert.assertEquals(1, client.selectTriggers("TGT", "ITEM").size());
        } finally {
            client.close();
        }
    }

    /**
     * 表克隆保留结构与数据。
     */
    @Test
    public void testCloneAndRename() throws Throwable {
        ShellH2Client client = new ShellH2Client(this.connect(this.tempDir.resolve("clone")));
        client.start();
        try {
            client.executeSql("CREATE TABLE src(id INT PRIMARY KEY, name VARCHAR(20))");
            client.executeSql("CREATE INDEX idx_src_name ON src(name)");
            client.executeSql("INSERT INTO src VALUES(1, 'a'),(2, 'b')");

            client.cloneTable("src", "dst", true);
            Assert.assertEquals(2, client.selectRowCount("dst"));
            H2Table dst = client.selectTable("dst");
            Assert.assertNotNull(dst);
            Assert.assertNotNull(client.selectTable("dst"));

            client.renameTable("dst", "dst2");
            Assert.assertNotNull(client.selectTable("dst2"));
            Assert.assertNull(client.selectTable("dst"));
        } finally {
            client.close();
        }
    }
}
