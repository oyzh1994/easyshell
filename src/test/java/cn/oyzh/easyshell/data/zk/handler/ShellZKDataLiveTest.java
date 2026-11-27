package cn.oyzh.easyshell.data.zk.handler;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.util.zk.ShellZKACLUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 真实 ZooKeeper 实例上的导入/导出/传输端到端测试。
 *
 * <p>默认连接 127.0.0.1:2181，可用 -Dzk.host=host:port 覆盖；连不上时自动跳过。
 * 所有数据都写在独立的随机根节点下，测试结束递归清理，不触碰已有节点。</p>
 */
public class ShellZKDataLiveTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private String host;

    private ShellZKClient client;

    private String root;

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        this.host = System.getProperty("zk.host", "127.0.0.1:2181");
        this.tempDir = Files.createTempDirectory("easyshell-zk-live-it");
        ShellConnect connect = new ShellConnect();
        connect.setName("zk-data-live-it");
        connect.setHost(this.host);
        connect.setConnectTimeOut(5_000);
        connect.setReadonly(false);
        this.client = new ShellZKClient(connect);
        try {
            this.client.start(5_000);
            Assume.assumeTrue("ZooKeeper 不可用: " + this.host, this.client.isConnected());
        } catch (Throwable ex) {
            this.client.close();
            Assume.assumeNoException("ZooKeeper 不可用: " + this.host, ex);
        }
        this.root = "/easyshell-data-it-" + UUID.randomUUID();
        this.client.createIncludeParents(this.root, new byte[0], CreateMode.PERSISTENT);
    }

    @After
    public void tearDown() throws Exception {
        if (this.client != null) {
            try {
                if (this.client.isConnected() && this.client.exists(this.root)) {
                    this.deleteRecursive(this.root);
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

    // ------------------------------------------------------------------
    // 导出 -> 导入：各种格式 + 各种奇怪数据
    // ------------------------------------------------------------------

    @Test
    public void roundTripJsonWithWeirdData() throws Exception {
        this.assertRoundTrip("json", "UTF-8", this.weirdData(), false);
    }

    @Test
    public void roundTripCsvWithWeirdData() throws Exception {
        this.assertRoundTrip("csv", "UTF-8", this.weirdData(), false);
    }

    @Test
    public void roundTripXmlWithWeirdData() throws Exception {
        this.assertRoundTrip("xml", "UTF-8", this.weirdData(), false);
    }

    @Test
    public void roundTripExcelWithWeirdData() throws Exception {
        this.assertRoundTrip("excel", "UTF-8", this.weirdData(), false);
    }

    @Test
    public void roundTripGbkChinese() throws Exception {
        Map<String, byte[]> data = new LinkedHashMap<>();
        data.put("gbk-name", "中文节点数据".getBytes(Charset.forName("GBK")));
        data.put("gbk-mixed", "中文 + ASCII 123".getBytes(Charset.forName("GBK")));
        data.put("gbk-empty", new byte[0]);
        this.assertRoundTrip("json", "GBK", data, false);
        this.assertRoundTrip("excel", "GBK", data, false);
    }

    @Test
    public void roundTripAcl() throws Exception {
        this.assertRoundTrip("json", "UTF-8", this.weirdData(), true);
    }

    /**
     * 各种奇怪数据：多字节、emoji、XML/CSV 特殊字符、换行/Tab、首尾空格、
     * 空数据、纯数字、长文本、深层路径、Unicode 路径名。
     */
    private Map<String, byte[]> weirdData() {
        Map<String, byte[]> data = new LinkedHashMap<>();
        data.put("chinese", "中文内容 😀 emoji".getBytes(UTF8));
        data.put("xml-special", "<tag attr=\"x\">&' </tag>".getBytes(UTF8));
        data.put("csv-special", ("逗号,分号;引号\"\n第二行\tTab").getBytes(UTF8));
        data.put("empty", new byte[0]);
        data.put("spaces", "  前后空格  ".getBytes(UTF8));
        data.put("backslash", "C:\\temp\\file / slash".getBytes(UTF8));
        data.put("digits", "1234567890".getBytes(UTF8));
        data.put("long-text", "长".repeat(1500).getBytes(UTF8));
        data.put("deep/a/b/c", "深层节点".getBytes(UTF8));
        data.put("中文节点", "unicode-path".getBytes(UTF8));
        data.put("dot.name", "点号路径名".getBytes(UTF8));
        return data;
    }

    private void assertRoundTrip(String fileType, String charsetName,
                                 Map<String, byte[]> data, boolean withAcl) throws Exception {
        Charset charset = Charset.forName(charsetName);
        String ns = this.root + "/rt-" + fileType + "-" + charsetName + "-" + withAcl;
        this.client.createIncludeParents(ns, new byte[0], CreateMode.PERSISTENT);
        for (Map.Entry<String, byte[]> entry : data.entrySet()) {
            this.client.createIncludeParents(ns + "/" + entry.getKey(), entry.getValue(), CreateMode.PERSISTENT);
        }
        List<ACL> digestAcl = Arrays.asList(new ACL(ZooDefs.Perms.ALL,
                new Id("digest", org.apache.zookeeper.server.auth.DigestAuthenticationProvider
                        .generateDigest("zkdata:secret"))));
        if (withAcl) {
            this.client.getZooKeeper().addAuthInfo("digest", "zkdata:secret".getBytes(UTF8));
            this.client.setACL(ns + "/chinese", digestAcl);
        }

        String extension = "excel".equals(fileType) ? "xlsx" : fileType;
        File file = this.tempDir.resolve("rt-" + fileType + "-" + charsetName + "-" + withAcl + "." + extension).toFile();
        ShellZKDataExportHandler export = new ShellZKDataExportHandler();
        export.setClient(this.client);
        export.setNodePath(ns);
        export.setFileType(fileType);
        export.charset(charsetName);
        export.includeTitle(true);
        export.setIncludeACL(withAcl);
        export.setBatchSize(3);
        export.filePath(file.getPath());
        export.doExport();
        assertTrue("导出文件应存在且非空: " + file, file.exists() && file.length() > 0);

        // 清空命名空间后从文件重建，验证完整往返（根 ns 本身保留）
        for (String child : this.client.getChildren(ns)) {
            this.deleteRecursive(ns + "/" + child);
        }

        ShellZKDataImportHandler handler = new ShellZKDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType(fileType);
        handler.charset(charsetName);
        handler.setIncludeACL(withAcl);
        if ("excel".equals(fileType) || "csv".equals(fileType)) {
            handler.dataRowStarts(2);
        }
        handler.filePath(file.getPath());
        handler.doImport();

        for (Map.Entry<String, byte[]> entry : data.entrySet()) {
            String path = ns + "/" + entry.getKey();
            assertTrue("节点应被重建: " + path, this.client.exists(path));
            assertArrayEquals("数据往返不一致: " + path, entry.getValue(), this.client.getData(path));
        }
        if (withAcl) {
            assertEquals("ACL 字符串应往返一致: " + ns + "/chinese",
                    ShellZKACLUtil.toAclStr(digestAcl),
                    ShellZKACLUtil.toAclStr(this.client.getACL(ns + "/chinese")));
        }
    }

    // ------------------------------------------------------------------
    // 手写文件导入：空路径、非法路径、数字/日期单元格
    // ------------------------------------------------------------------

    @Test
    public void importHandwrittenJsonWithOddRows() throws Exception {
        String ns = this.root + "/odd-json";
        File file = this.tempDir.resolve("odd.json").toFile();
        Files.write(file.toPath(), Arrays.asList(
                "[",
                "  {\"path\":\"\", \"data\":\"ignored\"},",
                "  {\"path\":\"" + ns + "/good\", \"data\":\"正常数据\"},",
                "  {\"path\":\"" + ns + "/bad/\\u0001\", \"data\":\"非法路径\"},",
                "  {\"path\":\"" + ns + "/no-data\"}",
                "]"
        ), UTF8);

        ShellZKDataImportHandler handler = new ShellZKDataImportHandler();
        handler.setClient(this.client);
        handler.setFileType("json");
        handler.charset(UTF8.name());
        handler.filePath(file.getPath());
        handler.doImport();

        assertTrue("正常行应导入", this.client.exists(ns + "/good"));
        assertEquals("正常数据", new String(this.client.getData(ns + "/good"), UTF8));
        assertTrue("缺 data 列应导入为空数据", this.client.exists(ns + "/no-data"));
        assertEquals(0, this.client.getData(ns + "/no-data").length);
        assertEquals("空白路径与非法路径都应跳过", 2, this.client.getChildren(ns).size());
    }

    // ------------------------------------------------------------------
    // 中断
    // ------------------------------------------------------------------

    @Test
    public void exportInterruptStopsTraversalOnLiveZK() throws Exception {
        String ns = this.root + "/interrupt";
        this.client.createIncludeParents(ns, new byte[0], CreateMode.PERSISTENT);
        for (int i = 0; i < 30; i++) {
            this.client.createIncludeParents(ns + "/n" + i, ("v" + i).getBytes(UTF8), CreateMode.PERSISTENT);
        }

        ShellZKDataExportHandler handler = new ShellZKDataExportHandler();
        handler.setClient(this.client);
        handler.setNodePath(ns);
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
    // 传输：源为真实 ShellZKClient，目标为同一实例上的 chroot 视图（真实 ZooKeeper 连接）
    // ------------------------------------------------------------------

    @Test
    public void transportCreatesSubtreeAndConvertsCharset() throws Exception {
        String sourceRoot = this.root + "/transport-src";
        String targetChroot = this.root.substring(1) + "/transport-dst";
        this.client.createIncludeParents("/" + targetChroot, new byte[0], CreateMode.PERSISTENT);

        Charset gbk = Charset.forName("GBK");
        this.client.createIncludeParents(sourceRoot + "/中文-child", "中文内容".getBytes(gbk), CreateMode.PERSISTENT);

        try (ChrootZKClient target = new ChrootZKClient(this.host + "/" + targetChroot)) {
            ShellZKDataTransportHandler handler = new ShellZKDataTransportHandler();
            handler.setSourceClient(this.client);
            handler.setTargetClient(target);
            handler.setNodePath(sourceRoot);
            handler.setExistsPolicy("0");
            handler.setSourceCharset(gbk);
            handler.setTargetCharset(UTF8);
            handler.doTransport();

            String childPath = sourceRoot + "/中文-child";
            assertTrue("目标应创建子节点", target.exists(childPath));
            assertEquals("中文内容", new String(target.getData(childPath), UTF8));
        }
    }

    /**
     * 源/目标字符集相同时，传输不应改写字节：任意二进制数据也要原样保留。
     * （不同字符集之间的转换本身就是有损的，不适用于二进制数据。）
     */
    @Test
    public void transportPreservesBinaryWithSameCharset() throws Exception {
        String sourceRoot = this.root + "/binary-src";
        byte[] binary = new byte[]{0, 1, 2, (byte) 0xFF, (byte) 0x80, 10, 13, 0};
        this.client.createIncludeParents(sourceRoot + "/binary", binary, CreateMode.PERSISTENT);

        String targetChroot = this.root.substring(1) + "/binary-dst";
        this.client.createIncludeParents("/" + targetChroot, new byte[0], CreateMode.PERSISTENT);
        try (ChrootZKClient target = new ChrootZKClient(this.host + "/" + targetChroot)) {
            ShellZKDataTransportHandler handler = new ShellZKDataTransportHandler();
            handler.setSourceClient(this.client);
            handler.setTargetClient(target);
            handler.setNodePath(sourceRoot);
            handler.setExistsPolicy("0");
            handler.setSourceCharset(UTF8);
            handler.setTargetCharset(UTF8);
            handler.doTransport();

            String path = sourceRoot + "/binary";
            assertTrue("目标应创建二进制节点", target.exists(path));
            assertArrayEquals("二进制数据应字节级保留", binary, target.getData(path));
        }
    }

    @Test
    public void transportSkipAndUpdatePolicies() throws Exception {
        String sourceRoot = this.root + "/policy-src";
        this.client.createIncludeParents(sourceRoot + "/same", "new".getBytes(UTF8), CreateMode.PERSISTENT);
        this.client.createIncludeParents(sourceRoot + "/only-source", "src".getBytes(UTF8), CreateMode.PERSISTENT);

        String targetChroot = this.root.substring(1) + "/policy-dst";
        this.client.createIncludeParents("/" + targetChroot, new byte[0], CreateMode.PERSISTENT);
        try (ChrootZKClient target = new ChrootZKClient(this.host + "/" + targetChroot)) {
            target.createIncludeParents(sourceRoot + "/same", "old".getBytes(UTF8), CreateMode.PERSISTENT);

            ShellZKDataTransportHandler skip = new ShellZKDataTransportHandler();
            skip.setSourceClient(this.client);
            skip.setTargetClient(target);
            skip.setNodePath(sourceRoot);
            skip.setExistsPolicy("0");
            skip.setSourceCharset(UTF8);
            skip.setTargetCharset(UTF8);
            skip.doTransport();

            assertEquals("跳过策略不应覆盖已有节点", "old",
                    new String(target.getData(sourceRoot + "/same"), UTF8));
            assertTrue("跳过策略仍应创建新节点", target.exists(sourceRoot + "/only-source"));

            this.client.setData(sourceRoot + "/same", "updated".getBytes(UTF8));
            ShellZKDataTransportHandler update = new ShellZKDataTransportHandler();
            update.setSourceClient(this.client);
            update.setTargetClient(target);
            update.setNodePath(sourceRoot);
            update.setExistsPolicy("1");
            update.setSourceCharset(UTF8);
            update.setTargetCharset(UTF8);
            update.doTransport();

            assertEquals("更新策略应覆盖已有节点", "updated",
                    new String(target.getData(sourceRoot + "/same"), UTF8));
        }
    }

    @Test
    public void transportMissingNodeCounterNonNegative() throws Exception {
        String targetChroot = this.root.substring(1) + "/missing-dst";
        this.client.createIncludeParents("/" + targetChroot, new byte[0], CreateMode.PERSISTENT);
        try (ChrootZKClient target = new ChrootZKClient(this.host + "/" + targetChroot)) {
            int[] processed = {0};
            ShellZKDataTransportHandler handler = new ShellZKDataTransportHandler();
            handler.setSourceClient(this.client);
            handler.setTargetClient(target);
            handler.setNodePath(this.root + "/does-not-exist");
            handler.setExistsPolicy("0");
            handler.setSourceCharset(UTF8);
            handler.setTargetCharset(UTF8);
            handler.setProcessedHandler(count -> processed[0] += count);
            handler.doTransport();
            assertTrue("计数不应为负，实际=" + processed[0], processed[0] >= 0);
        }
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    private void deleteRecursive(String path) throws Exception {
        for (String child : this.client.getChildren(path)) {
            this.deleteRecursive(path + "/" + child);
        }
        this.client.delete(path);
    }

    /**
     * 通过 chroot 连接同一 ZK 实例的另一个根目录，获得真正隔离的目标端。
     * 只实现传输处理器用到的方法。
     */
    private static final class ChrootZKClient extends ShellZKClient implements AutoCloseable {

        private final ZooKeeper zooKeeper;

        ChrootZKClient(String connectString) throws Exception {
            super(new ShellConnect());
            this.zooKeeper = new ZooKeeper(connectString, 8_000, (Watcher) event -> {
            });
            long deadline = System.currentTimeMillis() + 8_000;
            while (this.zooKeeper.getState() != ZooKeeper.States.CONNECTED
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            if (this.zooKeeper.getState() != ZooKeeper.States.CONNECTED) {
                throw new IllegalStateException("无法连接目标 chroot: " + connectString);
            }
        }

        @Override
        public boolean exists(String path) {
            try {
                return this.zooKeeper.exists(path, false) != null;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Stat checkExists(String path) throws Exception {
            return this.zooKeeper.exists(path, false);
        }

        @Override
        public byte[] getData(String path) throws Exception {
            return this.zooKeeper.getData(path, false, new Stat());
        }

        @Override
        public List<String> getChildren(String path) throws Exception {
            return this.zooKeeper.getChildren(path, false);
        }

        @Override
        public Stat setData(String path, byte[] data) throws Exception {
            return this.zooKeeper.setData(path, data, -1);
        }

        @Override
        public String createIncludeParents(String path, byte[] data, CreateMode createMode) throws Exception {
            String[] parts = path.split("/");
            StringBuilder current = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (parts[i].isEmpty()) {
                    continue;
                }
                current.append('/').append(parts[i]);
                boolean last = i == parts.length - 1;
                if (!this.exists(current.toString())) {
                    try {
                        this.zooKeeper.create(current.toString(), last ? data : new byte[0],
                                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                last && createMode != null ? createMode : CreateMode.PERSISTENT);
                    } catch (org.apache.zookeeper.KeeperException.NodeExistsException ignore) {
                        // 并发创建，忽略
                    }
                }
            }
            return path;
        }

        @Override
        public void close() {
            try {
                this.zooKeeper.close();
            } catch (Exception ignore) {
                // 忽略
            }
            super.close();
        }
    }
}
