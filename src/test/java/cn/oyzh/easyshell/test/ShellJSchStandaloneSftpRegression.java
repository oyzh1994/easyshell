package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.util.ShellClientUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

/**
 * 验证不经过 SSH 终端入口的独立 SFTP 客户端仍具备完整的会话重连与流式传输能力。
 */
public final class ShellJSchStandaloneSftpRegression {

    private ShellJSchStandaloneSftpRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("standalone SFTP regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(Integer.getInteger("test.host.index", 1));
        ShellConnect connect = host.newConnect();
        connect.setType("sftp");
        byte[] content = new byte[Integer.getInteger("test.sftp.stream.size", 1024 * 1024)];
        new Random(content.length).nextBytes(content);
        String remote = ShellFileUtil.concat("/tmp", "easyshell-standalone-sftp-" + UUID.randomUUID());
        ShellSFTPClient client = ShellClientUtil.newClient(connect);
        client.setVerifyFailureCallback(value -> null);
        ShellJSchTestHosts.acceptHostKeys(client);
        try {
            client.start(15_000);
            require(client.isConnected(), "standalone SFTP client is not connected");
            require(client.sftpClient() == client, "standalone SFTP client derived another file client");
            try (OutputStream output = client.putStream(remote, null)) {
                output.write(content);
            }
            require(client.exist(remote), "standalone SFTP upload was not found");

            ShellSFTPFile file = client.fileInfo(remote);
            byte[] downloaded;
            try (InputStream input = client.getStream(file, null)) {
                downloaded = input.readAllBytes();
            }
            require(java.util.Arrays.equals(content, downloaded), "standalone SFTP content mismatch");

            client.getSession().disconnect();
            require(client.exist(remote), "standalone SFTP session did not reconnect");
            client.delete(remote);
            remote = null;
        } finally {
            client.close();
            if (remote != null) {
                cleanup(host, remote);
            }
        }
    }

    private static void cleanup(ShellJSchTestHosts.Host host, String remote) {
        ShellConnect connect = host.newConnect();
        connect.setType("sftp");
        try (ShellSFTPClient cleanup = new ShellSFTPClient(connect)) {
            cleanup.setVerifyFailureCallback(value -> null);
            ShellJSchTestHosts.acceptHostKeys(cleanup);
            cleanup.start(15_000);
            if (cleanup.exist(remote)) {
                cleanup.delete(remote);
            }
        } catch (Exception ignored) {
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
