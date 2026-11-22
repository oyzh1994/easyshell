package cn.oyzh.easyshell.sqlserver;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataExportTable;
import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataImportFile;
import cn.oyzh.easyshell.data.sqlserver.handler.ShellSqlServerDataExportHandler;
import cn.oyzh.easyshell.data.sqlserver.handler.ShellSqlServerDataDumpHandler;
import cn.oyzh.easyshell.data.sqlserver.handler.ShellSqlServerDataImportHandler;
import cn.oyzh.easyshell.data.sqlserver.handler.ShellSqlServerDataRunSqlFileHandler;
import cn.oyzh.easyshell.data.sqlserver.handler.ShellSqlServerDataTransportHandler;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.check.SqlServerCheck;
import cn.oyzh.easyshell.sqlserver.function.SqlServerCreateFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerAlertFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerCreateProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerAlertProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.record.SqlServerDeleteRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerInsertRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerUpdateRecordParam;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerCreateSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.table.SqlServerAlertTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerCreateTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.sqlserver.view.SqlServerCreateViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerAlertViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerColumnUtil;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.DBObjects;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQL Server真实实例端到端测试。
 */
public class SqlServerIntegrationTest {

    private static final String DATABASE = "EASYSHELL_SQLSERVER";

    private static final String SOURCE_SCHEMA = "EASYSHELL_SRC";

    private static final String TARGET_SCHEMA = "EASYSHELL_DST";

    private static final String DUMP_SOURCE_SCHEMA = "EASYSHELL_DUMP_SRC";

    private static final String DUMP_TARGET_SCHEMA = "EASYSHELL_DUMP_DST";

    private static ShellSqlServerClient client;

    private static String host;

    private static int port;

    private static String user;

    private static String password;

    @BeforeClass
    public static void setUp() throws Throwable {
        host = System.getProperty("sqlserver.host", "127.0.0.1");
        port = Integer.parseInt(System.getProperty("sqlserver.port", "14333"));
        user = System.getProperty("sqlserver.user", "sa");
        password = System.getProperty("sqlserver.password", "easyshell@2026");
        ShellSqlServerColumnUtil.init();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
        }
        ensureDatabase();
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.SQLSERVER);
        connect.setHost(host + ":" + port);
        connect.setDatabase(DATABASE);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setSSLMode(false);
        connect.setConnectTimeOut(10);
        client = new ShellSqlServerClient(connect);
        client.start(10000);
        assertTrue(client.isConnected());
    }

    @AfterClass
    public static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    private static void ensureDatabase() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=master;encrypt=false;trustServerCertificate=true";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("IF DB_ID(N'" + DATABASE + "') IS NULL CREATE DATABASE ["
                    + DATABASE + "]");
        }
        String databaseUrl = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=" + DATABASE + ";encrypt=false;trustServerCertificate=true";
        try (Connection connection = DriverManager.getConnection(databaseUrl, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("IF SCHEMA_ID(N'" + SOURCE_SCHEMA + "') IS NULL EXEC(N'CREATE SCHEMA ["
                    + SOURCE_SCHEMA + "]')");
            statement.execute("IF SCHEMA_ID(N'" + TARGET_SCHEMA + "') IS NULL EXEC(N'CREATE SCHEMA ["
                    + TARGET_SCHEMA + "]')");
            statement.execute("IF SCHEMA_ID(N'" + DUMP_SOURCE_SCHEMA
                    + "') IS NULL EXEC(N'CREATE SCHEMA [" + DUMP_SOURCE_SCHEMA + "]')");
            statement.execute("IF SCHEMA_ID(N'" + DUMP_TARGET_SCHEMA
                    + "') IS NULL EXEC(N'CREATE SCHEMA [" + DUMP_TARGET_SCHEMA + "]')");
        }
    }

    @Test
    public void clientMetadataCrudAndCloneLifecycle() {
        String table = "COMPLEX_TYPES";
        String clone = "COMPLEX_TYPES_CLONE";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS ["
                + SOURCE_SCHEMA + "].[" + clone + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS ["
                + SOURCE_SCHEMA + "].[" + table + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_SRC].[COMPLEX_TYPES] (
                    [ID] BIGINT IDENTITY(10,1) NOT NULL PRIMARY KEY,
                    [C_INT] INT NULL,
                    [C_DECIMAL] DECIMAL(20,6) NULL,
                    [C_BIT] BIT NULL,
                    [C_NVARCHAR] NVARCHAR(200) NULL,
                    [C_VARBINARY] VARBINARY(MAX) NULL,
                    [C_DATETIME2] DATETIME2(7) NULL,
                    [C_DATETIMEOFFSET] DATETIMEOFFSET(7) NULL,
                    [C_UUID] UNIQUEIDENTIFIER NULL,
                    [C_XML] XML NULL,
                    [C_COMPUTED] AS ([C_INT] * 2) PERSISTED
                )
                """);
        assertTrue(client.tableSize(SOURCE_SCHEMA) >= 1);
        assertTrue(client.selectSchemas().stream()
                .anyMatch(schema -> SOURCE_SCHEMA.equals(schema.getName())));
        assertNotNull(client.selectTable(SOURCE_SCHEMA, table));
        var plan = client.explainSql(SOURCE_SCHEMA, "SELECT * FROM [EASYSHELL_SRC].["
                + table + "]");
        assertTrue(plan.isSuccess());
        assertTrue(plan.getResults().getFirst().isSuccess());

        List<SqlServerColumn> columns = client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, table));
        Map<String, SqlServerColumn> byName = columns.stream()
                .collect(Collectors.toMap(SqlServerColumn::getName, column -> column));
        assertTrue(byName.get("ID").isAutoIncrement());
        assertTrue(byName.get("C_COMPUTED").isGenerated());
        assertTrue(byName.get("C_COMPUTED").getGeneratedExpression().contains("[C_INT]"));
        assertEquals("PERSISTED", byName.get("C_COMPUTED").getGeneratedStorage());
        assertTrue(client.showCreateTable(SOURCE_SCHEMA, table).contains("[C_COMPUTED] AS"));

        SqlServerColumns recordColumns = new SqlServerColumns(columns);
        SqlServerRecord insert = new SqlServerRecord(recordColumns);
        insert.putValue("C_INT", 7);
        insert.putValue("C_DECIMAL", new java.math.BigDecimal("12.500000"));
        insert.putValue("C_BIT", true);
        insert.putValue("C_NVARCHAR", "中文 O'Reilly\nline2");
        insert.putValue("C_VARBINARY", new byte[]{0, 1, 2, (byte) 255});
        insert.putValue("C_DATETIME2", java.time.LocalDateTime.of(2026, 11, 22, 10, 20, 30));
        insert.putValue("C_DATETIMEOFFSET", java.time.OffsetDateTime.parse("2026-11-22T10:20:30+08:00"));
        insert.putValue("C_UUID", java.util.UUID.fromString("12345678-1234-5678-1234-567812345678"));
        insert.putValue("C_XML", "<root><value>中文</value></root>");
        SqlServerRecordPrimaryKey primaryKey = new SqlServerRecordPrimaryKey();
        primaryKey.init(byName.get("ID"), insert);
        SqlServerInsertRecordParam insertParam = new SqlServerInsertRecordParam();
        insertParam.setSchema(SOURCE_SCHEMA);
        insertParam.setTableName(table);
        insertParam.setRecord(insert.getRecordData());
        insertParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(insertParam));
        assertEquals(10L, ((Number) primaryKey.getReturnData()).longValue());

        SqlServerSelectRecordParam selectParam = new SqlServerSelectRecordParam();
        selectParam.setSchema(SOURCE_SCHEMA);
        selectParam.setTableName(table);
        selectParam.setStart(0L);
        selectParam.setLimit(10L);
        List<SqlServerRecord> records = client.selectRecords(selectParam);
        assertEquals(1, records.size());
        SqlServerRecord selected = records.getFirst();
        assertEquals(14, ((Number) selected.getValue("C_COMPUTED")).intValue());
        assertEquals("中文 O'Reilly\nline2", selected.getValue("C_NVARCHAR"));

        selected.putValue("C_NVARCHAR", "updated");
        selected.getProperty("C_NVARCHAR").setChanged(true);
        primaryKey.setData(primaryKey.getReturnData());
        primaryKey.setOriginalData(primaryKey.getReturnData());
        SqlServerUpdateRecordParam updateParam = new SqlServerUpdateRecordParam();
        updateParam.setSchema(SOURCE_SCHEMA);
        updateParam.setTableName(table);
        updateParam.setRecord(selected.getOriginalRecordData());
        updateParam.setUpdateRecord(selected.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.updateRecord(updateParam));

        assertEquals(clone, client.cloneTable(SOURCE_SCHEMA, table, clone, true));
        assertEquals(1, client.selectRecordCount(selectParamFor(SOURCE_SCHEMA, clone)));
        assertFalse(client.selectColumns(new SqlServerSelectColumnParam(SOURCE_SCHEMA, clone)).isEmpty());

        SqlServerDeleteRecordParam deleteParam = new SqlServerDeleteRecordParam();
        deleteParam.setSchema(SOURCE_SCHEMA);
        deleteParam.setTableName(table);
        deleteParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.deleteRecord(deleteParam));
    }

    @Test
    public void queryResultEditingUsesSourceTable() {
        String table = "QUERY_EDIT";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS ["
                + SOURCE_SCHEMA + "].[" + table + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_SRC].[QUERY_EDIT] (
                    [ID] BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    [LOG] NVARCHAR(100) NULL
                )
                """);

        var queryResult = client.executeSql(SOURCE_SCHEMA,
                "SELECT * FROM [EASYSHELL_SRC].[QUERY_EDIT]").getResults().getFirst();
        assertEquals(SOURCE_SCHEMA, queryResult.schema());
        assertEquals(table, queryResult.tableName());
        assertTrue(queryResult.isUpdatable());

        SqlServerColumns columns = queryResult.getColumns();
        SqlServerRecord record = new SqlServerRecord(columns);
        for (SqlServerColumn column : columns) {
            record.putValue(column, null);
        }
        record.putValue("LOG", "query edited");
        SqlServerRecordPrimaryKey primaryKey = new SqlServerRecordPrimaryKey();
        primaryKey.init(columns.column("ID"), record);
        SqlServerInsertRecordParam param = new SqlServerInsertRecordParam();
        param.setSchema(queryResult.schema());
        param.setTableName(queryResult.tableName());
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);

        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
        assertEquals("query edited", client.selectRecords(
                selectParamFor(SOURCE_SCHEMA, table)).getFirst().getValue("LOG"));
    }

    @Test
    public void clientShowsBuiltInSchemasAndDropsEmptySchema() {
        Map<String, SqlServerSchema> schemas = client.selectSchemas().stream()
                .collect(Collectors.toMap(SqlServerSchema::getName, schema -> schema));
        assertTrue(schemas.containsKey("dbo"));
        assertTrue(schemas.containsKey("guest"));
        assertTrue(schemas.containsKey("INFORMATION_SCHEMA"));
        assertTrue(schemas.containsKey("sys"));
        assertTrue(schemas.get("dbo").isSystemSchema());
        assertFalse(schemas.get(SOURCE_SCHEMA).isSystemSchema());
        assertNotNull(schemas.get(SOURCE_SCHEMA).getSchemaId());

        String schemaName = "EASYSHELL_DROP_TEST";
        if (client.existSchema(schemaName)) {
            client.dropSchema(schemaName);
        }
        SqlServerSchema schema = new SqlServerSchema();
        schema.setName(schemaName);
        client.createSchema(schema);
        assertTrue(client.existSchema(schemaName));
        assertTrue(client.dropSchema(schemaName));
        assertFalse(client.existSchema(schemaName));
    }

    @Test
    public void clientLoadsSchemaObjectCountsInOneQuery() {
        var counts = client.schemaObjectCounts(SOURCE_SCHEMA);
        assertEquals(client.tableSize(SOURCE_SCHEMA), counts.tables());
        assertEquals(client.viewSize(SOURCE_SCHEMA), counts.views());
        assertEquals(client.functionSize(SOURCE_SCHEMA), counts.functions());
        assertEquals(client.procedureSize(SOURCE_SCHEMA), counts.procedures());
        assertEquals(client.sequenceSize(SOURCE_SCHEMA), counts.sequences());
    }

    @Test
    public void clientViewFunctionProcedureAndSequenceLifecycle() {
        String table = "PROGRAMMABLE_BASE";
        String viewName = "PROGRAMMABLE_VIEW";
        String functionName = "PROGRAMMABLE_FUNCTION";
        String procedureName = "PROGRAMMABLE_PROCEDURE";
        String sequenceName = "PROGRAMMABLE_SEQUENCE";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP VIEW IF EXISTS [EASYSHELL_SRC].["
                + viewName + "]; DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + table + "]" );
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + functionName + "]', N'FN') IS NOT NULL DROP FUNCTION [EASYSHELL_SRC].[" + functionName + "]" );
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + procedureName + "]', N'P') IS NOT NULL DROP PROCEDURE [EASYSHELL_SRC].[" + procedureName + "]" );
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP SEQUENCE IF EXISTS [EASYSHELL_SRC].["
                + sequenceName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "CREATE TABLE [EASYSHELL_SRC].["
                + table + "] ([ID] INT NOT NULL PRIMARY KEY, [NAME] NVARCHAR(100) NULL)");
        client.executeSqlSimple(SOURCE_SCHEMA, "INSERT INTO [EASYSHELL_SRC].["
                + table + "] ([ID],[NAME]) VALUES (1,N'视图')");

        SqlServerView view = new SqlServerView();
        view.setName(viewName);
        view.setDefinition("SELECT [ID],[NAME] FROM [EASYSHELL_SRC].[" + table + "]");
        view.setComment("视图注释");
        SqlServerCreateViewParam viewParam = new SqlServerCreateViewParam();
        viewParam.setSchema(SOURCE_SCHEMA);
        viewParam.setView(view);
        client.createView(viewParam);
        assertTrue(client.existView(SOURCE_SCHEMA, viewName));
        assertEquals(1, client.viewRecords(SOURCE_SCHEMA, viewName, 0L, 10L, null).size());
        assertEquals("视图注释", client.selectView(SOURCE_SCHEMA, viewName).getComment());

        SqlServerFunction function = new SqlServerFunction();
        function.setName(functionName);
        function.setSchema(SOURCE_SCHEMA);
        SqlServerRoutineParam returnParam = new SqlServerRoutineParam();
        returnParam.setType("INT");
        function.setReturnParam(returnParam);
        function.setDefinition("RETURN 42;");
        SqlServerCreateFunctionParam functionParam = new SqlServerCreateFunctionParam();
        functionParam.setSchema(SOURCE_SCHEMA);
        functionParam.setFunction(function);
        client.createFunction(functionParam);
        assertNotNull(client.selectFunction(SOURCE_SCHEMA, functionName));
        var functionResults = client.executeSql(SOURCE_SCHEMA,
                "SELECT [EASYSHELL_SRC].[" + functionName + "]() AS [answer]");
        assertTrue(functionResults.isSuccess());
        assertEquals(42, ((Number) functionResults.getResults().getFirst()
                .getRecords().getFirst().getValue("answer")).intValue());

        SqlServerProcedure procedure = new SqlServerProcedure();
        procedure.setName(procedureName);
        procedure.setSchema(SOURCE_SCHEMA);
        procedure.setDefinition("SELECT 42 AS answer;");
        SqlServerCreateProcedureParam procedureParam = new SqlServerCreateProcedureParam();
        procedureParam.setSchema(SOURCE_SCHEMA);
        procedureParam.setProcedure(procedure);
        client.createProcedure(procedureParam);
        assertNotNull(client.selectProcedure(SOURCE_SCHEMA, procedureName));

        SqlServerView selectedView = client.selectView(SOURCE_SCHEMA, viewName);
        assertFalse(selectedView.getDefinition().stripLeading().toUpperCase()
                .startsWith("CREATE"));
        selectedView.setDefinition("SELECT [ID],[NAME] FROM [EASYSHELL_SRC].[" + table + "] WHERE [ID] > 0");
        selectedView.setComment("新视图注释");
        SqlServerAlertViewParam alertViewParam = new SqlServerAlertViewParam();
        alertViewParam.setSchema(SOURCE_SCHEMA);
        alertViewParam.setView(selectedView);
        client.alertView(alertViewParam);
        assertEquals(1, client.viewRecords(SOURCE_SCHEMA, viewName, 0L, 10L, null).size());
        assertEquals("新视图注释", client.selectView(SOURCE_SCHEMA, viewName).getComment());

        SqlServerFunction selectedFunction = client.selectFunction(SOURCE_SCHEMA, functionName);
        assertFalse(selectedFunction.getDefinition().stripLeading().toUpperCase()
                .startsWith("CREATE"));
        selectedFunction.setDefinition("RETURN 43;");
        SqlServerAlertFunctionParam alertFunctionParam = new SqlServerAlertFunctionParam();
        alertFunctionParam.setSchema(SOURCE_SCHEMA);
        alertFunctionParam.setFunction(selectedFunction);
        client.alertFunction(alertFunctionParam);
        var updatedFunctionResults = client.executeSql(SOURCE_SCHEMA,
                "SELECT [EASYSHELL_SRC].[" + functionName + "]() AS [answer]");
        assertEquals(43, ((Number) updatedFunctionResults.getResults().getFirst()
                .getRecords().getFirst().getValue("answer")).intValue());

        SqlServerProcedure selectedProcedure = client.selectProcedure(SOURCE_SCHEMA, procedureName);
        assertFalse(selectedProcedure.getDefinition().stripLeading().toUpperCase()
                .startsWith("CREATE"));
        assertEquals("CALLER", selectedProcedure.getSecurityType());
        selectedProcedure.setDefinition("SELECT 43 AS answer;");
        selectedProcedure.setSecurityType("SELF");
        SqlServerAlertProcedureParam alertProcedureParam = new SqlServerAlertProcedureParam();
        alertProcedureParam.setSchema(SOURCE_SCHEMA);
        alertProcedureParam.setProcedure(selectedProcedure);
        client.alertProcedure(alertProcedureParam);
        assertTrue(client.selectProcedure(SOURCE_SCHEMA, procedureName)
                .getDefinition().contains("43"));
        assertEquals("SELF", client.selectProcedure(SOURCE_SCHEMA, procedureName).getSecurityType());

        SqlServerSequence sequence = new SqlServerSequence();
        sequence.setName(sequenceName);
        sequence.setSchema(SOURCE_SCHEMA);
        sequence.setIncrementBy(java.math.BigInteger.ONE);
        sequence.setCurrentVal(java.math.BigInteger.valueOf(10));
        SqlServerCreateSequenceParam sequenceParam = new SqlServerCreateSequenceParam();
        sequenceParam.setSchema(SOURCE_SCHEMA);
        sequenceParam.setSequence(sequence);
        client.createSequence(sequenceParam);
        SqlServerSequence selectedSequence = client.selectSequence(SOURCE_SCHEMA, sequenceName);
        assertNotNull(selectedSequence);
        assertEquals(java.math.BigInteger.ONE, selectedSequence.getMinVal());
        assertEquals(java.math.BigInteger.TEN, selectedSequence.getCurrentVal());
        assertTrue(client.selectSequencesSimple(SOURCE_SCHEMA).stream()
                .anyMatch(item -> sequenceName.equals(item.getName())));
    }

    @Test
    public void clientFunctionAndProcedureParametersExecuteThroughClient() {
        String functionName = "PARAM_FUNCTION";
        String procedureName = "PARAM_PROCEDURE";
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + functionName + "]', N'FN') IS NOT NULL DROP FUNCTION [EASYSHELL_SRC].["
                + functionName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + procedureName + "]', N'P') IS NOT NULL DROP PROCEDURE [EASYSHELL_SRC].["
                + procedureName + "]");

        SqlServerRoutineParam functionParam = new SqlServerRoutineParam();
        functionParam.setName("value");
        functionParam.setType("INT");
        functionParam.setMode("IN");
        functionParam.setPosition(1);
        SqlServerRoutineParam outputParam = new SqlServerRoutineParam();
        outputParam.setName("other");
        outputParam.setType("INT");
        outputParam.setMode("OUT");
        outputParam.setPosition(2);
        SqlServerRoutineParam inoutParam = new SqlServerRoutineParam();
        inoutParam.setName("third");
        inoutParam.setType("INT");
        inoutParam.setMode("INOUT");
        inoutParam.setPosition(3);
        SqlServerFunction function = new SqlServerFunction();
        function.setName(functionName);
        function.setSchema(SOURCE_SCHEMA);
        function.setParams(List.of(functionParam, outputParam, inoutParam));
        SqlServerRoutineParam functionReturn = new SqlServerRoutineParam();
        functionReturn.setType("INT");
        function.setReturnParam(functionReturn);
        function.setDefinition("RETURN @value + @other + @third;");
        function.setSecurityType("CALLER");
        SqlServerCreateFunctionParam functionCreate = new SqlServerCreateFunctionParam();
        functionCreate.setSchema(SOURCE_SCHEMA);
        functionCreate.setFunction(function);
        client.createFunction(functionCreate);
        SqlServerFunction selectedFunction = client.selectFunction(SOURCE_SCHEMA, functionName);
        assertEquals("CALLER", selectedFunction.getSecurityType());
        assertTrue(selectedFunction.getParams().stream()
                .allMatch(param -> "IN".equals(param.getMode())));
        selectedFunction.setSecurityType("SELF");
        SqlServerAlertFunctionParam functionAlert = new SqlServerAlertFunctionParam();
        functionAlert.setSchema(SOURCE_SCHEMA);
        functionAlert.setFunction(selectedFunction);
        client.alertFunction(functionAlert);
        assertEquals("SELF", client.selectFunction(SOURCE_SCHEMA, functionName).getSecurityType());
        var functionResults = client.executeSql(SOURCE_SCHEMA,
                "SELECT [EASYSHELL_SRC].[" + functionName + "](1,2,3) AS [answer]");
        assertEquals(6, ((Number) functionResults.getResults().getFirst()
                .getRecords().getFirst().getValue("answer")).intValue());

        SqlServerRoutineParam procedureParam = new SqlServerRoutineParam();
        procedureParam.setName("value");
        procedureParam.setType("INT");
        procedureParam.setMode("IN");
        procedureParam.setPosition(1);
        SqlServerRoutineParam procedureOut = new SqlServerRoutineParam();
        procedureOut.setName("result");
        procedureOut.setType("INT");
        procedureOut.setMode("OUT");
        procedureOut.setPosition(2);
        SqlServerProcedure procedure = new SqlServerProcedure();
        procedure.setName(procedureName);
        procedure.setSchema(SOURCE_SCHEMA);
        procedure.setParams(List.of(procedureParam, procedureOut));
        procedure.setDefinition("SET @result = @value * 3; SELECT @result AS [answer];");
        SqlServerCreateProcedureParam procedureCreate = new SqlServerCreateProcedureParam();
        procedureCreate.setSchema(SOURCE_SCHEMA);
        procedureCreate.setProcedure(procedure);
        client.createProcedure(procedureCreate);
        SqlServerProcedure selectedProcedure = client.selectProcedure(SOURCE_SCHEMA, procedureName);
        assertEquals("OUT", selectedProcedure.getParams().stream()
                .filter(param -> "result".equals(param.getName()))
                .findFirst().orElseThrow().getMode());
        var procedureResults = client.executeSql(SOURCE_SCHEMA,
                "EXEC [EASYSHELL_SRC].[" + procedureName + "] @value=7, @result=0");
        assertEquals(21, ((Number) procedureResults.getResults().getFirst()
                .getRecords().getFirst().getValue("answer")).intValue());
    }

    @Test
    public void clientClonesRoutinesWithMultiSpaceCreateHeaders() {
        String procedureName = "CLONE_PROCEDURE_SRC";
        String procedureClone = "CLONE_PROCEDURE_DST";
        String functionName = "CLONE_FUNCTION_SRC";
        String functionClone = "CLONE_FUNCTION_DST";
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + procedureName + "]', N'P') IS NOT NULL DROP PROCEDURE [EASYSHELL_SRC].["
                + procedureName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + procedureClone + "]', N'P') IS NOT NULL DROP PROCEDURE [EASYSHELL_SRC].["
                + procedureClone + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + functionName + "]', N'FN') IS NOT NULL DROP FUNCTION [EASYSHELL_SRC].["
                + functionName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + functionClone + "]', N'FN') IS NOT NULL DROP FUNCTION [EASYSHELL_SRC].["
                + functionClone + "]");

        SqlServerProcedure procedure = new SqlServerProcedure();
        procedure.setName(procedureName);
        procedure.setSchema(SOURCE_SCHEMA);
        procedure.setDefinition("SELECT 1 AS [answer];");
        procedure.setSecurityType("SELF");
        SqlServerCreateProcedureParam procedureParam = new SqlServerCreateProcedureParam();
        procedureParam.setSchema(SOURCE_SCHEMA);
        procedureParam.setProcedure(procedure);
        client.createProcedure(procedureParam);
        client.cloneProcedure(SOURCE_SCHEMA, procedureName, procedureClone);
        assertEquals("SELF", client.selectProcedure(SOURCE_SCHEMA, procedureClone).getSecurityType());

        SqlServerFunction function = new SqlServerFunction();
        function.setName(functionName);
        function.setSchema(SOURCE_SCHEMA);
        function.setDefinition("RETURN 1;");
        SqlServerRoutineParam returnParam = new SqlServerRoutineParam();
        returnParam.setType("INT");
        function.setReturnParam(returnParam);
        SqlServerCreateFunctionParam functionParam = new SqlServerCreateFunctionParam();
        functionParam.setSchema(SOURCE_SCHEMA);
        functionParam.setFunction(function);
        client.createFunction(functionParam);
        client.cloneFunction(SOURCE_SCHEMA, functionName, functionClone);
        var cloneResult = client.executeSql(SOURCE_SCHEMA,
                "SELECT [EASYSHELL_SRC].[" + functionClone + "]() AS [answer]");
        assertEquals(1, ((Number) cloneResult.getResults().getFirst()
                .getRecords().getFirst().getValue("answer")).intValue());

        client.executeSqlSimple(SOURCE_SCHEMA, "DROP PROCEDURE [EASYSHELL_SRC].["
                + procedureName + "]; DROP PROCEDURE [EASYSHELL_SRC].[" + procedureClone + "];"
                + "DROP FUNCTION [EASYSHELL_SRC].[" + functionName + "];"
                + "DROP FUNCTION [EASYSHELL_SRC].[" + functionClone + "];");
    }

    @Test
    public void clientExportsAllFormatsAndImportsPortableFormats() throws Exception {
        String source = "EXPORT_TYPES";
        String target = "IMPORT_TYPES";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + target + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + source + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_SRC].[EXPORT_TYPES] (
                    [ID] INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    [C_INT] INT NULL,
                    [C_DECIMAL] DECIMAL(18,4) NULL,
                    [C_BIT] BIT NULL,
                    [C_NVARCHAR] NVARCHAR(200) NULL,
                    [C_VARBINARY] VARBINARY(64) NULL,
                    [C_DATE] DATE NULL,
                    [C_DATETIME2] DATETIME2(3) NULL,
                    [C_DATETIMEOFFSET] DATETIMEOFFSET(7) NULL,
                    [C_UUID] UNIQUEIDENTIFIER NULL,
                    [C_COMPUTED] AS ([C_INT] + 1) PERSISTED
                )
                """);
        client.executeSqlSimple(SOURCE_SCHEMA, """
                INSERT INTO [EASYSHELL_SRC].[EXPORT_TYPES]
                    ([C_INT],[C_DECIMAL],[C_BIT],[C_NVARCHAR],[C_VARBINARY],[C_DATE],[C_DATETIME2],
                     [C_DATETIMEOFFSET],[C_UUID])
                VALUES
                    (9,123.4500,1,N'中文,"quoted";line',0x00FF10,'2026-11-22',
                     '2026-11-22T10:20:30.123','2026-11-22T10:20:30.123+08:00',
                     '12345678-1234-5678-1234-567812345678'),
                    (10,-0.2500,0,N'',0x,'2024-02-29',
                     '2024-02-29T23:59:59.987',NULL,'87654321-4321-8765-4321-876543218765')
                """);
        SqlServerColumns columns = new SqlServerColumns(client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, source)));
        Path directory = Files.createTempDirectory("easyshell-sqlserver-export-");
        for (String type : List.of("csv", "json", "xml", "txt", "xls", "xlsx", "sql", "html")) {
            Path file = directory.resolve(source + "." + type);
            ShellSqlServerDataExportTable exportTable = new ShellSqlServerDataExportTable();
            exportTable.setName(source);
            exportTable.columns(columns);
            exportTable.setSelected(true);
            exportTable.setFilePath(file.toString());
            ShellSqlServerDataExportHandler handler =
                    new ShellSqlServerDataExportHandler(client, SOURCE_SCHEMA);
            handler.setFileType(type);
            handler.setTables(List.of(exportTable));
            handler.setContinueWithErrors(false);
            handler.doExport();
            assertTrue(type + " export is empty", Files.size(file) > 0);
        }

        for (String type : List.of("csv", "json", "xml", "txt", "xls", "xlsx")) {
            client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + target + "]");
            client.cloneTable(SOURCE_SCHEMA, source, target, false);
            ShellSqlServerDataImportFile importFile = new ShellSqlServerDataImportFile();
            importFile.setSchema(SOURCE_SCHEMA);
            importFile.setDbClient(client);
            importFile.setFile(directory.resolve(source + "." + type).toFile());
            importFile.setTargetTableName(target);
            ShellSqlServerDataImportHandler handler =
                    new ShellSqlServerDataImportHandler(client, SOURCE_SCHEMA);
            handler.setFileType(type.equals("xls") || type.equals("xlsx") ? "excel" : type);
            handler.setFiles(List.of(importFile));
            handler.importMode("2");
            handler.dateFormat("yyyy-MM-dd HH:mm:ss");
            handler.setContinueWithErrors(false);
            handler.doImport();
            assertEquals(type + " import row count", 2,
                    client.selectRecordCount(selectParamFor(SOURCE_SCHEMA, target)));
            List<SqlServerRecord> importedRecords = client.selectRecords(
                    selectParamFor(SOURCE_SCHEMA, target));
            SqlServerRecord imported = importedRecords.stream()
                    .filter(row -> ((Number) row.getValue("C_INT")).intValue() == 9)
                    .findFirst().orElseThrow();
            assertEquals(type + " unicode", "中文,\"quoted\";line", imported.getValue("C_NVARCHAR"));
            assertEquals(type + " computed", 10,
                    ((Number) imported.getValue("C_COMPUTED")).intValue());
            assertNotNull(type + " datetimeoffset", imported.getValue("C_DATETIMEOFFSET"));
            assertEquals(type + " bit", Boolean.TRUE, imported.getValue("C_BIT"));
            SqlServerRecord emptyRow = importedRecords.stream()
                    .filter(row -> ((Number) row.getValue("C_INT")).intValue() == 10)
                    .findFirst().orElseThrow();
            assertEquals(type + " empty string", "", emptyRow.getValue("C_NVARCHAR"));
            assertEquals(type + " empty binary", 0,
                    ((byte[]) emptyRow.getValue("C_VARBINARY")).length);
        }
    }

    @Test
    public void clientPreservesBitBinaryOffsetDateTimeAndEmptyStrings() {
        String table = "TYPE_EDGE_CASES";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + table + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_SRC].[TYPE_EDGE_CASES] (
                    [ID] BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    [C_BIT] BIT NULL,
                    [C_VARBINARY] VARBINARY(64) NULL,
                    [C_NVARCHAR] NVARCHAR(100) NULL,
                    [C_DATETIME2] DATETIME2(7) NULL,
                    [C_DATETIMEOFFSET] DATETIMEOFFSET(7) NULL,
                    [C_ROWVERSION] ROWVERSION
                )
                """);
        SqlServerColumns columns = new SqlServerColumns(client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, table)));
        SqlServerRecord record = new SqlServerRecord(columns);
        record.putValue("C_BIT", false);
        record.putValue("C_VARBINARY", new byte[]{0, 1, (byte) 0xFF});
        record.putValue("C_NVARCHAR", "");
        record.putValue("C_DATETIME2", java.time.LocalDateTime.of(2026, 11, 22, 10, 20, 30, 123_000_000));
        record.putValue("C_DATETIMEOFFSET", java.time.OffsetDateTime.parse("2026-11-22T10:20:30+08:00"));
        SqlServerRecordPrimaryKey primaryKey = new SqlServerRecordPrimaryKey();
        primaryKey.init(columns.column("ID"), record);
        SqlServerInsertRecordParam insertParam = new SqlServerInsertRecordParam();
        insertParam.setSchema(SOURCE_SCHEMA);
        insertParam.setTableName(table);
        insertParam.setRecord(record.getRecordData());
        insertParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(insertParam));
        assertNotNull(primaryKey.getReturnData());

        SqlServerRecord selected = client.selectRecords(selectParamFor(SOURCE_SCHEMA, table)).getFirst();
        assertEquals(Boolean.FALSE, selected.getValue("C_BIT"));
        assertTrue(selected.getValue("C_VARBINARY") instanceof byte[]);
        assertEquals("", selected.getValue("C_NVARCHAR"));
        assertNotNull(selected.getValue("C_DATETIMEOFFSET"));

        var query = client.executeSql(SOURCE_SCHEMA,
                "SELECT [C_BIT],[C_VARBINARY],[C_NVARCHAR],[C_DATETIMEOFFSET] FROM [EASYSHELL_SRC].["
                        + table + "]");
        SqlServerRecord queried = query.getResults().getFirst().getRecords().getFirst();
        assertEquals(Boolean.FALSE, queried.getValue("C_BIT"));
        assertTrue(queried.getValue("C_VARBINARY") instanceof byte[]);
        assertEquals("", queried.getValue("C_NVARCHAR"));
        assertTrue(queried.getValue("C_DATETIMEOFFSET").toString().contains("+08:00"));

        String insertSql = ShellSqlServerDataUtil.toInsertSql(columns, selected, true);
        assertTrue(insertSql.contains("0x0001FF"));
        assertFalse(insertSql.contains("E'\\\\x"));
    }

    @Test
    public void clientResetsSequenceDefaultsAfterImport() throws Exception {
        String sourceTable = "SEQ_IMPORT_SOURCE";
        String targetTable = "SEQ_IMPORT_TARGET";
        String sourceSequence = "SEQ_IMPORT_SOURCE_ID";
        String targetSequence = "SEQ_IMPORT_TARGET_ID";
        for (String schema : List.of(SOURCE_SCHEMA, TARGET_SCHEMA)) {
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + sourceTable + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + targetTable + "]");
            client.executeSqlSimple(schema, "DROP SEQUENCE IF EXISTS [" + schema + "].[" + sourceSequence + "]");
            client.executeSqlSimple(schema, "DROP SEQUENCE IF EXISTS [" + schema + "].[" + targetSequence + "]");
        }
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE SEQUENCE [EASYSHELL_SRC].[SEQ_IMPORT_SOURCE_ID]
                    AS INT START WITH 100 INCREMENT BY 2;
                CREATE TABLE [EASYSHELL_SRC].[SEQ_IMPORT_SOURCE] (
                    [ID] INT NOT NULL
                        CONSTRAINT [DF_SEQ_IMPORT_SOURCE_ID]
                        DEFAULT (NEXT VALUE FOR [EASYSHELL_SRC].[SEQ_IMPORT_SOURCE_ID]),
                    [NAME] NVARCHAR(100) NULL,
                    CONSTRAINT [PK_SEQ_IMPORT_SOURCE] PRIMARY KEY ([ID])
                );
                INSERT INTO [EASYSHELL_SRC].[SEQ_IMPORT_SOURCE] ([NAME])
                    VALUES (N'first'),(N'second'),(N'third');
                """);
        client.executeSqlSimple(TARGET_SCHEMA, """
                CREATE SEQUENCE [EASYSHELL_DST].[SEQ_IMPORT_TARGET_ID]
                    AS INT START WITH 1 INCREMENT BY 2;
                CREATE TABLE [EASYSHELL_DST].[SEQ_IMPORT_TARGET] (
                    [ID] INT NOT NULL
                        CONSTRAINT [DF_SEQ_IMPORT_TARGET_ID]
                        DEFAULT (NEXT VALUE FOR [EASYSHELL_DST].[SEQ_IMPORT_TARGET_ID]),
                    [NAME] NVARCHAR(100) NULL,
                    CONSTRAINT [PK_SEQ_IMPORT_TARGET] PRIMARY KEY ([ID])
                );
                """);

        SqlServerColumns sourceColumns = new SqlServerColumns(client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, sourceTable)));
        Path file = Files.createTempFile("easyshell-sqlserver-sequence-", ".csv");
        ShellSqlServerDataExportTable exportTable = new ShellSqlServerDataExportTable();
        exportTable.setName(sourceTable);
        exportTable.columns(sourceColumns);
        exportTable.setSelected(true);
        exportTable.setFilePath(file.toString());
        ShellSqlServerDataExportHandler exportHandler =
                new ShellSqlServerDataExportHandler(client, SOURCE_SCHEMA);
        exportHandler.setFileType("csv");
        exportHandler.setTables(List.of(exportTable));
        exportHandler.setContinueWithErrors(false);
        exportHandler.doExport();

        ShellSqlServerDataImportFile importFile = new ShellSqlServerDataImportFile();
        importFile.setSchema(TARGET_SCHEMA);
        importFile.setDbClient(client);
        importFile.setFile(file.toFile());
        importFile.setTargetTableName(targetTable);
        ShellSqlServerDataImportHandler importHandler =
                new ShellSqlServerDataImportHandler(client, TARGET_SCHEMA);
        importHandler.setFileType("csv");
        importHandler.setFiles(List.of(importFile));
        importHandler.importMode("1");
        importHandler.dateFormat("yyyy-MM-dd HH:mm:ss");
        importHandler.setContinueWithErrors(false);
        importHandler.doImport();
        assertEquals(3, client.selectRecordCount(selectParamFor(TARGET_SCHEMA, targetTable)));

        client.executeSqlSimple(TARGET_SCHEMA,
                "INSERT INTO [EASYSHELL_DST].[SEQ_IMPORT_TARGET] ([NAME]) VALUES (N'fourth')");
        Object count = client.executeSql(TARGET_SCHEMA,
                        "SELECT COUNT(*) AS [CNT] FROM [EASYSHELL_DST].[SEQ_IMPORT_TARGET] WHERE [ID] = 106")
                .getResults().getFirst().getRecords().getFirst().getValue("CNT");
        assertTrue(count instanceof Number);
        assertEquals(1, ((Number) count).intValue());
    }

    @Test
    public void clientDumpRunSqlFileAndGoBatches() throws Exception {
        String source = "DUMP_TYPES";
        String procedure = "DUMP_GO_PROCEDURE";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_DST].[" + source + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + source + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_DST].["
                + procedure + "]', N'P') IS NOT NULL DROP PROCEDURE [EASYSHELL_DST].["
                + procedure + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "IF OBJECT_ID(N'[EASYSHELL_SRC].["
                + procedure + "]', N'P') IS NOT NULL DROP PROCEDURE [EASYSHELL_SRC].["
                + procedure + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_SRC].[DUMP_TYPES] (
                    [ID] INT IDENTITY(5,1) NOT NULL PRIMARY KEY,
                    [NAME] NVARCHAR(100) NULL,
                    [DATA] VARBINARY(32) NULL,
                    [COMPUTED] AS (LEN([NAME])) PERSISTED
                );
                INSERT INTO [EASYSHELL_SRC].[DUMP_TYPES] ([NAME],[DATA])
                    VALUES (N'转储',0x010203);
                """);
        Path dumpFile = Files.createTempFile("easyshell-sqlserver-", ".sql");
        ShellSqlServerDataDumpHandler dumpHandler =
                new ShellSqlServerDataDumpHandler(client, SOURCE_SCHEMA);
        dumpHandler.setDumpType((byte) 2);
        dumpHandler.setTableName(source);
        dumpHandler.setDataType((byte) 0);
        dumpHandler.dumpFile(dumpFile.toFile());
        dumpHandler.setContinueWithErrors(false);
        dumpHandler.doDump();
        String dump = Files.readString(dumpFile, StandardCharsets.UTF_8);
        assertTrue(dump.contains("Source Schema : " + SOURCE_SCHEMA));
        assertTrue(dump.contains("GO"));
        assertTrue(dump.contains("[COMPUTED] AS"));

        ShellSqlServerDataRunSqlFileHandler runHandler =
                new ShellSqlServerDataRunSqlFileHandler(client, TARGET_SCHEMA);
        runHandler.setContinueWithErrors(false);
        runHandler.file(dumpFile.toFile());
        runHandler.runFile();
        assertEquals(1, client.selectRecordCount(selectParamFor(TARGET_SCHEMA, source)));
        assertTrue(client.selectColumns(new SqlServerSelectColumnParam(TARGET_SCHEMA, source)).stream()
                .anyMatch(column -> "COMPUTED".equals(column.getName()) && column.isGenerated()));

        String goProcedure = """
                DROP TABLE IF EXISTS [EASYSHELL_DST].[GO_BATCH_TABLE];
                GO
                CREATE TABLE [EASYSHELL_DST].[GO_BATCH_TABLE] ([ID] INT NOT NULL PRIMARY KEY);
                GO
                INSERT INTO [EASYSHELL_DST].[GO_BATCH_TABLE] ([ID]) VALUES (1);
                GO
                CREATE OR ALTER PROCEDURE [EASYSHELL_DST].[DUMP_GO_PROCEDURE]
                AS
                BEGIN
                    SELECT COUNT(*) AS [CNT] FROM [EASYSHELL_DST].[GO_BATCH_TABLE];
                END;
                GO
                SELECT COUNT(*) AS [CNT] FROM [EASYSHELL_DST].[GO_BATCH_TABLE];
                """;
        var goResults = client.executeSql(TARGET_SCHEMA, goProcedure);
        assertTrue(goResults.isSuccess());
        assertEquals(1, ((Number) goResults.getResults().getLast()
                .getRecords().getFirst().getValue("CNT")).intValue());
        assertNotNull(client.selectProcedure(TARGET_SCHEMA, procedure));
    }

    @Test
    public void clientFullSchemaDumpRestoresAllObjectKinds() throws Exception {
        String table = "DUMP_ALL_PARENT";
        String child = "DUMP_ALL_CHILD";
        String view = "DUMP_ALL_VIEW";
        String function = "DUMP_ALL_FUNCTION";
        String procedure = "DUMP_ALL_PROCEDURE";
        String sequence = "DUMP_ALL_SEQUENCE";
        String trigger = "DUMP_ALL_TRIGGER";
        String triggerLog = "DUMP_ALL_TRIGGER_LOG";
        for (String schema : List.of(DUMP_SOURCE_SCHEMA, DUMP_TARGET_SCHEMA)) {
            client.executeSqlSimple(schema, "DROP VIEW IF EXISTS [" + schema + "].[" + view + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + child + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + table + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + triggerLog + "]");
            client.executeSqlSimple(schema, "IF OBJECT_ID(N'[" + schema + "].["
                    + function + "]', N'FN') IS NOT NULL DROP FUNCTION [" + schema + "].["
                    + function + "]");
            client.executeSqlSimple(schema, "IF OBJECT_ID(N'[" + schema + "].["
                    + procedure + "]', N'P') IS NOT NULL DROP PROCEDURE [" + schema + "].["
                    + procedure + "]");
            client.executeSqlSimple(schema, "DROP SEQUENCE IF EXISTS [" + schema + "].["
                    + sequence + "]");
        }
        client.executeSqlSimple(DUMP_SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_DUMP_SRC].[DUMP_ALL_PARENT] (
                    [ID] INT IDENTITY(2,1) NOT NULL PRIMARY KEY,
                    [NAME] NVARCHAR(80) NOT NULL,
                    [VALUE] DECIMAL(18,2) NULL,
                    [DOUBLE_VALUE] AS ([VALUE] * 2) PERSISTED
                );
                CREATE TABLE [EASYSHELL_DUMP_SRC].[DUMP_ALL_CHILD] (
                    [ID] INT NOT NULL PRIMARY KEY,
                    [PARENT_ID] INT NOT NULL REFERENCES [EASYSHELL_DUMP_SRC].[DUMP_ALL_PARENT]([ID])
                );
                CREATE TABLE [EASYSHELL_DUMP_SRC].[DUMP_ALL_TRIGGER_LOG] (
                    [ID] INT IDENTITY(1,1) PRIMARY KEY,
                    [SOURCE_ID] INT NULL
                );
                CREATE INDEX [IX_DUMP_ALL_PARENT_NAME]
                    ON [EASYSHELL_DUMP_SRC].[DUMP_ALL_PARENT]([NAME]);
                CREATE OR ALTER TRIGGER [EASYSHELL_DUMP_SRC].[DUMP_ALL_TRIGGER]
                    ON [EASYSHELL_DUMP_SRC].[DUMP_ALL_PARENT] AFTER INSERT AS
                BEGIN
                    INSERT INTO [EASYSHELL_DUMP_SRC].[DUMP_ALL_TRIGGER_LOG]([SOURCE_ID])
                    SELECT [ID] FROM inserted;
                END;
                CREATE OR ALTER VIEW [EASYSHELL_DUMP_SRC].[DUMP_ALL_VIEW] AS
                    SELECT [ID],[NAME],[DOUBLE_VALUE] FROM [EASYSHELL_DUMP_SRC].[DUMP_ALL_PARENT];
                CREATE OR ALTER FUNCTION [EASYSHELL_DUMP_SRC].[DUMP_ALL_FUNCTION]()
                    RETURNS INT AS BEGIN RETURN (SELECT COUNT(*) FROM [EASYSHELL_DUMP_SRC].[DUMP_ALL_CHILD]); END;
                CREATE OR ALTER PROCEDURE [EASYSHELL_DUMP_SRC].[DUMP_ALL_PROCEDURE]
                    AS BEGIN SELECT COUNT(*) AS [CNT] FROM [EASYSHELL_DUMP_SRC].[DUMP_ALL_CHILD]; END;
                CREATE SEQUENCE [EASYSHELL_DUMP_SRC].[DUMP_ALL_SEQUENCE] START WITH 9 INCREMENT BY 2;
                INSERT INTO [EASYSHELL_DUMP_SRC].[DUMP_ALL_PARENT]([NAME],[VALUE]) VALUES(N'完整转储',12.5);
                INSERT INTO [EASYSHELL_DUMP_SRC].[DUMP_ALL_CHILD]([ID],[PARENT_ID]) VALUES(1,2);
                """);
        Path dumpFile = Files.createTempFile("easyshell-sqlserver-full-", ".sql");
        ShellSqlServerDataDumpHandler dumpHandler =
                new ShellSqlServerDataDumpHandler(client, DUMP_SOURCE_SCHEMA);
        dumpHandler.setDumpType((byte) 1);
        dumpHandler.setDataType((byte) 0);
        dumpHandler.dumpFile(dumpFile.toFile());
        dumpHandler.setContinueWithErrors(false);
        dumpHandler.doDump();
        String dump = Files.readString(dumpFile, StandardCharsets.UTF_8);
        assertTrue(dump.contains("Function structure for " + function));
        assertTrue(dump.contains("Procedure structure for " + procedure));
        assertTrue(dump.contains("View structure for " + view));
        assertTrue(dump.contains("Trigger structure for " + trigger));

        ShellSqlServerDataRunSqlFileHandler runHandler =
                new ShellSqlServerDataRunSqlFileHandler(client, DUMP_TARGET_SCHEMA);
        runHandler.setContinueWithErrors(false);
        runHandler.file(dumpFile.toFile());
        runHandler.runFile();

        assertEquals(1, client.selectRecordCount(selectParamFor(DUMP_TARGET_SCHEMA, table)));
        assertEquals(1, client.selectRecordCount(selectParamFor(DUMP_TARGET_SCHEMA, child)));
        assertEquals(1, client.selectRecordCount(selectParamFor(DUMP_TARGET_SCHEMA, triggerLog)));
        assertTrue(client.existView(DUMP_TARGET_SCHEMA, view));
        assertTrue(client.selectFunctionsSimple(DUMP_TARGET_SCHEMA).stream()
                .anyMatch(item -> function.equals(item.getName())));
        assertTrue(client.selectProceduresSimple(DUMP_TARGET_SCHEMA).stream()
                .anyMatch(item -> procedure.equals(item.getName())));
        assertTrue(client.selectSequencesSimple(DUMP_TARGET_SCHEMA).stream()
                .anyMatch(item -> sequence.equals(item.getName())));
        assertEquals(1, client.selectTriggers(DUMP_TARGET_SCHEMA, table).size());
        assertEquals(25.0, ((Number) client.selectRecords(
                selectParamFor(DUMP_TARGET_SCHEMA, table)).getFirst()
                .getValue("DOUBLE_VALUE")).doubleValue(), 0.001);
    }

    @Test
    public void clientTransportsSchemaObjectsAndData() throws Exception {
        String parent = "TRANSPORT_PARENT";
        String child = "TRANSPORT_CHILD";
        String view = "TRANSPORT_VIEW";
        String function = "TRANSPORT_FUNCTION";
        String procedure = "TRANSPORT_PROCEDURE";
        String sequence = "TRANSPORT_SEQUENCE";
        String triggerLog = "TRANSPORT_TRIGGER_LOG";
        String trigger = "TRG_TRANSPORT_PARENT";
        for (String schema : List.of(SOURCE_SCHEMA, TARGET_SCHEMA)) {
            client.executeSqlSimple(schema, "DROP VIEW IF EXISTS [" + schema + "].[" + view + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + child + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + parent + "]");
            client.executeSqlSimple(schema, "DROP TABLE IF EXISTS [" + schema + "].[" + triggerLog + "]");
            client.executeSqlSimple(schema, "IF OBJECT_ID(N'[" + schema + "].["
                    + function + "]', N'FN') IS NOT NULL DROP FUNCTION [" + schema + "].["
                    + function + "]");
            client.executeSqlSimple(schema, "IF OBJECT_ID(N'[" + schema + "].["
                    + procedure + "]', N'P') IS NOT NULL DROP PROCEDURE [" + schema + "].["
                    + procedure + "]");
            client.executeSqlSimple(schema, "DROP SEQUENCE IF EXISTS [" + schema + "].[" + sequence + "]");
        }
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TABLE [EASYSHELL_SRC].[TRANSPORT_PARENT] (
                    [ID] INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    [NAME] NVARCHAR(100) NOT NULL,
                    [VALUE] INT NULL,
                    [DOUBLE_VALUE] AS ([VALUE] * 2) PERSISTED,
                    CONSTRAINT [CK_TRANSPORT_PARENT] CHECK ([VALUE] >= 0)
                );
                CREATE INDEX [IX_TRANSPORT_PARENT_NAME]
                    ON [EASYSHELL_SRC].[TRANSPORT_PARENT] ([NAME]);
                CREATE TABLE [EASYSHELL_SRC].[TRANSPORT_CHILD] (
                    [ID] INT NOT NULL PRIMARY KEY,
                    [PARENT_ID] INT NOT NULL,
                    CONSTRAINT [FK_TRANSPORT_CHILD_PARENT]
                        FOREIGN KEY ([PARENT_ID])
                        REFERENCES [EASYSHELL_SRC].[TRANSPORT_PARENT] ([ID])
                );
                CREATE TABLE [EASYSHELL_SRC].[TRANSPORT_TRIGGER_LOG] (
                    [ID] INT IDENTITY(1,1) PRIMARY KEY,
                    [SOURCE_ID] INT NULL
                );
                CREATE OR ALTER TRIGGER [EASYSHELL_SRC].[TRG_TRANSPORT_PARENT]
                    ON [EASYSHELL_SRC].[TRANSPORT_PARENT] AFTER INSERT AS
                BEGIN
                    INSERT INTO [EASYSHELL_SRC].[TRANSPORT_TRIGGER_LOG] ([SOURCE_ID])
                    SELECT [ID] FROM inserted;
                END;
                EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'迁移注释',
                    @level0type=N'SCHEMA', @level0name=N'EASYSHELL_SRC',
                    @level1type=N'TABLE', @level1name=N'TRANSPORT_PARENT';
                INSERT INTO [EASYSHELL_SRC].[TRANSPORT_PARENT] ([NAME],[VALUE])
                    VALUES (N'父表',21);
                INSERT INTO [EASYSHELL_SRC].[TRANSPORT_CHILD] ([ID],[PARENT_ID])
                    VALUES (1,1);
                """);
        SqlServerView transportView = new SqlServerView();
        transportView.setName(view);
        transportView.setDefinition("SELECT [ID],[NAME],[DOUBLE_VALUE] FROM [EASYSHELL_SRC].["
                + parent + "]");
        SqlServerCreateViewParam viewParam = new SqlServerCreateViewParam();
        viewParam.setSchema(SOURCE_SCHEMA);
        viewParam.setView(transportView);
        client.createView(viewParam);

        SqlServerFunction transportFunction = new SqlServerFunction();
        transportFunction.setName(function);
        transportFunction.setSchema(SOURCE_SCHEMA);
        SqlServerRoutineParam returnParam = new SqlServerRoutineParam();
        returnParam.setType("INT");
        transportFunction.setReturnParam(returnParam);
        transportFunction.setDefinition("RETURN (SELECT COUNT(*) FROM [EASYSHELL_SRC].[" + child + "]);");
        SqlServerCreateFunctionParam functionParam = new SqlServerCreateFunctionParam();
        functionParam.setSchema(SOURCE_SCHEMA);
        functionParam.setFunction(transportFunction);
        client.createFunction(functionParam);

        SqlServerProcedure transportProcedure = new SqlServerProcedure();
        transportProcedure.setName(procedure);
        transportProcedure.setSchema(SOURCE_SCHEMA);
        transportProcedure.setDefinition("SELECT COUNT(*) AS [CNT] FROM [EASYSHELL_SRC].[" + child + "];");
        SqlServerCreateProcedureParam procedureParam = new SqlServerCreateProcedureParam();
        procedureParam.setSchema(SOURCE_SCHEMA);
        procedureParam.setProcedure(transportProcedure);
        client.createProcedure(procedureParam);

        SqlServerSequence transportSequence = new SqlServerSequence();
        transportSequence.setName(sequence);
        transportSequence.setSchema(SOURCE_SCHEMA);
        transportSequence.setIncrementBy(java.math.BigInteger.ONE);
        transportSequence.setCurrentVal(java.math.BigInteger.valueOf(20));
        SqlServerCreateSequenceParam sequenceParam = new SqlServerCreateSequenceParam();
        sequenceParam.setSchema(SOURCE_SCHEMA);
        sequenceParam.setSequence(transportSequence);
        client.createSequence(sequenceParam);

        ShellSqlServerDataTransportHandler handler = new ShellSqlServerDataTransportHandler();
        handler.setSourceClient(client);
        handler.setTargetClient(client);
        handler.setSourceDatabase(SOURCE_SCHEMA);
        handler.setTargetDatabase(TARGET_SCHEMA);
        handler.setDataType(0);
        handler.setContinueWithErrors(false);
        handler.setTables(List.of(transportObject(parent), transportObject(child),
                transportObject(triggerLog)));
        handler.setTriggers(List.of(transportObject(trigger)));
        handler.setViews(List.of(transportObject(view)));
        handler.setFunctions(List.of(transportObject(function)));
        handler.setProcedures(List.of(transportObject(procedure)));
        handler.setSequences(List.of(transportObject(sequence)));
        handler.doTransport();
        // 再执行一次，验证目标库已有父子外键时也能按子表优先删除并重建
        handler.doTransport();

        assertEquals(1, client.selectRecordCount(selectParamFor(TARGET_SCHEMA, parent)));
        assertEquals(1, client.selectRecordCount(selectParamFor(TARGET_SCHEMA, child)));
        assertEquals(42, ((Number) client.selectRecords(selectParamFor(TARGET_SCHEMA, parent))
                .getFirst().getValue("DOUBLE_VALUE")).intValue());
        assertEquals("迁移注释", client.selectTable(TARGET_SCHEMA, parent).getComment());
        assertTrue(client.existView(TARGET_SCHEMA, view));
        assertEquals(1, client.viewRecords(TARGET_SCHEMA, view, 0L, 10L, null).size());
        assertTrue(client.selectFunctionsSimple(TARGET_SCHEMA).stream()
                .anyMatch(item -> function.equals(item.getName())));
        assertTrue(client.selectProceduresSimple(TARGET_SCHEMA).stream()
                .anyMatch(item -> procedure.equals(item.getName())));
        assertTrue(client.selectSequencesSimple(TARGET_SCHEMA).stream()
                .anyMatch(item -> sequence.equals(item.getName())));
        assertTrue(client.indexes(TARGET_SCHEMA, parent).stream()
                .anyMatch(index -> "IX_TRANSPORT_PARENT_NAME".equals(index.getName())
                        && "NONCLUSTERED".equalsIgnoreCase(index.getMethod())));
        assertFalse(client.selectChecks(TARGET_SCHEMA, parent).isEmpty());
        assertFalse(client.selectForeignKeys(TARGET_SCHEMA, child).isEmpty());
        assertEquals(1, client.selectTriggers(TARGET_SCHEMA, parent).size());
        client.executeSqlSimple(TARGET_SCHEMA, "INSERT INTO [EASYSHELL_DST].["
                + parent + "] ([NAME],[VALUE]) VALUES (N'触发',1)");
        assertEquals(2, client.selectRecordCount(selectParamFor(TARGET_SCHEMA, triggerLog)));
    }

    @Test
    public void clientTableDesignCreatesAndAltersComputedColumnsAndConstraints() {
        String tableName = "DESIGN_COMPUTED";
        String triggerLog = "DESIGN_TRIGGER_LOG";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].["
                + triggerLog + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].["
                + tableName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "CREATE TABLE [EASYSHELL_SRC].["
                + triggerLog + "] ([ID] INT IDENTITY(1,1) PRIMARY KEY, [SOURCE_ID] INT NULL)");
        SqlServerTable table = new SqlServerTable();
        table.setSchema(SOURCE_SCHEMA);
        table.setName(tableName);
        table.setComment("设计表");

        SqlServerColumn id = designColumn("ID", "INT", null, 1);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        id.setNullable(false);
        SqlServerColumn name = designColumn("NAME", "NVARCHAR", 100, 2);
        name.setNullable(false);
        name.setDefaultValue("N''");
        SqlServerColumn amount = designColumn("AMOUNT", "DECIMAL", 18, 3);
        amount.setDigits(2);
        amount.setNullable(true);
        SqlServerColumn computed = designColumn("DOUBLE_AMOUNT", "DECIMAL", 18, 4);
        computed.setGenerated(true);
        computed.setGeneratedExpression("[AMOUNT] * 2");
        computed.setGeneratedStorage("PERSISTED");

        SqlServerIndex index = new SqlServerIndex();
        index.setName("IX_DESIGN_COMPUTED_NAME");
        index.setType("UNIQUE");
        index.addColumn("NAME");
        index.setCreated(true);
        SqlServerCheck check = new SqlServerCheck("CK_DESIGN_COMPUTED_AMOUNT");
        check.setClause("[AMOUNT] >= 0");
        check.setCreated(true);
        SqlServerTrigger trigger = new SqlServerTrigger();
        trigger.setName("TRG_DESIGN_COMPUTED_INSERT");
        trigger.setPolicy("AFTER INSERT");
        trigger.setDefinition("INSERT INTO [EASYSHELL_SRC].[" + triggerLog
                + "] ([SOURCE_ID]) SELECT [ID] FROM inserted;");

        SqlServerCreateTableParam createParam = new SqlServerCreateTableParam();
        createParam.setTable(table);
        createParam.setColumns(new SqlServerColumns(List.of(id, name, amount, computed)));
        createParam.setIndexes(DBObjects.of(List.of(index)));
        createParam.setChecks(DBObjects.of(List.of(check)));
        createParam.setTriggers(DBObjects.of(List.of(trigger)));
        client.createTable(createParam);

        List<SqlServerColumn> createdColumns = client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, tableName));
        SqlServerColumn createdComputed = createdColumns.stream()
                .filter(column -> "DOUBLE_AMOUNT".equals(column.getName()))
                .findFirst().orElseThrow();
        assertTrue(createdComputed.isGenerated());
        assertEquals("PERSISTED", createdComputed.getGeneratedStorage());
        assertTrue(client.indexes(SOURCE_SCHEMA, tableName).stream()
                .anyMatch(item -> "IX_DESIGN_COMPUTED_NAME".equals(item.getName())));
        assertFalse(client.selectChecks(SOURCE_SCHEMA, tableName).isEmpty());
        assertEquals("设计表", client.selectTable(SOURCE_SCHEMA, tableName).getComment());
        client.executeSqlSimple(SOURCE_SCHEMA, "INSERT INTO [EASYSHELL_SRC].["
                + tableName + "] ([NAME],[AMOUNT]) VALUES (N'trigger',1)");
        assertEquals(1, client.selectRecordCount(selectParamFor(SOURCE_SCHEMA, triggerLog)));
        assertEquals(1, client.selectTriggers(SOURCE_SCHEMA, tableName).size());
        SqlServerTrigger createdTrigger = client.selectTriggers(SOURCE_SCHEMA, tableName).getFirst();
        assertFalse(createdTrigger.getDefinition().contains("END;;"));
        createdTrigger.setDefinition("BEGIN INSERT INTO [EASYSHELL_SRC].[" + triggerLog
                + "] ([SOURCE_ID]) SELECT [ID] FROM inserted; END;;");
        createdTrigger.setChanged(true);

        createdColumns = client.selectColumns(new SqlServerSelectColumnParam(SOURCE_SCHEMA, tableName));
        createdColumns.stream()
                .filter(column -> "NAME".equals(column.getName()))
                .findFirst().orElseThrow()
                .setSize(200);
        createdColumns.stream()
                .filter(column -> "DOUBLE_AMOUNT".equals(column.getName()))
                .findFirst().orElseThrow()
                .setGeneratedExpression("[AMOUNT] * 3");
        SqlServerColumn added = designColumn("ADDED_COL", "NVARCHAR", 50,
                createdColumns.size() + 1);
        added.setCreated(true);
        added.setNullable(true);
        createdColumns.add(added);
        SqlServerColumn addedComputed = designColumn("NAME_LENGTH", "INT", null,
                createdColumns.size() + 1);
        addedComputed.setCreated(true);
        addedComputed.setGeneratedExpression("LEN([NAME])");
        addedComputed.setGeneratedStorage("VIRTUAL");
        createdColumns.add(addedComputed);

        SqlServerIndex addedIndex = new SqlServerIndex();
        addedIndex.setName("IX_DESIGN_COMPUTED_ADDED");
        addedIndex.setType("NORMAL");
        addedIndex.addColumn("ADDED_COL");
        addedIndex.setCreated(true);
        SqlServerCheck addedCheck = new SqlServerCheck("CK_DESIGN_COMPUTED_NAME");
        addedCheck.setClause("LEN([NAME]) > 0");
        addedCheck.setCreated(true);

        SqlServerTable alertTable = new SqlServerTable();
        alertTable.setSchema(SOURCE_SCHEMA);
        alertTable.setName(tableName);
        SqlServerAlertTableParam alertParam = new SqlServerAlertTableParam();
        alertParam.setTable(alertTable);
        alertParam.setColumns(new SqlServerColumns(createdColumns));
        alertParam.setIndexes(DBObjects.of(List.of(addedIndex)));
        alertParam.setChecks(DBObjects.of(List.of(addedCheck)));
        alertParam.setTriggers(DBObjects.of(List.of(createdTrigger)));
        alertParam.setExistAutoIncrement(client.existAutoIncrement(SOURCE_SCHEMA, tableName));
        alertParam.setPrimaryKeys(client.selectePrimaryKeys(SOURCE_SCHEMA, tableName));
        client.alertTable(alertParam);

        List<SqlServerColumn> alteredColumns = client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, tableName));
        assertEquals(200, alteredColumns.stream()
                .filter(column -> "NAME".equals(column.getName()))
                .findFirst().orElseThrow().getSize().intValue());
        assertTrue(alteredColumns.stream().anyMatch(column ->
                "DOUBLE_AMOUNT".equals(column.getName())
                        && column.getGeneratedExpression().contains("3")));
        assertTrue(alteredColumns.stream().anyMatch(column ->
                "NAME_LENGTH".equals(column.getName()) && column.isGenerated()));
        assertTrue(client.indexes(SOURCE_SCHEMA, tableName).stream()
                .anyMatch(item -> "IX_DESIGN_COMPUTED_ADDED".equals(item.getName())));
        assertTrue(client.selectChecks(SOURCE_SCHEMA, tableName).stream()
                .anyMatch(item -> "CK_DESIGN_COMPUTED_NAME".equals(item.getName())));
        String alteredTriggerDefinition = client.selectTriggers(SOURCE_SCHEMA, tableName)
                .getFirst().getDefinition();
        assertFalse(alteredTriggerDefinition.contains("END;;"));
        assertTrue(alteredTriggerDefinition.endsWith("END;"));
    }

    @Test
    public void clientAltersPrimaryKeyColumnType() {
        String tableName = "ALTER_PRIMARY_KEY_TYPE";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP TABLE IF EXISTS [EASYSHELL_SRC].["
                + tableName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "CREATE TABLE [EASYSHELL_SRC].[" + tableName
                + "] ([ID] INT NOT NULL PRIMARY KEY, [NAME] NVARCHAR(100) NULL)");

        List<SqlServerColumn> columns = client.selectColumns(
                new SqlServerSelectColumnParam(SOURCE_SCHEMA, tableName));
        SqlServerColumn id = columns.stream()
                .filter(column -> "ID".equals(column.getName()))
                .findFirst().orElseThrow();
        id.setType("BIGINT");

        SqlServerTable table = new SqlServerTable();
        table.setSchema(SOURCE_SCHEMA);
        table.setName(tableName);
        SqlServerAlertTableParam param = new SqlServerAlertTableParam();
        List<String> originalPrimaryKeys = client.selectePrimaryKeys(SOURCE_SCHEMA, tableName);
        param.setTable(table);
        param.setColumns(new SqlServerColumns(columns));
        param.setIndexes(new DBObjects<>());
        param.setForeignKeys(new DBObjects<>());
        param.setChecks(new DBObjects<>());
        param.setTriggers(new DBObjects<>());
        param.setPrimaryKeys(originalPrimaryKeys);
        param.setExistAutoIncrement(client.existAutoIncrement(SOURCE_SCHEMA, tableName));
        client.alertTable(param);

        SqlServerColumn altered = client.selectColumns(
                        new SqlServerSelectColumnParam(SOURCE_SCHEMA, tableName))
                .stream()
                .filter(column -> "ID".equals(column.getName()))
                .findFirst().orElseThrow();
        assertEquals("BIGINT", altered.getType());
        assertTrue(altered.isPrimaryKey());
        assertEquals(originalPrimaryKeys, client.selectePrimaryKeys(SOURCE_SCHEMA, tableName));
    }

    @Test
    public void clientEditsSimpleUpdatableViewWithBasePrimaryKey() {
        String tableName = "EDITABLE_VIEW_BASE";
        String viewName = "EDITABLE_VIEW";
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP VIEW IF EXISTS [EASYSHELL_SRC].["
                + viewName + "]; DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + tableName + "]");
        client.executeSqlSimple(SOURCE_SCHEMA, "CREATE TABLE [EASYSHELL_SRC].[" + tableName
                + "] ([ID] INT IDENTITY(1,1) NOT NULL PRIMARY KEY, [LOG] VARCHAR(20) NULL)");
        client.executeSqlSimple(SOURCE_SCHEMA, "INSERT INTO [EASYSHELL_SRC].["
                + tableName + "] ([LOG]) VALUES ('a')");

        SqlServerView view = new SqlServerView();
        view.setName(viewName);
        view.setDefinition("SELECT * FROM [EASYSHELL_SRC].[" + tableName + "]");
        SqlServerCreateViewParam createParam = new SqlServerCreateViewParam();
        createParam.setSchema(SOURCE_SCHEMA);
        createParam.setView(view);
        client.createView(createParam);

        SqlServerView selectedView = client.selectView(SOURCE_SCHEMA, viewName);
        assertTrue(selectedView.isUpdatable());
        List<SqlServerColumn> viewColumns = client.viewColumns(SOURCE_SCHEMA, viewName);
        SqlServerColumn idColumn = viewColumns.stream()
                .filter(column -> "ID".equals(column.getName()))
                .findFirst().orElseThrow();
        assertTrue(idColumn.isPrimaryKey());
        assertTrue(idColumn.isAutoIncrement());

        SqlServerRecord insert = new SqlServerRecord(new SqlServerColumns(viewColumns));
        insert.putValue(idColumn, null);
        insert.putValue(viewColumns.stream()
                .filter(column -> "LOG".equals(column.getName()))
                .findFirst().orElseThrow(), "b");
        SqlServerInsertRecordParam insertParam = new SqlServerInsertRecordParam();
        insertParam.setSchema(SOURCE_SCHEMA);
        insertParam.setTableName(viewName);
        insertParam.setRecord(insert.getRecordData());
        assertEquals(1, client.insertRecord(insertParam));

        SqlServerRecord selected = client.viewRecords(SOURCE_SCHEMA, viewName, 0L, 10L, null)
                .stream().filter(record -> "b".equals(record.getValue("LOG")))
                .findFirst().orElseThrow();
        selected.putValue("LOG", "c");
        selected.getProperty("LOG").setChanged(true);
        SqlServerRecordPrimaryKey primaryKey = new SqlServerRecordPrimaryKey();
        primaryKey.init(idColumn, selected);
        SqlServerUpdateRecordParam updateParam = new SqlServerUpdateRecordParam();
        updateParam.setSchema(SOURCE_SCHEMA);
        updateParam.setTableName(viewName);
        updateParam.setPrimaryKey(primaryKey);
        updateParam.setUpdateRecord(selected.getChangedRecordData());
        assertEquals(1, client.updateRecord(updateParam));

        SqlServerDeleteRecordParam deleteParam = new SqlServerDeleteRecordParam();
        deleteParam.setSchema(SOURCE_SCHEMA);
        deleteParam.setTableName(viewName);
        deleteParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.deleteRecord(deleteParam));
        client.executeSqlSimple(SOURCE_SCHEMA, "DROP VIEW IF EXISTS [EASYSHELL_SRC].["
                + viewName + "]; DROP TABLE IF EXISTS [EASYSHELL_SRC].[" + tableName + "]");
    }

    private SqlServerColumn designColumn(String name, String type, Integer size, int position) {
        SqlServerColumn column = new SqlServerColumn(name);
        column.setType(type);
        column.setSize(size);
        column.setPosition(position);
        column.setSchema(SOURCE_SCHEMA);
        column.setTableName("DESIGN_COMPUTED");
        return column;
    }

    private DBDataTransportObject transportObject(String name) {
        DBDataTransportObject object = new DBDataTransportObject();
        object.setName(name);
        return object;
    }

    private SqlServerSelectRecordParam selectParamFor(String schema, String table) {
        SqlServerSelectRecordParam param = new SqlServerSelectRecordParam();
        param.setSchema(schema);
        param.setTableName(table);
        param.setStart(0L);
        param.setLimit(100L);
        return param;
    }
}
