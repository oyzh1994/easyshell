package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证终端原位刷新会创建新的远端 shell，同时保留共享 SSH 客户端。
 */
public final class ShellJSchTerminalRefreshRegression {

    private ShellJSchTerminalRefreshRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch terminal refresh regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("terminal refresh target: " + host);

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);

            ShellSSHTtyConnector oldConnector = new ShellSSHTtyConnector(client);
            AtomicBoolean oldReading = new AtomicBoolean(true);
            AtomicReference<Throwable> oldFailure = new AtomicReference<>();
            StringBuilder oldOutput = new StringBuilder();
            Thread oldReader = null;
            try {
                oldConnector.ready();
                oldReader = startReader(oldConnector, oldOutput, oldReading, oldFailure);
                ShellSSHShell oldShell = client.getShell();
                String oldPid = readShellPid(client, oldConnector, oldOutput, oldFailure, "__REFRESH_OLD_");

                // 与控制器刷新顺序一致：先解除旧连接器的客户端所有权，再重建共享客户端会话。
                oldConnector.detachClient();
                client.reconnect();
                require(client.isConnected(), "shared SSH client was not reconnected");
                boolean detachedReadyRejected = false;
                try {
                    oldConnector.ready();
                } catch (IOException ex) {
                    detachedReadyRejected = true;
                }
                require(detachedReadyRejected, "detached connector rebound to the new shared shell");

                ShellSSHTtyConnector newConnector = new ShellSSHTtyConnector(client);
                AtomicBoolean newReading = new AtomicBoolean(true);
                AtomicReference<Throwable> newFailure = new AtomicReference<>();
                StringBuilder newOutput = new StringBuilder();
                newConnector.ready();
                ShellSSHShell newShell = client.getShell();
                require(newShell != null && newShell != oldShell, "shell was not reopened");
                Thread newReader = startReader(newConnector, newOutput, newReading, newFailure);
                try {
                    oldConnector.close();
                    require(client.isConnected(), "old connector closed the shared SSH client");
                    String newPid = readShellPid(client, newConnector, newOutput, newFailure, "__REFRESH_NEW_");
                    if (oldPid != null) {
                        require(!oldPid.equals(newPid), "terminal refresh reused the old shell, pid=" + oldPid);
                    }

                    String marker = "__EASYSHELL_REFRESH_EXEC_" + UUID.randomUUID().toString().replace("-", "") + "__";
                    String execOutput = client.exec("echo " + marker);
                    require(execOutput != null && execOutput.contains(marker), "shared client exec failed after refresh");
                    ShellSFTPClient sftp = client.sftpClient();
                    require(sftp.exist(client.getUserHome()), "SFTP failed after refresh");
                    System.out.println("shell refreshed: oldPid=" + oldPid + ", newPid=" + newPid);
                } finally {
                    newReading.set(false);
                    newConnector.close();
                    newReader.join(2000);
                }
            } finally {
                oldReading.set(false);
                oldConnector.close();
                if (oldReader != null) {
                    oldReader.join(2000);
                }
            }
        }
    }

    private static Thread startReader(ShellSSHTtyConnector connector, StringBuilder output,
                                      AtomicBoolean reading, AtomicReference<Throwable> failure) {
        Thread thread = new Thread(() -> {
            char[] buffer = new char[4096];
            try {
                while (reading.get()) {
                    int length = connector.read(buffer, 0, buffer.length);
                    if (length < 0) {
                        return;
                    }
                    if (length > 0) {
                        synchronized (output) {
                            output.append(buffer, 0, length);
                        }
                    }
                }
            } catch (Throwable ex) {
                if (reading.get()) {
                    failure.compareAndSet(null, ex);
                }
            }
        }, "terminal-refresh-reader");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static String readShellPid(ShellSSHClient client, ShellSSHTtyConnector connector, StringBuilder output,
                                       AtomicReference<Throwable> failure, String prefix) throws Exception {
        boolean posixShell = !client.isWindows() || client.isWindowsPosixShellType();
        if (!posixShell && !client.isPowershellType()) {
            return null;
        }
        String marker = prefix + UUID.randomUUID().toString().replace("-", "") + "__";
        if (client.isPowershellType()) {
            connector.write("Write-Output ('" + marker + "=' + $PID)\r");
        } else {
            connector.write("printf '" + marker + "=%s\\n' $$\r");
        }
        Pattern pattern = Pattern.compile(Pattern.quote(marker) + "=(\\d+)");
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            Throwable current = failure.get();
            if (current != null) {
                throw new IllegalStateException("terminal read failed", current);
            }
            String value;
            synchronized (output) {
                value = output.toString();
            }
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                return matcher.group(1);
            }
            Thread.sleep(30);
        }
        throw new IllegalStateException("shell pid was not observed: " + marker);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
