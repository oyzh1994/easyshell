package cn.oyzh.easyshell.mariadb;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.record.MariadbInsertRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.table.MariadbAlertTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbCreateTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB 生成列设计、DDL 执行和元数据回读集成测试。
 */
public class MariadbGeneratedColumnIntegrationTest {

    private static final String HOST = property("mariadb.host", "MARIADB_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("mariadb.port", "MARIADB_PORT", "33306"));
    private static final String USER = property("mariadb.user", "MARIADB_USER", "root");
    private static final String PASSWORD = property("mariadb.password", "MARIADB_PASSWORD", "123456");
    private static final String DATABASE = "easyshell_mariadb_generated_it";

    private static ShellMariadbClient client;

    @BeforeClass
    public static void setUp() throws Throwable {
        Assume.assumeTrue("MariaDB endpoint is not reachable", isReachable(HOST, PORT));
        ShellMariadbColumnUtil.init();
        client = openClient();
        dropDatabase();
        MariadbDatabase database = new MariadbDatabase();
        database.setName(DATABASE);
        database.setCharset("utf8mb4");
        client.createDatabase(database);
    }

    @AfterClass
    public static void tearDown() {
        try {
            if (client != null) {
                dropDatabase();
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void createsGeneratedColumnsAndRoundTripsExpressionThroughClient() {
        String tableName = "create_generated";
        MariadbColumn id = column("id", "BIGINT");
        id.setNullable(false);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        MariadbColumn source = column("source", "VARCHAR");
        source.setSize(200);
        MariadbColumn virtual = column("virtual_value", "VARCHAR");
        virtual.setSize(500);
        virtual.setGeneratedExpression(
                "CONCAT(`source`, ' quoted''value ', 'C:\\\\tmp')");
        virtual.setGeneratedStorage("VIRTUAL");
        virtual.normalizeGeneratedAttributes();
        MariadbColumn stored = column("stored_value", "INT");
        stored.setGeneratedExpression("CHAR_LENGTH(`source`)");
        stored.setGeneratedStorage("STORED");
        stored.normalizeGeneratedAttributes();

        MariadbTable table = table(tableName);
        MariadbCreateTableParam param = new MariadbCreateTableParam();
        param.setTable(table);
        param.setColumns(new MariadbColumns(List.of(id, source, virtual, stored)));
        String sql = MariadbTableCreateSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains("GENERATED ALWAYS AS"));
        client.createTable(param);

        MariadbColumn selectedVirtual = selectedColumn(tableName, "virtual_value");
        MariadbColumn selectedStored = selectedColumn(tableName, "stored_value");
        assertTrue(selectedVirtual.isGenerated());
        assertTrue(selectedVirtual.isVirtual());
        assertTrue(selectedVirtual.getGeneratedExpression(), selectedVirtual.getGeneratedExpression()
                .contains("quoted"));
        assertTrue(selectedStored.isGenerated());
        assertFalse(selectedStored.isVirtual());

        insertAndAssertGeneratedValues(tableName);

        // 使用从 SHOW CREATE TABLE 提取的表达式切换存储方式，验证表达式不是仅用于显示。
        selectedVirtual.setGeneratedStorage("STORED");
        selectedVirtual.setChanged(true);
        alterColumn(tableName, selectedVirtual);
        MariadbColumn rebuilt = selectedColumn(tableName, "virtual_value");
        assertTrue(rebuilt.isGenerated());
        assertFalse(rebuilt.isVirtual());
        assertGeneratedValues(tableName);
    }

    @Test
    public void rebuildsColumnForGeneratedKindAndStorageChanges() {
        String tableName = "alter_generated";
        client.executeSqlSimple(DATABASE, """
                CREATE TABLE `alter_generated` (
                  `id` BIGINT NOT NULL,
                  `source` VARCHAR(200),
                  `calculated` INT DEFAULT NULL,
                  PRIMARY KEY (`id`)
                )
                """);

        MariadbColumn calculated = selectedColumn(tableName, "calculated");
        calculated.setGenerated(true);
        calculated.normalizeGeneratedAttributes();
        calculated.setGeneratedExpression("CHAR_LENGTH(`source`)");
        calculated.setGeneratedStorage("VIRTUAL");
        calculated.setChanged(true);
        alterColumn(tableName, calculated);

        MariadbColumn virtual = selectedColumn(tableName, "calculated");
        assertTrue(virtual.isGenerated());
        assertTrue(virtual.isVirtual());
        assertTrue(virtual.getGeneratedExpression().contains("char_length"));

        virtual.setGeneratedStorage("STORED");
        virtual.setChanged(true);
        alterColumn(tableName, virtual);

        MariadbColumn stored = selectedColumn(tableName, "calculated");
        assertTrue(stored.isGenerated());
        assertFalse(stored.isVirtual());

        stored.setGenerated(false);
        stored.normalizeGeneratedAttributes();
        stored.setChanged(true);
        alterColumn(tableName, stored);

        MariadbColumn normal = selectedColumn(tableName, "calculated");
        assertFalse(normal.isGenerated());
        assertNull(normal.getGeneratedExpression());
        assertNull(normal.getGeneratedStorage());
        assertEquals("calculated", selectColumns(tableName).getLast().getName());
    }

    private static void insertAndAssertGeneratedValues(String tableName) {
        List<MariadbColumn> columns = selectColumns(tableName);
        MariadbRecord record = new MariadbRecord(columns);
        record.putValue("source", "alpha");
        MariadbRecordPrimaryKey primaryKey = new MariadbRecordPrimaryKey();
        primaryKey.init(new MariadbColumns(columns).column("id"), record);
        MariadbInsertRecordParam param = new MariadbInsertRecordParam();
        param.setDbName(DATABASE);
        param.setTableName(tableName);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
        assertGeneratedValues(tableName);
    }

    private static void assertGeneratedValues(String tableName) {
        MariadbSelectRecordParam param = new MariadbSelectRecordParam();
        param.setDbName(DATABASE);
        param.setTableName(tableName);
        param.setStart(0L);
        param.setLimit(10L);
        List<MariadbRecord> records = client.selectRecords(param);
        assertEquals(1, records.size());
        String virtual = String.valueOf(records.getFirst().getValue("virtual_value"));
        assertTrue(virtual, virtual.contains("alpha"));
        assertTrue(virtual, virtual.contains("quoted'value"));
        Number stored = (Number) records.getFirst().getValue("stored_value");
        assertEquals("alpha".length(), stored.intValue());
    }

    private static void alterColumn(String tableName, MariadbColumn column) {
        List<MariadbColumn> columns = selectColumns(tableName);
        columns.replaceAll(candidate -> column.getName().equals(candidate.getName())
                ? column : candidate);
        MariadbAlertTableParam param = new MariadbAlertTableParam();
        param.setTable(table(tableName));
        param.setColumns(new MariadbColumns(columns));
        client.alertTable(param);
    }

    private static MariadbColumn selectedColumn(String tableName, String columnName) {
        return selectColumns(tableName).stream()
                .filter(column -> columnName.equals(column.getName()))
                .findFirst()
                .orElseThrow();
    }

    private static List<MariadbColumn> selectColumns(String tableName) {
        return client.selectColumns(new MariadbSelectColumnParam(DATABASE, tableName));
    }

    private static MariadbTable table(String tableName) {
        MariadbTable table = new MariadbTable();
        table.setDbName(DATABASE);
        table.setName(tableName);
        return table;
    }

    private static MariadbColumn column(String name, String type) {
        MariadbColumn column = new MariadbColumn(name);
        column.setType(type);
        column.setNullable(true);
        return column;
    }

    private static ShellMariadbClient openClient() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.MARIADB);
        connect.setHost(HOST + ":" + PORT);
        connect.setUser(USER);
        connect.setPassword(PASSWORD);
        connect.setSSLMode(false);
        connect.setConnectTimeOut(5);
        ShellMariadbClient client = new ShellMariadbClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
        return client;
    }

    private static void dropDatabase() {
        client.executeSqlSimple(null, "DROP DATABASE IF EXISTS `" + DATABASE + "`");
    }

    private static String property(String property, String environment, String defaultValue) {
        return System.getProperty(property,
                System.getenv().getOrDefault(environment, defaultValue));
    }

    private static boolean isReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
