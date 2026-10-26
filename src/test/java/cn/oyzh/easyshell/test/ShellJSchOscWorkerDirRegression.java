package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.ssh.ShellSSHUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证交互式 shell 能通过 OSC 上报工作目录，并驱动终端的工作目录属性。
 */
public final class ShellJSchOscWorkerDirRegression {

    private static final char ESC = '\u001B';

    private ShellJSchOscWorkerDirRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch OSC worker directory regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("OSC worker directory target: " + host);

        ShellConnect connect = host.newConnect();
        ShellSSHUtil.setEnableOscWorkerDir(connect, true);
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.setResolveWorkerDir(true);
            client.start(15000);

            ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
            StringBuilder output = new StringBuilder();
            AtomicReference<Throwable> readFailure = new AtomicReference<>();
            AtomicBoolean reading = new AtomicBoolean(true);
            Thread reader = new Thread(() -> read(connector, output, reading, readFailure), "osc-reader");
            reader.setDaemon(true);
            try {
                connector.ready();
                reader.start();
                // 首屏输出安静 600ms 后才会安装 hook；安装命令自身还需无回显窗口。
                sleep(2200);
                if (client.isWindows()) {
                    if (client.isPowershellType()) {
                        connector.write("Set-Location C:\\Windows\r");
                    } else {
                        connector.write("cd /c/Windows\r");
                    }
                    verify(client, output, readFailure, "/C:/Windows", 10000);
                } else {
                    connector.write("cd /tmp\r");
                    verify(client, output, readFailure, "/tmp", 10000);

                    connector.write("mkdir -p '/tmp/easyshell osc 100%'\r");
                    connector.write("cd '/tmp/easyshell osc 100%'\r");
                    verify(client, output, readFailure, "/tmp/easyshell osc 100%", 10000);

                    connector.write("cd /tmp\r");
                    connector.write("rmdir '/tmp/easyshell osc 100%'\r");
                }
            } finally {
                reading.set(false);
                connector.close();
                reader.join(2000);
            }
        }
    }

    private static void read(ShellSSHTtyConnector connector, StringBuilder output,
                             AtomicBoolean reading, AtomicReference<Throwable> readFailure) {
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
                readFailure.compareAndSet(null, ex);
            }
        }
    }

    private static void verify(ShellSSHClient client, StringBuilder output,
                               AtomicReference<Throwable> readFailure, String expectedDir,
                               long timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < deadline) {
            Throwable failure = readFailure.get();
            if (failure != null) {
                throw new IllegalStateException("terminal read failed", failure);
            }
            String raw;
            synchronized (output) {
                raw = output.toString();
            }
            boolean oscSeen = raw.contains(ESC + "]7;") || raw.contains(ESC + "]9;9;");
            if (oscSeen && expectedDir.equals(client.workDirProperty().get())) {
                System.out.println("OSC work directory verified: " + expectedDir);
                return;
            }
            sleep(50);
        }
        String raw;
        synchronized (output) {
            raw = output.toString();
        }
        throw new IllegalStateException("OSC work directory was not observed: expected=" + expectedDir
                + ", actual=" + client.workDirProperty().get()
                + ", osc=" + (raw.contains(ESC + "]7;") || raw.contains(ESC + "]9;9;"))
                + ", output=" + visible(raw));
    }

    private static String visible(String value) {
        return value.replace("\u001B", "<ESC>").replace("\u0007", "<BEL>").replace("\r", "<CR>");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
