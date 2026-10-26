package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 验证 Shell 建立后能够读取首屏 MOTD、登录提示和交互提示符。
 */
public final class ShellJSchShellBannerRegression {

    private ShellJSchShellBannerRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch shell banner regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        int minInitialBytes = Integer.getInteger("test.shell.min.initial.bytes", 1);
        boolean requireBanner = Boolean.parseBoolean(System.getProperty("test.shell.require.banner", "false"));
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Shell banner target: " + host);

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            verifyCycle(client, "initial", minInitialBytes, requireBanner);
            client.getShell().close();
            verifyCycle(client, "reopen", 0, false);
        }
    }

    private static void verifyCycle(ShellSSHClient client, String cycle, int minInitialBytes, boolean requireBanner)
            throws Exception {
        ShellSSHShell shell = client.openShell();
        int shellId = shell.getId();
        ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
        connector.ready();
        require(client.getShell().getId() == shellId, cycle + " ready() reopened an already connected shell");
        InputStream input = connector.input();
        OutputStream output = connector.output();
        byte[] initial = readInitial(input, 8000, 1200);
        String initialText = new String(initial, StandardCharsets.UTF_8);
        boolean prompt = containsPrompt(initialText);
        boolean banner = containsBanner(initialText);
        System.out.println(cycle + ": bytes=" + initial.length + ", lines=" + lineCount(initialText)
                + ", prompt=" + prompt + ", banner=" + banner);
        if (Boolean.getBoolean("test.shell.dump.output")) {
            System.out.println("--- " + cycle + " output ---\n" + initialText + "\n--- " + cycle + " output end ---");
        }
        require(initial.length >= minInitialBytes, cycle + " initial shell output was empty or too short: "
                + initial.length + " bytes");
        require(prompt, cycle + " initial shell output did not contain a prompt");
        if (requireBanner) {
            require(banner, cycle + " initial shell output did not contain MOTD/banner markers");
        }

        String marker = "__EASYSHELL_SHELL_READY_" + UUID.randomUUID().toString().replace("-", "") + "__";
        output.write(("printf '" + marker + "\\n'\r").getBytes(StandardCharsets.UTF_8));
        output.flush();
        String commandOutput = readUntil(input, marker, 8000);
        require(commandOutput.contains(marker), cycle + " shell command output did not contain readiness marker");
        System.out.println(cycle + " interactive shell verified");
    }

    private static byte[] readInitial(InputStream input, long maxWaitMillis, long quietMillis) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long start = System.currentTimeMillis();
        long lastRead = 0L;
        byte[] buffer = new byte[8192];
        while (System.currentTimeMillis() - start < maxWaitMillis) {
            int available = input.available();
            if (available > 0) {
                int read = input.read(buffer, 0, Math.min(available, buffer.length));
                if (read > 0) {
                    output.write(buffer, 0, read);
                    lastRead = System.currentTimeMillis();
                }
                continue;
            }
            if (output.size() > 0 && System.currentTimeMillis() - lastRead >= quietMillis) {
                break;
            }
            Thread.sleep(20L);
        }
        return output.toByteArray();
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

    private static boolean containsPrompt(String output) {
        return output.contains("$ ") || output.contains("# ") || output.contains("> ")
                || output.contains("$") || output.contains("#");
    }

    private static boolean containsBanner(String output) {
        String value = output.toLowerCase();
        return value.contains("welcome to") || value.contains("system information")
                || value.contains("last login") || value.contains("ubuntu")
                || value.contains("uos") || value.contains("documentation")
                || value.contains("expanded security maintenance") || value.contains("memory usage")
                || value.contains("disk usage") || value.contains("load average");
    }

    private static int lineCount(String output) {
        if (output.isEmpty()) {
            return 0;
        }
        return output.split("\\R", -1).length;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
