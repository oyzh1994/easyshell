package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.util.ShellUtil;

/**
 * Windows SSH 输出编码回归。重点验证 UTF-8 BOM 文件经 type 原样输出时，
 * 通用 exec 不会按 GBK 控制台代码页误解码，同时本地化错误信息仍可回退到远端字符集。
 */
public final class ShellJSchWindowsEncodingRegression {

    private static final String HOSTS = "C:\\Windows\\System32\\drivers\\etc\\hosts";
    private static final String MISSING_COMMAND = "__easyshell_missing_encoding_test__";

    private ShellJSchWindowsEncodingRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch Windows encoding regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.windows.encoding.host.index", 4);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Windows encoding target: " + host);

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(20000);
            require(client.isWindows(), "target host is not Windows");
            require("GBK".equalsIgnoreCase(client.getRemoteCharset())
                            || "GB18030".equalsIgnoreCase(client.getRemoteCharset()),
                    "Windows test host is not using a GBK-compatible console charset: " + client.getRemoteCharset());

            String bomCommand = "powershell -NoProfile -NonInteractive -Command "
                    + "\"[Convert]::ToBase64String([IO.File]::ReadAllBytes('" + HOSTS + "')[0..2])\"";
            String bom = client.exec(bomCommand);
            require("77u/".equals(bom == null ? null : bom.trim()),
                    "hosts file is not UTF-8 BOM encoded: " + bom);

            String normal = client.exec("type \"" + HOSTS + "\"");
            String utf8 = client.execUtf8("type \"" + HOSTS + "\"");
            verifyHostsOutput(normal, "exec");
            verifyHostsOutput(utf8, "execUtf8");
            require(normal.equals(utf8), "exec and execUtf8 decoded the same BOM output differently");
            String shellOutput = verifyInteractiveShell(client);

            String missing = client.execUtf8(MISSING_COMMAND);
            require(ShellUtil.isWindowsCommandNotFound(missing, "__easyshell_missing_encoding_test__"),
                    "execUtf8 did not fall back to the GBK console charset: " + missing);

            System.out.println("UTF-8 BOM hosts output verified: chars=" + normal.length()
                    + ", lines=" + normal.lines().count()
                    + ", shellChars=" + shellOutput.length()
                    + ", remoteCharset=" + client.getRemoteCharset());
        }
    }

    private static String verifyInteractiveShell(ShellSSHClient client) throws Exception {
        ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
        try {
            connector.ready();
            connector.write("type \"" + HOSTS + "\"\r\n");
            StringBuilder output = new StringBuilder();
            char[] buffer = new char[1024];
            long deadline = System.currentTimeMillis() + 10000;
            while (System.currentTimeMillis() < deadline) {
                int read = connector.read(buffer, 0, buffer.length);
                if (read < 0) {
                    break;
                }
                output.append(buffer, 0, read);
                if (output.indexOf("# End of section") >= 0) {
                    break;
                }
            }
            String value = output.toString();
            verifyHostsOutput(value, "interactive shell");
            return value;
        } finally {
            connector.close();
        }
    }

    private static void verifyHostsOutput(String output, String method) {
        require(output != null && !output.isBlank(), method + " hosts output is blank");
        require(output.contains("127.0.0.1"), method + " hosts output lost ASCII content");
        require(output.indexOf('\uFEFF') < 0, method + " hosts output still contains a UTF-8 BOM");
        require(!output.contains("\u9518"), method + " hosts output contains the GBK-decoded BOM mojibake");
        require(!output.contains("\uFFFD"), method + " hosts output contains a replacement character");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
