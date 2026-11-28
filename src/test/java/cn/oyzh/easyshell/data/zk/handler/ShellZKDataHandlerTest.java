package cn.oyzh.easyshell.data.zk.handler;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.zk.ShellZKClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * zk 数据导入/导出/传输处理器测试。
 * 使用内存客户端替代真实 zk 连接，重点验证文件内容往返、字符集、类型兼容与中断行为。
 */
public class ShellZKDataHandlerTest {

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        this.tempDir = Files.createTempDirectory("easyshell-zk-data-test");
    }

    @After
    public void tearDown() throws Exception {
        if (this.tempDir == null) {
            return;
        }
        try (var paths = Files.walk(this.tempDir)) {
            paths.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignore) {
                            // 清理失败不影响测试结论
                        }
                    });
        }
    }

    // ------------------------------------------------------------------
    // 导出 -> 导入 往返（各文件类型）
    // ------------------------------------------------------------------

    @Test
    public void roundTripJson() throws Exception {
        this.assertRoundTrip("json", StandardCharsets.UTF_8.name(), "中文内容", "line1\nline2", "123");
    }

    @Test
    public void roundTripCsv() throws Exception {
        this.assertRoundTrip("csv", StandardCharsets.UTF_8.name(), "中文内容", "plain-value", "123");
    }

    @Test
    public void roundTripXml() throws Exception {
        this.assertRoundTrip("xml", StandardCharsets.UTF_8.name(), "中文内容", "plain-value", "123");
    }

    @Test
    public void roundTripExcel() throws Exception {
        this.assertRoundTrip("excel", StandardCharsets.UTF_8.name(), "中文内容", "plain-value", "456");
    }

    /**
     * base-store 的 XML 写入器不转义 &lt; &gt; &amp;，导出端必须先转义。
     */
    @Test
    public void roundTripXmlWithSpecialCharacters() throws Exception {
        String special = "<tag attr=\"x\">&' </tag>";
        this.assertRoundTrip("xml", StandardCharsets.UTF_8.name(), special, "换行\n第二行", "123");
    }

    /**
     * 非 UTF-8 字符集往返：修复前导入把字符集方向写反，这里会失败。
     */
    @Test
    public void roundTripWithGbkCharset() throws Exception {
        Charset gbk = Charset.forName("GBK");
        MemoryZKClient source = new MemoryZKClient();
        source.put("/app/name", "中文内容".getBytes(gbk));
        source.put("/app/empty", new byte[0]);

        File file = this.tempDir.resolve("zk-gbk.json").toFile();
        ShellZKDataExportHandler export = new ShellZKDataExportHandler();
        export.setClient(source);
        export.setNodePath("/");
        export.setFileType("json");
        export.charset("GBK");
        export.filePath(file.getPath());
        export.doAction();

        MemoryZKClient target = new MemoryZKClient();
        ShellZKDataImportHandler importHandler = new ShellZKDataImportHandler();
        importHandler.setClient(target);
        importHandler.setFileType("json");
        importHandler.charset("GBK");
        importHandler.filePath(file.getPath());
        importHandler.doAction();

        assertArrayEquals("中文内容".getBytes(gbk), target.get("/app/name"));
        assertArrayEquals(new byte[0], target.get("/app/empty"));
    }

    /**
     * Excel 数字单元格读取为 Double，修复前强转 String 会 ClassCastException。
     */
    @Test
    public void importExcelNumericCell() throws Exception {
        File file = this.tempDir.resolve("numeric.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Nodes");
            Row row = sheet.createRow(0);
            Cell pathCell = row.createCell(0);
            pathCell.setCellValue("/numeric");
            Cell dataCell = row.createCell(1);
            dataCell.setCellValue(123D);
            workbook.write(out);
        }

        MemoryZKClient target = new MemoryZKClient();
        ShellZKDataImportHandler handler = new ShellZKDataImportHandler();
        handler.setClient(target);
        handler.setFileType("excel");
        handler.charset(StandardCharsets.UTF_8.name());
        handler.dataRowStarts(1);
        handler.filePath(file.getPath());
        handler.doAction();

        assertEquals("123", new String(target.get("/numeric"), StandardCharsets.UTF_8));
    }

    /**
     * 已存在节点勾选包含 ACL 时，修复前只更新数据不更新 ACL。
     */
    @Test
    public void importUpdatesAclForExistingNode() throws Exception {
        Charset utf8 = StandardCharsets.UTF_8;
        MemoryZKClient client = new MemoryZKClient();
        client.put("/app/exist", "old".getBytes(utf8));
        client.setAcls("/app/exist", ZooDefs.Ids.READ_ACL_UNSAFE);

        File file = this.tempDir.resolve("acl.json").toFile();
        Files.write(file.toPath(), Arrays.asList(
                "[",
                "  {",
                "    \"path\" : \"/app/exist\",",
                "    \"data\" : \"new\",",
                "    \"acl\" : \"world:anyone:rw\"",
                "  }",
                "]"
        ), utf8);

        ShellZKDataImportHandler handler = new ShellZKDataImportHandler();
        handler.setClient(client);
        handler.setFileType("json");
        handler.charset(utf8.name());
        handler.setIncludeACL(true);
        handler.filePath(file.getPath());
        handler.doAction();

        assertEquals("new", new String(client.get("/app/exist"), utf8));
        assertEquals("world:anyone:wr", cn.oyzh.easyshell.util.zk.ShellZKACLUtil.toAclStr(client.getAcls("/app/exist")));
    }

    /**
     * 中断必须终止整棵树的遍历，修复前只跳过写入但仍会读取全部节点。
     */
    @Test
    public void exportInterruptStopsTraversal() throws Exception {
        MemoryZKClient source = new MemoryZKClient();
        for (int i = 0; i < 30; i++) {
            source.put("/app/n" + i, ("v" + i).getBytes(StandardCharsets.UTF_8));
        }

        ShellZKDataExportHandler handler = new ShellZKDataExportHandler();
        handler.setClient(source);
        handler.setNodePath("/");
        handler.setFileType("json");
        handler.charset(StandardCharsets.UTF_8.name());
        handler.filePath(this.tempDir.resolve("interrupt.json").toString());
        handler.setBatchSize(1);
        handler.setProcessedHandler(count -> {
            if (count > 0) {
                handler.interrupt();
            }
        });

        try {
            handler.doAction();
            fail("导出中断应抛出异常以终止任务");
        } catch (RuntimeException ex) {
            assertTrue("异常链中应包含 InterruptedException", hasInterruptedException(ex));
        }
        assertTrue("中断后不应继续读取全部节点，实际读取数=" + source.getDataReads(),
                source.getDataReads() < 30);
    }

    // ------------------------------------------------------------------
    // 传输
    // ------------------------------------------------------------------

    @Test
    public void transportCopiesSubtreeWithSkipPolicy() throws Exception {
        MemoryZKClient source = new MemoryZKClient();
        source.put("/app/a", "new-a".getBytes(StandardCharsets.UTF_8));
        source.put("/app/b", "b".getBytes(StandardCharsets.UTF_8));
        source.putEphemeral("/app/ephemeral", "e".getBytes(StandardCharsets.UTF_8));

        MemoryZKClient target = new MemoryZKClient();
        target.put("/app/a", "old-a".getBytes(StandardCharsets.UTF_8));

        ShellZKDataTransportHandler handler = new ShellZKDataTransportHandler();
        handler.setSourceClient(source);
        handler.setTargetClient(target);
        handler.setNodePath("/app");
        handler.setExistsPolicy("0");
        handler.setSourceCharset(StandardCharsets.UTF_8);
        handler.setTargetCharset(StandardCharsets.UTF_8);
        handler.doAction();

        assertEquals("old-a", new String(target.get("/app/a"), StandardCharsets.UTF_8));
        assertEquals("b", new String(target.get("/app/b"), StandardCharsets.UTF_8));
        assertFalse("临时节点不应被传输", target.exists("/app/ephemeral"));
    }

    @Test
    public void transportUpdatesExistingNodeWithUpdatePolicy() throws Exception {
        MemoryZKClient source = new MemoryZKClient();
        source.put("/app/a", "new-a".getBytes(StandardCharsets.UTF_8));
        MemoryZKClient target = new MemoryZKClient();
        target.put("/app/a", "old-a".getBytes(StandardCharsets.UTF_8));

        ShellZKDataTransportHandler handler = new ShellZKDataTransportHandler();
        handler.setSourceClient(source);
        handler.setTargetClient(target);
        handler.setNodePath("/app");
        handler.setExistsPolicy("1");
        handler.setSourceCharset(StandardCharsets.UTF_8);
        handler.setTargetCharset(StandardCharsets.UTF_8);
        handler.doAction();

        assertEquals("new-a", new String(target.get("/app/a"), StandardCharsets.UTF_8));
    }

    /**
     * 源节点不存在时计数不应变成负数。
     */
    @Test
    public void transportMissingNodeDoesNotDecrementCounter() throws Exception {
        MemoryZKClient source = new MemoryZKClient();
        MemoryZKClient target = new MemoryZKClient();
        int[] processed = {0};
        ShellZKDataTransportHandler handler = new ShellZKDataTransportHandler();
        handler.setSourceClient(source);
        handler.setTargetClient(target);
        handler.setNodePath("/not-exists");
        handler.setExistsPolicy("0");
        handler.setSourceCharset(StandardCharsets.UTF_8);
        handler.setTargetCharset(StandardCharsets.UTF_8);
        handler.setProcessedHandler(count -> processed[0] += count);
        handler.doAction();

        assertTrue("计数不应为负，实际=" + processed[0], processed[0] >= 0);
    }

    // ------------------------------------------------------------------
    // 辅助方法
    // ------------------------------------------------------------------

    private void assertRoundTrip(String fileType, String charsetName, String... values) throws Exception {
        Charset charset = Charset.forName(charsetName);
        MemoryZKClient source = new MemoryZKClient();
        source.put("/app/name", values[0].getBytes(charset));
        source.put("/app/desc", values[1].getBytes(charset));
        source.put("/app/num", values[2].getBytes(charset));

        String extension = "excel".equals(fileType) ? "xlsx" : fileType;
        File file = this.tempDir.resolve("roundtrip-" + fileType + "." + extension).toFile();

        ShellZKDataExportHandler export = new ShellZKDataExportHandler();
        export.setClient(source);
        export.setNodePath("/");
        export.setFileType(fileType);
        export.charset(charsetName);
        export.includeTitle(true);
        export.filePath(file.getPath());
        export.doAction();
        assertTrue("导出文件应存在且非空", file.exists() && file.length() > 0);

        MemoryZKClient target = new MemoryZKClient();
        ShellZKDataImportHandler importHandler = new ShellZKDataImportHandler();
        importHandler.setClient(target);
        importHandler.setFileType(fileType);
        importHandler.charset(charsetName);
        if ("excel".equals(fileType) || "csv".equals(fileType)) {
            importHandler.dataRowStarts(2);
        }
        importHandler.filePath(file.getPath());
        importHandler.doAction();

        assertArrayEquals(values[0].getBytes(charset), target.get("/app/name"));
        assertArrayEquals(values[1].getBytes(charset), target.get("/app/desc"));
        assertArrayEquals(values[2].getBytes(charset), target.get("/app/num"));
    }

    private static boolean hasInterruptedException(Throwable ex) {
        while (ex != null) {
            if (ex instanceof InterruptedException) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    /**
     * 内存版 zk 客户端，只实现导入/导出/传输使用到的方法。
     */
    private static class MemoryZKClient extends ShellZKClient {

        private final Map<String, byte[]> data = new TreeMap<>();
        private final Map<String, List<ACL>> acls = new HashMap<>();
        private final Set<String> ephemeral = new HashSet<>();
        private int dataReads;

        MemoryZKClient() {
            super(new ShellConnect());
            this.data.put("/", new byte[0]);
            this.acls.put("/", ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }

        void put(String path, byte[] bytes) {
            this.createParents(path);
            this.data.put(path, bytes == null ? new byte[0] : bytes);
            this.acls.put(path, ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }

        void putEphemeral(String path, byte[] bytes) {
            this.put(path, bytes);
            this.ephemeral.add(path);
        }

        void setAcls(String path, List<ACL> aclList) {
            this.acls.put(path, aclList);
        }

        byte[] get(String path) {
            return this.data.get(path);
        }

        List<ACL> getAcls(String path) {
            return this.acls.get(path);
        }

        int getDataReads() {
            return this.dataReads;
        }

        private void createParents(String path) {
            if (path == null || !path.startsWith("/")) {
                return;
            }
            String[] parts = path.split("/");
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (parts[i].isEmpty()) {
                    continue;
                }
                builder.append('/').append(parts[i]);
                String parent = builder.toString();
                this.data.putIfAbsent(parent, new byte[0]);
                this.acls.putIfAbsent(parent, ZooDefs.Ids.OPEN_ACL_UNSAFE);
            }
        }

        @Override
        public boolean exists(String path) {
            return this.data.containsKey(path);
        }

        @Override
        public Stat checkExists(String path) {
            if (!this.data.containsKey(path)) {
                return null;
            }
            Stat stat = new Stat();
            stat.setEphemeralOwner(this.ephemeral.contains(path) ? 1L : 0L);
            return stat;
        }

        @Override
        public byte[] getData(String path) throws KeeperException.NoNodeException {
            this.dataReads++;
            byte[] value = this.data.get(path);
            if (value == null) {
                // 与真实 ZooKeeper 行为一致：节点不存在时抛异常而不是返回 null
                throw new KeeperException.NoNodeException(path);
            }
            return value;
        }

        @Override
        public List<String> getChildren(String path) {
            String prefix = path.endsWith("/") ? path : path + "/";
            List<String> children = new ArrayList<>();
            for (String key : this.data.keySet()) {
                if (!key.startsWith(prefix) || key.equals(path)) {
                    continue;
                }
                String rest = key.substring(prefix.length());
                if (!rest.isEmpty() && !rest.contains("/")) {
                    children.add(rest);
                }
            }
            return children;
        }

        @Override
        public List<ACL> getACL(String path) {
            return this.acls.getOrDefault(path, ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }

        @Override
        public Stat setData(String path, byte[] bytes, Integer version) {
            this.data.put(path, bytes == null ? new byte[0] : bytes);
            return new Stat();
        }

        @Override
        public Stat setACL(String path, List<ACL> aclList, Integer version) {
            this.acls.put(path, aclList);
            return new Stat();
        }

        @Override
        public String create(String path, byte[] bytes, List<ACL> aclList, Long ttl, CreateMode mode, boolean createParents) {
            if (createParents) {
                this.createParents(path);
            }
            this.data.put(path, bytes == null ? new byte[0] : bytes);
            this.acls.put(path, aclList == null ? ZooDefs.Ids.OPEN_ACL_UNSAFE : aclList);
            if (mode != null && mode.isEphemeral()) {
                this.ephemeral.add(path);
            }
            return path;
        }
    }
}
