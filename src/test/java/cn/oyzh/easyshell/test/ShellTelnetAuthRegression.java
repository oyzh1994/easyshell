package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.easyshell.telnet.ShellTelnetTtyConnector;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 使用本地 telnet 容器验证交互式登录。
 */
public final class ShellTelnetAuthRegression {

    private static final Pattern SHELL_PROMPT = Pattern.compile("(?m)^[^\\r\\n]*[#$%>]\\s*$");

    private ShellTelnetAuthRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("telnet auth regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setHost(System.getProperty("test.telnet.host", "127.0.0.1:2323"));
        connect.setUser(System.getProperty("test.telnet.user", "root"));
        connect.setPassword(System.getProperty("test.telnet.password", "malware"));
        connect.setCharset(StandardCharsets.UTF_8.name());
        connect.setConnectTimeOut(10_000);

        ShellTelnetTtyConnector connector = null;
        AtomicBoolean reading = new AtomicBoolean(true);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        StringBuilder output = new StringBuilder();
        Thread reader = null;
        try (ShellTelnetClient client = new ShellTelnetClient(connect)) {
            client.start(10_000);
            connector = new ShellTelnetTtyConnector(client);
            connector.ready();
            ShellTelnetTtyConnector activeConnector = connector;
            reader = new Thread(() -> read(activeConnector, output, reading, failure), "telnet-auth-reader");
            reader.setDaemon(true);
            reader.start();

            waitFor(() -> SHELL_PROMPT.matcher(output).find(), failure, output, "shell prompt");
            String marker = "__TELNET_AUTH_" + UUID.randomUUID().toString().replace("-", "") + "__";
            connector.write("printf '" + marker + "\\n'\r\n");
            waitFor(() -> output.toString().contains(marker), failure, output, "command output");
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

    private static void read(ShellTelnetTtyConnector connector, StringBuilder output,
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
                throw new IllegalStateException("telnet read failed", current);
            }
            if (check.done()) {
                return;
            }
            Thread.sleep(50);
        }
        synchronized (output) {
            throw new IllegalStateException("telnet " + name + " was not observed: " + output);
        }
    }

    @FunctionalInterface
    private interface Check {

        boolean done();
    }
}
