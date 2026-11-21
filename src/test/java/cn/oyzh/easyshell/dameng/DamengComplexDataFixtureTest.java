package cn.oyzh.easyshell.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 生成供人工执行达梦复杂字段导入、导出和传输测试的持久数据。
 */
public class DamengComplexDataFixtureTest {

    static final String SOURCE_SCHEMA = "DEMO_DAMENG_COMPLEX";
    static final String TARGET_SCHEMA = "DEMO_DAMENG_COMPLEX_TARGET";
    static final String TABLE = "COMPLEX_TYPES";
    static final String IMPORT_TABLE = "COMPLEX_TYPES_IMPORT";
    static final String UDT_TABLE = "UDT_TYPES";

    static final String UDT_TABLE_DDL = """
            CREATE TABLE "UDT_TYPES" (
              "ID" BIGINT IDENTITY(1, 1) PRIMARY KEY,
              "ADDRESS_VALUE" "ADDRESS_T",
              "NUMBER_VALUES" "INT_ARRAY"
            )
            """;

    private static final String HOST = property("dameng.host", "DAMENG_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("dameng.port", "DAMENG_PORT", "15237"));
    private static final String USER = property("dameng.user", "DAMENG_USER", "SYSDBA");
    private static final String PASSWORD = property("dameng.password", "DAMENG_PASSWORD", "SYSDBA");

    static final String TABLE_DDL = """
            CREATE TABLE "COMPLEX_TYPES" (
              "ID" BIGINT IDENTITY(1, 1) PRIMARY KEY,
              "C_TINY" TINYINT,
              "C_SMALL" SMALLINT,
              "C_INT" INT,
              "C_BIG" BIGINT,
              "C_DECIMAL" DECIMAL(30,10),
              "C_FLOAT" FLOAT,
              "C_DOUBLE" DOUBLE,
              "C_BIT" BIT,
              "C_CHAR" CHAR(8),
              "C_VARCHAR" VARCHAR(200),
              "C_VARCHAR2" VARCHAR2(200),
              "C_CLOB" CLOB,
              "C_TEXT" TEXT,
              "C_LONGVARCHAR" LONGVARCHAR,
              "C_BINARY" BINARY(8),
              "C_VARBINARY" VARBINARY(100),
              "C_RAW" RAW(16),
              "C_BLOB" BLOB,
              "C_IMAGE" IMAGE,
              "C_LONGVARBINARY" LONGVARBINARY,
              "C_DATE" DATE,
              "C_TIME" TIME,
              "C_DATETIME" DATETIME,
              "C_TIMESTAMP" TIMESTAMP,
              "C_TIMESTAMP_TZ" TIMESTAMP WITH TIME ZONE,
              "C_INTERVAL_YM" INTERVAL YEAR TO MONTH,
              "C_INTERVAL_DS" INTERVAL DAY TO SECOND,
              "C_XML" XMLTYPE,
              "C_VIRTUAL" VARCHAR(400) GENERATED ALWAYS AS
                (CONCAT("C_VARCHAR", '-virtual')) VIRTUAL
            )
            """;

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("Set -Ddameng.complex.fixture=true to create persistent fixture data",
                Boolean.getBoolean("dameng.complex.fixture"));
        Assume.assumeTrue("Dameng endpoint is not reachable", isReachable(HOST, PORT));
        ShellDamengColumnUtil.init();
    }

    @Test
    public void createPersistentComplexDataFixture() throws Throwable {
        ShellDamengClient client = openClient();
        try {
            recreateSchema(client, SOURCE_SCHEMA);
            recreateSchema(client, TARGET_SCHEMA);
            client.executeSqlSimple(SOURCE_SCHEMA, TABLE_DDL);
            client.executeSqlSimple(SOURCE_SCHEMA,
                    TABLE_DDL.replaceFirst("\"COMPLEX_TYPES\"", "\"" + IMPORT_TABLE + "\""));
            client.executeSqlSimple(TARGET_SCHEMA,
                    TABLE_DDL.replaceFirst("\"COMPLEX_TYPES\"", "\"" + TABLE + "\""));
            createUdtObjects(client, SOURCE_SCHEMA, UDT_TABLE, UDT_TABLE + "_IMPORT");
            createUdtObjects(client, TARGET_SCHEMA, UDT_TABLE, null);

            List<DamengColumn> columns = client.selectColumns(
                    new DamengSelectColumnParam(SOURCE_SCHEMA, TABLE));
            for (int i = 1; i <= 10; i++) {
                DamengRecord record = new DamengRecord(columns);
                values(i).forEach(record::putValue);
                DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
                primaryKey.init(columns.stream()
                        .filter(column -> "ID".equals(column.getName()))
                        .findFirst()
                        .orElseThrow(), record);
                DamengInsertRecordParam param = new DamengInsertRecordParam();
                param.setSchema(SOURCE_SCHEMA);
                param.setTableName(TABLE);
                param.setRecord(record.getRecordData());
                param.setPrimaryKey(primaryKey);
                assertEquals(1, client.insertRecord(param));
                assertNotNull(primaryKey.getReturnData());
            }
            insertUdtRows(client);
        } finally {
            client.close();
        }
    }

    private static void createUdtObjects(ShellDamengClient client, String schema,
                                         String tableName, String importTableName) {
        client.executeSqlSimple(schema,
                "CREATE TYPE \"ADDRESS_T\" AS OBJECT (\"STREET\" VARCHAR(100), \"ZIP\" INT)");
        client.executeSqlSimple(schema,
                "CREATE TYPE \"INT_ARRAY\" AS VARRAY(10) OF INT");
        client.executeSqlSimple(schema,
                UDT_TABLE_DDL.replaceFirst("\"UDT_TYPES\"", "\"" + tableName + "\""));
        if (importTableName != null) {
            client.executeSqlSimple(schema,
                    UDT_TABLE_DDL.replaceFirst("\"UDT_TYPES\"", "\"" + importTableName + "\""));
        }
    }

    private static void insertUdtRows(ShellDamengClient client) {
        List<DamengColumn> columns = client.selectColumns(
                new DamengSelectColumnParam(SOURCE_SCHEMA, UDT_TABLE));
        for (int i = 1; i <= 10; i++) {
            DamengRecord record = new DamengRecord(columns);
            record.putValue("ADDRESS_VALUE", "ADDRESS_T('Street " + i + "'," + (10000 + i) + ")");
            record.putValue("NUMBER_VALUES",
                    "INT_ARRAY(" + i + "," + (i + 1) + "," + (i + 2) + ")");
            DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
            primaryKey.init(columns.stream()
                    .filter(column -> "ID".equals(column.getName()))
                    .findFirst()
                    .orElseThrow(), record);
            DamengInsertRecordParam param = new DamengInsertRecordParam();
            param.setSchema(SOURCE_SCHEMA);
            param.setTableName(UDT_TABLE);
            param.setRecord(record.getRecordData());
            param.setPrimaryKey(primaryKey);
            assertEquals(1, client.insertRecord(param));
        }
    }

    private static Map<String, Object> values(int index) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("C_TINY", index);
        values.put("C_SMALL", 1000 + index);
        values.put("C_INT", 100000 + index);
        values.put("C_BIG", 9000000000L + index);
        values.put("C_DECIMAL", new BigDecimal("12345678901234567890.1234567890")
                .add(BigDecimal.valueOf(index, 4)));
        values.put("C_FLOAT", 100.25F + index);
        values.put("C_DOUBLE", 90000.1234D + index * 3.25D);
        values.put("C_BIT", (index & 1) == 0);
        values.put("C_CHAR", "row" + index);
        values.put("C_VARCHAR", "row" + index + "-中文-😀");
        values.put("C_VARCHAR2", "row" + index + "-varchar2");
        values.put("C_CLOB", "CLOB-" + index + "-中文-😀");
        values.put("C_TEXT", "TEXT-" + index + " 'quote' \\ slash");
        values.put("C_LONGVARCHAR", "LONGVARCHAR-" + index);
        values.put("C_BINARY", new byte[]{0, 1, (byte) index, 3, 4, 5, 6, 7});
        values.put("C_VARBINARY", new byte[]{0, 1, (byte) index, (byte) 0xFE, (byte) 0xFF});
        values.put("C_RAW", new byte[]{8, 7, (byte) index, 5, 4, 3, 2, 1});
        values.put("C_BLOB", new byte[]{(byte) index, 0x20, 0x30, 0x40});
        values.put("C_IMAGE", ("IMAGE-" + index).getBytes());
        values.put("C_LONGVARBINARY", ("LONGVARBINARY-" + index).getBytes());
        values.put("C_DATE", java.sql.Date.valueOf("2026-11-" + String.format("%02d", index)));
        values.put("C_TIME", java.sql.Time.valueOf(String.format("%02d:%02d:%02d",
                8 + index, index * 2, index)));
        values.put("C_DATETIME", java.sql.Timestamp.valueOf(String.format(
                "2026-11-%02d 10:%02d:%02d", index, index, index)));
        values.put("C_TIMESTAMP", java.sql.Timestamp.valueOf(String.format(
                "2026-12-%02d 11:%02d:%02d", index, index, index)));
        values.put("C_TIMESTAMP_TZ", OffsetDateTime.parse(String.format(
                "2026-12-%02dT11:%02d:%02d+08:00", index, index, index)));
        values.put("C_INTERVAL_YM", "INTERVAL '" + index + "-" + index + "' YEAR TO MONTH");
        values.put("C_INTERVAL_DS", "INTERVAL '" + index
                + " 01:02:03.456789' DAY TO SECOND");
        values.put("C_XML", "<root><id>" + index + "</id><name>张三" + index
                + "</name></root>");
        return values;
    }

    private static void recreateSchema(ShellDamengClient client, String schemaName) {
        if (client.existSchema(schemaName)) {
            client.dropSchema(schemaName);
        }
        DamengSchema schema = new DamengSchema();
        schema.setName(schemaName);
        client.createSchema(schema);
    }

    private static ShellDamengClient openClient() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.DAMENG);
        connect.setHost(HOST + ":" + PORT);
        connect.setUser(USER);
        connect.setPassword(PASSWORD);
        connect.setConnectTimeOut(5);
        ShellDamengClient client = new ShellDamengClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
        return client;
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
