package cn.oyzh.easyshell.test.oracle;

import cn.oyzh.common.db.SqlDatabase;
import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataDumpHandler;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataRunSqlFileHandler;
import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.ShellOracleConnManager;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.function.OracleAlertFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleCreateFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.record.OracleInsertRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleRecordPrimaryKey;
import cn.oyzh.easyshell.oracle.record.OracleSelectRecordParam;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.view.OracleAlertViewParam;
import cn.oyzh.easyshell.oracle.view.OracleCreateViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.sql.DBOraclePagination;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
        assertInvalidServiceRejected();
    }

    @Test
    public void refreshesExternalTableMetadata() throws Exception {
        ShellConnect shellConnect = new ShellConnect();
        shellConnect.setType(ShellPrototype.ORACLE);
        shellConnect.setHost(host + ":" + port);
        shellConnect.setDatabase(mode.equals("11g") ? "XE" : "FREEPDB1");
        shellConnect.setUser(user);
        shellConnect.setPassword(password);
        shellConnect.setConnectTimeOut(10);

        ShellOracleClient client = new ShellOracleClient(shellConnect);
        String tableName = "EASYSHELL_EXTERNAL_TABLE";
        try {
            try {
                client.start(10000);
            } catch (Exception ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            if (client.selectTablesSimple("DEMO").stream()
                    .anyMatch(table -> tableName.equals(table.getName()))) {
                client.dropTable("DEMO", tableName);
            }
            try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
                statement.executeUpdate("CREATE TABLE \"DEMO\".\"" + tableName
                        + "\" (\"ID\" NUMBER(10) PRIMARY KEY)");
            }
            assertTrue(client.selectTablesSimple("DEMO").stream()
                    .anyMatch(table -> tableName.equals(table.getName())));
            assertEquals(client.tableSize("DEMO"), client.selectTablesSimple("DEMO").size());
        } finally {
            if (client.selectTablesSimple("DEMO").stream()
                    .anyMatch(table -> tableName.equals(table.getName()))) {
                client.dropTable("DEMO", tableName);
            }
            client.close();
        }
    }

    @Test
    public void insertsLobRecordInTemporarySchema() throws Exception {
        String schema = "E2E_LOB_" + Long.toUnsignedString(System.nanoTime(), 16).toUpperCase(Locale.ROOT);
        String password = "Lob2026";
        String tableName = "LOB_INSERT_TEST";
        createTemporarySchema(schema, password);
        ShellOracleClient client = null;
        try {
            try (Connection connection = open(url, schema, password);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE \"" + schema + "\".\"" + tableName + "\" ("
                        + (mode.equals("23c")
                        ? "\"ID\" NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,"
                        : "\"ID\" NUMBER PRIMARY KEY,")
                        + "\"C_CLOB\" CLOB,"
                        + "\"C_NCLOB\" NCLOB,"
                        + "\"C_BLOB\" BLOB,"
                        + "\"C_LONG\" LONG,"
                        + "\"C_XML\" XMLTYPE,"
                        + "\"C_BFILE\" BFILE,"
                        + "\"C_ROWID\" ROWID,"
                        + "\"C_UROWID\" UROWID,"
                        + "\"C_PLAIN\" VARCHAR2(50),"
                        + "\"C_VIRTUAL\" VARCHAR2(50) GENERATED ALWAYS AS (SUBSTR(\"C_PLAIN\", 1, 10)) VIRTUAL"
                        + ")");
                if (mode.equals("11g")) {
                    statement.execute("CREATE SEQUENCE \"" + schema + "\".\"LOB_INSERT_SEQ\"");
                    statement.execute("CREATE OR REPLACE TRIGGER \"" + schema + "\".\"LOB_INSERT_TRG\""
                            + " BEFORE INSERT ON \"" + schema + "\".\"" + tableName + "\" FOR EACH ROW"
                            + " BEGIN"
                            + "   IF :NEW.\"ID\" IS NULL THEN"
                            + "     SELECT \"LOB_INSERT_SEQ\".NEXTVAL INTO :NEW.\"ID\" FROM DUAL;"
                            + "   END IF;"
                            + " END;");
                }
            }

            client = openShellClient(schema, password);
            OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
            columnParam.setSchema(schema);
            columnParam.setTableName(tableName);
            List<OracleColumn> columns = client.selectColumns(columnParam);
            OracleColumn id = columns.stream().filter(column -> "ID".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn virtual = columns.stream().filter(column -> "C_VIRTUAL".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn xml = columns.stream().filter(column -> "C_XML".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn bfile = columns.stream().filter(column -> "C_BFILE".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn clob = columns.stream().filter(column -> "C_CLOB".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn nclob = columns.stream().filter(column -> "C_NCLOB".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn blob = columns.stream().filter(column -> "C_BLOB".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn longColumn = columns.stream().filter(column -> "C_LONG".equals(column.getName()))
                    .findFirst().orElseThrow();
            OracleColumn plain = columns.stream().filter(column -> "C_PLAIN".equals(column.getName()))
                    .findFirst().orElseThrow();
            assertTrue(id.isAutoIncrement());
            assertTrue(virtual.isVirtual());
            assertFalse(virtual.isDmlWritable());
            assertTrue(xml.isXmlType());
            assertTrue(xml.isDmlWritable());
            assertFalse(bfile.isDmlWritable());

            String clobText = "C".repeat(7000);
            String nclobText = "中".repeat(3000);
            String longText = "L".repeat(7000);
            byte[] blobBytes = "B".repeat(7000).getBytes(StandardCharsets.UTF_8);
            String xmlText = "<root>" + "X".repeat(6000) + "</root>";

            OracleRecord record = new OracleRecord(columns);
            record.putValue(clob, clobText);
            record.putValue(nclob, nclobText);
            record.putValue(blob, blobBytes);
            record.putValue(longColumn, longText);
            record.putValue(xml, xmlText);
            record.putValue(plain, "plain-value");
            record.putValue(columns.stream().filter(column -> "C_ROWID".equals(column.getName()))
                    .findFirst().orElseThrow(), null);
            record.putValue(columns.stream().filter(column -> "C_UROWID".equals(column.getName()))
                    .findFirst().orElseThrow(), null);

            OracleRecordPrimaryKey primaryKey = new OracleRecordPrimaryKey();
            primaryKey.init(id, record);
            OracleInsertRecordParam insertParam = new OracleInsertRecordParam();
            insertParam.setSchema(schema);
            insertParam.setTableName(tableName);
            insertParam.setRecord(record.getRecordData());
            insertParam.setPrimaryKey(primaryKey);
            assertEquals(1, client.insertRecord(insertParam));
            assertTrue(primaryKey.getReturnData() != null);

            long insertedId = ((Number) primaryKey.getReturnData()).longValue();
            try (Connection connection = open(url, schema, password);
                 PreparedStatement statement = connection.prepareStatement("SELECT \"C_CLOB\", \"C_NCLOB\","
                         + " \"C_BLOB\", \"C_LONG\", XMLSERIALIZE(CONTENT \"C_XML\" AS CLOB), \"C_VIRTUAL\" FROM \""
                         + schema + "\".\""
                         + tableName + "\" WHERE \"ID\" = ?")) {
                statement.setLong(1, insertedId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertEquals(clobText, resultSet.getString(1));
                    assertEquals(nclobText, resultSet.getString(2));
                    assertTrue(java.util.Arrays.equals(blobBytes, resultSet.getBytes(3)));
                    assertEquals(longText, resultSet.getString(4));
                    assertEquals(xmlText, resultSet.getString(5).trim());
                    assertEquals("plain-valu", resultSet.getString(6));
                    assertFalse(resultSet.next());
                }
            }

            OracleRecord emptyXmlRecord = new OracleRecord(columns);
            emptyXmlRecord.putValue(clob, clobText);
            emptyXmlRecord.putValue(longColumn, longText);
            emptyXmlRecord.putValue(xml, "");
            emptyXmlRecord.putValue(plain, "empty-xml");
            OracleRecordPrimaryKey emptyXmlPrimaryKey = new OracleRecordPrimaryKey();
            emptyXmlPrimaryKey.init(id, emptyXmlRecord);
            OracleInsertRecordParam emptyXmlParam = new OracleInsertRecordParam();
            emptyXmlParam.setSchema(schema);
            emptyXmlParam.setTableName(tableName);
            emptyXmlParam.setRecord(emptyXmlRecord.getRecordData());
            emptyXmlParam.setPrimaryKey(emptyXmlPrimaryKey);
            assertEquals(1, client.insertRecord(emptyXmlParam));
            try (Connection connection = open(url, schema, password);
                 PreparedStatement statement = connection.prepareStatement("SELECT XMLSERIALIZE(CONTENT \"C_XML\""
                         + " AS CLOB), \"C_CLOB\" FROM \"" + schema + "\".\"" + tableName + "\" WHERE \"ID\" = ?")) {
                statement.setLong(1, ((Number) emptyXmlPrimaryKey.getReturnData()).longValue());
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertNull(resultSet.getString(1));
                    assertEquals(clobText, resultSet.getString(2));
                    assertFalse(resultSet.next());
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
            dropTemporarySchema(schema);
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

            assertTableDesignRoundTrip(client);
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
            assertExecuteSqlSimpleTerminator(client);
            assertDropIfExistsBlock(client);
            assertDumpAndRunSqlFile(client);
            assertDataTransportWithTerminators(client);
            assertProcedureRenameExecution(client);
            assertFunctionParameterRoundTrip(client);
            assertFunctionParameterCreationRoundTrip(client);
            assertFunctionSecurityRoundTrip(client);
            assertPipelinedMetadataFallback(client);
            assertViewCreateAndAlertExecution(client);
            assertForeignKeyPolicyRoundTrip(client);
            assertMetadataReloadStability(client);

            OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
            columnParam.setSchema("DEMO");
            columnParam.setTableName("USERS");
            List<OracleColumn> columns = client.selectColumns(columnParam);
            assertTrue(columns.stream().anyMatch(column -> "ID".equals(column.getName())));
            assertTrue(client.selectForeignKeys("DEMO", "ORDERS").stream()
                    .anyMatch(foreignKey -> "ORDERS_USER_FK".equals(foreignKey.getName())
                            && "NO ACTION".equals(foreignKey.getUpdatePolicy())
                            && "NO ACTION".equals(foreignKey.getDeletePolicy())));

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
            assertTrue(viewResult.getCount() > 0);
            Object viewTimestamp = ShellOracleDataUtil.valueStandardization(
                    viewResult.getRecords().get(0).getValue("CREATED_AT"));
            assertTrue(viewTimestamp instanceof java.sql.Timestamp);

            var expressionResult = client.executeSingleSql("DEMO", "SELECT ID + 1 AS NEXT_ID FROM USERS");
            assertEquals("DEMO", expressionResult.schema());
            assertFullDumpAndReplay(client);
        } finally {
            client.close();
        }
    }

    private static void assertInvalidServiceRejected() {
        boolean rejected = false;
        try (Connection ignored = open("jdbc:oracle:thin:@tcp://" + host + ":" + port
                + "/EASYSHELL_INVALID_SERVICE", user, password)) {
        } catch (SQLException ex) {
            rejected = ex.getMessage() != null && ex.getMessage().contains("12514");
        }
        assertTrue("invalid Oracle service should be rejected with ORA-12514", rejected);
    }

    private static void assertMetadataReloadStability(ShellOracleClient client) {
        for (int attempt = 0; attempt < 2; attempt++) {
            assertTrue(client.selectTables("DEMO").stream()
                    .anyMatch(table -> "USERS".equals(table.getName())));
            assertTrue(client.selectViews("DEMO").stream()
                    .anyMatch(view -> "V_USERS".equals(view.getName())
                            && !view.getDefinition().isBlank()
                            && !view.getCreateDefinition().isBlank()));

            OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
            columnParam.setSchema("DEMO");
            columnParam.setTableName("USERS");
            assertFalse(client.selectColumns(columnParam).isEmpty());
            assertFalse(client.viewColumns("DEMO", "V_ORDERS").isEmpty());
        }
    }

    private static void assertTableDesignRoundTrip(ShellOracleClient client) throws Exception {
        String tableName = "e2e_table_design";
        OracleTable table = new OracleTable();
        table.setSchema("DEMO");
        table.setName(tableName);

        OracleColumn text = new OracleColumn("a");
        text.setSchema("DEMO");
        text.setTableName(tableName);
        text.setType("VARCHAR");
        text.setNullable(true);

        OracleColumn id = new OracleColumn("id");
        id.setSchema("DEMO");
        id.setTableName(tableName);
        id.setType("INT");
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");

        OracleCreateTableParam createParam = new OracleCreateTableParam();
        createParam.setTable(table);
        createParam.setColumns(new OracleColumns(List.of(text, id)));
        createParam.setTriggers(new DBObjects<>());
        try {
            dropGeneratedAutoIncrementObjects(client, tableName);
            client.createTable(createParam);

            OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
            columnParam.setSchema("DEMO");
            columnParam.setTableName(tableName);
            List<OracleColumn> createdColumns = client.selectColumns(columnParam);
            OracleColumn createdText = createdColumns.stream()
                    .filter(column -> "a".equals(column.getName()))
                    .findFirst()
                    .orElseThrow();
            OracleColumn createdId = createdColumns.stream()
                    .filter(column -> "id".equals(column.getName()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Integer.valueOf(255), createdText.getSize());
            assertTrue(createdId.isPrimaryKey());
            assertTrue(createdId.isAutoIncrement());
            assertEquals(mode.equals("11g") ? "SEQUENCE_TRIGGER" : "IDENTITY",
                    createdId.getAutoIncrementType());

            try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
                statement.executeUpdate("INSERT INTO \"DEMO\".\"" + tableName
                        + "\" (\"a\") VALUES ('smoke')");
            }
            try (Statement statement = client.getConnManager().connection("DEMO").createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT \"id\" FROM \"DEMO\".\"" + tableName
                                 + "\" WHERE \"a\" = 'smoke'")) {
                assertTrue(resultSet.next());
                assertTrue(resultSet.getLong(1) > 0);
                assertFalse(resultSet.next());
            }

            OracleRecord generatedRecord = new OracleRecord(createdColumns);
            generatedRecord.putValue(createdText, "generated-key");
            OracleRecordPrimaryKey generatedPrimaryKey = new OracleRecordPrimaryKey();
            generatedPrimaryKey.init(createdId, generatedRecord);
            OracleInsertRecordParam insertParam = new OracleInsertRecordParam();
            insertParam.setSchema("DEMO");
            insertParam.setTableName(tableName);
            insertParam.setRecord(generatedRecord.getRecordData());
            insertParam.setPrimaryKey(generatedPrimaryKey);
            assertEquals(1, client.insertRecord(insertParam));
            assertTrue(generatedPrimaryKey.getReturnData() != null);

            OracleRecord defaultKeyRecord = new OracleRecord(createdColumns);
            defaultKeyRecord.putValue(createdText, "generated-key-with-default");
            if (createdId.getDefaultValue() != null) {
                defaultKeyRecord.putValue(createdId, createdId.getDefaultValue());
            }
            OracleRecordPrimaryKey defaultKeyPrimaryKey = new OracleRecordPrimaryKey();
            defaultKeyPrimaryKey.init(createdId, defaultKeyRecord);
            OracleInsertRecordParam defaultKeyParam = new OracleInsertRecordParam();
            defaultKeyParam.setSchema("DEMO");
            defaultKeyParam.setTableName(tableName);
            defaultKeyParam.setRecord(defaultKeyRecord.getRecordData());
            defaultKeyParam.setPrimaryKey(defaultKeyPrimaryKey);
            assertEquals(1, client.insertRecord(defaultKeyParam));
            assertTrue(defaultKeyPrimaryKey.getReturnData() != null);

            createdText.setSize(128);
            OracleColumn createdAt = new OracleColumn("created_at");
            createdAt.setSchema("DEMO");
            createdAt.setTableName(tableName);
            createdAt.setType("TIMESTAMP");
            createdAt.setNullable(true);
            createdAt.setCreated(true);

            OracleAlertTableParam alertParam = new OracleAlertTableParam();
            alertParam.setTable(table);
            alertParam.setColumns(new OracleColumns(List.of(createdText, createdId, createdAt)));
            OracleCheck check = new OracleCheck();
            check.setName("T1_CHECK_SMOKE");
            check.setClause("\"a\" IS NOT NULL");
            check.setCreated(true);
            alertParam.setChecks(new DBObjects<>(List.of(check)));
            alertParam.setTriggers(new DBObjects<>());
            client.alertTable(alertParam);

            createdColumns = client.selectColumns(columnParam);
            assertEquals(Integer.valueOf(128), createdColumns.stream()
                    .filter(column -> "a".equals(column.getName()))
                    .findFirst()
                    .orElseThrow()
                    .getSize());
            assertTrue(createdColumns.stream().anyMatch(column ->
                    "created_at".equals(column.getName()) && column.isDateTimeType()));
            assertTrue(client.selectChecks("DEMO", tableName).stream()
                    .anyMatch(item -> "T1_CHECK_SMOKE".equals(item.getName())
                            && "\"a\" IS NOT NULL".equalsIgnoreCase(item.getClause())));
        } finally {
            try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
                try {
                    statement.executeUpdate("ALTER TABLE \"DEMO\".\"" + tableName
                            + "\" DROP CONSTRAINT \"T1_CHECK_SMOKE\"");
                } catch (SQLException ignored) {
                }
            }
            if (client.selectTablesSimple("DEMO").stream()
                    .anyMatch(existing -> tableName.equals(existing.getName()))) {
                client.dropTable("DEMO", tableName);
            }
            dropGeneratedAutoIncrementObjects(client, tableName);
        }
    }

    private static void assertForeignKeyPolicyRoundTrip(ShellOracleClient client) throws Exception {
        String parentName = "E2E_FK_PARENT";
        String childName = "E2E_FK_CHILD";
        String constraintName = "E2E_FK_CHILD_PARENT_FK";

        dropForeignKeyTables(client, parentName, childName);
        try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
            statement.executeUpdate("CREATE TABLE \"DEMO\".\"" + parentName
                    + "\" (\"ID\" NUMBER(10) PRIMARY KEY)");
        }

        OracleTable childTable = new OracleTable();
        childTable.setSchema("DEMO");
        childTable.setName(childName);

        OracleColumn parentId = new OracleColumn("parent_id");
        parentId.setSchema("DEMO");
        parentId.setTableName(childName);
        parentId.setType("NUMBER");
        parentId.setSize(10);
        parentId.setPrimaryKey(true);
        parentId.setNullable(false);

        OracleForeignKey foreignKey = new OracleForeignKey();
        foreignKey.setName(constraintName);
        foreignKey.setColumns(List.of("parent_id"));
        foreignKey.setReferencedDatabase("DEMO");
        foreignKey.setReferencedTable(parentName);
        foreignKey.setReferencedColumns(List.of("ID"));
        foreignKey.setDeletePolicy("RESTRICT");
        foreignKey.setUpdatePolicy("RESTRICT");

        OracleCreateTableParam createParam = new OracleCreateTableParam();
        createParam.setTable(childTable);
        createParam.setColumns(new OracleColumns(List.of(parentId)));
        createParam.setForeignKeys(new DBObjects<>(List.of(foreignKey)));
        OracleCheck childCheck = new OracleCheck();
        childCheck.setName("E2E_FK_CHILD_PARENT_CHECK");
        childCheck.setClause("\"parent_id\" > 0");
        createParam.setChecks(new DBObjects<>(List.of(childCheck)));
        createParam.setIndexes(new DBObjects<>());
        createParam.setTriggers(new DBObjects<>());

        try {
            client.createTable(createParam);
            assertForeignKeyPolicies(client, childName, "NO ACTION", "NO ACTION");

            String cloneName = childName + "_CLONE_TEST";
            try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
                try {
                    statement.executeUpdate("DROP TABLE \"DEMO\".\"" + cloneName + "\"");
                } catch (SQLException ignored) {
                }
            }
            try {
                client.cloneTable("DEMO", childName, cloneName, false);
                assertTrue(client.selectTablesSimple("DEMO").stream()
                        .anyMatch(table -> cloneName.equals(table.getName())));
                List<OracleForeignKey> clonedForeignKeys = client.selectForeignKeys("DEMO", cloneName);
                assertEquals(1, clonedForeignKeys.size());
                assertFalse(constraintName.equals(clonedForeignKeys.get(0).getName()));
                assertEquals(parentName, clonedForeignKeys.get(0).getReferencedTable());
                var clonedChecks = client.selectChecks("DEMO", cloneName);
                assertEquals(1, clonedChecks.size());
                assertFalse(childCheck.getName().equals(clonedChecks.get(0).getName()));
                assertEquals("\"parent_id\" > 0", clonedChecks.get(0).getClause());
            } finally {
                try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
                    try {
                        statement.executeUpdate("DROP TABLE \"DEMO\".\"" + cloneName + "\"");
                    } catch (SQLException ignored) {
                    }
                }
            }

            OracleForeignKey loaded = client.selectForeignKeys("DEMO", childName).stream()
                    .filter(item -> constraintName.equals(item.getName()))
                    .findFirst()
                    .orElseThrow();
            loaded.setDeletePolicy("CASCADE");
            alertForeignKeyPolicy(client, childTable, loaded);
            assertForeignKeyPolicies(client, childName, "CASCADE", "NO ACTION");

            loaded = client.selectForeignKeys("DEMO", childName).stream()
                    .filter(item -> constraintName.equals(item.getName()))
                    .findFirst()
                    .orElseThrow();
            loaded.setDeletePolicy("RESTRICT");
            loaded.setUpdatePolicy("RESTRICT");
            alertForeignKeyPolicy(client, childTable, loaded);
            assertForeignKeyPolicies(client, childName, "NO ACTION", "NO ACTION");
        } finally {
            dropForeignKeyTables(client, parentName, childName);
        }
    }

    private static void alertForeignKeyPolicy(ShellOracleClient client, OracleTable table,
                                              OracleForeignKey foreignKey) throws Exception {
        OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
        columnParam.setSchema(table.getSchema());
        columnParam.setTableName(table.getName());

        OracleAlertTableParam alertParam = new OracleAlertTableParam();
        alertParam.setTable(table);
        alertParam.setColumns(new OracleColumns(client.selectColumns(columnParam)));
        alertParam.setForeignKeys(new DBObjects<>(List.of(foreignKey)));
        alertParam.setChecks(new DBObjects<>());
        alertParam.setIndexes(new DBObjects<>());
        alertParam.setTriggers(new DBObjects<>());
        client.alertTable(alertParam);
    }

    private static void assertForeignKeyPolicies(ShellOracleClient client, String tableName,
                                                 String deletePolicy, String updatePolicy) {
        OracleForeignKey foreignKey = client.selectForeignKeys("DEMO", tableName).stream()
                .filter(item -> "E2E_FK_CHILD_PARENT_FK".equals(item.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(deletePolicy, foreignKey.getDeletePolicy());
        assertEquals(updatePolicy, foreignKey.getUpdatePolicy());
    }

    private static void dropForeignKeyTables(ShellOracleClient client, String parentName,
                                             String childName) throws Exception {
        try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
            try {
                statement.executeUpdate("DROP TABLE \"DEMO\".\"" + childName + "\" CASCADE CONSTRAINTS");
            } catch (SQLException ignored) {
            }
            try {
                statement.executeUpdate("DROP TABLE \"DEMO\".\"" + parentName + "\" CASCADE CONSTRAINTS");
            } catch (SQLException ignored) {
            }
        }
    }

    private static void dropGeneratedAutoIncrementObjects(ShellOracleClient client, String tableName) throws Exception {
        String upperName = tableName.toUpperCase(Locale.ROOT);
        try (Statement statement = client.getConnManager().connection("DEMO").createStatement()) {
            try {
                statement.executeUpdate("DROP TRIGGER \"DEMO\".\"" + upperName + "_ID_TRG\"");
            } catch (SQLException ignored) {
            }
            try {
                statement.executeUpdate("DROP SEQUENCE \"DEMO\".\"" + upperName + "_ID_SEQ\"");
            } catch (SQLException ignored) {
            }
        } catch (Exception ex) {
            throw new AssertionError("Unable to clean generated auto-increment objects", ex);
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

    private static void assertExecuteSqlSimpleTerminator(ShellOracleClient client) {
        String schema = "DEMO";
        String name = "E2E_SQL_TERMINATOR_SEQ";
        try {
            if (client.selectSequencesSimple(schema).stream().anyMatch(sequence -> name.equals(sequence.getName()))) {
                client.executeSqlSimple(schema, "DROP SEQUENCE \"" + name + "\";");
            }
            client.executeSqlSimple(schema, "CREATE SEQUENCE \"" + name + "\";");
            assertTrue(client.selectSequencesSimple(schema).stream()
                    .anyMatch(sequence -> name.equals(sequence.getName())));
        } finally {
            if (client.selectSequencesSimple(schema).stream().anyMatch(sequence -> name.equals(sequence.getName()))) {
                client.executeSqlSimple(schema, "DROP SEQUENCE \"" + name + "\";");
            }
        }
    }

    private static void assertDataTransportWithTerminators(ShellOracleClient client) throws Exception {
        String schema = "DEMO";
        String targetSchema = "SYSTEM";
        String tableName = "E2E_TRANSPORT_TABLE";
        String sequenceName = "E2E_TRANSPORT_SEQ";
        String triggerName = "E2E_TRANSPORT_TRG";
        ShellConnect targetConnect = new ShellConnect();
        targetConnect.setType(ShellPrototype.ORACLE);
        targetConnect.setHost(host + ":" + port);
        targetConnect.setDatabase(mode.equals("11g") ? "XE" : "FREEPDB1");
        targetConnect.setUser(systemUser);
        targetConnect.setPassword(systemPassword);
        targetConnect.setConnectTimeOut(10);
        ShellOracleClient targetClient = new ShellOracleClient(targetConnect);
        try {
            try {
                targetClient.start(10000);
            } catch (Exception ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            if (client.selectTablesSimple(schema).stream().anyMatch(table -> tableName.equals(table.getName()))) {
                client.executeSqlSimple(schema, "DROP TABLE \"" + tableName + "\";");
            }
            if (client.selectSequencesSimple(schema).stream().anyMatch(sequence -> sequenceName.equals(sequence.getName()))) {
                client.executeSqlSimple(schema, "DROP SEQUENCE \"" + sequenceName + "\";");
            }
            if (client.selectTriggersSimple(schema).stream().anyMatch(trigger -> triggerName.equals(trigger.getName()))) {
                client.executeSqlSimple(schema, "DROP TRIGGER \"" + triggerName + "\";");
            }
            if (targetClient.selectTablesSimple(targetSchema).stream().anyMatch(table -> tableName.equals(table.getName()))) {
                targetClient.executeSqlSimple(targetSchema, "DROP TABLE \"" + tableName + "\";");
            }
            if (targetClient.selectSequencesSimple(targetSchema).stream().anyMatch(sequence -> sequenceName.equals(sequence.getName()))) {
                targetClient.executeSqlSimple(targetSchema, "DROP SEQUENCE \"" + sequenceName + "\";");
            }
            if (targetClient.selectTriggersSimple(targetSchema).stream().anyMatch(trigger -> triggerName.equals(trigger.getName()))) {
                targetClient.executeSqlSimple(targetSchema, "DROP TRIGGER \"" + triggerName + "\";");
            }
            assertFalse(targetClient.existObject(targetSchema, tableName, "TABLE"));
            assertFalse(targetClient.existObject(targetSchema, sequenceName, "SEQUENCE"));
            assertFalse(targetClient.existObject(targetSchema, triggerName, "TRIGGER"));
            client.executeSqlSimple(schema, "CREATE TABLE \"" + tableName + "\" (\"ID\" NUMBER PRIMARY KEY);");
            client.executeSqlSimple(schema, "INSERT INTO \"" + tableName + "\" (\"ID\") VALUES (1);");
            client.executeSqlSimple(schema, "CREATE SEQUENCE \"" + sequenceName + "\";");
            client.executeSqlSimple(schema, "CREATE OR REPLACE TRIGGER \"" + triggerName
                    + "\" BEFORE INSERT ON \"" + tableName + "\" FOR EACH ROW WHEN (NEW.\"ID\" IS NULL) BEGIN NULL; END;");

            ShellOracleDataTransportHandler handler = new ShellOracleDataTransportHandler();
            handler.setSourceClient(client);
            handler.setTargetClient(targetClient);
            handler.setSourceDatabase(schema);
            handler.setTargetDatabase(targetSchema);
            handler.setDataType(0);
            DBDataTransportObject table = new DBDataTransportObject();
            table.setName(tableName);
            DBDataTransportObject sequence = new DBDataTransportObject();
            sequence.setName(sequenceName);
            DBDataTransportObject trigger = new DBDataTransportObject();
            trigger.setName(triggerName);
            handler.setTables(List.of(table));
            handler.setSequences(List.of(sequence));
            handler.setTriggers(List.of(trigger));
            List<String> messages = new ArrayList<>();
            handler.setMessageHandler(messages::add);
            handler.doTransport();
            assertTrue(messages.stream().noneMatch(message -> message != null && message.contains("ORA-")));
            assertTrue(client.selectTablesSimple(schema).stream()
                    .anyMatch(item -> tableName.equals(item.getName())));
            assertTrue(client.selectSequencesSimple(schema).stream()
                    .anyMatch(item -> sequenceName.equals(item.getName())));
            assertTrue(targetClient.existObject(targetSchema, tableName, "TABLE"));
            assertTrue(targetClient.existObject(targetSchema, sequenceName, "SEQUENCE"));
            assertTrue(targetClient.existObject(targetSchema, triggerName, "TRIGGER"));
        } finally {
            if (client.selectTablesSimple(schema).stream().anyMatch(table -> tableName.equals(table.getName()))) {
                client.executeSqlSimple(schema, "DROP TABLE \"" + tableName + "\";");
            }
            if (client.selectSequencesSimple(schema).stream().anyMatch(sequence -> sequenceName.equals(sequence.getName()))) {
                client.executeSqlSimple(schema, "DROP SEQUENCE \"" + sequenceName + "\";");
            }
            if (client.selectTriggersSimple(schema).stream().anyMatch(trigger -> triggerName.equals(trigger.getName()))) {
                client.executeSqlSimple(schema, "DROP TRIGGER \"" + triggerName + "\";");
            }
            if (targetClient.selectTablesSimple(targetSchema).stream().anyMatch(table -> tableName.equals(table.getName()))) {
                targetClient.executeSqlSimple(targetSchema, "DROP TABLE \"" + tableName + "\";");
            }
            if (targetClient.selectSequencesSimple(targetSchema).stream().anyMatch(sequence -> sequenceName.equals(sequence.getName()))) {
                targetClient.executeSqlSimple(targetSchema, "DROP SEQUENCE \"" + sequenceName + "\";");
            }
            if (targetClient.selectTriggersSimple(targetSchema).stream().anyMatch(trigger -> triggerName.equals(trigger.getName()))) {
                targetClient.executeSqlSimple(targetSchema, "DROP TRIGGER \"" + triggerName + "\";");
            }
            targetClient.close();
        }
    }

    private static void assertDropIfExistsBlock(ShellOracleClient client) {
        String schema = "DEMO";
        String name = "E2E_DROP_IF_EXISTS_SEQ";
        String functionName = "E2E_COMMENTED_FUNC";
        try {
            client.executeSqlSimple(schema, "-- leading comment\n"
                    + ShellOracleHelper.buildDropIfExistsSql("SEQUENCE", name));
            client.executeSqlSimple(schema, "CREATE SEQUENCE \"" + name + "\"");
            assertTrue(client.existObject(schema, name, "SEQUENCE"));
            client.executeSqlSimple(schema, "/* leading block comment */\n"
                    + ShellOracleHelper.buildDropIfExistsSql("SEQUENCE", name));
            assertFalse(client.existObject(schema, name, "SEQUENCE"));

            String functionSql = """
                    -- leading comment
                    CREATE OR REPLACE FUNCTION "E2E_COMMENTED_FUNC" RETURN NUMBER AS
                    BEGIN
                        RETURN 1;
                    END;
                    """;
            client.executeSqlSimple(schema, functionSql);
            assertTrue(client.existObject(schema, functionName, "FUNCTION"));
        } finally {
            if (client.existObject(schema, name, "SEQUENCE")) {
                client.executeSqlSimple(schema, "DROP SEQUENCE \"" + name + "\";");
            }
            if (client.existObject(schema, functionName, "FUNCTION")) {
                client.executeSqlSimple(schema, "DROP FUNCTION \"" + functionName + "\";");
            }
        }
    }

    private static void assertDumpAndRunSqlFile(ShellOracleClient client) throws Exception {
        String schema = "DEMO";
        String tableName = "E2E_DUMP_RUN_TABLE";
        Path dumpFile = Files.createTempFile("easyshell-oracle-dump-", ".sql");
        try {
            if (client.existObject(schema, tableName, "TABLE")) {
                client.executeSqlSimple(schema, "DROP TABLE \"" + tableName + "\";");
            }
            client.executeSqlSimple(schema, "CREATE TABLE \"" + tableName + "\" ("
                    + "\"ID\" NUMBER(10) NOT NULL, \"NAME\" VARCHAR2(100), PRIMARY KEY (\"ID\"));");
            client.executeSqlSimple(schema, "INSERT INTO \"" + tableName + "\" (\"ID\", \"NAME\") VALUES (1, 'one');");

            ShellOracleDataDumpHandler dumpHandler = new ShellOracleDataDumpHandler(client, schema);
            dumpHandler.dumpFile(dumpFile.toFile());
            dumpHandler.setDumpType((byte) 2);
            dumpHandler.setDataType((byte) 0);
            dumpHandler.setTableName(tableName);
            dumpHandler.setContinueWithErrors(false);
            dumpHandler.doDump();

            client.executeSqlSimple(schema, "DROP TABLE \"" + tableName + "\";");

            List<String> messages = new ArrayList<>();
            ShellOracleDataRunSqlFileHandler runHandler = new ShellOracleDataRunSqlFileHandler(client, schema);
            runHandler.file(dumpFile.toFile());
            runHandler.setContinueWithErrors(false);
            runHandler.setMessageHandler(messages::add);
            runHandler.runFile();

            assertTrue(messages.stream().noneMatch(message -> message != null && message.contains("ORA-")));
            assertTrue(client.existObject(schema, tableName, "TABLE"));
            OracleSelectRecordParam param = new OracleSelectRecordParam();
            param.setSchema(schema);
            param.setTableName(tableName);
            param.setReadonly(true);
            param.setStart(0L);
            param.setLimit(10L);
            assertEquals(1, client.selectRecords(param).size());
        } finally {
            if (client.existObject(schema, tableName, "TABLE")) {
                client.executeSqlSimple(schema, "DROP TABLE \"" + tableName + "\";");
            }
            Files.deleteIfExists(dumpFile);
        }
    }

    private static void assertFullDumpAndReplay(ShellOracleClient client) throws Exception {
        String schema = "DEMO";
        String targetSchema = "E2E_RPL_" + Long.toUnsignedString(System.nanoTime(), 16).toUpperCase(Locale.ROOT);
        String targetPassword = "Replay2026";
        ShellOracleClient targetClient = null;
        Path dumpFile = Files.createTempFile("easyshell-oracle-full-dump-", ".sql");
        try {
            createTemporarySchema(targetSchema, targetPassword);
            targetClient = openShellClient(targetSchema, targetPassword);
            ShellOracleDataDumpHandler dumpHandler = new ShellOracleDataDumpHandler(client, schema);
            dumpHandler.dumpFile(dumpFile.toFile());
            dumpHandler.setDumpType((byte) 1);
            dumpHandler.setDataType((byte) 0);
            dumpHandler.setContinueWithErrors(false);
            dumpHandler.doDump();

            String content = Files.readString(dumpFile);
            assertTrue(content.contains("-- View structure for V_USERS"));
            assertTrue(content.contains("-- Procedure structure for ADD_USER"));
            assertFalse(content.contains("ISEQ$$_"));
            assertTrue(content.contains("TABLESPACE \"USERS\""));
            List<String> dumpStatements = ShellOracleDataRunSqlFileHandler.splitOracleSql(content);
            assertTrue(dumpStatements.stream()
                    .map(ShellOracleHelper::stripLeadingComments)
                    .anyMatch(sql -> sql.matches("(?is)^CREATE\\s+(?:OR\\s+REPLACE\\s+)?"
                            + "(?:(?:FORCE|NOFORCE)\\s+)?(?:(?:NON)?EDITIONABLE\\s+)?VIEW\\b.*")));
            assertTrue(dumpStatements.stream()
                    .map(ShellOracleHelper::stripLeadingComments)
                    .anyMatch(sql -> sql.matches("(?is)^CREATE\\s+(?:OR\\s+REPLACE\\s+)?"
                            + "(?:(?:NON)?EDITIONABLE\\s+)?PROCEDURE\\b.*")));

            List<String> messages = new ArrayList<>();
            ShellOracleDataRunSqlFileHandler runHandler = new ShellOracleDataRunSqlFileHandler(targetClient, targetSchema);
            runHandler.file(dumpFile.toFile());
            runHandler.setContinueWithErrors(false);
            runHandler.setMessageHandler(messages::add);
            runHandler.runFile();

            assertTrue(messages.stream().noneMatch(message -> message != null && message.contains("ORA-")));
            assertTrue(targetClient.selectViews(targetSchema).stream().anyMatch(view -> "V_USERS".equals(view.getName())));
            assertTrue(targetClient.selectProcedures(targetSchema).stream()
                    .anyMatch(procedure -> "ADD_USER".equals(procedure.getName())));
            assertTrue(targetClient.selectForeignKeys(targetSchema, "ORDERS").stream()
                    .anyMatch(foreignKey -> "ORDERS_USER_FK".equals(foreignKey.getName())));
            targetClient.executeSqlSimple(targetSchema, "UPDATE USERS SET EMAIL = EMAIL WHERE ROWNUM = 1");
        } finally {
            if (targetClient != null) {
                targetClient.close();
            }
            dropTemporarySchema(targetSchema);
            Files.deleteIfExists(dumpFile);
        }
    }

    private static void createTemporarySchema(String schema, String password) throws Exception {
        try (Connection connection = open(url, systemUser, systemPassword);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE USER \"" + schema + "\" IDENTIFIED BY \"" + password + "\""
                    + " DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP QUOTA UNLIMITED ON USERS");
            statement.execute("GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE,"
                    + " CREATE PROCEDURE, CREATE TRIGGER TO \"" + schema + "\"");
        }
    }

    private static void dropTemporarySchema(String schema) {
        try (Connection connection = open(url, systemUser, systemPassword);
             Statement statement = connection.createStatement()) {
            statement.execute("DROP USER \"" + schema + "\" CASCADE");
        } catch (Exception ignored) {
        }
    }

    private static ShellOracleClient openShellClient(String schema, String password) throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.ORACLE);
        connect.setHost(host + ":" + port);
        connect.setDatabase(mode.equals("11g") ? "XE" : "FREEPDB1");
        connect.setUser(schema);
        connect.setPassword(password);
        connect.setConnectTimeOut(10);
        ShellOracleClient client = new ShellOracleClient(connect);
        try {
            client.start(10000);
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
        return client;
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
            OracleRoutineParam editedReturnParam = new OracleRoutineParam();
            editedReturnParam.setType("NUMBER");
            function.setReturnParam(editedReturnParam);
            function.setDefinition("BEGIN\n    p_out_value := 1;\n    RETURN 2;\nEND;");
            OracleAlertFunctionParam alertParam = new OracleAlertFunctionParam();
            alertParam.setSchema(schema);
            alertParam.setFunction(function);
            client.alertFunction(alertParam);

            OracleFunction loaded = client.selectFunction(schema, name);
            assertEquals(3, loaded.getParams().size());
            assertEquals("NUMBER", loaded.getReturnType());
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
        function.setDefinition("BEGIN\n    p_out_value := 1;\n    p_inout_value := p_in_value;\n    RETURN 2;\nEND;");
        OracleCreateFunctionParam param = new OracleCreateFunctionParam();
        param.setSchema(schema);
        param.setFunction(function);
        client.createFunction(param);
        try {
            OracleFunction loaded = client.selectFunction(schema, name);
            assertEquals(3, loaded.getParams().size());
            assertEquals("NUMBER", loaded.getReturnType());
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_IN_VALUE".equals(item.getName()) && "IN".equals(item.getMode())));
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_OUT_VALUE".equals(item.getName()) && "OUT".equals(item.getMode())));
            assertTrue(loaded.getParams().stream().anyMatch(item ->
                    "P_INOUT_VALUE".equals(item.getName()) && "IN/OUT".equals(item.getMode())));
            assertTrue(loaded.getCreateDefinition().contains("P_IN_VALUE"));
            assertTrue(loaded.getCreateDefinition().contains("P_OUT_VALUE"));
            assertTrue(loaded.getCreateDefinition().contains("P_INOUT_VALUE"));
            assertFunctionInvocation(client, schema, name);
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
            assertEquals("NUMBER", created.getReturnType());

            function.setSecurityType("DEFINER");
            OracleAlertFunctionParam alertParam = new OracleAlertFunctionParam();
            alertParam.setSchema(schema);
            alertParam.setFunction(function);
            client.alertFunction(alertParam);

            OracleFunction altered = client.selectFunction(schema, name);
            assertEquals("DEFINER", altered.getSecurityType());
            assertEquals(2, altered.getParams().size());
            assertEquals("NUMBER", altered.getReturnType());
            assertTrue(altered.getParams().stream().anyMatch(item ->
                    "P_IN_VALUE".equals(item.getName()) && "IN".equals(item.getMode())));
            assertTrue(altered.getParams().stream().anyMatch(item ->
                    "P_OUT_VALUE".equals(item.getName()) && "OUT".equals(item.getMode())));
        } finally {
            dropFunctionIfPresent(client, schema, name);
        }
    }

    private static void assertFunctionInvocation(ShellOracleClient client, String schema, String functionName) {
        String sql = "{? = call " + functionName + "(?, ?, ?)}";
        try (CallableStatement statement = client.getConnManager().connection(schema).prepareCall(sql)) {
            statement.registerOutParameter(1, Types.NUMERIC);
            statement.setString(2, "input");
            statement.registerOutParameter(3, Types.NUMERIC);
            statement.registerOutParameter(4, Types.VARCHAR);
            statement.execute();
            assertEquals(BigInteger.valueOf(2), statement.getBigDecimal(1).toBigInteger());
            assertEquals(BigInteger.ONE, statement.getBigDecimal(3).toBigInteger());
            assertEquals("input", statement.getString(4));
        } catch (Exception ex) {
            throw new AssertionError("Oracle function invocation failed", ex);
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
            assertFalse(sequences.isEmpty());
            assertTrue(sequences.stream().allMatch(sequence -> sequence.getMaxVal() != null));
            if (mode.equals("11g")) {
                assertTrue(sequences.stream().anyMatch(sequence ->
                        sequence.getMaxVal().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0));
            }

            String sequenceName = "EASYSHELL_SYSTEM_BIG_SEQ";
            if (sequences.stream().anyMatch(sequence -> sequenceName.equals(sequence.getName()))) {
                OracleSequence existing = new OracleSequence(sequenceName);
                existing.setSchema("SYSTEM");
                client.dropSequence(existing);
            }
            OracleSequence extreme = new OracleSequence(sequenceName);
            extreme.setSchema("SYSTEM");
            extreme.setMinVal(BigInteger.ONE);
            extreme.setMaxVal(new BigInteger("9999999999999999999999999999"));
            extreme.setCacheSize(BigInteger.valueOf(20));
            extreme.setIncrementBy(BigInteger.ONE);
            extreme.setCycle(false);
            extreme.setOrder(false);
            OracleCreateSequenceParam createParam = new OracleCreateSequenceParam();
            createParam.setSchema("SYSTEM");
            createParam.setSequence(extreme);
            client.createSequence(createParam);
            try {
                OracleSequence loaded = client.selectSequence("SYSTEM", sequenceName);
                assertEquals(new BigInteger("9999999999999999999999999999"), loaded.getMaxVal());
                assertEquals(BigInteger.valueOf(20), loaded.getCacheSize());
                assertEquals(BigInteger.ONE, loaded.getIncrementBy());
            } finally {
                client.dropSequence(extreme);
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
