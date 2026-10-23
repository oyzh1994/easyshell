package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;

import java.util.UUID;

/** 验证 ShellSSHTtyConnector 的 read 重写在非 Windows 主机上保持原行为。 */
public final class ShellJSchTtyConnectorReadRegression {

    private ShellJSchTtyConnectorReadRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch tty connector read regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("TTY connector target: " + host);
        String marker = "__EASYSHELL_TTY_READ_" + UUID.randomUUID().toString().replace("-", "") + "__";

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
            try {
                connector.ready();
                connector.write("printf '" + marker + "\\n'\r");
                StringBuilder output = new StringBuilder();
                char[] buffer = new char[1024];
                long deadline = System.currentTimeMillis() + 10000;
                while (System.currentTimeMillis() < deadline && output.indexOf(marker) < 0) {
                    int read = connector.read(buffer, 0, buffer.length);
                    if (read < 0) {
                        break;
                    }
                    output.append(buffer, 0, read);
                }
                require(output.indexOf(marker) >= 0, "TTY connector did not return command output");
                System.out.println("TTY connector read verified: chars=" + output.length());
            } finally {
                connector.close();
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
