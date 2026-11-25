package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.data.postgresql.handler.ShellPostgresqlDataDumpHandler;
import cn.oyzh.easyshell.data.postgresql.handler.ShellPostgresqlDataRunSqlFileHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.postgresql.type.PostgresqlTypeDefinition;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * PostgreSQL 自定义类型转储集成测试。
 */
public class PostgresqlCustomTypeIntegrationTest {

    private static final String SOURCE_SCHEMA = "EASYSHELL_PG_TYPE_SRC";
    private static final String TARGET_SCHEMA = "EASYSHELL_PG_TYPE_DST";
    private static final String TABLE = "CUSTOM_TYPE_TABLE";

    private static ShellPostgresqlClient client;

    @BeforeClass
    public static void setUp() throws Throwable {
        String host = System.getProperty("pg.host", System.getenv().getOrDefault("PG_HOST", "127.0.0.1"));
        int port = Integer.parseInt(System.getProperty("pg.port", System.getenv().getOrDefault("PG_PORT", "15432")));
        String user = System.getProperty("pg.user", System.getenv().getOrDefault("PG_USER", "postgres"));
        String password = System.getProperty("pg.password", System.getenv().getOrDefault("PG_PASSWORD", ""));
        Assume.assumeTrue("PostgreSQL endpoint is not reachable", isReachable(host, port));
        Assume.assumeFalse("PostgreSQL password was not provided", password.isBlank());

        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.POSTGRESQL);
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setConnectTimeOut(5);
        client = new ShellPostgresqlClient(connect);
        client.start(5000);
        assertTrue(client.isConnected());
        recreateSchema(SOURCE_SCHEMA);
        recreateSchema(TARGET_SCHEMA);
    }

    @AfterClass
    public static void tearDown() {
        if (client != null) {
            for (String schema : List.of(SOURCE_SCHEMA, TARGET_SCHEMA)) {
                if (client.existSchema(schema)) {
                    client.dropSchema(schema);
                }
            }
            client.close();
        }
    }

    @Test
    public void dumpsRangeAndOrderedCompositeTypes() throws Exception {
        client.executeSqlSimple(SOURCE_SCHEMA, """
                CREATE TYPE "mood" AS ENUM ('happy', 'sad');
                CREATE DOMAIN "positive_int" AS int CHECK (VALUE > 0);
                CREATE TYPE "address" AS (street text, zip int);
                CREATE TYPE "price_range" AS RANGE (subtype = numeric);
                CREATE TYPE "order_line" AS (price "price_range", addr "address");
                CREATE TABLE "CUSTOM_TYPE_TABLE" (
                    id int PRIMARY KEY,
                    mood "mood",
                    pos "positive_int",
                    addr "address",
                    price "price_range",
                    line "order_line"
                );
                INSERT INTO "CUSTOM_TYPE_TABLE" VALUES
                    (1, 'happy', 5, ROW('Main St', 10001),
                     '[1.5,2.5)'::"price_range",
                     ROW('[1.5,2.5)'::"price_range", ROW('Main St', 10001)::"address"));
                """);

        List<PostgresqlTypeDefinition> types = client.selectTypes(SOURCE_SCHEMA);
        String sourceDdl = String.join("\n", types.stream()
                .map(PostgresqlTypeDefinition::getCreateDefinition).toList());
        assertTrue(sourceDdl, sourceDdl.contains("AS ENUM"));
        assertTrue(sourceDdl, sourceDdl.contains("CREATE DOMAIN"));
        assertTrue(sourceDdl, sourceDdl.contains("AS RANGE (SUBTYPE = numeric)"));
        assertTrue(types.stream().anyMatch(type -> PostgresqlTypeDefinition.OBJECT_TYPE_RANGE
                .equals(type.getObjectType())));
        int priceRangeIndex = sourceDdl.indexOf("AS RANGE (SUBTYPE = numeric)");
        int orderLineIndex = sourceDdl.indexOf(
                "CREATE TYPE \"" + SOURCE_SCHEMA + "\".\"order_line\"");
        assertTrue(sourceDdl, priceRangeIndex >= 0);
        assertTrue(sourceDdl, orderLineIndex > priceRangeIndex);

        Path dumpFile = Files.createTempFile("easyshell-pg-custom-type-", ".sql");
        ShellPostgresqlDataDumpHandler dumpHandler =
                new ShellPostgresqlDataDumpHandler(client, SOURCE_SCHEMA);
        dumpHandler.setDumpType((byte) 1);
        dumpHandler.setDataType((byte) 0);
        dumpHandler.dumpFile(dumpFile.toFile());
        dumpHandler.setContinueWithErrors(false);
        dumpHandler.doDump();

        ShellPostgresqlDataRunSqlFileHandler runHandler =
                new ShellPostgresqlDataRunSqlFileHandler(client, TARGET_SCHEMA);
        runHandler.setContinueWithErrors(false);
        runHandler.setFile(dumpFile.toFile());
        runHandler.runFile();

        String dump = Files.readString(dumpFile, StandardCharsets.UTF_8);
        assertTrue(dump, dump.contains("-- Custom types"));
        assertTrue(dump, dump.contains("AS RANGE"));
        List<PostgresqlTypeDefinition> targetTypes = client.selectTypes(TARGET_SCHEMA);
        assertTrue(targetTypes.stream().anyMatch(type -> type.getCreateDefinition().contains("AS RANGE")));
        assertTrue(client.selectFunctions(TARGET_SCHEMA).stream()
                .noneMatch(function -> function.getName().startsWith("price_")));
        assertEquals(1, client.selectRecordCount(selectParamFor(TARGET_SCHEMA, TABLE)));
    }

    private static void recreateSchema(String schema) {
        if (client.existSchema(schema)) {
            client.dropSchema(schema);
        }
        PostgresqlSchema postgresqlSchema = new PostgresqlSchema();
        postgresqlSchema.setName(schema);
        client.createSchema(postgresqlSchema);
    }

    private static PostgresqlSelectRecordParam selectParamFor(String schema, String table) {
        PostgresqlSelectRecordParam param = new PostgresqlSelectRecordParam();
        param.setSchema(schema);
        param.setTableName(table);
        param.setStart(0L);
        param.setLimit(10L);
        return param;
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
