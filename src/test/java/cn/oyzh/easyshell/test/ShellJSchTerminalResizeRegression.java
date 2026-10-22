package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证短时间内连续 resize 只向远端发送最终 PTY 尺寸。
 */
public final class ShellJSchTerminalResizeRegression {

    private ShellJSchTerminalResizeRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch terminal resize regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("terminal resize target: " + host);
        String remoteLog = "/tmp/easyshell-resize-" + UUID.randomUUID() + ".log";
        String ready = "__EASYSHELL_RESIZE_READY_" + UUID.randomUUID().toString().replace("-", "") + "__";
        String finish = "__EASYSHELL_RESIZE_FINISH_" + UUID.randomUUID().toString().replace("-", "") + "__";

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            ShellSSHShell shell = client.openShell();
            InputStream input = shell.getInputStream();
            OutputStream output = shell.getOutputStream();
            drain(input, 800);
            write(output, ": > " + quote(remoteLog)
                    + "; trap 'stty size >> " + quote(remoteLog) + "' WINCH;"
                    + " printf '" + ready + "\\n'\r");
            require(readUntil(input, ready, 8000).contains(ready), "resize trap was not installed");

            List<int[]> sizes = List.of(
                    new int[]{90, 28, 720, 480},
                    new int[]{100, 31, 800, 530},
                    new int[]{108, 34, 864, 580},
                    new int[]{116, 37, 928, 630},
                    new int[]{120, 40, 960, 680});
            for (int[] size : sizes) {
                client.setPtySize(size[0], size[1], size[2], size[3]);
                Thread.sleep(15L);
            }
            Thread.sleep(1200L);

            write(output, "printf 'RESIZE_COUNT='; wc -l < " + quote(remoteLog)
                    + "; printf 'RESIZE_SIZE='; tail -n 1 " + quote(remoteLog)
                    + "; printf '" + finish + "\\n'\r");
            String result = readUntil(input, finish, 8000);
            Matcher countMatcher = Pattern.compile("RESIZE_COUNT=\\s*(\\d+)").matcher(result);
            Matcher sizeMatcher = Pattern.compile("RESIZE_SIZE=\\s*(\\d+\\s+\\d+)").matcher(result);
            require(countMatcher.find(), "resize count was not reported");
            require(sizeMatcher.find(), "resize size was not reported");
            int count = Integer.parseInt(countMatcher.group(1));
            String size = sizeMatcher.group(1).trim();
            System.out.println("terminal resize count=" + count + ", final=" + size);
            require(count == 1, "consecutive resize should send one window-change, actual=" + count);
            require("40 120".equals(size), "final PTY size mismatch: " + size);
        } finally {
            try (ShellSSHClient cleanup = new ShellSSHClient(host.newConnect())) {
                cleanup.setVerifyFailureCallback(current -> null);
                ShellJSchTestHosts.acceptHostKeys(cleanup);
                cleanup.start(15000);
                cleanup.exec("rm -f " + quote(remoteLog));
            } catch (Exception ignored) {
            }
        }
    }

    private static void write(OutputStream output, String value) throws Exception {
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private static String readUntil(InputStream input, String marker, long maxWaitMillis) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[8192];
        String value = "";
        while (System.currentTimeMillis() - start < maxWaitMillis) {
            int available = input.available();
            if (available > 0) {
                int read = input.read(buffer, 0, Math.min(available, buffer.length));
                if (read > 0) {
                    output.write(buffer, 0, read);
                    value = output.toString(StandardCharsets.UTF_8);
                    if (value.contains(marker)) {
                        return value;
                    }
                }
                continue;
            }
            Thread.sleep(20L);
        }
        return value;
    }

    private static void drain(InputStream input, long quietMillis) throws Exception {
        long start = System.currentTimeMillis();
        long lastRead = start;
        byte[] buffer = new byte[8192];
        while (System.currentTimeMillis() - start < 5000L) {
            int available = input.available();
            if (available > 0) {
                input.read(buffer, 0, Math.min(available, buffer.length));
                lastRead = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastRead >= quietMillis) {
                return;
            } else {
                Thread.sleep(20L);
            }
        }
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
