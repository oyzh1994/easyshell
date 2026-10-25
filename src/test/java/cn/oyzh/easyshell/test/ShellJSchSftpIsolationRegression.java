package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellBaseSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 验证 SFTP 与控制命令使用独立 SSH 会话，且长连接流关闭后立即归还通道。
 */
public final class ShellJSchSftpIsolationRegression {

    private ShellJSchSftpIsolationRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("SFTP isolation regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(Integer.getInteger("test.host.index", 6));
        int contentSize = Integer.getInteger("test.sftp.stream.size", 2 * 1024 * 1024);
        byte[] content = new byte[contentSize];
        new Random(contentSize).nextBytes(content);
        String remote = null;
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(value -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15_000);
            ShellSFTPClient sftp = client.sftpClient();
            String remoteDir = client.isWindows() ? client.getUserHome() : "/tmp";
            cleanupStaleFiles(sftp, remoteDir);
            remote = ShellFileUtil.concat(remoteDir, "easyshell-sftp-isolation-" + UUID.randomUUID());
            AtomicLong uploadProgress = new AtomicLong();
            try (OutputStream output = sftp.putStream(remote, length -> {
                uploadProgress.addAndGet(length);
                return true;
            })) {
                output.write(content);
            }
            require(uploadProgress.get() == content.length, "stream upload progress mismatch");
            require(delayChannels(sftp).isEmpty(), "closed upload stream leaked its SFTP channel");
            require(!client.isTwoFactorAuthenticationUsed(), "non-2FA host was marked as 2FA");
            require(sftp.getSession() != client.getSession(), "SFTP still shares the control SSH session");

            ShellSFTPFile file = sftp.fileInfo(remote);
            int permitsBefore = availablePermits(sftp);
            AtomicLong downloadProgress = new AtomicLong();
            byte[] downloaded;
            try (InputStream input = sftp.getStream(file, length -> {
                downloadProgress.addAndGet(length);
                return true;
            })) {
                require(client.exec("echo control-ok", 3_000).equals("control-ok"),
                        "control command failed while SFTP stream was open");
                downloaded = input.readAllBytes();
            }
            require(java.util.Arrays.equals(content, downloaded), "stream download content mismatch");
            require(downloadProgress.get() == content.length, "stream download progress mismatch");
            require(delayChannels(sftp).isEmpty(), "closed download stream leaked its SFTP channel");
            require(availablePermits(sftp) == permitsBefore, "closed download stream leaked a dynamic channel permit");
            sftp.getSession().disconnect();
            require(sftp.exist(remote), "SFTP did not reconnect its dedicated session");
            require(client.exec("echo control-after-reconnect", 3_000).equals("control-after-reconnect"),
                    "control session was affected by SFTP reconnect");
            sftp.delete(remote);
            remote = null;
        } finally {
            if (remote != null) {
                cleanup(host, remote);
            }
        }
    }

    private static void cleanupStaleFiles(ShellSFTPClient sftp, String remoteDir) throws Exception {
        for (ShellSFTPFile file : sftp.lsFile(remoteDir)) {
            if (!file.isDirectory() && file.getFileName().startsWith("easyshell-sftp-isolation-")) {
                sftp.delete(file.getFilePath());
            }
        }
    }

    private static void cleanup(ShellJSchTestHosts.Host host, String remote) {
        try (ShellSSHClient cleanup = new ShellSSHClient(host.newConnect())) {
            cleanup.setVerifyFailureCallback(value -> null);
            ShellJSchTestHosts.acceptHostKeys(cleanup);
            cleanup.start(15_000);
            ShellSFTPClient sftp = cleanup.sftpClient();
            if (sftp.exist(remote)) {
                sftp.delete(remote);
            }
        } catch (Exception ignored) {
        }
    }

    private static Queue<?> delayChannels(ShellSFTPClient client) throws Exception {
        Field field = ShellSFTPClient.class.getDeclaredField("delayChannels");
        field.setAccessible(true);
        return (Queue<?>) field.get(client);
    }

    private static int availablePermits(ShellSFTPClient client) throws Exception {
        Field field = ShellBaseSSHClient.class.getDeclaredField("dynamicChannelPermits");
        field.setAccessible(true);
        return ((Semaphore) field.get(client)).availablePermits();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
