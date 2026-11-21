package cn.oyzh.easyshell.mysql;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 生成供人工执行 MySQL 导入、导出和传输测试的持久数据。
 *
 * <p>默认不执行，使用 {@code -Dmysql.complex.fixture=true} 生成。
 * 该测试不会删除生成后的数据库和表。</p>
 */
public class MysqlComplexDataFixtureTest {

    static final String SOURCE_DB = "demo_mysql_complex";
    static final String TARGET_DB = "demo_mysql_complex_target";
    static final String TABLE = "complex_types";
    static final String IMPORT_TABLE = "complex_types_import";

    private static final String HOST = property("mysql.host", "MYSQL_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("mysql.port", "MYSQL_PORT", "13306"));
    private static final String USER = property("mysql.user", "MYSQL_USER", "root");
    private static final String PASSWORD = property("mysql.password", "MYSQL_PASSWORD", "123456");

    static final String COMPLEX_TABLE_DDL = """
            CREATE TABLE `complex_types` (
              `id` BIGINT NOT NULL AUTO_INCREMENT,
              `c_tiny` TINYINT,
              `c_utiny` TINYINT UNSIGNED,
              `c_small` SMALLINT,
              `c_medium` MEDIUMINT,
              `c_int` INT,
              `c_uint` INT UNSIGNED,
              `c_big` BIGINT,
              `c_ubig` BIGINT UNSIGNED,
              `c_decimal` DECIMAL(30,10),
              `c_float` FLOAT,
              `c_double` DOUBLE,
              `c_bit` BIT(13),
              `c_bool` BOOLEAN,
              `c_date` DATE,
              `c_time` TIME(6),
              `c_datetime` DATETIME(6),
              `c_timestamp` TIMESTAMP(6) NULL,
              `c_year` YEAR,
              `c_char` CHAR(8),
              `c_varchar` VARCHAR(100),
              `c_tinytext` TINYTEXT,
              `c_text` TEXT,
              `c_mediumtext` MEDIUMTEXT,
              `c_longtext` LONGTEXT,
              `c_binary` BINARY(8),
              `c_varbinary` VARBINARY(100),
              `c_tinyblob` TINYBLOB,
              `c_blob` BLOB,
              `c_mediumblob` MEDIUMBLOB,
              `c_longblob` LONGBLOB,
              `c_enum` ENUM('alpha','beta','gamma'),
              `c_set` SET('red','green','blue'),
              `c_json` JSON,
              `c_point` POINT,
              `c_linestring` LINESTRING,
              `c_polygon` POLYGON,
              `c_multipoint` MULTIPOINT,
              `c_multilinestring` MULTILINESTRING,
              `c_multipolygon` MULTIPOLYGON,
              `c_geometry` GEOMETRY,
              `c_geomcollection` GEOMETRYCOLLECTION,
              `c_virtual` VARCHAR(160) GENERATED ALWAYS AS (CONCAT(`c_varchar`, '-virtual')) VIRTUAL,
              `c_stored` VARCHAR(160) GENERATED ALWAYS AS (CONCAT(`c_varchar`, '-stored')) STORED,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """;

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("Set -Dmysql.complex.fixture=true to create persistent fixture data",
                Boolean.getBoolean("mysql.complex.fixture"));
        Assume.assumeTrue("MySQL endpoint is not reachable", isReachable(HOST, PORT));
        ShellMysqlColumnUtil.init();
    }

    @Test
    public void createPersistentComplexDataFixture() throws Throwable {
        ShellMysqlClient client = openClient();
        try {
            recreateDatabase(client, SOURCE_DB);
            recreateDatabase(client, TARGET_DB);
            client.executeSqlSimple(SOURCE_DB, COMPLEX_TABLE_DDL);
            client.executeSqlSimple(SOURCE_DB, "CREATE TABLE `" + IMPORT_TABLE
                    + "` LIKE `" + TABLE + "`");
            client.executeSqlSimple(TARGET_DB, "CREATE TABLE `" + TABLE
                    + "` LIKE `" + SOURCE_DB + "`.`" + TABLE + "`");

            List<MysqlColumn> columns = client.selectColumns(new MysqlSelectColumnParam(SOURCE_DB, TABLE));
            MysqlColumns mysqlColumns = new MysqlColumns(columns);
            for (int i = 1; i <= 10; i++) {
                insertRow(client, mysqlColumns, i);
            }

            MysqlSelectRecordParam selectParam = new MysqlSelectRecordParam();
            selectParam.setDbName(SOURCE_DB);
            selectParam.setTableName(TABLE);
            selectParam.setColumns(columns);
            List<MysqlRecord> records = client.selectRecords(selectParam);
            assertEquals(10, records.size());
            for (MysqlRecord record : records) {
                assertNotNull(record.getValue("id"));
                assertTrue(String.valueOf(record.getValue("c_virtual"))
                        .endsWith("-virtual"));
                assertTrue(String.valueOf(record.getValue("c_stored"))
                        .endsWith("-stored"));
            }
            assertEquals(0, selectRecords(client, SOURCE_DB, IMPORT_TABLE).size());
            assertEquals(0, selectRecords(client, TARGET_DB, TABLE).size());
        } finally {
            client.close();
        }
    }

    private static List<MysqlRecord> selectRecords(ShellMysqlClient client,
                                                   String database, String table) {
        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
        param.setDbName(database);
        param.setTableName(table);
        param.setColumns(client.selectColumns(new MysqlSelectColumnParam(database, table)));
        return client.selectRecords(param);
    }

    private static void insertRow(ShellMysqlClient client, MysqlColumns columns, int index) {
        MysqlRecord record = new MysqlRecord(columns);
        rowValues(index).forEach(record::putValue);

        MysqlRecordPrimaryKey primaryKey = new MysqlRecordPrimaryKey();
        primaryKey.init(columns.column("id"), record);
        MysqlInsertRecordParam param = new MysqlInsertRecordParam();
        param.setDbName(SOURCE_DB);
        param.setTableName(TABLE);
        param.setRecord(record.getRecordData());
        param.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(param));
        assertNotNull(primaryKey.getReturnData());
    }

    private static Map<String, Object> rowValues(int index) {
        String[] enumValues = {"alpha", "beta", "gamma"};
        String[] setValues = {"red", "red,blue", "green,blue", "red,green,blue"};
        int x = index * 2;
        int y = index * 3;

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_tiny", (byte) (index - 6));
        values.put("c_utiny", (short) (240 + index));
        values.put("c_small", (short) (-3000 + index * 17));
        values.put("c_medium", 100000 + index * 101);
        values.put("c_int", -900000 + index * 7001);
        values.put("c_uint", 4000000000L + index);
        values.put("c_big", -9000000000L + index * 1001L);
        values.put("c_ubig", new BigInteger("18446744073709551615")
                .subtract(BigInteger.valueOf(index - 1L)));
        values.put("c_decimal", new BigDecimal("12345678901234567890.1234567890")
                .add(BigDecimal.valueOf(index, 4)));
        values.put("c_float", 100.25F + index);
        values.put("c_double", 90000.1234D + index * 3.25D);
        values.put("c_bit", new byte[]{(byte) index, (byte) (index * 7)});
        values.put("c_bool", (index & 1) == 0);
        values.put("c_date", LocalDate.of(2026, 1, index));
        values.put("c_time", LocalTime.of(8 + index, index * 2 % 60, index % 60));
        values.put("c_datetime", LocalDateTime.of(2026, 3, index, 10, index, index));
        values.put("c_timestamp", LocalDateTime.of(2026, 4, index, 11, index, index));
        values.put("c_year", 2020 + index);
        values.put("c_char", "row" + index);
        values.put("c_varchar", "row" + index + "-中文-😀");
        values.put("c_tinytext", "tiny-" + index + "-中文");
        values.put("c_text", "row-" + index + " 'quote' \\ slash");
        values.put("c_mediumtext", "row-" + index + "\nsecond line\t😀");
        values.put("c_longtext", "row-" + index + "-" + "L".repeat(1200));
        values.put("c_binary", new byte[]{0, 1, (byte) index, 3, 4, 5, 6, 7});
        values.put("c_varbinary", new byte[]{0, 1, (byte) index, (byte) 0xFE, (byte) 0xFF});
        values.put("c_tinyblob", ("tiny-blob-" + index).getBytes());
        values.put("c_blob", new byte[]{(byte) index, 0x20, 0x30, 0x40});
        values.put("c_mediumblob", ("medium-blob-" + index).getBytes());
        values.put("c_longblob", ("long-blob-" + index + "-" + "B".repeat(800)).getBytes());
        values.put("c_enum", enumValues[(index - 1) % enumValues.length]);
        values.put("c_set", setValues[(index - 1) % setValues.length]);
        values.put("c_json", "{\"id\":" + index
                + ",\"name\":\"row" + index + " 中文 😀\""
                + ",\"nested\":{\"quoted\":\"a\\\"b\",\"slash\":\"C:\\\\tmp\""
                + ",\"items\":[" + index + ",\"x\"]}}");
        values.put("c_point", "POINT(" + x + " " + y + ")");
        values.put("c_linestring", "LINESTRING(0 0," + x + " 1," + (x + 1) + " 3)");
        values.put("c_polygon", "POLYGON((0 0," + x + " 0," + x + " " + y
                + ",0 " + y + ",0 0))");
        values.put("c_multipoint", "MULTIPOINT((0 0),(" + x + " " + y + "))");
        values.put("c_multilinestring", "MULTILINESTRING((0 0,1 1),(" + x + " " + y
                + "," + (x + 1) + " " + (y + 1) + "))");
        values.put("c_multipolygon", "MULTIPOLYGON(((0 0,2 0,2 2,0 2,0 0)),(("
                + x + " " + y + "," + (x + 2) + " " + y + ","
                + (x + 2) + " " + (y + 2) + "," + x + " " + (y + 2)
                + "," + x + " " + y + ")))");
        values.put("c_geometry", "POINT(" + (x + 10) + " " + (y + 10) + ")");
        values.put("c_geomcollection", "GEOMETRYCOLLECTION(POINT(" + x + " " + y
                + "),LINESTRING(0 0,1 1," + x + " " + y + "))");
        return values;
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

    private static void recreateDatabase(ShellMysqlClient client, String database) {
        client.executeSqlSimple(null, "DROP DATABASE IF EXISTS `" + database + "`");
        MysqlDatabase mysqlDatabase = new MysqlDatabase();
        mysqlDatabase.setName(database);
        mysqlDatabase.setCharset("utf8mb4");
        client.createDatabase(mysqlDatabase);
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
