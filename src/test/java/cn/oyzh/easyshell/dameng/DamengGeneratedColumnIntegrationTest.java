package cn.oyzh.easyshell.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.table.DamengAlertTableParam;
import cn.oyzh.easyshell.dameng.table.DamengCreateTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 达梦虚列建表、元数据、记录和改表集成测试。
 */
public class DamengGeneratedColumnIntegrationTest {

    private static final String HOST = property("dameng.host", "DAMENG_HOST", "127.0.0.1");
    private static final int PORT = Integer.parseInt(property("dameng.port", "DAMENG_PORT", "15237"));
    private static final String USER = property("dameng.user", "DAMENG_USER", "SYSDBA");
    private static final String PASSWORD = property("dameng.password", "DAMENG_PASSWORD", "SYSDBA");
    private static final String SCHEMA = "EASYSHELL_DAMENG_GENERATED_IT";

    private static ShellDamengClient client;

    @BeforeClass
    public static void setUp() throws Throwable {
        Assume.assumeTrue("Dameng endpoint is not reachable", isReachable(HOST, PORT));
        ShellDamengColumnUtil.init();
        client = openClient();
        if (client.existSchema(SCHEMA)) {
            client.dropSchema(SCHEMA);
        }
        DamengSchema schema = new DamengSchema();
        schema.setName(SCHEMA);
        client.createSchema(schema);
    }

    @AfterClass
    public static void tearDown() {
        try {
            if (client != null && client.existSchema(SCHEMA)) {
                client.dropSchema(SCHEMA);
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void createsGeneratedColumnAndRoundTripsMetadata() {
        String tableName = "CREATE_GENERATED";
        DamengColumn id = column("ID", "BIGINT");
        DamengColumn source = column("SOURCE", "VARCHAR");
        source.setSize(200);
        DamengColumn generated = column("GENERATED_VALUE", "VARCHAR");
        generated.setSize(500);
        generated.setGeneratedExpression("CONCAT(\"SOURCE\", '-generated')");
        generated.normalizeGeneratedAttributes();

        DamengCreateTableParam param = new DamengCreateTableParam();
        param.setTable(table(tableName));
        param.setColumns(new DamengColumns(List.of(id, source, generated)));
        client.createTable(param);

        DamengColumn selected = selectedColumn(tableName, "GENERATED_VALUE");
        assertTrue(selected.isGenerated());
        assertTrue(selected.isVirtual());
        assertTrue(selected.getGeneratedExpression(),
                selected.getGeneratedExpression().toUpperCase().contains("CONCAT"));

        DamengRecord record = new DamengRecord(selectColumns(tableName));
        record.putValue("ID", 1L);
        record.putValue("SOURCE", "alpha");
        record.putValue("GENERATED_VALUE", "ignored");
        assertTrue(record.getProperty("GENERATED_VALUE").isReadonly());
        DamengInsertRecordParam insertParam = new DamengInsertRecordParam();
        insertParam.setSchema(SCHEMA);
        insertParam.setTableName(tableName);
        insertParam.setRecord(record.getRecordData());
        assertEquals(1, client.insertRecord(insertParam));

        DamengRecord inserted = selectRecords(tableName).getFirst();
        assertEquals("alpha-generated", inserted.getValue("GENERATED_VALUE").toString());

        selected.setGeneratedExpression("UPPER(\"SOURCE\")");
        selected.setChanged(true);
        alterColumn(tableName, selected);
        selected = selectedColumn(tableName, "GENERATED_VALUE");
        assertTrue(selected.getGeneratedExpression().toUpperCase().contains("UPPER"));
        assertEquals("ALPHA", selectRecords(tableName).getFirst()
                .getValue("GENERATED_VALUE").toString());
    }

    @Test
    public void changesNormalAndGeneratedColumnKinds() {
        String tableName = "ALTER_GENERATED";
        client.executeSqlSimple(SCHEMA,
                "CREATE TABLE \"" + tableName + "\" (\"SOURCE\" BIGINT, \"COMPUTED\" INT)");

        DamengColumn computed = selectedColumn(tableName, "COMPUTED");
        computed.setGenerated(true);
        computed.normalizeGeneratedAttributes();
        computed.setGeneratedExpression("\"SOURCE\" + 1");
        computed.setChanged(true);
        alterColumn(tableName, computed);

        computed = selectedColumn(tableName, "COMPUTED");
        assertTrue(computed.isGenerated());
        assertTrue(computed.getGeneratedExpression().contains("SOURCE"));

        computed.setGenerated(false);
        computed.normalizeGeneratedAttributes();
        computed.setChanged(true);
        alterColumn(tableName, computed);
        computed = selectedColumn(tableName, "COMPUTED");
        assertFalse(computed.isGenerated());
        assertNull(computed.getGeneratedExpression());
    }

    private static List<DamengRecord> selectRecords(String tableName) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setSchema(SCHEMA);
        param.setTableName(tableName);
        param.setStart(0L);
        param.setLimit(10L);
        return client.selectRecords(param);
    }

    private static void alterColumn(String tableName, DamengColumn column) {
        List<DamengColumn> columns = selectColumns(tableName);
        columns.replaceAll(candidate -> column.getName().equals(candidate.getName())
                ? column : candidate);
        DamengAlertTableParam param = new DamengAlertTableParam();
        param.setTable(table(tableName));
        param.setColumns(new DamengColumns(columns));
        client.alertTable(param);
    }

    private static DamengColumn selectedColumn(String tableName, String columnName) {
        return selectColumns(tableName).stream()
                .filter(column -> columnName.equals(column.getName()))
                .findFirst()
                .orElseThrow();
    }

    private static List<DamengColumn> selectColumns(String tableName) {
        return client.selectColumns(new DamengSelectColumnParam(SCHEMA, tableName));
    }

    private static DamengTable table(String tableName) {
        DamengTable table = new DamengTable();
        table.setSchema(SCHEMA);
        table.setName(tableName);
        return table;
    }

    private static DamengColumn column(String name, String type) {
        DamengColumn column = new DamengColumn(name);
        column.setType(type);
        column.setSchema(SCHEMA);
        column.setNullable(true);
        return column;
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
