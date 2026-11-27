package cn.oyzh.easyshell.data.redis.handler;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.args.ExpiryOption;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Redis 导入处理器的内存测试，不依赖外部 Redis 实例。
 * 重点覆盖 Excel 单元格类型、非法行、中断和备份键安全性。
 */
public class ShellRedisDataHandlerTest {

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        this.tempDir = Files.createTempDirectory("easyshell-redis-data-test");
    }

    @After
    public void tearDown() throws Exception {
        if (this.tempDir == null) {
            return;
        }
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

    @Test
    public void importExcelNumericCells() throws Exception {
        File file = this.tempDir.resolve("numeric.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Nodes");
            Row header = sheet.createRow(0);
            String[] titles = {"key", "value", "dbIndex", "type", "ttl"};
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("/redis/numeric");
            row.createCell(1).setCellValue("值");
            // Excel 数字单元格读出来是 Double，修复前强转 Integer 会 ClassCastException
            row.createCell(2).setCellValue(3D);
            row.createCell(3).setCellValue("string");
            workbook.write(out);
        }

        MemoryRedisClient client = new MemoryRedisClient();
        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(client);
        handler.setFileType("excel");
        handler.charset(StandardCharsets.UTF_8.name());
        handler.dataRowStarts(2);
        handler.filePath(file.getPath());
        handler.doImport();

        assertEquals("值", client.get(3, "/redis/numeric"));
    }

    @Test
    public void importSkipsInvalidRowsWithoutNegativeCount() throws Exception {
        File file = this.tempDir.resolve("invalid.json").toFile();
        Files.write(file.toPath(), Arrays.asList(
                "[",
                "{\"key\":\"\",\"value\":\"x\",\"dbIndex\":0,\"type\":\"string\"},",
                "{\"key\":\"/bad-type\",\"value\":\"x\",\"dbIndex\":0,\"type\":\"unknown\"},",
                "{\"key\":\"/no-db\",\"value\":\"x\",\"type\":\"string\"},",
                "{\"key\":\"/ok\",\"value\":\"ok\",\"dbIndex\":2,\"type\":\"string\"}",
                "]"
        ), StandardCharsets.UTF_8);

        MemoryRedisClient client = new MemoryRedisClient();
        int[] processed = {0};
        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(client);
        handler.setFileType("json");
        handler.charset(StandardCharsets.UTF_8.name());
        handler.setProcessedHandler(count -> processed[0] += count);
        handler.filePath(file.getPath());
        handler.doImport();

        assertTrue("计数不应为负，实际=" + processed[0], processed[0] >= 0);
        assertFalse(client.exists(0, "/bad-type"));
        assertFalse(client.exists(0, "/no-db"));
        assertEquals("ok", client.get(2, "/ok"));
    }

    @Test
    public void importInterruptStopsTraversal() throws Exception {
        File file = this.tempDir.resolve("interrupt.json").toFile();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"key\":\"/n").append(i)
                    .append("\",\"value\":\"v").append(i)
                    .append("\",\"dbIndex\":0,\"type\":\"string\"}");
        }
        json.append(']');
        Files.write(file.toPath(), json.toString().getBytes(StandardCharsets.UTF_8));

        MemoryRedisClient client = new MemoryRedisClient();
        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(client);
        handler.setFileType("json");
        handler.charset(StandardCharsets.UTF_8.name());
        handler.filePath(file.getPath());
        handler.setProcessedHandler(count -> {
            if (count > 0) {
                handler.interrupt();
            }
        });
        try {
            handler.doImport();
            fail("导入中断应抛出异常终止任务");
        } catch (Exception ex) {
            Throwable cause = ex;
            boolean found = false;
            while (cause != null) {
                found |= cause instanceof InterruptedException;
                cause = cause.getCause();
            }
            assertTrue("异常链应包含 InterruptedException", found);
        }
        assertTrue("中断后不应继续导入全部键，实际=" + client.size(), client.size() < 20);
    }

    @Test
    public void importUpdateDoesNotOverwriteUserBackup() throws Exception {
        String key = "/exist";
        File file = this.tempDir.resolve("update.json").toFile();
        Files.write(file.toPath(), Arrays.asList(
                "[{\"key\":\"" + key + "\",\"value\":\"new\",\"dbIndex\":0,\"type\":\"string\"}]"
        ), StandardCharsets.UTF_8);

        MemoryRedisClient client = new MemoryRedisClient();
        client.set(0, key, "old");
        client.set(0, key + "_backup", "user-data");
        ShellRedisDataImportHandler handler = new ShellRedisDataImportHandler();
        handler.setClient(client);
        handler.setFileType("json");
        handler.charset(StandardCharsets.UTF_8.name());
        handler.filePath(file.getPath());
        handler.doImport();

        assertEquals("new", client.get(0, key));
        assertEquals("user-data", client.get(0, key + "_backup"));
        assertEquals("临时备份键应被清理", 2, client.size());
    }

    /**
     * 只实现导入处理器使用到的方法。
     */
    private static class MemoryRedisClient extends ShellRedisClient {

        private final Map<String, Map<String, String>> data = new LinkedHashMap<>();

        MemoryRedisClient() {
            super(new ShellConnect());
        }

        int size() {
            return this.data.values().stream().mapToInt(Map::size).sum();
        }

        String get(int db, String key) {
            Map<String, String> dbData = this.data.get(db + "");
            return dbData == null ? null : dbData.get(key);
        }

        @Override
        public boolean exists(Integer dbIndex, String key) {
            Map<String, String> dbData = this.data.get(dbIndex + "");
            return dbData != null && dbData.containsKey(key);
        }

        @Override
        public String set(Integer dbIndex, String key, String value) {
            this.data.computeIfAbsent(dbIndex + "", k -> new LinkedHashMap<>()).put(key, value);
            return "OK";
        }

        @Override
        public String set(Integer dbIndex, byte[] key, byte[] value) {
            return this.set(dbIndex, new String(key, StandardCharsets.UTF_8), new String(value, StandardCharsets.UTF_8));
        }

        @Override
        public String jsonSet(Integer dbIndex, String key, String value) {
            return this.set(dbIndex, key, value);
        }

        @Override
        public long rpush(Integer dbIndex, String key, String... values) {
            return this.set(dbIndex, key, String.join(",", values)) == null ? 0 : values.length;
        }

        @Override
        public long sadd(Integer dbIndex, String key, String... members) {
            return this.set(dbIndex, key, String.join(",", members)) == null ? 0 : members.length;
        }

        @Override
        public long zadd(Integer dbIndex, String key, Map<String, Double> scoreMembers) {
            return this.set(dbIndex, key, String.valueOf(scoreMembers)) == null ? 0 : scoreMembers.size();
        }

        @Override
        public String hmset(Integer dbIndex, String key, Map<String, String> hash) {
            return this.set(dbIndex, key, String.valueOf(hash));
        }

        @Override
        public StreamEntryID xadd(Integer dbIndex, String key, StreamEntryID id, Map<String, String> hash) {
            this.set(dbIndex, key, String.valueOf(hash));
            return id == null ? new StreamEntryID() : id;
        }

        @Override
        public long persist(Integer dbIndex, String key) {
            return 1;
        }

        @Override
        public long expire(Integer dbIndex, String key, long seconds, ExpiryOption option) {
            return 1;
        }

        @Override
        public String rename(Integer dbIndex, String key, String newKey) {
            Map<String, String> dbData = this.data.get(dbIndex + "");
            if (dbData == null || !dbData.containsKey(key)) {
                throw new IllegalStateException("no such key: " + key);
            }
            dbData.put(newKey, dbData.remove(key));
            return "OK";
        }

        @Override
        public long del(Integer dbIndex, String... keys) {
            long count = 0;
            Map<String, String> dbData = this.data.get(dbIndex + "");
            if (dbData != null) {
                for (String key : keys) {
                    if (dbData.remove(key) != null) {
                        count++;
                    }
                }
            }
            return count;
        }
    }
}
