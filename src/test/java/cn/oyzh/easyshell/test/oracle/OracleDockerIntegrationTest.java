package cn.oyzh.easyshell.test.oracle;

import cn.oyzh.common.db.SqlDatabase;
import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.ShellOracleConnManager;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.function.OracleAlertFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleCreateFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.view.OracleAlertViewParam;
import cn.oyzh.easyshell.oracle.view.OracleCreateViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.sql.DBOraclePagination;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Oracle Docker JDBC集成测试；仅在提供oracle.docker.url时运行。
 */
public class OracleDockerIntegrationTest {

    private static String mode;

    private static String url;

    private static String user;

    private static String password;

    private static String systemUser;

    private static String systemPassword;

    private static String host;

    private static int port;

    @BeforeClass
    public static void loadConfiguration() {
        ShellOracleColumnUtil.init();
        mode = value("oracle.docker.mode", "ORACLE_DOCKER_MODE", "23c").toLowerCase(Locale.ROOT);
        url = value("oracle.docker.url", "ORACLE_DOCKER_URL", null);
        user = value("oracle.docker.user", "ORACLE_DOCKER_USER", "DEMO");
        password = value("oracle.docker.password", "ORACLE_DOCKER_PASSWORD", "Demo2026");
        systemUser = value("oracle.docker.system.user", "ORACLE_DOCKER_SYSTEM_USER", "SYSTEM");
        systemPassword = value("oracle.docker.system.password", "ORACLE_DOCKER_SYSTEM_PASSWORD", "easyshell@2026");
        Assume.assumeTrue("Set oracle.docker.url to run Oracle Docker integration tests", url != null && !url.isBlank());
        Assume.assumeTrue("oracle.docker.mode must be 11g or 23c", mode.equals("11g") || mode.equals("23c"));

        var matcher = java.util.regex.Pattern
                .compile("@tcp://([^:/]+):(\\d+)")
                .matcher(url);
        Assume.assumeTrue("oracle.docker.url must be a TCP thin URL", matcher.find());
        host = matcher.group(1);
        port = Integer.parseInt(matcher.group(2));
    }

    @Test
    public void validatesJdbcUrlsMetadataCrudPlSqlAndPagination() throws Exception {
        try (Connection connection = open(url, user, password);
             ShellOracleConnManager manager = createManager(user, password);
             Connection managedConnection = manager.initConnection("DEMO", user, password)) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertEquals("Oracle", metadata.getDatabaseProductName());
            String version = metadata.getDatabaseProductVersion();
            assertEquals(mode.equals("11g"), !DBOraclePagination.supportsOffsetFetch(version));

            assertScalar(managedConnection, "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL", "DEMO");
            assertObjects(connection);
            assertResultMetadata(connection);
            assertTimestampDisplay(connection);
            assertCrud(connection);
            assertPlSql(connection);
            assertPagination(connection, version);
            assertEasyShellClientMetadata();
            assertSystemSequencesUseBigInteger();
        }

        String serviceDatabase = mode.equals("11g") ? "XE" : "FREEPDB1";
        DBConnConfig serviceConfig = config(serviceDatabase);
        assertEquals("jdbc:oracle:thin:@tcp://" + host + ":" + port + "/" + serviceDatabase,
                ShellOracleConnManager.buildConnectionString(serviceConfig));

        String sid = mode.equals("11g") ? "XE" : "FREE";
        DBConnConfig sidConfig = config("SID:" + sid);
        String sidUrl = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
        assertEquals(sidUrl, ShellOracleConnManager.buildConnectionString(sidConfig));
        try (Connection ignored = open(sidUrl, systemUser, systemPassword)) {
        }

        String descriptor = "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=" + host + ")(PORT=" + port
                + "))(CONNECT_DATA=(SERVICE_NAME=" + serviceDatabase + ")))";
        DBConnConfig descriptorConfig = config(descriptor);
        assertEquals("jdbc:oracle:thin:@" + descriptor,
                ShellOracleConnManager.buildConnectionString(descriptorConfig));
        try (Connection ignored = open("jdbc:oracle:thin:@" + descriptor, user, password)) {
        }
    }

    private static void assertObjects(Connection connection) throws SQLException {
        assertTrue(queryValues(connection, "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = 'DEMO' AND OBJECT_TYPE = 'TABLE'").containsAll(List.of("USERS", "ORDERS")));
        assertTrue(queryValues(connection, "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = 'DEMO' AND OBJECT_TYPE = 'VIEW'").contains("V_USERS"));
        assertTrue(queryValues(connection, "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = 'DEMO'").contains("DEMO_SEQ"));
        assertTrue(queryValues(connection, "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = 'DEMO' AND OBJECT_TYPE = 'PROCEDURE'").contains("ADD_USER"));
        assertTrue(queryValues(connection, "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = 'DEMO' AND OBJECT_TYPE = 'FUNCTION'").contains("USER_COUNT"));
        assertTrue(queryValues(connection, "SELECT TRIGGER_NAME FROM ALL_TRIGGERS WHERE OWNER = 'DEMO'").containsAll(List.of("USERS_UPDATED_AT_TRG")));

        List<String> autoIncrementObjects = queryValues(connection,
                "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = 'DEMO' AND OBJECT_NAME LIKE '%_ID_%'");
        if (mode.equals("11g")) {
            assertTrue(autoIncrementObjects.containsAll(List.of("USERS_ID_SEQ", "USERS_ID_TRG", "ORDERS_ID_SEQ", "ORDERS_ID_TRG")));
        } else {
            assertFalse(autoIncrementObjects.stream().anyMatch(name -> name.endsWith("_ID_TRG")));
        }

        try (ResultSet keys = connection.getMetaData().getPrimaryKeys(null, "DEMO", "USERS")) {
            assertTrue(keys.next());
            assertEquals("ID", keys.getString("COLUMN_NAME"));
        }
    }

    private static void assertResultMetadata(Connection connection) throws SQLException {
        String sql = "SELECT ID, USERNAME FROM (SELECT ID, USERNAME FROM USERS ORDER BY ID) WHERE ROWNUM <= 1";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            ResultSetMetaData metadata = resultSet.getMetaData();
            assertEquals(2, metadata.getColumnCount());
            assertEquals("ID", metadata.getColumnLabel(1));
            assertEquals("USERNAME", metadata.getColumnLabel(2));
            assertEquals("NUMBER", metadata.getColumnTypeName(1));
            assertEquals("VARCHAR2", metadata.getColumnTypeName(2));
        }
    }

    private static void assertTimestampDisplay(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT CREATED_AT FROM USERS WHERE ROWNUM = 1")) {
            assertTrue(resultSet.next());
            Object value = ShellOracleDataUtil.valueStandardization(resultSet.getObject(1));
            assertTrue(value instanceof java.sql.Timestamp);
        }
    }

    private static void assertCrud(Connection connection) throws Exception {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM USERS WHERE USERNAME = 'easyshell_jdbc_check'");
            statement.executeUpdate("INSERT INTO USERS(USERNAME, EMAIL) VALUES ('easyshell_jdbc_check', 'jdbc@example.com')");
            statement.executeUpdate("UPDATE USERS SET EMAIL = 'updated@example.com' WHERE USERNAME = 'easyshell_jdbc_check'");
        }
        assertScalar(connection, "SELECT EMAIL FROM USERS WHERE USERNAME = 'easyshell_jdbc_check'", "updated@example.com");
        connection.commit();
        try (Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("DELETE FROM USERS WHERE USERNAME = 'easyshell_jdbc_check'"));
        }
        connection.commit();
        connection.setAutoCommit(autoCommit);
    }

    private static void assertPlSql(Connection connection) throws Exception {
        String script = """
                CREATE OR REPLACE PROCEDURE EASYSHELL_JDBC_CHECK_PROC AS
                BEGIN
                    INSERT INTO USERS(USERNAME, EMAIL) VALUES ('easyshell_plsql_check', 'plsql@example.com');
                END;
                /

                BEGIN
                    EASYSHELL_JDBC_CHECK_PROC;
                    COMMIT;
                END;
                /
                """;
        List<String> statements = SqlUtil.split(script, SqlDatabase.ORACLE);
        assertEquals(2, statements.size());
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
        assertScalar(connection, "SELECT COUNT(*) FROM USERS WHERE USERNAME = 'easyshell_plsql_check'", "1");
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP PROCEDURE EASYSHELL_JDBC_CHECK_PROC");
            statement.executeUpdate("DELETE FROM USERS WHERE USERNAME = 'easyshell_plsql_check'");
            statement.execute("COMMIT");
        }
    }

    private static void assertPagination(Connection connection, String version) throws SQLException {
        String source = "SELECT ID, USERNAME FROM USERS ORDER BY ID";
        String sql = DBOraclePagination.paginate(source, 1, 2, version);
        List<String> values = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                values.add(resultSet.getString("USERNAME"));
            }
        }
        assertEquals(List.of("bob", "carol"), values);
    }

    private static void assertEasyShellClientMetadata() throws Exception {
        ShellConnect shellConnect = new ShellConnect();
        shellConnect.setType(ShellPrototype.ORACLE);
        shellConnect.setHost(host + ":" + port);
        shellConnect.setDatabase(mode.equals("11g") ? "XE" : "FREEPDB1");
        shellConnect.setUser(user);
        shellConnect.setPassword(password);
        shellConnect.setConnectTimeOut(10);

        ShellOracleClient client = new ShellOracleClient(shellConnect);
        try {
            try {
                client.start(10000);
            } catch (Exception ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }

            assertTrue(client.selectTablesSimple("DEMO").stream()
                    .allMatch(table -> StringUtil.isBlank(table.getCreateDefinition())));
            assertTrue(client.selectTables("DEMO").stream().anyMatch(table -> "USERS".equals(table.getName())
                    && !table.getCreateDefinition().isBlank()));
            assertTrue(client.selectViewsSimple("DEMO").stream()
                    .allMatch(view -> StringUtil.isBlank(view.getDefinition())
                            && StringUtil.isBlank(view.getCreateDefinition())));
            var views = client.selectViews("DEMO");
            assertTrue(views.size() >= 3);
            assertTrue(views.stream().anyMatch(view -> "V_USERS".equals(view.getName())
                    && !view.getDefinition().isBlank() && !view.getCreateDefinition().isBlank()));
            assertTrue(views.stream().allMatch(view -> !view.getCreateDefinition().isBlank()));
            assertTrue(client.selectProcedures("DEMO").stream().anyMatch(procedure -> "ADD_USER".equals(procedure.getName())
                    && !procedure.getCreateDefinition().isBlank()));
            assertTrue(client.selectFunctions("DEMO").stream().anyMatch(function -> "USER_COUNT".equals(function.getName())
                    && !function.getCreateDefinition().isBlank()));
            assertTrue(client.selectTriggersSimple("DEMO").stream()
                    .allMatch(trigger -> StringUtil.isBlank(trigger.getDefinition())
                            && StringUtil.isBlank(trigger.getCreateDefinition())));
            assertTrue(client.selectTriggers("DEMO").stream().anyMatch(trigger -> "USERS_UPDATED_AT_TRG".equals(trigger.getName())
                    && !trigger.getCreateDefinition().isBlank()));

            OracleSequence sequence = client.selectSequence("DEMO", "DEMO_SEQ");
            assertEquals("DEMO_SEQ", sequence.getName());
            assertEquals(BigInteger.ONE, sequence.getMinVal());
            assertEquals(BigInteger.valueOf(5), sequence.getIncrementBy());
            assertTrue(sequence.getMaxVal().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
            assertFalse(sequence.getCreateDefinition().isBlank());
            assertSequenceAlterExecution(client);
            assertProcedureRenameExecution(client);
            assertFunctionParameterRoundTrip(client);
            assertFunctionParameterCreationRoundTrip(client);
            assertFunctionSecurityRoundTrip(client);
            assertPipelinedMetadataFallback(client);
            assertViewCreateAndAlertExecution(client);

            OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
            columnParam.setSchema("DEMO");
            columnParam.setTableName("USERS");
            List<OracleColumn> columns = client.selectColumns(columnParam);
            assertTrue(columns.stream().anyMatch(column -> "ID".equals(column.getName())));

            List<OracleColumn> viewColumns = client.viewColumns("DEMO", "V_ORDERS");
            OracleColumn viewId = viewColumns.stream()
                    .filter(column -> "ID".equals(column.getName()))
                    .findFirst()
                    .orElseThrow();
            assertTrue(viewId.isPrimaryKey());
            OracleColumn viewCreatedAt = viewColumns.stream()
                    .filter(column -> "CREATED_AT".equals(column.getName()))
                    .findFirst()
                    .orElseThrow();
            assertTrue(viewCreatedAt.supportTimestamp());
            assertTrue(viewCreatedAt.isDateTimeType());

            var viewResult = client.executeSingleSql("DEMO", "SELECT * FROM V_ORDERS");
            assertTrue(viewResult.getPrimaryKey() != null && "ID".equals(viewResult.getPrimaryKey().getName()));
            assertTrue(viewResult.getColumns().column("CREATED_AT").supportTimestamp());
        } finally {
            client.close();
        }
    }

    private static void assertSequenceAlterExecution(ShellOracleClient client) {
        String name = "ALTER_SEQ_TEST";
        String schema = "DEMO";
        BigInteger maxVal = new BigInteger("9999999999999999999999999998");
        OracleSequence original = new OracleSequence(name);
        original.setSchema(schema);
        original.setMinVal(BigInteger.ONE);
        original.setMaxVal(maxVal.add(BigInteger.ONE));
        original.setCurrentVal(BigInteger.ONE);
        original.setCacheSize(BigInteger.TWO);
        original.setIncrementBy(BigInteger.valueOf(5));
        original.setCycle(false);
        original.setOrder(false);
        OracleSequence sequence = new OracleSequence(name);
        sequence.setSchema(schema);
        sequence.setMinVal(BigInteger.ONE);
        sequence.setMaxVal(maxVal);
        sequence.setCurrentVal(BigInteger.ONE);
        sequence.setCacheSize(BigInteger.TWO);
        sequence.setIncrementBy(BigInteger.valueOf(5));
        sequence.setCycle(false);
        sequence.setOrder(false);

        OracleCreateSequenceParam createParam = new OracleCreateSequenceParam();
        createParam.setSchema(schema);
        createParam.setSequence(original);
        client.createSequence(createParam);
        try {
            OracleAlertSequenceParam alertParam = new OracleAlertSequenceParam();
            alertParam.setSchema(schema);
            alertParam.setSequence(sequence);
            client.alertSequence(alertParam);

            OracleSequence altered = client.selectSequence(schema, name);
            assertEquals(BigInteger.ONE, altered.getMinVal());
            assertEquals(maxVal, altered.getMaxVal());
            assertEquals(BigInteger.TWO, altered.getCacheSize());
            assertEquals(BigInteger.valueOf(5), altered.getIncrementBy());
        } finally {
            client.dropSequence(sequence);
        }
    }

    private static void assertViewCreateAndAlertExecution(ShellOracleClient client) {
        String viewName = "EASYSHELL_VIEW_CREATE_TEST";
        OracleView view = new OracleView();
        view.setSchema("DEMO");
        view.setName(viewName);
        view.setDefinition("SELECT ID, USERNAME FROM USERS");
        view.setSecurityType("CURRENT_USER");
        view.setComment("EASYSHELL_VIEW_COMMENT");
        view.setUpdatable(false);
        OracleCreateViewParam param = new OracleCreateViewParam();
        param.setSchema("DEMO");
        param.setView(view);
        client.createView(param);
        try {
            OracleView created = client.selectView("DEMO", viewName);
            assertFalse("created view should be read only", created.isUpdatable());
            if (mode.equals("11g")) {
                assertTrue(StringUtil.isBlank(created.getSecurityType()));
            } else {
                assertEquals("CURRENT_USER", created.getSecurityType());
            }
            assertEquals("EASYSHELL_VIEW_COMMENT", created.getComment());
            assertFalse(created.getDefinition().toUpperCase(Locale.ROOT).contains("WITH READ ONLY"));

            view.setDefinition(created.getDefinition() + "\nWITH READ ONLY");
            view.setSecurityType("DEFINER");
            view.setComment("EASYSHELL_VIEW_COMMENT_UPDATED");
            view.setUpdatable(false);
            OracleAlertViewParam alertParam = new OracleAlertViewParam();
            alertParam.setSchema("DEMO");
            alertParam.setView(view);
            client.alertView(alertParam);

            OracleView altered = client.selectView("DEMO", viewName);
            assertFalse("altered view should be read only", altered.isUpdatable());
            if (mode.equals("11g")) {
                assertTrue(StringUtil.isBlank(altered.getSecurityType()));
            } else {
                assertEquals("DEFINER", altered.getSecurityType());
            }
            assertEquals("EASYSHELL_VIEW_COMMENT_UPDATED", altered.getComment());
            assertEquals(0, altered.getDefinition().toUpperCase(Locale.ROOT)
                    .split("WITH READ ONLY", -1).length - 1);
        } finally {
            client.dropView(view);
        }
    }

    private static void assertProcedureRenameExecution(ShellOracleClient client) throws Exception {
        String schema = "DEMO";
        String oldName = "EASYSHELL_RENAME_PROC";
        String newName = "EASYSHELL_RENAME_PROC_NEW";
        boolean renamed = false;
        dropProcedureIfPresent(client, schema, oldName);
        dropProcedureIfPresent(client, schema, newName);
        try (Statement statement = client.getConnManager().connection(schema).createStatement()) {
            statement.execute("CREATE OR REPLACE PROCEDURE " + oldName + " AS BEGIN NULL; END;");
        }
        try {
            client.renameProcedure(schema, oldName, newName);
            renamed = true;
            assertTrue(client.selectProceduresSimple(schema).stream()
                    .anyMatch(procedure -> newName.equals(procedure.getName())));
            client.renameProcedure(schema, newName, oldName);
            renamed = false;
            assertTrue(client.selectProceduresSimple(schema).stream()
                    .anyMatch(procedure -> oldName.equals(procedure.getName())));
        } finally {
            if (renamed) {
                client.renameProcedure(schema, newName, oldName);
            }
            dropProcedureIfPresent(client, schema, oldName);
            dropProcedureIfPresent(client, schema, newName);
        }
    }

    private static void dropProcedureIfPresent(ShellOracleClient client, String schema, String name) {
        if (client.selectProceduresSimple(schema).stream().anyMatch(procedure -> name.equals(procedure.getName()))) {
            OracleProcedure procedure = new OracleProcedure();
            procedure.setSchema(schema);
            procedure.setName(name);
            client.dropProcedure(procedure);
        }
    }

    private static void assertFunctionParameterRoundTrip(ShellOracleClient client) {
        String schema = "DEMO";
        String name = "EASYSHELL_PARAM_FUNC";
        dropFunctionIfPresent(client, schema, name);
        OracleFunction function = new OracleFunction();
        function.setSchema(schema);
        function.setName(name);
        function.setDefinition("BEGIN\n    RETURN 2;\nEND;");
        OracleRoutineParam initialReturnParam = new OracleRoutineParam();
        initialReturnParam.setType("NUMBER");
        function.setReturnParam(initialReturnParam);
        OracleCreateFunctionParam createParam = new OracleCreateFunctionParam();
        createParam.setSchema(schema);
        createParam.setFunction(function);
        client.createFunction(createParam);
        try {
            function.setParams(List.of(
                    routineParam("P_IN_VALUE", "VARCHAR2", "IN"),
                    routineParam("P_OUT_VALUE", "NUMBER", "OUT"),
                    routineParam("P_INOUT_VALUE", "VARCHAR2", "IN/OUT")
            ));
            function.setReturnParam(null);
            function.setDefinition("BEGIN\n    p_out_value := 1;\n    RETURN 2;\nEND;");
            OracleAlertFunctionParam alertParam = new OracleAlertFunctionParam();
            alertParam.setSchema(schema);
            alertParam.setFunction(function);
            client.alertFunction(alertParam);

            OracleFunction loaded = client.selectFunction(schema, name);
            assertEquals(3, loaded.getParams().size());
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_IN_VALUE".equals(item.getName()) && "IN".equals(item.getMode())));
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_OUT_VALUE".equals(item.getName()) && "OUT".equals(item.getMode())));
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_INOUT_VALUE".equals(item.getName()) && "IN/OUT".equals(item.getMode())));
            assertTrue(loaded.getCreateDefinition().contains("P_IN_VALUE"));
            assertTrue(loaded.getCreateDefinition().contains("P_OUT_VALUE"));
            assertTrue(loaded.getCreateDefinition().contains("P_INOUT_VALUE"));
        } finally {
            dropFunctionIfPresent(client, schema, name);
        }
    }

    private static void assertFunctionParameterCreationRoundTrip(ShellOracleClient client) {
        String schema = "DEMO";
        String name = "EASYSHELL_PARAM_CREATE_FUNC";
        dropFunctionIfPresent(client, schema, name);
        OracleFunction function = new OracleFunction();
        function.setSchema(schema);
        function.setName(name);
        function.setParams(List.of(
                routineParam("P_IN_VALUE", "VARCHAR2", "IN"),
                routineParam("P_OUT_VALUE", "NUMBER", "OUT"),
                routineParam("P_INOUT_VALUE", "VARCHAR2", "IN/OUT")
        ));
        OracleRoutineParam returnParam = new OracleRoutineParam();
        returnParam.setType("NUMBER");
        function.setReturnParam(returnParam);
        function.setDefinition("BEGIN\n    p_out_value := 1;\n    RETURN 2;\nEND;");
        OracleCreateFunctionParam param = new OracleCreateFunctionParam();
        param.setSchema(schema);
        param.setFunction(function);
        client.createFunction(param);
        try {
            OracleFunction loaded = client.selectFunction(schema, name);
            assertEquals(3, loaded.getParams().size());
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_IN_VALUE".equals(item.getName()) && "IN".equals(item.getMode())));
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_OUT_VALUE".equals(item.getName()) && "OUT".equals(item.getMode())));
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_INOUT_VALUE".equals(item.getName()) && "IN/OUT".equals(item.getMode())));
            assertTrue(loaded.getCreateDefinition().contains("P_IN_VALUE"));
            assertTrue(loaded.getCreateDefinition().contains("P_OUT_VALUE"));
            assertTrue(loaded.getCreateDefinition().contains("P_INOUT_VALUE"));
        } finally {
            dropFunctionIfPresent(client, schema, name);
        }
    }

    private static void assertFunctionSecurityRoundTrip(ShellOracleClient client) {
        String schema = "DEMO";
        String name = "EASYSHELL_SECURITY_FUNC";
        dropFunctionIfPresent(client, schema, name);
        OracleFunction function = new OracleFunction();
        function.setSchema(schema);
        function.setName(name);
        function.setParams(List.of(
                routineParam("P_IN_VALUE", "VARCHAR2", "IN"),
                routineParam("P_OUT_VALUE", "NUMBER", "OUT")
        ));
        OracleRoutineParam returnParam = new OracleRoutineParam();
        returnParam.setType("NUMBER");
        function.setReturnParam(returnParam);
        function.setSecurityType("CURRENT_USER");
        function.setDefinition("BEGIN\n    p_out_value := 1;\n    RETURN 2;\nEND;");
        OracleCreateFunctionParam createParam = new OracleCreateFunctionParam();
        createParam.setSchema(schema);
        createParam.setFunction(function);
        client.createFunction(createParam);
        try {
            OracleFunction created = client.selectFunction(schema, name);
            assertEquals("CURRENT_USER", created.getSecurityType());
            assertEquals(2, created.getParams().size());

            function.setSecurityType("DEFINER");
            OracleAlertFunctionParam alertParam = new OracleAlertFunctionParam();
            alertParam.setSchema(schema);
            alertParam.setFunction(function);
            client.alertFunction(alertParam);

            OracleFunction altered = client.selectFunction(schema, name);
            assertEquals("DEFINER", altered.getSecurityType());
            assertEquals(2, altered.getParams().size());
            assertTrue(altered.getParams().stream().anyMatch(item ->
                    "P_IN_VALUE".equals(item.getName()) && "IN".equals(item.getMode())));
            assertTrue(altered.getParams().stream().anyMatch(item ->
                    "P_OUT_VALUE".equals(item.getName()) && "OUT".equals(item.getMode())));
        } finally {
            dropFunctionIfPresent(client, schema, name);
        }
    }

    private static void assertPipelinedMetadataFallback(ShellOracleClient client) throws Exception {
        String schema = "DEMO";
        String name = "EASYSHELL_INVALID_PIPE_FUNC";
        dropFunctionIfPresent(client, schema, name);
        try (Statement statement = client.getConnManager().connection(schema).createStatement()) {
            statement.execute("CREATE OR REPLACE FUNCTION " + name
                    + " RETURN NUMBER PIPELINED AUTHID DEFINER AS BEGIN RETURN; END;");
        }
        try {
            OracleFunction loaded = client.selectFunction(schema, name);
            assertEquals("DEFINER", loaded.getSecurityType());
            assertTrue(loaded.getCharacteristic().contains("PIPELINED"));
        } finally {
            dropFunctionIfPresent(client, schema, name);
        }
    }

    private static OracleRoutineParam routineParam(String name, String type, String mode) {
        OracleRoutineParam param = new OracleRoutineParam();
        param.setName(name);
        param.setType(type);
        param.setMode(mode);
        param.setSize(22);
        return param;
    }

    private static void dropFunctionIfPresent(ShellOracleClient client, String schema, String name) {
        if (client.selectFunctionsSimple(schema).stream().anyMatch(function -> name.equals(function.getName()))) {
            OracleFunction function = new OracleFunction();
            function.setSchema(schema);
            function.setName(name);
            client.dropFunction(function);
        }
    }

    private static void assertSystemSequencesUseBigInteger() throws Exception {
        ShellConnect shellConnect = new ShellConnect();
        shellConnect.setType(ShellPrototype.ORACLE);
        shellConnect.setHost(host + ":" + port);
        shellConnect.setDatabase(mode.equals("11g") ? "XE" : "FREEPDB1");
        shellConnect.setUser(systemUser);
        shellConnect.setPassword(systemPassword);
        shellConnect.setConnectTimeOut(10);

        ShellOracleClient client = new ShellOracleClient(shellConnect);
        try {
            try {
                client.start(10000);
            } catch (Exception ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            var sequences = client.selectSequencesSimple("SYSTEM");
            if (mode.equals("11g")) {
                assertTrue(sequences.stream().anyMatch(sequence -> sequence.getMaxVal() != null
                        && sequence.getMaxVal().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0));
            }
        } finally {
            client.close();
        }
    }

    private static List<String> queryValues(Connection connection, String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }

    private static void assertScalar(Connection connection, String sql, String expected) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            assertTrue(resultSet.next());
            assertEquals(expected, resultSet.getString(1));
        }
    }

    private static Connection open(String connectionString, String username, String secret) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", secret);
        properties.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        return DriverManager.getConnection(connectionString, properties);
    }

    private static ShellOracleConnManager createManager(String username, String secret) {
        ShellOracleConnManager manager = new ShellOracleConnManager();
        manager.setConfig(configFor(mode.equals("11g") ? "XE" : "FREEPDB1", username, secret));
        return manager;
    }

    private static DBConnConfig config(String database) {
        return configFor(database, user, password);
    }

    private static DBConnConfig configFor(String database, String username, String secret) {
        DBConnConfig config = new DBConnConfig();
        config.setHost(host);
        config.setPort(port);
        config.setDatabase(database);
        config.setUser(username);
        config.setPassword(secret);
        config.setConnectTimeout(10);
        return config;
    }

    private static String value(String property, String environment, String defaultValue) {
        String result = System.getProperty(property);
        if (result == null || result.isBlank()) {
            result = System.getenv(environment);
        }
        return result == null || result.isBlank() ? defaultValue : result;
    }
}
