package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.data.postgresql.dto.ShellPostgresqlDataImportFile;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlCsvTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlDataImportHelper;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlExcelTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlJsonTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlTxtTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlXmlTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.handler.ShellPostgresqlDataImportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.column.PostgresqlSelectColumnParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlColumnUtil;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataCsvTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataExcelTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataJsonTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTxtTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataXmlTypeFileReader;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostgresqlDataFormatIntegrationTest {

    private static String host;
    private static int port;
    private static String sourceSchema;
    private static String sourceTable;
    private static ShellPostgresqlClient client;
    private static PostgresqlColumns columns;
    private static List<PostgresqlRecord> records;

    @BeforeClass
    public static void setUp() throws Throwable {
        DBColumnFieldManager.registerInitializer(DBDialect.POSTGRESQL, ShellPostgresqlColumnUtil::init);
        host = System.getProperty("pg.host", System.getenv().getOrDefault("PG_HOST", "127.0.0.1"));
        port = Integer.parseInt(System.getProperty("pg.port", System.getenv().getOrDefault("PG_PORT", "15432")));
        String user = System.getProperty("pg.user", System.getenv().getOrDefault("PG_USER", "postgres"));
        String password = System.getProperty("pg.password", System.getenv().getOrDefault("PG_PASSWORD", ""));
        sourceSchema = System.getProperty("pg.schema", "demo");
        sourceTable = System.getProperty("pg.table", "dt_demo_misc");
        Assume.assumeTrue("PostgreSQL endpoint is not reachable", isReachable());
        Assume.assumeFalse("PostgreSQL password was not provided", password.isBlank());

        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.POSTGRESQL);
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setConnectTimeOut(5);
        client = new ShellPostgresqlClient(connect);
        client.start(5000);
        Assume.assumeTrue("Source table does not exist", client.selectTable(sourceSchema, sourceTable) != null);
        columns = new PostgresqlColumns(client.selectColumns(new PostgresqlSelectColumnParam(sourceSchema, sourceTable)));
        PostgresqlSelectRecordParam param = new PostgresqlSelectRecordParam();
        param.setSchema(sourceSchema);
        param.setTableName(sourceTable);
        param.setColumns(columns);
        param.setLimit(10_000L);
        param.setReadonly(true);
        records = client.selectRecords(param);
        Assume.assumeFalse("Source table has no records", records.isEmpty());
    }

    @AfterClass
    public static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void importsRealRecordsInEveryDataFormat() throws Exception {
        for (String[] format : List.of(
                new String[]{"json", "json"},
                new String[]{"csv", "csv"},
                new String[]{"txt", "txt"},
                new String[]{"xml", "xml"},
                new String[]{"excel", "xlsx"})) {
            verifyFormat(format[0], format[1]);
        }
    }

    private void verifyFormat(String format, String extension) throws Exception {
        String targetSchema = "easyshell_it_" + format;
        Connection connection = client.getConnManager().connection();
        File file = File.createTempFile("postgresql-it-" + format + "-", "." + extension);
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS \"" + targetSchema + "\" CASCADE");
            statement.execute("CREATE SCHEMA \"" + targetSchema + "\"");
            for (String sql : ShellPostgresqlDataImportHelper.splitSql(
                    ShellPostgresqlUtil.replaceSchema(
                            client.showCreateTable(sourceSchema, sourceTable), sourceSchema, targetSchema))) {
                statement.execute(sql);
            }

            DBDataExportConfig config = new DBDataExportConfig();
            ShellPostgresqlTypeFileWriter writer = switch (format) {
                case "json" -> new ShellPostgresqlJsonTypeFileWriter(file.getAbsolutePath(), config, columns);
                case "csv" -> new ShellPostgresqlCsvTypeFileWriter(file.getAbsolutePath(), config, columns);
                case "txt" -> new ShellPostgresqlTxtTypeFileWriter(file.getAbsolutePath(), config, columns);
                case "xml" -> new ShellPostgresqlXmlTypeFileWriter(file.getAbsolutePath(), config, columns);
                case "excel" -> new ShellPostgresqlExcelTypeFileWriter(file.getAbsolutePath(), config, columns);
                default -> throw new IllegalArgumentException(format);
            };
            writer.writeHeader();
            for (PostgresqlRecord record : records) {
                writer.writeRecord(record.toMap());
            }
            writer.writeTrial();
            writer.close();

            assertEquals(format, "$.\"row1\".\"name\"", firstValue(format, file, "c_jsonpath"));
            List<Map<String, Object>> importedRows = allValues(format, file);
            for (Map<String, Object> importedRow : importedRows) {
                String path = String.valueOf(importedRow.get("c_jsonpath"));
                assertTrue(format, path.endsWith("\""));
            }

            ShellPostgresqlDataImportFile importFile = new ShellPostgresqlDataImportFile();
            importFile.setSchema(targetSchema);
            importFile.setDbClient(client);
            importFile.setFile(file);
            importFile.setTargetTableName(sourceTable);
            ShellPostgresqlDataImportHandler handler = new ShellPostgresqlDataImportHandler(client, targetSchema);
            handler.setFiles(List.of(importFile));
            handler.setFileType(format);
            handler.doAction();

            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT COUNT(*), MAX(CASE WHEN \"id\" = 1 THEN \"c_jsonpath\"::text END), "
                            + "MAX(CASE WHEN \"id\" = 1 THEN \"c_xml\"::text END) FROM \""
                            + targetSchema + "\".\"" + sourceTable + "\"")) {
                resultSet.next();
                assertEquals(format, records.size(), resultSet.getInt(1));
                assertEquals(format, "$.\"row1\".\"name\"", resultSet.getString(2));
                assertEquals(format, "<row><id>1</id><val>10</val></row>", resultSet.getString(3));
            }
        } finally {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP SCHEMA IF EXISTS \"" + targetSchema + "\" CASCADE");
            }
            Files.deleteIfExists(file.toPath());
        }
    }

    private Object firstValue(String format, File file, String column) throws Exception {
        return allValues(format, file).get(0).get(column);
    }

    private List<Map<String, Object>> allValues(String format, File file) throws Exception {
        DBDataImportConfig config = new DBDataImportConfig();
        return switch (format) {
            case "json" -> {
                try (DBDataJsonTypeFileReader reader = new DBDataJsonTypeFileReader(file, config)) {
                    yield reader.readRecords(10_000);
                }
            }
            case "csv" -> {
                try (DBDataCsvTypeFileReader reader = new DBDataCsvTypeFileReader(file, config)) {
                    yield reader.readRecords(10_000);
                }
            }
            case "txt" -> {
                try (DBDataTxtTypeFileReader reader = new DBDataTxtTypeFileReader(file, config)) {
                    yield reader.readRecords(10_000);
                }
            }
            case "xml" -> {
                try (DBDataXmlTypeFileReader reader = new DBDataXmlTypeFileReader(file, config)) {
                    yield reader.readRecords(10_000);
                }
            }
            case "excel" -> {
                try (DBDataExcelTypeFileReader reader = new DBDataExcelTypeFileReader(file, config)) {
                    yield reader.readRecords(10_000);
                }
            }
            default -> throw new IllegalArgumentException(format);
        };
    }

    private static boolean isReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
