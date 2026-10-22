package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.ssh.ShellSSHClient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证 known_hosts 的 IPv6、哈希 IPv6、并发首次信任和不可写路径错误语义。
 */
public final class ShellJSchHostKeyEdgeRegression {

    private static final String KNOWN_HOSTS_PROPERTY = "easyshell.known_hosts.path";

    private ShellJSchHostKeyEdgeRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch host key edge regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        testEndpointParsing();
        int hostIndex = Integer.getInteger("test.host.index", 5);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("host key edge target: " + host);
        String previous = System.getProperty(KNOWN_HOSTS_PROPERTY);
        List<Path> files = new ArrayList<>();
        try {
            if (isIpv6Reachable(host.port())) {
                Path ipv6KnownHosts = newKnownHosts(files);
                testIpv6(host, ipv6KnownHosts);
            } else {
                System.out.println("IPv6 loopback SSH is unavailable, skipped");
            }
            Path concurrentKnownHosts = newKnownHosts(files);
            testConcurrentFirstTrust(host, concurrentKnownHosts);
            testUnwritablePath(host);
        } finally {
            restoreProperty(previous);
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        }
    }

    private static void testEndpointParsing() {
        ShellConnect ipv6 = new ShellConnect();
        ipv6.setHost("[::1]:2202");
        require("::1".equals(ipv6.hostIp()), "bracketed IPv6 host was not parsed: " + ipv6.hostIp());
        require(ipv6.hostPort() == 2202, "bracketed IPv6 port was not parsed: " + ipv6.hostPort());

        ShellConnect ipv4 = new ShellConnect();
        ipv4.setHost("127.0.0.1:22");
        require("127.0.0.1".equals(ipv4.hostIp()), "IPv4 host was not parsed: " + ipv4.hostIp());
        require(ipv4.hostPort() == 22, "IPv4 port was not parsed: " + ipv4.hostPort());
        System.out.println("endpoint parsing verified");
    }

    private static void testIpv6(ShellJSchTestHosts.Host host, Path knownHosts) throws Exception {
        System.setProperty(KNOWN_HOSTS_PROPERTY, knownHosts.toString());
        String endpoint = "[::1]:" + host.port();
        AtomicInteger prompts = new AtomicInteger();
        try (ShellSSHClient client = newClient(endpoint, host.user(), host.password())) {
            client.setHostKeyConfirmCallback(message -> {
                prompts.incrementAndGet();
                return true;
            });
            client.start(15000);
            require(client.isConnected(), "IPv6 SSH connection did not establish");
            requireExec(client, "ipv6");
        }
        require(prompts.get() == 1, "IPv6 host key prompt count was not 1: " + prompts.get());
        String content = Files.readString(knownHosts, StandardCharsets.UTF_8);
        String expected = knownHostsHost("::1", host.port());
        require(content.contains(expected), "known_hosts does not contain IPv6 endpoint " + expected);

        try (ShellSSHClient client = newClient(endpoint, host.user(), host.password())) {
            client.setHostKeyConfirmCallback(message -> {
                throw new IllegalStateException("IPv6 persisted host unexpectedly prompted: " + message);
            });
            client.start(15000);
            requireExec(client, "ipv6-persist");
        }

        Process hashing = new ProcessBuilder("ssh-keygen", "-H", "-f", knownHosts.toString())
                .redirectErrorStream(true).start();
        require(hashing.waitFor(10, TimeUnit.SECONDS), "ssh-keygen -H timed out");
        require(hashing.exitValue() == 0, "ssh-keygen -H failed: " + new String(
                hashing.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        Files.writeString(knownHosts, "\n# IPv6 hashed known_hosts compatibility\n\n",
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        try (ShellSSHClient client = newClient(endpoint, host.user(), host.password())) {
            client.setHostKeyConfirmCallback(message -> {
                throw new IllegalStateException("hashed IPv6 host unexpectedly prompted: " + message);
            });
            client.start(15000);
            requireExec(client, "ipv6-hashed");
        }
        System.out.println("IPv6 known_hosts plain/hashed verified");
    }

    private static void testConcurrentFirstTrust(ShellJSchTestHosts.Host host, Path knownHosts) throws Exception {
        System.setProperty(KNOWN_HOSTS_PROPERTY, knownHosts.toString());
        int connections = Integer.getInteger("test.hostkey.concurrent.connections", 8);
        ExecutorService executor = Executors.newFixedThreadPool(connections);
        CountDownLatch ready = new CountDownLatch(connections);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger prompts = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < connections; index++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await(10, TimeUnit.SECONDS);
                    try (ShellSSHClient client = newClient(host.host() + ":" + host.port(), host.user(), host.password())) {
                        client.setHostKeyConfirmCallback(message -> {
                            prompts.incrementAndGet();
                            return true;
                        });
                        client.start(15000);
                        require(client.isConnected(), "concurrent SSH connection did not establish");
                        requireExec(client, "concurrent-" + UUID.randomUUID());
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                    return null;
                }));
            }
            require(ready.await(10, TimeUnit.SECONDS), "concurrent clients were not ready");
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
        String content = Files.readString(knownHosts, StandardCharsets.UTF_8);
        String expected = knownHostsHost(host.host(), host.port());
        long entries = content.lines().filter(line -> line.contains(expected)).count();
        require(entries >= 1, "concurrent known_hosts is missing " + expected);
        require(entries <= 1, "concurrent known_hosts contains duplicate entries: " + entries);
        try (ShellSSHClient client = newClient(host.host() + ":" + host.port(), host.user(), host.password())) {
            client.setHostKeyConfirmCallback(message -> {
                throw new IllegalStateException("concurrent persisted host unexpectedly prompted: " + message);
            });
            client.start(15000);
            requireExec(client, "concurrent-persist");
        }
        System.out.println("concurrent first trust verified: connections=" + connections
                + ", prompts=" + prompts.get() + ", entries=" + entries);
    }

    private static void testUnwritablePath(ShellJSchTestHosts.Host host) throws Exception {
        String invalid = "/dev/null/easyshell-known_hosts";
        System.setProperty(KNOWN_HOSTS_PROPERTY, invalid);
        try (ShellSSHClient client = newClient(host.host() + ":" + host.port(), host.user(), host.password())) {
            client.setHostKeyConfirmCallback(message -> true);
            try {
                client.start(15000);
                throw new IllegalStateException("unwritable known_hosts unexpectedly succeeded");
            } catch (ShellException ex) {
                require(containsMessage(ex, "initialize known_hosts failed"),
                        "unwritable known_hosts returned an unexpected error: " + ex);
            }
        }
        System.out.println("unwritable known_hosts reported explicitly");
    }

    private static Path newKnownHosts(List<Path> files) throws Exception {
        Path file = Files.createTempFile("easyshell-jsch-known-hosts-edge-", ".txt");
        files.add(file);
        return file;
    }

    private static String knownHostsHost(String host, int port) {
        return port == 22 ? host : "[" + host + "]:" + port;
    }

    private static boolean isIpv6Reachable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName("::1"), port), 2000);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean containsMessage(Throwable throwable, String expected) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(expected)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void requireExec(ShellSSHClient client, String prefix) {
        String marker = prefix + "-" + UUID.randomUUID();
        require(marker.equals(client.exec("printf " + quote(marker))), "exec output mismatch for " + prefix);
    }

    private static ShellSSHClient newClient(String endpoint, String user, String password) {
        ShellConnect connect = new ShellConnect();
        connect.setHost(endpoint);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        return client;
    }

    private static void restoreProperty(String value) {
        if (value == null) {
            System.clearProperty(KNOWN_HOSTS_PROPERTY);
        } else {
            System.setProperty(KNOWN_HOSTS_PROPERTY, value);
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
