package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;

import java.util.Arrays;

/**
 * 对主机矩阵执行只读平台、压缩协商和 Docker CLI 可用性探测。
 */
public final class ShellJSchPlatformRegression {

    private ShellJSchPlatformRegression() {
    }

    public static void main(String[] args) {
        try {
            int[] indices = Arrays.stream(System.getProperty("test.host.indices", "1,2,3,4,5,6").split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();
            int failed = 0;
            for (int index : indices) {
                if (!probe(index)) {
                    failed++;
                }
            }
            if (failed > 0) {
                throw new IllegalStateException("platform probe failed for " + failed + " host(s)");
            }
            System.out.println("JSch platform regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean probe(int index) {
        ShellJSchTestHosts.Host host;
        try {
            host = ShellJSchTestHosts.load(index);
        } catch (Exception ex) {
            System.out.println("host[" + index + "]: LOAD FAILED: " + ex.getClass().getSimpleName());
            return false;
        }
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.getShellConnect().setEnableCompress(true);
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            String uname = client.exec("uname -s", 10000);
            boolean windows = client.isWindows();
            String os;
            String arch;
            String docker;
            if (windows) {
                os = oneLine(client.exec("cmd.exe /c ver", 10000));
                arch = oneLine(client.exec("cmd.exe /c echo %PROCESSOR_ARCHITECTURE%", 10000));
                docker = oneLine(client.exec("cmd.exe /c where docker", 10000));
            } else {
                os = oneLine(uname);
                arch = oneLine(client.exec("uname -m", 10000));
                docker = oneLine(client.exec("command -v docker || echo unavailable", 10000));
            }
            String c2s = client.getSession().getCompressionAlgorithmC2S();
            String s2c = client.getSession().getCompressionAlgorithmS2C();
            System.out.println("host[" + index + "," + host.label() + "]: os=" + os + ", arch=" + arch
                    + ", docker=" + dockerAvailability(docker)
                    + ", compression=" + c2s + "/" + s2c);
            return client.getSession().isConnected();
        } catch (Exception ex) {
            System.out.println("host[" + index + "," + host.label() + "]: FAILED: " + ex.getClass().getSimpleName()
                    + ": " + safeMessage(ex));
            return false;
        }
    }

    private static boolean isCommandMissing(String output) {
        if (output == null || output.isBlank()) {
            return true;
        }
        String value = output.toLowerCase();
        return value.contains("not recognized") || value.contains("not found")
                || value.contains("command not found") || value.contains("不是内部或外部命令")
                || value.contains("无法找到文件") || value.contains("找不到文件");
    }

    private static String dockerAvailability(String output) {
        if (output == null || isCommandMissing(output)) {
            return "unavailable";
        }
        return "available";
    }

    private static String oneLine(String output) {
        if (output == null || output.isBlank()) {
            return "unknown";
        }
        String value = output.lines().map(String::trim).filter(line -> !line.isBlank()).findFirst().orElse("unknown");
        return value.length() > 80 ? value.substring(0, 80) : value;
    }

    private static String safeMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) {
            return "";
        }
        return message.lines().findFirst().orElse("");
    }
}
