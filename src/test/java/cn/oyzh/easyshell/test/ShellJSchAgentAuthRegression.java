package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.ssh.util.JschUtil;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.Identity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 验证 JSch 通过本机 ssh-agent 完成公钥认证，以及连续、并发和复用连接下的可靠性。
 */
public final class ShellJSchAgentAuthRegression {

    private ShellJSchAgentAuthRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch agent authentication regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        String host = System.getenv().getOrDefault("EASYSHELL_AGENT_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("EASYSHELL_AGENT_PORT", "2222"));
        String user = System.getenv().getOrDefault("EASYSHELL_AGENT_USER", "root");
        String password = requireEnv("EASYSHELL_AGENT_PASSWORD");
        Path publicKey = Path.of(requireEnv("EASYSHELL_AGENT_PUBLIC_KEY"));
        Path privateKey = Path.of(requireEnv("EASYSHELL_AGENT_PRIVATE_KEY"));
        int iterations = Integer.getInteger("test.agent.iterations", 8);
        int concurrency = Integer.getInteger("test.agent.concurrency", 6);

        require(Files.isRegularFile(publicKey), "agent public key file is missing");
        require(!Files.exists(privateKey), "private key file still exists; agent storage was not proven");
        verifyAgentIdentities();

        String marker = "easyshell-agent-" + UUID.randomUUID().toString().replace("-", "");
        ShellSSHClient passwordClient = newPasswordClient(host, port, user, password);
        boolean authorized = false;
        ShellAuthorizedKey authorizedKey = null;
        try {
            passwordClient.start(15000);
            require(passwordClient.isConnected(), "password session for key installation failed");
            authorizedKey = authorizePublicKey(passwordClient, publicKey, marker);
            authorized = true;

            verifySequentialConnections(host, port, user, iterations);
            verifyConnectionReuse(host, port, user);
            verifySftp(host, port, user, marker);
            verifyConcurrentConnections(host, port, user, concurrency);
            verifyUnauthorizedAgentKeyRejected(passwordClient, authorizedKey, host, port, user);
        } finally {
            if (authorized && authorizedKey != null) {
                restoreAuthorizedKey(passwordClient, authorizedKey);
            }
            passwordClient.close();
        }

        System.out.println("agent auth verified: sequential=" + iterations
                + ", reuse=5, concurrent=" + concurrency
                + ", sftp=PASS, unauthorized=rejected");
    }

    private static void verifyAgentIdentities() throws Exception {
        AgentIdentityRepository repository = JschUtil.initAgentIdentityRepository();
        List<Identity> identities = new ArrayList<>(repository.getIdentities());
        require(!identities.isEmpty(), "ssh-agent contains no identities");
        System.out.println("ssh-agent identities=" + identities.size());
    }

    private static void verifySequentialConnections(String host, int port, String user, int iterations)
            throws Exception {
        long start = System.currentTimeMillis();
        for (int index = 0; index < iterations; index++) {
            String expected = "agent-sequential-" + index;
            try (ShellSSHClient client = newAgentClient(host, port, user)) {
                client.start(15000);
                require(client.isConnected(), "sequential agent connection failed: " + index);
                requireExec(client, expected);
            }
        }
        System.out.println("sequential agent connections= " + iterations
                + ", elapsed=" + (System.currentTimeMillis() - start) + "ms");
    }

    private static void verifyConnectionReuse(String host, int port, String user) {
        long start = System.currentTimeMillis();
        try (ShellSSHClient client = newAgentClient(host, port, user)) {
            client.start(15000);
            require(client.isConnected(), "reused agent connection failed");
            for (int index = 0; index < 5; index++) {
                requireExec(client, "agent-reuse-" + index);
            }
        }
        System.out.println("agent connection reuse=5, elapsed=" + (System.currentTimeMillis() - start) + "ms");
    }

    private static void verifySftp(String host, int port, String user, String marker) throws Exception {
        String remote = "/tmp/" + marker + ".txt";
        String content = marker + "-sftp";
        Path local = Files.createTempFile("easyshell-agent-", ".txt");
        try (ShellSSHClient client = newAgentClient(host, port, user)) {
            client.start(15000);
            require(client.isConnected(), "SFTP agent connection failed");
            try {
                client.sftpClient().put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                        remote, null);
                ShellSFTPFile file = client.sftpClient().fileInfo(remote);
                require(file != null, "agent SFTP metadata is missing");
                client.sftpClient().get(file, local.toString(), null);
                require(content.equals(Files.readString(local, StandardCharsets.UTF_8)),
                        "agent SFTP content mismatch");
            } finally {
                try {
                    if (client.sftpClient().exist(remote)) {
                        client.sftpClient().delete(remote);
                    }
                } catch (Exception ignored) {
                }
            }
        } finally {
            Files.deleteIfExists(local);
        }
        System.out.println("agent SFTP upload/download=PASS");
    }

    private static void verifyConcurrentConnections(String host, int port, String user, int concurrency)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        long start = System.currentTimeMillis();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < concurrency; index++) {
                int taskIndex = index;
                futures.add(executor.submit(() -> {
                    String expected = "agent-concurrent-" + taskIndex;
                    try (ShellSSHClient client = newAgentClient(host, port, user)) {
                        client.start(15000);
                        require(client.isConnected(), "concurrent agent connection failed: " + taskIndex);
                        requireExec(client, expected);
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get(45, TimeUnit.SECONDS);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof Exception exception) {
                        throw exception;
                    }
                    throw ex;
                }
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        System.out.println("concurrent agent connections=" + concurrency
                + ", elapsed=" + (System.currentTimeMillis() - start) + "ms");
    }

    private static void verifyUnauthorizedAgentKeyRejected(ShellSSHClient passwordClient,
                                                            ShellAuthorizedKey authorizedKey,
                                                            String host, int port, String user) {
        removeAuthorizedKey(passwordClient, authorizedKey);
        long start = System.currentTimeMillis();
        try (ShellSSHClient client = newAgentClient(host, port, user)) {
            try {
                client.start(10000);
                throw new IllegalStateException("unauthorized agent key unexpectedly authenticated");
            } catch (ShellException expected) {
                require(!client.isConnected(), "unauthorized agent client is still connected");
                System.out.println("unauthorized agent key rejected: elapsed="
                        + (System.currentTimeMillis() - start) + "ms");
            }
        }
    }

    private static void requireExec(ShellSSHClient client, String expected) {
        String output = client.exec("printf '%s' " + quote(expected), 10000);
        require(output != null && output.contains(expected), "agent Exec output mismatch: " + expected);
    }

    private static ShellAuthorizedKey authorizePublicKey(ShellSSHClient client, Path publicKey, String marker)
            throws Exception {
        String keyLine = publicKeyLine(publicKey, marker);
        String home = client.getUserHome();
        require(home != null && !home.isBlank(), "remote user home is unavailable");
        String sshDir = home + ".ssh";
        String authorizedKeys = sshDir + "/authorized_keys";
        String backup = "/tmp/" + marker + ".authorized_keys.bak";
        boolean existed = client.sftpClient().exist(authorizedKeys);
        if (existed) {
            String output = client.exec("cp -p " + quote(authorizedKeys) + " " + quote(backup)
                    + " && test -f " + quote(backup) + " && echo BACKUP_OK", 10000);
            require(output != null && output.contains("BACKUP_OK"), "authorized_keys backup failed");
        }
        String install = "mkdir -p " + quote(sshDir)
                + " && chmod 700 " + quote(sshDir)
                + " && touch " + quote(authorizedKeys)
                + " && chmod 600 " + quote(authorizedKeys)
                + " && (grep -qxF " + quote(keyLine) + " " + quote(authorizedKeys)
                + " || printf '%s\\n' " + quote(keyLine) + " >> " + quote(authorizedKeys) + ")";
        client.exec(install, 10000);
        String count = client.exec("grep -F -c " + quote(marker) + " " + quote(authorizedKeys), 10000);
        require(count != null && "1".equals(count.trim()), "agent public key was not authorized exactly once");
        System.out.println("agent public key authorized: existed=" + existed);
        return new ShellAuthorizedKey(authorizedKeys, backup, existed, marker);
    }

    private static void removeAuthorizedKey(ShellSSHClient client, ShellAuthorizedKey key) {
        String temporary = key.authorizedKeys + ".tmp";
        client.exec("grep -vF " + quote(key.marker) + " " + quote(key.authorizedKeys)
                + " > " + quote(temporary) + " || true; "
                + "mv " + quote(temporary) + " " + quote(key.authorizedKeys) + "; "
                + "chmod 600 " + quote(key.authorizedKeys), 10000);
    }

    private static void restoreAuthorizedKey(ShellSSHClient client, ShellAuthorizedKey key) {
        if (key.existed) {
            String output = client.exec("cp -p " + quote(key.backup) + " " + quote(key.authorizedKeys)
                    + " && cmp -s " + quote(key.backup) + " " + quote(key.authorizedKeys)
                    + " && rm -f " + quote(key.backup) + " && echo RESTORED", 10000);
            require(output != null && output.contains("RESTORED"), "authorized_keys restore failed");
            System.out.println("authorized_keys restored");
        } else {
            String output = client.exec("rm -f " + quote(key.backup) + " " + quote(key.authorizedKeys)
                    + " && test ! -e " + quote(key.authorizedKeys) + " && echo REMOVED", 10000);
            require(output != null && output.contains("REMOVED"), "temporary authorized_keys cleanup failed");
            System.out.println("temporary authorized_keys removed");
        }
    }

    private static String publicKeyLine(Path publicKey, String marker) throws Exception {
        String[] parts = Files.readString(publicKey, StandardCharsets.UTF_8).trim().split("\\s+");
        require(parts.length >= 2, "agent public key format is invalid");
        return parts[0] + " " + parts[1] + " " + marker;
    }

    private static ShellSSHClient newPasswordClient(String host, int port, String user, String password) {
        return newClient(host, port, user, "password", password);
    }

    private static ShellSSHClient newAgentClient(String host, int port, String user) {
        return newClient(host, port, user, "sshAgent", null);
    }

    private static ShellSSHClient newClient(String host, int port, String user,
                                            String authMethod, String password) {
        ShellConnect connect = new ShellConnect();
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod(authMethod);
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        return client;
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
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

    private record ShellAuthorizedKey(String authorizedKeys, String backup, boolean existed, String marker) {
    }
}
