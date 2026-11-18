package cn.oyzh.easyshell.test.oracle;

import cn.oyzh.common.db.SqlDatabase;
import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.ShellOracleConnManager;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.sql.DBOraclePagination;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

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
            assertCrud(connection);
            assertPlSql(connection);
            assertPagination(connection, version);
            assertEasyShellClientMetadata();
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

            assertTrue(client.selectTables("DEMO").stream().anyMatch(table -> "USERS".equals(table.getName())
                    && !table.getCreateDefinition().isBlank()));
            assertTrue(client.selectViews("DEMO").stream().anyMatch(view -> "V_USERS".equals(view.getName())
                    && !view.getCreateDefinition().isBlank()));
            assertTrue(client.selectProcedures("DEMO").stream().anyMatch(procedure -> "ADD_USER".equals(procedure.getName())
                    && !procedure.getCreateDefinition().isBlank()));
            assertTrue(client.selectFunctions("DEMO").stream().anyMatch(function -> "USER_COUNT".equals(function.getName())
                    && !function.getCreateDefinition().isBlank()));
            assertTrue(client.selectTriggers("DEMO").stream().anyMatch(trigger -> "USERS_UPDATED_AT_TRG".equals(trigger.getName())
                    && !trigger.getCreateDefinition().isBlank()));

            OracleSequence sequence = client.selectSequence("DEMO", "DEMO_SEQ");
            assertEquals("DEMO_SEQ", sequence.getName());
            assertEquals(Long.valueOf(1), sequence.getMinVal());
            assertEquals(Long.valueOf(5), sequence.getIncrementBy());
            assertNull(sequence.getMaxVal());
            assertFalse(sequence.getCreateDefinition().isBlank());

            OracleSelectColumnParam columnParam = new OracleSelectColumnParam();
            columnParam.setSchema("DEMO");
            columnParam.setTableName("USERS");
            List<OracleColumn> columns = client.selectColumns(columnParam);
            assertTrue(columns.stream().anyMatch(column -> "ID".equals(column.getName())));
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
