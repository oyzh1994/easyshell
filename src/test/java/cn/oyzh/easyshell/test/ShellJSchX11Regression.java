package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;

/**
 * 验证开启 X11 转发后，非交互式 exec/SFTP 通道不会被 X11 请求影响。
 */
public final class ShellJSchX11Regression {

    private ShellJSchX11Regression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch X11 regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(Integer.getInteger("test.host.index", 1));
        System.out.println("X11 regression target: " + host);
        ShellConnect connect = host.newConnect();
        connect.setX11forwarding(true);
        ShellX11Config x11Config = new ShellX11Config();
        x11Config.setHost("192.0.2.1");
        x11Config.setPort(6010);
        connect.setX11Config(x11Config);

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            require("x11-exec-ok".equals(client.exec("echo x11-exec-ok")), "exec failed with X11 enabled");
            ShellSFTPClient sftp = client.sftpClient();
            require(sftp.lsFile("/").size() > 0, "sftp failed with X11 enabled");
            ShellSSHShell shell = client.openShell();
            require(!shell.isClosed(), "shell failed with X11 enabled");
            shell.close();
            require("x11-after-shell".equals(client.exec("echo x11-after-shell")),
                    "exec failed after opening shell with X11 enabled");
            System.out.println("X11 shell/exec/sftp verified, x11Rejected="
                    + client.isX11ForwardingRejected());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
