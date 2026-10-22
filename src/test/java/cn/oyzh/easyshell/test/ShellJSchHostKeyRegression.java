package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import com.jcraft.jsch.JSchChangedHostKeyException;
import com.jcraft.jsch.JSchUnknownHostKeyException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 known_hosts 的未知主机接受/拒绝、持久化和主机密钥变更处理。
 */
public final class ShellJSchHostKeyRegression {

    private ShellJSchHostKeyRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        String mode = System.getProperty("test.hostkey.mode", "accept-new");
        String host = System.getProperty("test.hostkey.host", "127.0.0.1");
        int port = Integer.getInteger("test.hostkey.port", 2225);
        String user = System.getProperty("test.hostkey.user", "root");
        String password = requireProperty("test.hostkey.password", "EASYSHELL_HOSTKEY_PASSWORD");
        Path knownHosts = Path.of(requireProperty("easyshell.known_hosts.path"));
        Files.createDirectories(knownHosts.toAbsolutePath().getParent());
        if (!Files.exists(knownHosts)) {
            Files.createFile(knownHosts);
        }

        switch (mode) {
            case "reject-new" -> rejectNew(host, port, user, password, knownHosts);
            case "accept-new" -> acceptNew(host, port, user, password, knownHosts);
            case "persist" -> persist(host, port, user, password);
            case "reject-changed" -> rejectChanged(host, port, user, password, knownHosts);
            case "accept-changed" -> acceptChanged(host, port, user, password, knownHosts);
            case "persist-changed" -> persist(host, port, user, password);
            default -> throw new IllegalArgumentException("unknown host key mode: " + mode);
        }
        System.out.println("host key mode passed: " + mode);
    }

    private static void rejectNew(String host, int port, String user, String password, Path knownHosts)
            throws Exception {
        String before = Files.readString(knownHosts);
        AtomicReference<String> prompt = new AtomicReference<>();
        try (ShellSSHClient client = newClient(host, port, user, password)) {
            client.setHostKeyConfirmCallback(message -> {
                prompt.set(message);
                return false;
            });
            expectFailure(client, JSchUnknownHostKeyException.class, "unknown host key rejection");
        }
        require(prompt.get() != null && prompt.get().contains("can't be established"),
                "unknown host key prompt was not observed");
        require(before.equals(Files.readString(knownHosts)), "rejected unknown host key changed known_hosts");
        System.out.println("unknown host key rejected without persistence");
    }

    private static void acceptNew(String host, int port, String user, String password, Path knownHosts)
            throws Exception {
        AtomicInteger prompts = new AtomicInteger();
        try (ShellSSHClient client = newClient(host, port, user, password)) {
            client.setHostKeyConfirmCallback(message -> {
                prompts.incrementAndGet();
                return message.contains("can't be established");
            });
            client.start(15000);
            require(client.isConnected(), "unknown host key acceptance did not connect");
            require(prompts.get() == 1, "unknown host key prompt count was not 1");
            requireContainsHost(Files.readString(knownHosts), host, port);
            requireExec(client);
        }
        System.out.println("unknown host key accepted and persisted");
    }

    private static void persist(String host, int port, String user, String password) throws Exception {
        try (ShellSSHClient client = newClient(host, port, user, password)) {
            client.setHostKeyConfirmCallback(message -> {
                throw new IllegalStateException("known host unexpectedly prompted: " + message);
            });
            client.start(15000);
            require(client.isConnected(), "persisted known host did not connect");
            requireExec(client);
        }
        System.out.println("persisted host key accepted without prompt");
    }

    private static void rejectChanged(String host, int port, String user, String password, Path knownHosts)
            throws Exception {
        String before = Files.readString(knownHosts);
        AtomicReference<String> prompt = new AtomicReference<>();
        try (ShellSSHClient client = newClient(host, port, user, password)) {
            client.setHostKeyConfirmCallback(message -> {
                prompt.set(message);
                return false;
            });
            expectFailure(client, JSchChangedHostKeyException.class, "changed host key rejection");
        }
        require(prompt.get() != null && prompt.get().contains("IDENTIFICATION HAS CHANGED"),
                "changed host key warning was not observed");
        require(before.equals(Files.readString(knownHosts)), "rejected changed host key updated known_hosts");
        System.out.println("changed host key rejected without updating known_hosts");
    }

    private static void acceptChanged(String host, int port, String user, String password, Path knownHosts)
            throws Exception {
        String before = Files.readString(knownHosts);
        AtomicReference<String> prompt = new AtomicReference<>();
        try (ShellSSHClient client = newClient(host, port, user, password)) {
            client.setHostKeyConfirmCallback(message -> {
                prompt.set(message);
                return true;
            });
            client.start(15000);
            require(client.isConnected(), "changed host key acceptance did not connect");
            requireExec(client);
        }
        require(prompt.get() != null && prompt.get().contains("IDENTIFICATION HAS CHANGED"),
                "changed host key confirmation was not requested");
        String after = Files.readString(knownHosts);
        require(!before.equals(after), "accepted changed host key did not update known_hosts");
        if (!before.contains("|1|")) {
            requireContainsHost(after, host, port);
        }
        System.out.println("changed host key accepted and persisted");
    }

    private static void expectFailure(ShellSSHClient client, Class<? extends Throwable> expected,
                                      String operation) {
        try {
            client.start(15000);
            throw new IllegalStateException(operation + " unexpectedly succeeded");
        } catch (ShellException ex) {
            if (!containsCause(ex, expected)) {
                throw new IllegalStateException(operation + " failed with an unexpected cause: " + ex, ex);
            }
        }
    }

    private static boolean containsCause(Throwable throwable, Class<? extends Throwable> expected) {
        Throwable current = throwable;
        while (current != null) {
            if (expected.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void requireExec(ShellSSHClient client) {
        String marker = "hostkey-" + UUID.randomUUID();
        require(marker.equals(client.exec("printf " + quote(marker))), "host key exec output mismatch");
    }

    private static void requireContainsHost(String knownHosts, String host, int port) {
        String entry = "[" + host + "]:" + port;
        require(knownHosts.contains(entry), "known_hosts does not contain " + entry);
    }

    private static ShellSSHClient newClient(String host, int port, String user, String password) {
        ShellConnect connect = new ShellConnect();
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        return client;
    }

    private static String requireProperty(String name) {
        return requireProperty(name, null);
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
