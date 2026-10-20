package cn.oyzh.easyshell.test.terminal.zk_v2;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.zk_v2.ZKCommandDescriptor;
import cn.oyzh.easyshell.terminal.zk_v2.ZKCommandExecutor;
import cn.oyzh.easyshell.terminal.zk_v2.ZKCommandRegistry;
import cn.oyzh.easyshell.terminal.zk_v2.ZKCommandTokenizer;
import cn.oyzh.easyshell.zk.ShellZKClient;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 可选的本地 ZooKeeper v2 端到端回归测试。
 */
public class ZKV2LiveRegressionTest {

    @Test
    public void executesDynamicZooKeeperCommands() throws Exception {
        Assume.assumeTrue("Enable with -Dzk.v2.live=true",
                Boolean.getBoolean("zk.v2.live"));
        String host = System.getProperty("zk.host", "127.0.0.1:2181");
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);

        ShellConnect connect = new ShellConnect();
        connect.setName("zk-v2-live");
        connect.setHost(host);
        connect.setConnectTimeOut(5_000);
        connect.setReadonly(false);

        ShellZKClient client = new ShellZKClient(connect);
        ZKCommandExecutor executor = new ZKCommandExecutor();
        String suffix = UUID.randomUUID().toString();
        String root = "/easyshell-zk-v2-regression-" + suffix;
        String child = root + "/child";
        try {
            client.start(5_000);
            assertTrue("ZooKeeper client did not connect", client.isConnected());
            ZooKeeper zooKeeper = client.getZooKeeper();
            assertNotNull(zooKeeper);

            String version = execute(executor, "version", List.of("version"), zooKeeper, hostIp, port);
            assertTrue(version.contains("ZooKeeper CLI version:"));

            execute(executor, "create", List.of("create", root, "initial-" + suffix),
                    zooKeeper, hostIp, port);
            assertNotNull(zooKeeper.exists(root, false));
            String initial = execute(executor, "get", List.of("get", root), zooKeeper, hostIp, port);
            assertTrue(initial.contains("initial-" + suffix));

            execute(executor, "set", List.of("set", root, "updated-" + suffix),
                    zooKeeper, hostIp, port);
            String updated = execute(executor, "get", List.of("get", root), zooKeeper, hostIp, port);
            assertTrue(updated.contains("updated-" + suffix));

            execute(executor, "create", List.of("create", child, "child-" + suffix),
                    zooKeeper, hostIp, port);
            String children = execute(executor, "ls", List.of("ls", root), zooKeeper, hostIp, port);
            assertTrue(children.contains("child"));
            String stat = execute(executor, "stat", List.of("stat", root), zooKeeper, hostIp, port);
            assertTrue(stat, stat.contains("cversion = "));
            String fourLetterStat = execute(executor, "stat", List.of("stat"), zooKeeper, hostIp, port);
            assertTrue(fourLetterStat,
                    fourLetterStat.contains("Zookeeper version:")
                            || fourLetterStat.contains("stat is not executed because it is not in the whitelist"));
            String count = execute(executor, "getAllChildrenNumber",
                    List.of("getAllChildrenNumber", root), zooKeeper, hostIp, port);
            assertTrue(count.contains("1"));

            String fourLetterWord = execute(executor, "ruok", List.of("ruok"), zooKeeper, hostIp, port);
            assertTrue(fourLetterWord,
                    fourLetterWord.contains("imok")
                            || fourLetterWord.contains("ruok is not executed because it is not in the whitelist"));

            execute(executor, "deleteall", List.of("deleteall", root), zooKeeper, hostIp, port);
            assertNull(zooKeeper.exists(root, false));
        } finally {
            if (client.isConnected() && client.getZooKeeper() != null
                    && client.getZooKeeper().exists(root, false) != null) {
                try {
                    execute(executor, "deleteall", List.of("deleteall", root),
                            client.getZooKeeper(), hostIp, port);
                } catch (Exception ignored) {
                }
            }
            client.close();
        }
    }

    private String execute(ZKCommandExecutor executor, String commandName, List<String> args,
                           ZooKeeper zooKeeper, String host, int port) throws Exception {
        ZKCommandDescriptor descriptor = ZKCommandRegistry.find(args).orElseThrow();
        assertEquals(commandName, args.getFirst());
        assertEquals(ZKCommandTokenizer.tokenize(String.join(" ", args)), args);
        return executor.execute(descriptor, args, zooKeeper, host, port);
    }
}
