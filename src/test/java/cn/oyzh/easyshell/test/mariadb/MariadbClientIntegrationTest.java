package cn.oyzh.easyshell.test.mariadb;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.mariadb.function.MariadbCreateFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.procedure.MariadbCreateProcedureParam;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.record.MariadbDeleteRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbInsertRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbUpdateRecordParam;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.view.MariadbCreateViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbExecuteResult;
import cn.oyzh.fx.db.query.DBQueryResults;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB JDBC、元数据、SQL、记录与数据库对象集成测试
 *
 * @since 2026-10-09
 */
public class MariadbClientIntegrationTest {

    private static final String DB_NAME = "easyshell_mariadb_it";
    private static ShellMariadbClient client;

    @BeforeClass
    public static void setUp() throws Throwable {
        String host = System.getProperty("mariadb.host", System.getenv().getOrDefault("MARIADB_HOST", "127.0.0.1"));
        int port = Integer.parseInt(System.getProperty("mariadb.port", System.getenv().getOrDefault("MARIADB_PORT", "33306")));
        String user = System.getProperty("mariadb.user", System.getenv().getOrDefault("MARIADB_USER", "root"));
        String password = System.getProperty("mariadb.password", System.getenv().getOrDefault("MARIADB_PASSWORD", "123456"));
        Assume.assumeTrue("MariaDB endpoint is not reachable", isReachable(host, port));
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.MARIADB);
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setSSLMode(false);
        connect.setConnectTimeOut(5);
        client = new ShellMariadbClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
    }

    @AfterClass
    public static void tearDown() throws Throwable {
        if (client == null) {
            return;
        }
        try {
            client.executeSqlSimple(DB_NAME, "DROP TRIGGER IF EXISTS `mariadb_it_trigger`");
            client.executeSqlSimple(DB_NAME, "DROP EVENT IF EXISTS `mariadb_it_event`");
            client.executeSqlSimple(DB_NAME, "DROP PROCEDURE IF EXISTS `mariadb_it_procedure`");
            client.executeSqlSimple(DB_NAME, "DROP FUNCTION IF EXISTS `mariadb_it_function`");
            client.executeSqlSimple(DB_NAME, "DROP VIEW IF EXISTS `mariadb_it_view`");
        } catch (Exception ignored) {
            // Best effort cleanup keeps the integration test repeatable.
        }
        try {
            client.dropDatabase(DB_NAME);
        } catch (Exception ignored) {
            // Best effort cleanup keeps the integration test repeatable.
        } finally {
            client.close();
        }
    }

    @Test
    public void endToEndMariaDbLifecycle() {
        assertTrue(client.selectVersion().toLowerCase().contains("mariadb"));
        assertTrue(client.selectProduct().toLowerCase().contains("mariadb"));

        MariadbDatabase database = new MariadbDatabase();
        database.setName(DB_NAME);
        database.setCharset("utf8mb4");
        if (client.existDatabase(DB_NAME)) {
            client.dropDatabase(DB_NAME);
        }
        client.createDatabase(database);
        assertTrue(client.existDatabase(DB_NAME));
        assertTrue(client.databaseNames().contains(DB_NAME));

        client.executeSqlSimple(DB_NAME, """
                CREATE TABLE `items` (
                    `id` INT NOT NULL AUTO_INCREMENT,
                    `name` VARCHAR(100) NOT NULL,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_items_name` (`name`),
                    CHECK (CHAR_LENGTH(`name`) > 0)
                ) ENGINE=InnoDB
                """);
        MariadbTable table = client.selectTable(DB_NAME, "items");
        assertNotNull(table);
        assertEquals("items", table.getName());
        List<MariadbColumn> columns = client.selectColumns(new MariadbSelectColumnParam(DB_NAME, "items"));
        assertEquals(2, columns.size());
        List<MariadbIndex> indexes = client.selectIndexes(DB_NAME, "items");
        assertTrue(indexes.stream().anyMatch(index -> "uk_items_name".equals(index.getName())));
        assertFalse(client.selectChecks(DB_NAME, "items").isEmpty());

        MariadbColumn idColumn = columns.stream().filter(column -> "id".equals(column.getName())).findFirst().orElseThrow();
        MariadbRecord inserted = new MariadbRecord(columns);
        inserted.putValue("name", "integration");
        MariadbRecordPrimaryKey primaryKey = new MariadbRecordPrimaryKey();
        primaryKey.init(idColumn, inserted);
        MariadbInsertRecordParam insertParam = new MariadbInsertRecordParam();
        insertParam.setDbName(DB_NAME);
        insertParam.setTableName("items");
        insertParam.setRecord(inserted.getRecordData());
        insertParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.insertRecord(insertParam));
        assertNotNull(primaryKey.getReturnData());

        MariadbSelectRecordParam selectParam = new MariadbSelectRecordParam();
        selectParam.setDbName(DB_NAME);
        selectParam.setTableName("items");
        selectParam.setStart(0L);
        selectParam.setLimit(10L);
        List<MariadbRecord> records = client.selectRecords(selectParam);
        assertEquals(1, records.size());
        assertEquals("integration", records.get(0).getValue("name"));
        assertEquals(1L, client.selectRecordCount(selectParam));

        MariadbRecord updateRecord = records.get(0);
        updateRecord.putValue("name", "updated");
        updateRecord.getProperty("name").setChanged(true);
        primaryKey.setData(primaryKey.getReturnData());
        primaryKey.setOriginalData(primaryKey.getReturnData());
        MariadbUpdateRecordParam updateParam = new MariadbUpdateRecordParam();
        updateParam.setDbName(DB_NAME);
        updateParam.setTableName("items");
        updateParam.setRecord(updateRecord.getOriginalRecordData());
        updateParam.setUpdateRecord(updateRecord.getChangedRecordData());
        updateParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.updateRecord(updateParam));
        records = client.selectRecords(selectParam);
        assertEquals("updated", records.get(0).getValue("name"));

        MariadbView view = view("mariadb_it_view");
        view.setDefinition("SELECT `id`, `name` FROM `items`");
        MariadbCreateViewParam viewParam = new MariadbCreateViewParam();
        viewParam.setDbName(DB_NAME);
        viewParam.setView(view);
        client.createView(viewParam);
        assertTrue(client.existView(DB_NAME, "mariadb_it_view"));
        assertEquals(1, client.viewRecords(DB_NAME, "mariadb_it_view", 0L, 10L, null).size());

        MariadbFunction function = function("mariadb_it_function");
        function.setDefinition("RETURN 42");
        function.setCharacteristic("DETERMINISTIC");
        MariadbRoutineParam returnParam = new MariadbRoutineParam();
        returnParam.setType("INT");
        function.setReturnParam(returnParam);
        MariadbCreateFunctionParam functionParam = new MariadbCreateFunctionParam();
        functionParam.setDbName(DB_NAME);
        functionParam.setFunction(function);
        client.createFunction(functionParam);
        assertNotNull(client.selectFunction(DB_NAME, "mariadb_it_function"));

        MariadbProcedure procedure = procedure("mariadb_it_procedure");
        procedure.setDefinition("BEGIN SELECT 42 AS answer; END");
        procedure.setCharacteristic("DETERMINISTIC");
        MariadbCreateProcedureParam procedureParam = new MariadbCreateProcedureParam();
        procedureParam.setDbName(DB_NAME);
        procedureParam.setProcedure(procedure);
        client.createProcedure(procedureParam);
        assertNotNull(client.selectProcedure(DB_NAME, "mariadb_it_procedure"));

        client.executeSqlSimple(DB_NAME, "CREATE TRIGGER `mariadb_it_trigger` BEFORE INSERT ON `items` FOR EACH ROW SET @mariadb_trigger_seen = NEW.id");
        assertFalse(client.selectTriggers(DB_NAME).isEmpty());

        MariadbEvent event = event("mariadb_it_event");
        event.setType("ONE TIME");
        event.setExecuteAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        event.setOnCompletion("NOT PRESERVE");
        event.setDefinition("UPDATE `items` SET `name` = 'scheduled'");
        client.createEvent(DB_NAME, event);
        assertNotNull(client.selectEvent(DB_NAME, "mariadb_it_event"));

        DBQueryResults<ShellMariadbExecuteResult> queryResults = client.executeSql(DB_NAME,
                "SELECT `id`, `name` FROM `items`; SELECT COUNT(*) AS `count` FROM `items`");
        assertTrue(queryResults.isSuccess());
        assertEquals(2, queryResults.getResults().size());
        assertTrue(queryResults.getResults().stream().allMatch(ShellMariadbExecuteResult::isSuccess));
        assertNotNull(client.explainSql(DB_NAME, "SELECT * FROM `items`"));

        primaryKey.setData(primaryKey.getReturnData());
        MariadbDeleteRecordParam deleteParam = new MariadbDeleteRecordParam();
        deleteParam.setDbName(DB_NAME);
        deleteParam.setTableName("items");
        deleteParam.setPrimaryKey(primaryKey);
        assertEquals(1, client.deleteRecord(deleteParam));
        assertEquals(0L, client.selectRecordCount(selectParam));
    }

    private static MariadbView view(String name) {
        MariadbView view = new MariadbView();
        view.setDbName(DB_NAME);
        view.setName(name);
        return view;
    }

    private static MariadbFunction function(String name) {
        MariadbFunction function = new MariadbFunction();
        function.setDbName(DB_NAME);
        function.setName(name);
        return function;
    }

    private static MariadbProcedure procedure(String name) {
        MariadbProcedure procedure = new MariadbProcedure();
        procedure.setDbName(DB_NAME);
        procedure.setName(name);
        return procedure;
    }

    private static MariadbEvent event(String name) {
        MariadbEvent event = new MariadbEvent();
        event.setDbName(DB_NAME);
        event.setName(name);
        return event;
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
