package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 验证只提供 diffie-hellman-group14-sha1 与 ssh-rsa 的旧 SSH 服务端兼容性。
 */
public final class ShellJSchLegacyAlgorithmRegression {

    private ShellJSchLegacyAlgorithmRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch legacy algorithm regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        String host = requireProperty("test.legacy.host", null);
        int port = Integer.parseInt(requireProperty("test.legacy.port", null));
        String user = requireProperty("test.legacy.user", "EASYSHELL_LEGACY_USER");
        String password = requireProperty("test.legacy.password", "EASYSHELL_LEGACY_PASSWORD");

        ShellConnect connect = new ShellConnect();
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);
        boolean jump = Boolean.getBoolean("test.legacy.jump");
        if (jump && Boolean.getBoolean("test.legacy.jump.sessionOnly")) {
            requireJumpFallback(host, port, user, password);
            System.out.println("legacy kex + ssh-rsa jump fallback and forwarding verified");
            return;
        }
        if (jump) {
            int jumpIndex = Integer.getInteger("test.legacy.jump.index", -1);
            ShellJumpConfig jumpConfig;
            if (jumpIndex > 0) {
                jumpConfig = ShellJSchTestHosts.load(jumpIndex).newJumpConfig();
            } else {
                jumpConfig = new ShellJumpConfig();
                jumpConfig.setHost(host);
                jumpConfig.setPort(port);
                jumpConfig.setUser(user);
                jumpConfig.setPassword(password);
                jumpConfig.setAuthMethod("password");
                jumpConfig.setTimeout(15000);
            }
            connect.setJumpConfigs(List.of(jumpConfig));
        }

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            client.setHostKeyConfirmCallback(message -> true);
            client.start(15000);
            require(client.isConnected(), "legacy session is not connected");

            String marker = "legacy-" + UUID.randomUUID();
            require(marker.equals(client.exec("printf " + quote(marker))), "legacy exec output mismatch");
            requireSftp(client, marker);
        }
        System.out.println("legacy kex + ssh-rsa SSH/Exec/SFTP verified, jump=" + jump);
    }

    private static void requireJumpFallback(String host, int port, String user, String password) throws Exception {
        SSHConnect hop = new SSHConnect();
        hop.setHost(host);
        hop.setPort(port);
        hop.setUser(user);
        hop.setPassword(password);
        hop.setAuthMethod("password");
        hop.setTimeout(15000);
        SSHConnect target = new SSHConnect();
        target.setHost(host);
        target.setPort(Integer.getInteger("test.legacy.target.port", 22));
        target.setUser(user);
        target.setPassword(password);
        target.setAuthMethod("password");
        target.setTimeout(15000);
        try (SSHJumpForwarder forwarder = new SSHJumpForwarder()) {
            int localPort = forwarder.forward(List.of(hop), target);
            require(localPort > 0, "legacy jump forwarding port is invalid");
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), localPort), 5000);
                socket.setSoTimeout(5000);
                byte[] banner = socket.getInputStream().readNBytes(8);
                require(new String(banner, StandardCharsets.US_ASCII).startsWith("SSH-2.0"),
                        "legacy jump forwarding did not receive an SSH banner");
            }
        }
    }

    private static void requireSftp(ShellSSHClient client, String marker) throws Exception {
        String remote = "/tmp/easyshell-jsch-legacy-" + UUID.randomUUID() + ".txt";
        Path local = Files.createTempFile("easyshell-legacy-", ".txt");
        try {
            client.sftpClient().put(new ByteArrayInputStream(marker.getBytes(StandardCharsets.UTF_8)), remote, null);
            client.sftpClient().get(client.sftpClient().fileInfo(remote), local.toString());
            require(marker.equals(Files.readString(local, StandardCharsets.UTF_8)), "legacy sftp content mismatch");
        } finally {
            client.exec("rm -f " + quote(remote));
            Files.deleteIfExists(local);
        }
    }

    private static String requireProperty(String name, String environmentName) {
        String value = System.getProperty(name);
        if ((value == null || value.isBlank()) && environmentName != null) {
            value = System.getenv(environmentName);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
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
