package cn.oyzh.easyshell.mysql;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.mysql.generator.table.MysqlTableCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.mysql.table.MysqlAlertTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlCreateTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
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
 * MySQL 生成列设计、DDL 执行和元数据回读集成测试。
 */
public class MysqlGeneratedColumnIntegrationTest {

    private static final String HOST = property("mysql.host", "MYSQL_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("mysql.port", "MYSQL_PORT", "13306"));
    private static final String USER = property("mysql.user", "MYSQL_USER", "root");
    private static final String PASSWORD = property("mysql.password", "MYSQL_PASSWORD", "123456");
    private static final String DATABASE = "easyshell_mysql_generated_it";

    private static ShellMysqlClient client;

    @BeforeClass
    public static void setUp() throws Throwable {
        Assume.assumeTrue("MySQL endpoint is not reachable", isReachable(HOST, PORT));
        ShellMysqlColumnUtil.init();
        client = openClient();
        dropDatabase();
        MysqlDatabase database = new MysqlDatabase();
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
        MysqlColumn id = column("id", "BIGINT");
        id.setNullable(false);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        MysqlColumn source = column("source", "VARCHAR");
        source.setSize(200);
        MysqlColumn virtual = column("virtual_value", "VARCHAR");
        virtual.setSize(500);
        virtual.setGeneratedExpression(
                "CONCAT(`source`, ' quoted''value ', 'C:\\\\tmp')");
        virtual.setGeneratedStorage("VIRTUAL");
        virtual.normalizeGeneratedAttributes();
        MysqlColumn stored = column("stored_value", "INT");
        stored.setGeneratedExpression("CHAR_LENGTH(`source`)");
        stored.setGeneratedStorage("STORED");
        stored.normalizeGeneratedAttributes();

        MysqlTable table = table(tableName);
        MysqlCreateTableParam param = new MysqlCreateTableParam();
        param.setTable(table);
        param.setColumns(new MysqlColumns(List.of(id, source, virtual, stored)));
        String sql = MysqlTableCreateSqlGenerator.generateSqlSingle(param);
        assertTrue(sql, sql.contains("GENERATED ALWAYS AS"));
        client.createTable(param);

        MysqlColumn selectedVirtual = selectedColumn(tableName, "virtual_value");
        MysqlColumn selectedStored = selectedColumn(tableName, "stored_value");
        assertTrue(selectedVirtual.isGenerated());
        assertTrue(selectedVirtual.isVirtual());
        assertTrue(selectedVirtual.getGeneratedExpression(), selectedVirtual.getGeneratedExpression()
                .contains("quoted"));
        assertTrue(selectedStored.isGenerated());
        assertFalse(selectedStored.isVirtual());

        insertAndAssertGeneratedValues(tableName);

        // 使用从 INFORMATION_SCHEMA 还原出的表达式切换存储方式，验证表达式不是仅用于显示。
        selectedVirtual.setGeneratedStorage("STORED");
        selectedVirtual.setChanged(true);
        alterColumn(tableName, selectedVirtual);
        MysqlColumn rebuilt = selectedColumn(tableName, "virtual_value");
        assertTrue(rebuilt.isGenerated());
        assertFalse(rebuilt.isVirtual());
        assertGeneratedValues(tableName);
    }

    @Test
    public void viewUsesBaseGeneratedColumnMetadata() {
        String tableName = "view_generated_source";
        String viewName = "view_generated_result";
        client.executeSqlSimple(DATABASE, "DROP VIEW IF EXISTS `" + viewName + "`");
        client.executeSqlSimple(DATABASE, "DROP TABLE IF EXISTS `" + tableName + "`");
        client.executeSqlSimple(DATABASE, """
                CREATE TABLE `view_generated_source` (
                  `id` INT PRIMARY KEY,
                  `source` VARCHAR(100),
                  `calculated` INT GENERATED ALWAYS AS (CHAR_LENGTH(`source`)) VIRTUAL
                )
                """);
        client.executeSqlSimple(DATABASE, "CREATE VIEW `" + viewName
                + "` AS SELECT * FROM `" + tableName + "`");
        client.executeSqlSimple(DATABASE, "INSERT INTO `" + tableName
                + "`(`id`, `source`) VALUES(1, 'view-value')");

        List<MysqlColumn> viewColumns = client.viewColumns(DATABASE, viewName);
        MysqlColumn calculated = viewColumns.stream()
                .filter(column -> "calculated".equals(column.getName()))
                .findFirst().orElseThrow();
        assertTrue(calculated.isGenerated());

        MysqlRecord record = client.viewRecords(DATABASE, viewName, 0L, 10L, null, viewColumns).getFirst();
        assertTrue(record.getColumns().column("calculated").isGenerated());
        assertTrue(record.getProperty("calculated").isReadonly());
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

        MysqlColumn calculated = selectedColumn(tableName, "calculated");
        calculated.setGenerated(true);
        calculated.normalizeGeneratedAttributes();
        calculated.setGeneratedExpression("CHAR_LENGTH(`source`)");
        calculated.setGeneratedStorage("VIRTUAL");
        calculated.setChanged(true);
        alterColumn(tableName, calculated);

        MysqlColumn virtual = selectedColumn(tableName, "calculated");
        assertTrue(virtual.isGenerated());
        assertTrue(virtual.isVirtual());
        assertTrue(virtual.getGeneratedExpression().contains("char_length"));

        virtual.setGeneratedStorage("STORED");
        virtual.setChanged(true);
        alterColumn(tableName, virtual);

        MysqlColumn stored = selectedColumn(tableName, "calculated");
        assertTrue(stored.isGenerated());
        assertFalse(stored.isVirtual());

        stored.setGenerated(false);
        stored.normalizeGeneratedAttributes();
        stored.setChanged(true);
        alterColumn(tableName, stored);

        MysqlColumn normal = selectedColumn(tableName, "calculated");
        assertFalse(normal.isGenerated());
        assertNull(normal.getGeneratedExpression());
        assertNull(normal.getGeneratedStorage());
        assertEquals("calculated", selectColumns(tableName).getLast().getName());
    }

    private static void insertAndAssertGeneratedValues(String tableName) {
        List<MysqlColumn> columns = selectColumns(tableName);
        MysqlRecord record = new MysqlRecord(columns);
        record.putValue("source", "alpha");
        MysqlRecordPrimaryKey primaryKey = new MysqlRecordPrimaryKey();
        primaryKey.init(new MysqlColumns(columns).column("id"), record);
        MysqlInsertRecordParam param = new MysqlInsertRecordParam();
        param.setDbName(DATABASE);
        param.setTableName(tableName);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
        assertGeneratedValues(tableName);
    }

    private static void assertGeneratedValues(String tableName) {
        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
        param.setDbName(DATABASE);
        param.setTableName(tableName);
        param.setStart(0L);
        param.setLimit(10L);
        List<MysqlRecord> records = client.selectRecords(param);
        assertEquals(1, records.size());
        String virtual = String.valueOf(records.getFirst().getValue("virtual_value"));
        assertTrue(virtual, virtual.contains("alpha"));
        assertTrue(virtual, virtual.contains("quoted'value"));
        Number stored = (Number) records.getFirst().getValue("stored_value");
        assertEquals("alpha".length(), stored.intValue());
    }

    private static void alterColumn(String tableName, MysqlColumn column) {
        List<MysqlColumn> columns = selectColumns(tableName);
        columns.replaceAll(candidate -> column.getName().equals(candidate.getName())
                ? column : candidate);
        MysqlAlertTableParam param = new MysqlAlertTableParam();
        param.setTable(table(tableName));
        param.setColumns(new MysqlColumns(columns));
        client.alertTable(param);
    }

    private static MysqlColumn selectedColumn(String tableName, String columnName) {
        return selectColumns(tableName).stream()
                .filter(column -> columnName.equals(column.getName()))
                .findFirst()
                .orElseThrow();
    }

    private static List<MysqlColumn> selectColumns(String tableName) {
        return client.selectColumns(new MysqlSelectColumnParam(DATABASE, tableName));
    }

    private static MysqlTable table(String tableName) {
        MysqlTable table = new MysqlTable();
        table.setDbName(DATABASE);
        table.setName(tableName);
        return table;
    }

    private static MysqlColumn column(String name, String type) {
        MysqlColumn column = new MysqlColumn(name);
        column.setType(type);
        column.setNullable(true);
        return column;
    }

    private static ShellMysqlClient openClient() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.MYSQL);
        connect.setHost(HOST + ":" + PORT);
        connect.setUser(USER);
        connect.setPassword(PASSWORD);
        connect.setSSLMode(false);
        connect.setConnectTimeOut(5);
        ShellMysqlClient client = new ShellMysqlClient(connect);
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
