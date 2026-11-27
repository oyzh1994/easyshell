package cn.oyzh.easyshell.data.redis.handler;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis 复测数据准备工具（数据全部写入 DB 0，且不会自动清理）。
 *
 * <p>运行方式（在项目根目录）：</p>
 * <pre>
 *   mvn -q test-compile dependency:build-classpath -Dmdep.outputFile=/tmp/easyshell-cp.txt
 *   javac -cp "target/test-classes:target/classes:$(cat /tmp/easyshell-cp.txt)" \
 *         -d target/test-classes src/test/java/cn/oyzh/easyshell/data/redis/handler/ShellRedisRetestFixture.java
 *   java -cp "target/test-classes:target/classes:$(cat /tmp/easyshell-cp.txt)" \
 *        cn.oyzh.easyshell.data.redis.handler.ShellRedisRetestFixture
 * </pre>
 *
 * <p>键前缀固定为 {@code easyshell:retest:}，涵盖 string/json/list/set/zset/hash/stream 全部类型，
 * 以及中文、emoji、特殊字符、逗号、引号、换行、Tab、空值、长文本、二进制值、TTL 等奇怪数据。
 * 同时在项目根目录 {@code retest-data/} 下生成 json/csv/xml/excel 四种导出样例文件，
 * 可直接用于导入复测。重复运行会先按前缀清理旧键再重建。</p>
 */
public class ShellRedisRetestFixture {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private static final int DB = 0;

    private static final String PREFIX = "easyshell:retest:";

    private static final String HOST = System.getProperty("redis.host", "127.0.0.1:6379");

    private static final String PASSWORD = System.getProperty("redis.password", "redis@2026");

    private ShellRedisClient client;

    private Path outputDir;

    public static void main(String[] args) throws Exception {
        ShellRedisRetestFixture fixture = new ShellRedisRetestFixture();
        fixture.run();
    }

    private void run() throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setName("redis-retest-fixture");
        connect.setHost(HOST);
        connect.setPassword(PASSWORD);
        connect.setConnectTimeOut(5_000);
        connect.setExecuteTimeOut(5_000);
        connect.setReadonly(false);
        this.client = new ShellRedisClient(connect);
        this.client.startDatabase(DB);
        if (!this.client.isConnected()) {
            throw new IllegalStateException("Redis 连接失败: " + HOST);
        }

        this.outputDir = new File(System.getProperty("user.dir"), "retest-data").toPath();
        Files.createDirectories(this.outputDir);

        // 清理旧数据，保证可重复运行
        this.cleanOldKeys();

        // 写入全部数据
        this.writeAllTypes();

        // 生成导出样例文件
        for (String fileType : Arrays.asList("json", "csv", "xml", "excel")) {
            this.export(fileType);
        }

        // 生成一份手写导入样例（含一行会跳过的非法数据）
        this.writeManualImportSample();

        long count = this.client.allKeys(DB, PREFIX + "*").size();
        System.out.println("DONE");
        System.out.println("HOST=" + HOST + " DB=" + DB);
        System.out.println("PREFIX=" + PREFIX);
        System.out.println("KEYS=" + count);
        System.out.println("FILES=" + this.outputDir.toAbsolutePath());
        this.client.close();
        // 客户端内部线程可能阻止 JVM 退出，显式结束
        System.exit(0);
    }

    private void cleanOldKeys() {
        Set<String> keys = this.client.allKeys(DB, PREFIX + "*");
        if (!keys.isEmpty()) {
            this.client.del(DB, keys);
            System.out.println("cleaned old keys: " + keys.size());
        }
    }

    private String k(String suffix) {
        return PREFIX + suffix;
    }

    private void writeAllTypes() {
        // string：中文、emoji、XML 特殊字符、引号、换行、Tab、首尾空格
        this.client.set(DB, this.k("string"), "中文 😀 <tag>&\"'\n第二行\tTab  首尾空格  ");
        // string：空值
        this.client.set(DB, this.k("empty"), "");
        // string：长文本
        this.client.set(DB, this.k("long"), "长".repeat(1500));
        // string：纯数字字符串
        this.client.set(DB, this.k("number"), "1234567890");
        // string：二进制值（含 0xFF，仅同字符集传输可字节级保留）
        this.client.set(DB, this.k("binary").getBytes(UTF8), new byte[]{0, 1, 2, (byte) 0xFF, (byte) 0x80, 0});
        // string：Unicode + 特殊字符键名
        this.client.set(DB, this.k("中文<>&键"), "unicode-key");
        // json
        this.client.jsonSet(DB, this.k("json"), "{\"中文\":\"值<>&\",\"n\":123,\"arr\":[1,2,3]}");
        // list（注意顺序：LRANGE 头到尾）
        this.client.lpush(DB, this.k("list"), "d", "c", "中文,逗号", "a");
        // set
        this.client.sadd(DB, this.k("set"), "s1", "中文", "a,b", "<x>");
        // zset
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("中文", 1.5D);
        scores.put("a,b", -2D);
        scores.put("<x>", 0D);
        this.client.zadd(DB, this.k("zset"), scores);
        // hash
        Map<String, String> hash = new LinkedHashMap<>();
        hash.put("字段", "值<>&\"'");
        hash.put("empty", "");
        hash.put("comma", "a,b;c");
        this.client.hmset(DB, this.k("hash"), hash);
        // stream
        Map<String, String> streamFields = new LinkedHashMap<>();
        streamFields.put("f1", "中文<>&");
        streamFields.put("f2", "a,b");
        this.client.xadd(DB, this.k("stream"), null, streamFields);
        // TTL（1 小时，方便复测时仍能观察到）
        this.client.set(DB, this.k("ttl"), "with-ttl");
        this.client.expire(DB, this.k("ttl"), 3600, null);
        // 深层 key
        this.client.set(DB, this.k("deep:path:key"), "deep-value");
        System.out.println("written keys: " + this.client.allKeys(DB, PREFIX + "*").size());
    }

    private void export(String fileType) throws Exception {
        String extension = "excel".equals(fileType) ? "xlsx" : fileType;
        File file = this.outputDir.resolve("redis-retest." + extension).toFile();

        ShellRedisDataExportHandler export = new ShellRedisDataExportHandler();
        export.setClient(this.client);
        export.setDatabase(DB);
        export.setPattern(PREFIX + "*");
        export.setKeyTypes(Arrays.asList("string", "json", "list", "set", "zset", "hash", "stream"));
        export.setRetainTTL(true);
        export.setFileType(fileType);
        export.charset(UTF8.name());
        export.includeTitle(true);
        export.setBatchSize(3);
        export.filePath(file.getPath());
        export.doExport();
        System.out.println("exported " + fileType + " -> " + file.getAbsolutePath() + " (" + file.length() + " bytes)");
    }

    /**
     * 生成一份手写的 JSON 导入样例，含两个会跳过的非法行（空白 key、非法 type）。
     * 导入后会产生 easyshell:retest:manual-1 / manual-2 两个键。
     */
    private void writeManualImportSample() throws Exception {
        File file = this.outputDir.resolve("redis-retest-manual-import.json").toFile();
        String json = "["
                + "{\"key\":\"" + this.k("manual-1") + "\",\"value\":\"手工导入中文 😀\",\"dbIndex\":0,\"type\":\"string\"},"
                + "{\"key\":\"" + this.k("manual-2") + "\",\"value\":\"v2\",\"dbIndex\":0,\"type\":\"string\"},"
                + "{\"key\":\"\",\"value\":\"应跳过\",\"dbIndex\":0,\"type\":\"string\"},"
                + "{\"key\":\"" + this.k("bad-type") + "\",\"value\":\"应跳过\",\"dbIndex\":0,\"type\":\"unknown\"}"
                + "]";
        Files.write(file.toPath(), json.getBytes(UTF8));
        System.out.println("manual import sample -> " + file.getAbsolutePath());
    }
}
