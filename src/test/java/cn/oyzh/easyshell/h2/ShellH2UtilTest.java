package cn.oyzh.easyshell.h2;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;

/**
 * H2 工具与连接配置测试。
 *
 * @since 2026-11-28
 */
public class ShellH2UtilTest {

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        this.tempDir = Files.createTempDirectory("easyshell-h2-util-test-");
    }

    @After
    public void tearDown() throws Exception {
        if (this.tempDir != null && Files.exists(this.tempDir)) {
            Files.walk(this.tempDir).sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private ShellConnect connect(Path file, String env) {
        ShellConnect connect = new ShellConnect();
        connect.setName("h2-test");
        connect.setType(ShellPrototype.H2);
        connect.setOsType(ShellPrototype.H2);
        connect.setHost(file.toString());
        connect.setConnectTimeOut(5);
        connect.setEnvironment(env);
        return connect;
    }

    /**
     * 方言与标识符转义。
     */
    @Test
    public void testDialectAndQuote() {
        Assert.assertEquals("\"order\"", ShellH2Helper.quote("order"));
        Assert.assertEquals("\"a\"\"b\"", ShellH2Helper.quote("a\"b"));
        Assert.assertEquals("NULL", ShellH2Helper.displayValue(null));
        Assert.assertTrue(ShellH2Helper.displayValue(new byte[]{0x01, 0x02}).startsWith("X'0102"));
    }

    /**
     * 环境参数解析与保留。
     */
    @Test
    public void testEnvironment() {
        String env = ShellH2Helper.defaultEnvironment();
        Assert.assertTrue(env.contains("urlMode=file"));
        Assert.assertTrue(env.contains("schema=PUBLIC"));
        Properties parsed = ShellH2Helper.parseEnvironment(env);
        Assert.assertEquals("file", parsed.getProperty("urlMode"));
        // setEnvironmentValue 保留顺序并替换值
        String updated = ShellH2Helper.setEnvironmentValue(env, "schema", "TEST");
        Assert.assertTrue(updated.contains("schema=TEST"));
        Assert.assertTrue(updated.indexOf("urlMode") < updated.indexOf("schema"));
        // 无对应键时追加
        String appended = ShellH2Helper.setEnvironmentValue(env, "autoServer", "true");
        Assert.assertTrue(appended.contains("autoServer=true"));
        // 注释与空行跳过
        Properties withComment = ShellH2Helper.parseEnvironment("# comment\nurlMode=mem\n\n");
        Assert.assertEquals("mem", withComment.getProperty("urlMode"));
    }

    /**
     * 事务命令识别。
     */
    @Test
    public void testTransactionControl() {
        Assert.assertTrue(ShellH2Helper.isTransactionControl("BEGIN"));
        Assert.assertTrue(ShellH2Helper.isTransactionControl("COMMIT;"));
        Assert.assertTrue(ShellH2Helper.isTransactionControl("-- comment\nROLLBACK"));
        Assert.assertEquals("ROLLBACK_TO", ShellH2Helper.transactionCommand("ROLLBACK TO sp1"));
        Assert.assertFalse(ShellH2Helper.isTransactionControl("SELECT 1"));
        Assert.assertFalse(ShellH2Helper.isTransactionControl("INSERT INTO t VALUES('BEGIN')"));
    }

    /**
     * 文件模式连接配置归一化与 .mv.db 后缀剥离。
     */
    @Test
    public void testFileConnConfig() throws Exception {
        Path file = this.tempDir.resolve("db1.mv.db");
        Files.write(file, new byte[0]);
        // 剥离 .mv.db 后缀
        Path raw = this.tempDir.resolve("db1.mv.db");
        ShellH2ConnConfig config = ShellH2Helper.connConfig(this.connect(raw, "urlMode=file\ncreateIfMissing=false\n"));
        Assert.assertEquals(file.toAbsolutePath().normalize().toString().replace(".mv.db", ""),
                config.resolveFilePath().toString());
        Assert.assertTrue(ShellH2Helper.isDatabaseFile(file.toString()));
        Assert.assertFalse(ShellH2Helper.isDatabaseFile(
                this.tempDir.resolve("db.mv_副本.db").toString()));
        Assert.assertEquals(file.toAbsolutePath().normalize(),
                config.actualFilePath());
        Assert.assertFalse(config.isCreateIfMissing());
        Assert.assertEquals("PUBLIC", config.getSchema());
        Assert.assertTrue(config.isFileMode());
        Assert.assertFalse(config.isReadOnly());
    }

    /**
     * 连接串构造：file / mem / tcp 三种模式。
     */
    @Test
    public void testConnectionStrings() throws Exception {
        // 文件模式
        Path file = this.tempDir.resolve("filedb");
        Files.write(Path.of(file + ".mv.db"), new byte[0]);
        ShellH2ConnManager manager = new ShellH2ConnManager();
        manager.setConfig(ShellH2Helper.connConfig(this.connect(file, "urlMode=file\ncreateIfMissing=false\n")));
        Assert.assertTrue(manager.getConnectionString()
                .startsWith("jdbc:h2:file:" + file.toAbsolutePath().normalize()));
        Assert.assertTrue(manager.getConnectionString().contains("IFEXISTS=TRUE"));
        Assert.assertTrue(manager.getConnectionString().contains("SCHEMA=PUBLIC"));

        // 文件不存在且未开启创建时报错
        Path missing = this.tempDir.resolve("missingdb");
        ShellH2ConnManager manager2 = new ShellH2ConnManager();
        manager2.setConfig(ShellH2Helper.connConfig(this.connect(missing, "urlMode=file\ncreateIfMissing=false\n")));
        try {
            manager2.getConnectionString();
            Assert.fail("should reject missing file");
        } catch (IllegalArgumentException ignore) {
        }

        // 内存模式
        ShellH2ConnManager manager3 = new ShellH2ConnManager();
        manager3.setConfig(ShellH2Helper.connConfig(this.connect(this.tempDir.resolve("x"),
                "urlMode=mem\n")));
        String memUrl = manager3.getConnectionString();
        Assert.assertTrue(memUrl.startsWith("jdbc:h2:mem:"));
        Assert.assertTrue(memUrl.contains("DB_CLOSE_DELAY=-1"));

        // 远程模式
        ShellConnect tcp = this.connect(this.tempDir.resolve("x"), "urlMode=tcp\n");
        tcp.setHost("127.0.0.1:9092");
        tcp.setDatabase("testdb");
        ShellH2ConnManager manager4 = new ShellH2ConnManager();
        manager4.setConfig(ShellH2Helper.connConfig(tcp));
        Assert.assertEquals("jdbc:h2:tcp://127.0.0.1:9092/testdb;SCHEMA=PUBLIC", manager4.getConnectionString());
    }

    /**
     * 只读模式连接串包含 ACCESS_MODE_DATA=r。
     */
    @Test
    public void testReadOnlyConnectionString() throws Exception {
        Path file = this.tempDir.resolve("rodb");
        Files.write(Path.of(file + ".mv.db"), new byte[0]);
        ShellConnect connect = this.connect(file, "urlMode=file\ncreateIfMissing=false\n");
        connect.setReadonly(true);
        ShellH2ConnManager manager = new ShellH2ConnManager();
        manager.setConfig(ShellH2Helper.connConfig(connect));
        Assert.assertTrue(manager.getConnectionString().contains("ACCESS_MODE_DATA=r"));
    }

    /**
     * H2 是强类型数据库，字段能力不能沿用 SQLite 的弱类型规则。
     */
    @Test
    public void testStrongTypeCapabilities() {
        ShellH2ColumnUtil.init();

        Assert.assertTrue(DBColumnFieldManager.supportInteger(DBDialect.H2, "INTEGER"));
        Assert.assertTrue(DBColumnFieldManager.supportAutoIncrement(DBDialect.H2, "INT"));
        Assert.assertFalse(DBColumnFieldManager.supportSize(DBDialect.H2, "INTEGER"));
        Assert.assertFalse(DBColumnFieldManager.supportString(DBDialect.H2, "INTEGER"));
        Assert.assertFalse(DBColumnFieldManager.supportText(DBDialect.H2, "BIGINT"));

        Assert.assertTrue(DBColumnFieldManager.supportString(
                DBDialect.H2, "CHARACTER VARYING"));
        Assert.assertTrue(DBColumnFieldManager.supportSize(DBDialect.H2, "VARCHAR"));
        Assert.assertFalse(DBColumnFieldManager.supportInteger(DBDialect.H2, "VARCHAR"));

        Assert.assertTrue(DBColumnFieldManager.supportSize(DBDialect.H2, "NUMERIC"));
        Assert.assertTrue(DBColumnFieldManager.supportDigits(DBDialect.H2, "DECIMAL"));
        Assert.assertFalse(DBColumnFieldManager.supportString(DBDialect.H2, "NUMERIC"));

        Assert.assertTrue(DBColumnFieldManager.supportBoolean(DBDialect.H2, "BOOL"));
        Assert.assertFalse(DBColumnFieldManager.supportString(DBDialect.H2, "BOOLEAN"));
        Assert.assertTrue(DBColumnFieldManager.supportJson(DBDialect.H2, "JSON"));
        Assert.assertFalse(DBColumnFieldManager.supportText(DBDialect.H2, "JSON"));

        Assert.assertFalse(ShellH2ColumnUtil.isIntegerType("POINT"));
        Assert.assertFalse(ShellH2ColumnUtil.isIntegerType("INTERVAL"));
        Assert.assertTrue(ShellH2ColumnUtil.isGeometryType("GEOMETRY"));
        Assert.assertEquals(Integer.valueOf(12), ShellH2ColumnUtil.parseSize("NUMERIC(12, 3)"));
        Assert.assertEquals(Integer.valueOf(3), ShellH2ColumnUtil.parseDigits("NUMERIC(12, 3)"));
    }
}
