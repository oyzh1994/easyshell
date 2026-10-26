package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;

/**
 * 测量 SSH 连接、终端就绪和首屏首包的耗时，用于排查交互式终端启动性能。
 */
public final class ShellSSHFirstScreenProfile {

    private ShellSSHFirstScreenProfile() {
    }

    public static void main(String[] args) throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 6);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);

            long start = System.nanoTime();
            client.start(15000);
            long connected = System.nanoTime();

            ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
            long connectorCreated = System.nanoTime();
            connector.ready();
            long ready = System.nanoTime();

            char[] buffer = new char[16 * 1024];
            int length = connector.read(buffer, 0, buffer.length);
            long firstRead = System.nanoTime();

            System.out.printf("host[%d] connect=%dms ready=%dms firstRead=%dms chars=%d%n",
                    hostIndex,
                    elapsedMillis(start, connected),
                    elapsedMillis(connectorCreated, ready),
                    elapsedMillis(ready, firstRead),
                    length);
            System.out.println(new String(buffer, 0, Math.max(0, length)).replace("\u001b", "<ESC>"));
            connector.close();
            System.exit(0);
        }
    }

    private static long elapsedMillis(long start, long end) {
        return (end - start) / 1_000_000;
    }
}
