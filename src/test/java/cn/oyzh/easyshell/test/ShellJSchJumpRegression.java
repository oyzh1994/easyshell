package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.ssh.SSHForwarder;
import cn.oyzh.ssh.jump.SSHJumpForwarder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ShellJSchJumpRegression {

    private ShellJSchJumpRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch jump regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int jumpIndex = Integer.getInteger("test.jump.index", 1);
        int secondJumpIndex = Integer.getInteger("test.second.jump.index", 3);
        int thirdJumpIndex = Integer.getInteger("test.third.jump.index", 2);
        int targetIndex = Integer.getInteger("test.target.index", 6);
        ShellJSchTestHosts.Host jump = ShellJSchTestHosts.load(jumpIndex);
        ShellJSchTestHosts.Host secondJump = ShellJSchTestHosts.load(secondJumpIndex);
        ShellJSchTestHosts.Host thirdJump = ShellJSchTestHosts.load(thirdJumpIndex);
        ShellJSchTestHosts.Host target = ShellJSchTestHosts.load(targetIndex);
        require(new java.util.HashSet<>(List.of(jumpIndex, secondJumpIndex, thirdJumpIndex, targetIndex)).size() == 4,
                "jump and target indices must be distinct");
        System.out.println("Jump chain: " + jump + " -> " + secondJump + " -> " + thirdJump + " -> " + target);

        String directHostname;
        try (ShellSSHClient direct = new ShellSSHClient(target.newConnect())) {
            direct.setVerifyFailureCallback(current -> null);
            direct.start(15000);
            directHostname = requireNotBlank(direct.exec("hostname"), "direct hostname is blank");
        }

        ShellConnect singleHop = target.newConnect();
        singleHop.setJumpConfigs(List.of(jump.newJumpConfig()));
        requireJump(singleHop, directHostname, 1, "single-hop");

        ShellConnect multiHop = target.newConnect();
        multiHop.setJumpConfigs(List.of(jump.newJumpConfig(), secondJump.newJumpConfig()));
        requireJump(multiHop, directHostname, 2, "multi-hop");

        ShellConnect deepHop = target.newConnect();
        deepHop.setJumpConfigs(List.of(jump.newJumpConfig(), secondJump.newJumpConfig(), thirdJump.newJumpConfig()));
        requireJump(deepHop, directHostname, 3, "deep-hop");
    }

    private static void requireJump(ShellConnect connect, String expectedHostname, int expectedSessions, String name)
            throws Exception {
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        SSHJumpForwarder forwarder = null;
        int localPort = -1;
        try {
            client.start(15000);
            require(client.getSession() != null && client.getSession().isConnected(), name + " session is not connected");
            require("127.0.0.1".equals(client.getSession().getHost()), name + " did not connect through local forwarding");
            localPort = client.getSession().getPort();
            String hostname = requireNotBlank(client.exec("hostname"), name + " hostname output is blank");
            require(expectedHostname.equals(hostname), name + " reached an unexpected target");

            forwarder = jumpForwarder(client);
            Set<?> sessions = jumpSessions(forwarder);
            require(sessions.size() == expectedSessions,
                    name + " forwarding session count mismatch: expected " + expectedSessions + ", actual " + sessions.size());

            requireSftp(client, name);
            client.getSession().disconnect();
            String reconnectedHostname = requireNotBlank(client.exec("hostname"), name + " reconnect hostname is blank");
            require(expectedHostname.equals(reconnectedHostname), name + " reconnect reached an unexpected target");
            Set<?> reconnectedSessions = jumpSessions(forwarder);
            require(reconnectedSessions.size() == expectedSessions,
                    name + " forwarding session count changed after reconnect: " + reconnectedSessions.size());
            requireSftp(client, name);
            System.out.println(name + " verified: sessions=" + sessions.size() + ", reconnectSessions="
                    + reconnectedSessions.size() + ", targetMatched=true");
        } finally {
            client.close();
            if (forwarder != null) {
                require(!forwarder.isAlive(), name + " forwarding resources were not released");
            }
        }
        requirePortClosed(localPort, name + " forwarding port was not released");
    }

    private static void requireSftp(ShellSSHClient client, String name) throws Exception {
        String token = UUID.randomUUID().toString();
        String remote = "/tmp/easyshell-jsch-jump-" + token + ".txt";
        String content = name + "-sftp-" + token;
        Path downloaded = Files.createTempFile("easyshell-jump-", ".txt");
        try {
            client.sftpClient().put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), remote, null);
            ShellSFTPFile file = client.sftpClient().fileInfo(remote);
            require(file != null, name + " sftp metadata is missing");
            client.sftpClient().get(file, downloaded.toString());
            require(content.equals(Files.readString(downloaded, StandardCharsets.UTF_8)),
                    name + " sftp content mismatch");
            System.out.println(name + " SFTP upload/download verified");
        } finally {
            client.exec("rm -f " + quote(remote));
            Files.deleteIfExists(downloaded);
        }
    }

    private static SSHJumpForwarder jumpForwarder(ShellSSHClient client) throws Exception {
        Field field = ShellSSHClient.class.getDeclaredField("jumpForwarder");
        field.setAccessible(true);
        Object value = field.get(client);
        require(value instanceof SSHJumpForwarder, "jump forwarder is not initialized");
        return (SSHJumpForwarder) value;
    }

    private static Set<?> jumpSessions(SSHForwarder forwarder) throws Exception {
        Field field = SSHForwarder.class.getDeclaredField("sessions");
        field.setAccessible(true);
        return (Set<?>) field.get(forwarder);
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static String requireNotBlank(String value, String message) {
        require(value != null && !value.isBlank(), message);
        return value;
    }

    private static void requirePortClosed(int port, String message) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 300);
            } catch (IOException expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new IllegalStateException(message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
