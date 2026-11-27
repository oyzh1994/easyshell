package cn.oyzh.easyshell.data.redis.handler;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 真实 Redis 实例上的导入/导出/传输端到端测试。
 *
 * <p>默认连接 127.0.0.1:6379（密码 redis@2026），可用 -Dredis.host / -Dredis.password 覆盖；
 * 连不上时自动跳过。所有键使用随机前缀，且只操作 DB 0/1 中该前缀的键，测试结束清理。</p>
 */
public class ShellRedisDataLiveTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private static final int SRC_DB = 0;

    private static final int DST_DB = 1;

    private String host;

    private String password;

    private ShellRedisClient client;

    private String prefix;

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        this.host = System.getProperty("redis.host", "127.0.0.1:6379");
        this.password = System.getProperty("redis.password", "redis@2026");
        this.tempDir = Files.createTempDirectory("easyshell-redis-live-it");
        ShellConnect connect = new ShellConnect();
        connect.setName("redis-data-live-it");
        connect.setHost(this.host);
        connect.setPassword(this.password);
        connect.setConnectTimeOut(5_000);
        connect.setExecuteTimeOut(5_000);
        connect.setReadonly(false);
        this.client = new ShellRedisClient(connect);
        try {
            this.client.startDatabase(SRC_DB);
            Assume.assumeTrue("Redis 不可用: " + this.host, this.client.isConnected());
        } catch (Throwable ex) {
            this.client.close();
            Assume.assumeNoException("Redis 不可用: " + this.host, ex);
        }
        this.prefix = "easyshell:data-it:" + UUID.randomUUID() + ":";
        this.cleanKeys();
    }

    @After
    public void tearDown() throws Exception {
        if (this.client != null) {
            try {
                if (this.client.isConnected()) {
                    this.cleanKeys();
                }
            } catch (Exception ignore) {
                // 清理失败不影响测试结论
            }
            this.client.close();
        }
        if (this.tempDir != null) {
            try (var paths = Files.walk(this.tempDir)) {
                paths.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignore) {
                        // 清理失败不影响测试结论
                    }
                });
            }
        }
    }

    private void cleanKeys() {
        for (int db : new int[]{SRC_DB, DST_DB}) {
            Set<String> keys = this.client.allKeys(db, this.prefix + "*");
            if (!keys.isEmpty()) {
                this.client.del(db, keys);
            }
        }
    }

    private String k(String suffix) {
        return this.prefix + suffix;
    }

    // ------------------------------------------------------------------
    // 各种奇怪数据：所有 Redis 类型 + 多字节/特殊字符/空值/长文本/二进制
    // ------------------------------------------------------------------

    private List<String> createWeirdData() {
        List<String> keys = new ArrayList<>();
        this.client.set(SRC_DB, this.k("string"), "中文 😀 <tag>&\"'\n第二行\tTab  首尾空格  ");
        keys.add("string");
        this.client.set(SRC_DB, this.k("empty"), "");
        keys.add("empty");
        this.client.set(SRC_DB, this.k("long"), "长".repeat(1500));
        keys.add("long");
        this.client.set(SRC_DB, this.k("binary").getBytes(UTF8), new byte[]{0, 1, 2, (byte) 0xFF, (byte) 0x80, 0});
        keys.add("binary");
        this.client.set(SRC_DB, this.k("中文<>&键"), "unicode-key");
        keys.add("中文<>&键");
        this.client.jsonSet(SRC_DB, this.k("json"), "{\"中文\":\"值<>&\",\"n\":123,\"arr\":[1,2,3]}");
        keys.add("json");
        this.client.lpush(SRC_DB, this.k("list"), "a", "中文,逗号", "<tag>&", "");
        keys.add("list");
        this.client.sadd(SRC_DB, this.k("set"), "s1", "中文", "a,b", "<x>");
        keys.add("set");
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("中文", 1.5D);
        scores.put("a,b", -2D);
        scores.put("<x>", 0D);
        this.client.zadd(SRC_DB, this.k("zset"), scores);
        keys.add("zset");
        Map<String, String> hash = new LinkedHashMap<>();
        hash.put("字段", "值<>&\"'");
        hash.put("empty", "");
        hash.put("comma", "a,b;c");
        this.client.hmset(SRC_DB, this.k("hash"), hash);
        keys.add("hash");
        Map<String, String> streamFields = new LinkedHashMap<>();
        streamFields.put("f1", "中文<>&");
        streamFields.put("f2", "a,b");
        this.client.xadd(SRC_DB, this.k("stream"), null, streamFields);
        keys.add("stream");
        this.client.set(SRC_DB, this.k("ttl"), "with-ttl");
        this.client.expire(SRC_DB, this.k("ttl"), 120, null);
        keys.add("ttl");
        return keys;
    }

    private String snapshot(int db, String key) {
        ShellRedisKey redisKey = ShellRedisKeyUtil.getKey(db, key, false, true, true, this.client);
        return redisKey == null ? null : redisKey.typeName() + "|" + ShellRedisKeyUtil.serializeNode(redisKey);
    }

    private void assertRoundTrip(String fileType, String charsetName) throws Exception {
        Charset charset = Charset.forName(charsetName);
        List<String> suffixes = this.createWeirdData();
        Map<String, String> before = new LinkedHashMap<>();
        for (String suffix : suffixes) {
            before.put(suffix, this.snapshot(SRC_DB, this.k(suffix)));
        }
        long ttlBefore = this.client.ttl(SRC_DB, this.k("ttl"));
        assertTrue("TTL 应已设置", ttlBefore > 0);

        String extension = "excel".equals(fileType) ? "xlsx" : fileType;
        File file = this.tempDir.resolve("roundtrip-" + fileType + "-" + charsetName + "." + extension).toFile();

        ShellRedisDataExportHandler export = new ShellRedisDataExportHandler();
        export.setClient(this.client);
        export.setDatabase(SRC_DB);
        export.setPattern(this.prefix + "*");
        export.setKeyTypes(Arrays.asList("string", "json", "list", "set", "zset", "hash", "stream"));
        export.setRetainTTL(true);
        export.setFileType(fileType);
        export.charset(charsetName);
        export.includeTitle(true);
        export.setBatchSize(3);
        export.filePath(file.getPath());
        export.doExport();
        assertTrue("导出文件应存在且非空: " + file, file.exists() && file.length() > 0);

        this.cleanKeys();

        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType(fileType);
        handler.charset(charsetName);
        handler.setRetainTTL(true);
        if ("excel".equals(fileType) || "csv".equals(fileType)) {
            handler.dataRowStarts(2);
        }
        handler.filePath(file.getPath());
        handler.doImport();

        for (Map.Entry<String, String> entry : before.entrySet()) {
            String key = this.k(entry.getKey());
            assertTrue("键应被重建: " + key, this.client.exists(SRC_DB, key));
            assertEquals("键内容往返不一致: " + key, entry.getValue(), this.snapshot(SRC_DB, key));
        }
        long ttlAfter = this.client.ttl(SRC_DB, this.k("ttl"));
        assertTrue("TTL 应保留，实际=" + ttlAfter, ttlAfter > 0);
    }

    /**
     * 回归：二进制 string 值（含 0x00/0x01/0xFF）导出 XML 时曾被写成裸控制字符，
     * 导致 XML 1.0 解析失败（ParseError Unicode: 0x0）。修复后应输出 0x'hex' 且可无损往返。
     */
    @Test
    public void binaryValueXmlExportIsParseableAndRoundTrips() throws Exception {
        String key = this.k("bin-xml");
        byte[] raw = new byte[]{0, 1, 2, (byte) 0xFF, (byte) 0x80, 10, 13, 9, 0};
        this.client.set(SRC_DB, key.getBytes(UTF8), raw);

        File file = this.tempDir.resolve("binary.xml").toFile();
        ShellRedisDataExportHandler export = new ShellRedisDataExportHandler();
        export.setClient(this.client);
        export.setDatabase(SRC_DB);
        export.setPattern(key);
        export.setKeyTypes(Arrays.asList("string"));
        export.setFileType("xml");
        export.charset(UTF8.name());
        export.includeTitle(true);
        export.filePath(file.getPath());
        export.doExport();

        // XML 文件不能含裸控制字符（除 tab/LF/CR）
        byte[] xml = Files.readAllBytes(file.toPath());
        for (byte b : xml) {
            int c = b & 0xFF;
            assertTrue("导出的 XML 不应含裸控制字符 0x" + Integer.toHexString(c),
                    c == 9 || c == 10 || c == 13 || c >= 0x20);
        }
        String text = new String(xml, UTF8);
        assertTrue("二进制值应编码为 0x'hex'", text.toLowerCase().contains("0x'000102ff800a0d0900'"));

        this.client.del(SRC_DB, key);
        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType("xml");
        handler.charset(UTF8.name());
        handler.filePath(file.getPath());
        handler.doImport();

        assertArrayEquals("二进制值应字节级还原", raw, this.client.get(SRC_DB, key.getBytes(UTF8)));
    }

    @Test
    public void roundTripJson() throws Exception {
        this.assertRoundTrip("json", UTF8.name());
    }

    @Test
    public void roundTripCsv() throws Exception {
        this.assertRoundTrip("csv", UTF8.name());
    }

    @Test
    public void roundTripXml() throws Exception {
        this.assertRoundTrip("xml", UTF8.name());
    }

    @Test
    public void roundTripExcel() throws Exception {
        this.assertRoundTrip("excel", UTF8.name());
    }

    @Test
    public void roundTripGbk() throws Exception {
        this.assertRoundTrip("json", "GBK");
        this.assertRoundTrip("csv", "GBK");
    }

    // ------------------------------------------------------------------
    // 导入边界
    // ------------------------------------------------------------------

    /**
     * Excel 数字单元格会读成 Double，修复前强转 Integer/String 会 ClassCastException。
     */
    @Test
    public void importExcelNumericCells() throws Exception {
        File file = this.tempDir.resolve("numeric.xlsx").toFile();
        String key = this.k("numeric");
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Nodes");
            Row header = sheet.createRow(0);
            String[] titles = {"key", "value", "dbIndex", "type", "ttl"};
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(key);
            row.createCell(1).setCellValue("数字导入");
            row.createCell(2).setCellValue((double) SRC_DB);
            row.createCell(3).setCellValue("string");
            workbook.write(out);
        }

        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType("excel");
        handler.charset(UTF8.name());
        handler.dataRowStarts(2);
        handler.filePath(file.getPath());
        handler.doImport();

        assertTrue("数字 dbIndex 单元格应正确导入", this.client.exists(SRC_DB, key));
        assertEquals("数字导入", this.client.get(SRC_DB, key));
    }

    /**
     * 修复前用固定的 &lt;key&gt;_backup 作备份名，会覆盖用户已有的同名键。
     */
    @Test
    public void importDoesNotOverwriteUserBackupKey() throws Exception {
        String key = this.k("exist");
        String userBackup = key + "_backup";
        this.client.set(SRC_DB, key, "old");
        this.client.set(SRC_DB, userBackup, "user-data");

        File file = this.tempDir.resolve("update.json").toFile();
        Files.write(file.toPath(), Arrays.asList(
                "[{\"key\":\"" + key + "\",\"value\":\"new\",\"dbIndex\":" + SRC_DB + ",\"type\":\"string\"}]"
        ), UTF8);

        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType("json");
        handler.charset(UTF8.name());
        handler.filePath(file.getPath());
        handler.doImport();

        assertEquals("已存在键应更新", "new", this.client.get(SRC_DB, key));
        assertEquals("用户已有的 _backup 键不应被覆盖", "user-data", this.client.get(SRC_DB, userBackup));
    }

    /**
     * 空白 key、非法类型、缺失 dbIndex 应跳过，且计数不能为负。
     */
    @Test
    public void importSkipsInvalidRowsWithoutNegativeCount() throws Exception {
        File file = this.tempDir.resolve("invalid.json").toFile();
        Files.write(file.toPath(), Arrays.asList(
                "[",
                "{\"key\":\"\",\"value\":\"x\",\"dbIndex\":0,\"type\":\"string\"},",
                "{\"key\":\"" + this.k("bad-type") + "\",\"value\":\"x\",\"dbIndex\":0,\"type\":\"unknown\"},",
                "{\"key\":\"" + this.k("no-db") + "\",\"value\":\"x\",\"type\":\"string\"},",
                "{\"key\":\"" + this.k("ok") + "\",\"value\":\"ok\",\"dbIndex\":" + SRC_DB + ",\"type\":\"string\"}",
                "]"
        ), UTF8);

        int[] processed = {0};
        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType("json");
        handler.charset(UTF8.name());
        handler.setProcessedHandler(count -> processed[0] += count);
        handler.filePath(file.getPath());
        handler.doImport();

        assertTrue("计数不应为负，实际=" + processed[0], processed[0] >= 0);
        assertFalse("空白 key 应跳过", this.client.exists(SRC_DB, ""));
        assertFalse("非法类型应跳过", this.client.exists(SRC_DB, this.k("bad-type")));
        assertFalse("缺失 dbIndex 应跳过", this.client.exists(SRC_DB, this.k("no-db")));
        assertEquals("ok", this.client.get(SRC_DB, this.k("ok")));
    }

    @Test
    public void exportInterruptStopsTraversal() throws Exception {
        for (int i = 0; i < 30; i++) {
            this.client.set(SRC_DB, this.k("n" + i), "v" + i);
        }
        ShellRedisDataExportHandler handler = new ShellRedisDataExportHandler();
        handler.setClient(this.client);
        handler.setDatabase(SRC_DB);
        handler.setPattern(this.prefix + "*");
        handler.setKeyTypes(Arrays.asList("string", "json", "list", "set", "zset", "hash", "stream"));
        handler.setFileType("json");
        handler.charset(UTF8.name());
        handler.setBatchSize(1);
        handler.filePath(this.tempDir.resolve("interrupt.json").toString());
        handler.setProcessedHandler(count -> {
            if (count > 0) {
                handler.interrupt();
            }
        });
        try {
            handler.doExport();
            fail("导出中断应抛出异常终止任务");
        } catch (RuntimeException ex) {
            Throwable cause = ex;
            boolean found = false;
            while (cause != null) {
                found |= cause instanceof InterruptedException;
                cause = cause.getCause();
            }
            assertTrue("异常链应包含 InterruptedException", found);
        }
    }

    // ------------------------------------------------------------------
    // 传输（同一实例，源 DB 0 -> 目标 DB 1）
    // ------------------------------------------------------------------

    @Test
    public void transportCreatesAllTypesIncludingBinary() throws Exception {
        List<String> suffixes = this.createWeirdData();
        Map<String, String> before = new LinkedHashMap<>();
        for (String suffix : suffixes) {
            before.put(suffix, this.snapshot(SRC_DB, this.k(suffix)));
        }

        ShellRedisDataTransportHandler handler = new ShellRedisDataTransportHandler();
        handler.setSourceClient(this.client);
        handler.setTargetClient(this.client);
        handler.setSourceDatabase(SRC_DB);
        handler.setTargetDatabase(DST_DB);
        handler.setPattern(this.prefix + "*");
        handler.setKeyTypes(Arrays.asList("string", "json", "list", "set", "zset", "hash", "stream"));
        handler.setExistsPolicy("0");
        handler.setRetainTTL(true);
        handler.doTransport();

        for (Map.Entry<String, String> entry : before.entrySet()) {
            String key = this.k(entry.getKey());
            assertTrue("目标键应创建: " + key, this.client.exists(DST_DB, key));
            assertEquals("传输内容不一致: " + key, entry.getValue(), this.snapshot(DST_DB, key));
        }
        assertTrue("传输应保留 TTL", this.client.ttl(DST_DB, this.k("ttl")) > 0);
    }

    @Test
    public void transportSkipAndUpdatePolicies() throws Exception {
        String key = this.k("policy");
        this.client.set(SRC_DB, key, "new");
        this.client.set(DST_DB, key, "old");

        ShellRedisDataTransportHandler skip = new ShellRedisDataTransportHandler();
        skip.setSourceClient(this.client);
        skip.setTargetClient(this.client);
        skip.setSourceDatabase(SRC_DB);
        skip.setTargetDatabase(DST_DB);
        skip.setPattern(this.prefix + "*");
        skip.setKeyTypes(Arrays.asList("string", "json", "list", "set", "zset", "hash", "stream"));
        skip.setExistsPolicy("0");
        skip.doTransport();
        assertEquals("跳过策略不应覆盖目标键", "old", this.client.get(DST_DB, key));

        ShellRedisDataTransportHandler update = new ShellRedisDataTransportHandler();
        update.setSourceClient(this.client);
        update.setTargetClient(this.client);
        update.setSourceDatabase(SRC_DB);
        update.setTargetDatabase(DST_DB);
        update.setPattern(this.prefix + "*");
        update.setKeyTypes(Arrays.asList("string", "json", "list", "set", "zset", "hash", "stream"));
        update.setExistsPolicy("1");
        update.doTransport();
        assertEquals("更新策略应覆盖目标键", "new", this.client.get(DST_DB, key));
    }

    @Test
    public void transportKeyTypesFilter() throws Exception {
        this.client.set(SRC_DB, this.k("only-string"), "s");
        this.client.lpush(SRC_DB, this.k("only-list"), "l");

        ShellRedisDataTransportHandler handler = new ShellRedisDataTransportHandler();
        handler.setSourceClient(this.client);
        handler.setTargetClient(this.client);
        handler.setSourceDatabase(SRC_DB);
        handler.setTargetDatabase(DST_DB);
        handler.setPattern(this.prefix + "*");
        handler.setKeyTypes(Arrays.asList("string"));
        handler.setExistsPolicy("0");
        handler.doTransport();

        assertTrue("string 键应传输", this.client.exists(DST_DB, this.k("only-string")));
        assertFalse("list 键应被类型过滤", this.client.exists(DST_DB, this.k("only-list")));
    }
}
