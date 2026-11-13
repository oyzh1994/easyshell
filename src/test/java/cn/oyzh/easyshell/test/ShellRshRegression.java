package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.rsh.ShellRshClient;
import cn.oyzh.easyshell.rsh.ShellRshTtyConnector;
import cn.oyzh.easyshell.util.ShellClientUtil;
import com.jediterm.core.util.TermSize;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 使用本地 rsh 容器验证交互式 shell。
 */
public final class ShellRshRegression {

    private static final Pattern SHELL_PROMPT = Pattern.compile("(?m)^[^\\r\\n]*[#$%>]\\s*$");

    private ShellRshRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("rsh regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setHost(System.getProperty("test.rsh.host", "127.0.0.1:1514"));
        connect.setUser(System.getProperty("test.rsh.user", "root"));
        connect.setTermType("xterm");
        connect.setCharset(StandardCharsets.UTF_8.name());
        connect.setConnectTimeOut(10_000);
        connect.setType("rsh");
        if (!connect.isRshType() || !connect.isTermType()) {
            throw new IllegalStateException("rsh connection type was not recognized");
        }

        ShellRshTtyConnector connector = null;
        AtomicBoolean reading = new AtomicBoolean(true);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        StringBuilder output = new StringBuilder();
        Thread reader = null;
        try (ShellRshClient client = ShellClientUtil.newClient(connect)) {
            client.start(10_000);
            if (!client.isPtyResizeSupported()) {
                throw new IllegalStateException("remote pty was not detected");
            }
            connector = new ShellRshTtyConnector(client);
            connector.ready();
            connector.resize(new TermSize(100, 40));
            ShellRshTtyConnector activeConnector = connector;
            reader = new Thread(() -> read(activeConnector, output, reading, failure), "rsh-reader");
            reader.setDaemon(true);
            reader.start();

            waitFor(() -> SHELL_PROMPT.matcher(output).find(), failure, output, "shell prompt");
            // 验证远端分配了真正的 pty
            connector.write("tty\r");
            waitFor(() -> output.toString().contains("/dev/pts/"), failure, output, "pty device");
            // 验证动态窗口尺寸
            connector.write("stty size\r");
            waitFor(() -> output.toString().contains("40 100"), failure, output, "pty size");
            // 验证普通命令
            String marker = "__RSH_" + UUID.randomUUID().toString().replace("-", "") + "__";
            connector.write("printf '" + marker + "\\n'\r");
            waitFor(() -> output.toString().contains(marker), failure, output, "command output");
            // 验证 Ctrl+C 可以中断前台进程
            connector.write("sleep 30\r");
            Thread.sleep(500);
            connector.write("\u0003");
            Thread.sleep(300);
            String interrupted = "__RSH_CTRL_C_" + UUID.randomUUID().toString().replace("-", "") + "__";
            connector.write("printf '" + interrupted + "\\n'\r");
            waitFor(() -> output.toString().contains(interrupted), failure, output, "interrupt recovery");
        } finally {
            reading.set(false);
            if (connector != null) {
                connector.close();
            }
            if (reader != null) {
                reader.join(2_000);
            }
        }
    }

    private static void read(ShellRshTtyConnector connector, StringBuilder output,
                             AtomicBoolean reading, AtomicReference<Throwable> failure) {
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
    }

    private static void waitFor(Check check, AtomicReference<Throwable> failure,
                                StringBuilder output, String name) throws Exception {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            Throwable current = failure.get();
            if (current != null) {
                throw new IllegalStateException("rsh read failed", current);
            }
            if (check.done()) {
                return;
            }
            Thread.sleep(50);
        }
        synchronized (output) {
            throw new IllegalStateException("rsh " + name + " was not observed: " + output);
        }
    }

    @FunctionalInterface
    private interface Check {

        boolean done();
    }
}
