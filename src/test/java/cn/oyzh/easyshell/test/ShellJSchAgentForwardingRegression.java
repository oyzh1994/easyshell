package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.ssh.util.JschUtil;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.Identity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 验证 JSch Agent 转发和 SSH 环境变量在 Exec、Shell 及嵌套 SSH 中的行为。
 */
public final class ShellJSchAgentForwardingRegression {

    private ShellJSchAgentForwardingRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch agent forwarding and environment regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        String jumpHost = System.getenv().getOrDefault("EASYSHELL_FORWARD_JUMP_HOST", "127.0.0.1");
        int jumpPort = Integer.parseInt(System.getenv().getOrDefault("EASYSHELL_FORWARD_JUMP_PORT", "2223"));
        String targetEndpointHost = System.getenv().getOrDefault("EASYSHELL_FORWARD_TARGET_HOST", "127.0.0.1");
        int targetEndpointPort = Integer.parseInt(System.getenv().getOrDefault("EASYSHELL_FORWARD_TARGET_PORT", "2222"));
        String targetSshHost = System.getenv().getOrDefault("EASYSHELL_FORWARD_TARGET_SSH_HOST", "target");
        int targetSshPort = Integer.parseInt(System.getenv().getOrDefault("EASYSHELL_FORWARD_TARGET_SSH_PORT", "22"));
        String user = System.getenv().getOrDefault("EASYSHELL_FORWARD_USER", "root");
        String password = requireEnv("EASYSHELL_FORWARD_PASSWORD");
        Path publicKey = Path.of(requireEnv("EASYSHELL_AGENT_PUBLIC_KEY"));
        Path privateKey = Path.of(requireEnv("EASYSHELL_AGENT_PRIVATE_KEY"));
        String envName = System.getenv().getOrDefault("EASYSHELL_FORWARD_ENV_NAME", "EASYSHELL_TEST_ENV");
        String envValue = System.getenv().getOrDefault("EASYSHELL_FORWARD_ENV_VALUE", "agent-forward-env");

        require(Files.isRegularFile(publicKey), "agent public key file is missing");
        require(!Files.exists(privateKey), "private key file still exists; agent storage was not proven");
        verifyAgentIdentities();

        String marker = "easyshell-forward-" + UUID.randomUUID().toString().replace("-", "");
        String environment = envName + "=" + envValue + "\nEASYSHELL_TEST_SECOND=" + envValue + "-second";
        ShellSSHClient jumpPasswordClient = newPasswordClient(jumpHost, jumpPort, user, password);
        ShellSSHClient targetPasswordClient = newPasswordClient(targetEndpointHost, targetEndpointPort, user, password);
        AuthorizedKey jumpAuthorizedKey = null;
        AuthorizedKey targetAuthorizedKey = null;
        try {
            jumpPasswordClient.start(15000);
            targetPasswordClient.start(15000);
            require(jumpPasswordClient.isConnected(), "jump password session failed");
            require(targetPasswordClient.isConnected(), "target password session failed");
            targetAuthorizedKey = authorizePublicKey(targetPasswordClient, publicKey, marker + "-target");

            // Password auth to the jump host plus forwarded agent auth to the target. The agent key
            // is deliberately not authorized on the jump host yet, proving forwardAgent works even
            // when the outer connection did not authenticate through the agent.
            try (ShellSSHClient client = newPasswordForwardClient(jumpHost, jumpPort, user, password, environment)) {
                client.start(15000);
                require(client.isConnected(), "password forwarding jump connection failed");
                verifyExecForwarding(client, envName, envValue, targetSshHost, targetSshPort, user, marker, "password");
                verifyShellEnvironment(client, envName, envValue);
            }

            jumpAuthorizedKey = authorizePublicKey(jumpPasswordClient, publicKey, marker + "-jump");
            try (ShellSSHClient client = newAgentClient(jumpHost, jumpPort, user, true, environment)) {
                client.start(15000);
                require(client.isConnected(), "agent forwarding jump connection failed");
                verifyExecForwarding(client, envName, envValue, targetSshHost, targetSshPort, user, marker, "agent");
            }
            verifyForwardingDisabled(jumpHost, jumpPort, user, environment, targetSshHost, targetSshPort);
        } finally {
            if (jumpAuthorizedKey != null) {
                restoreAuthorizedKey(jumpPasswordClient, jumpAuthorizedKey);
            }
            if (targetAuthorizedKey != null) {
                restoreAuthorizedKey(targetPasswordClient, targetAuthorizedKey);
            }
            jumpPasswordClient.close();
            targetPasswordClient.close();
        }

        System.out.println("agent forwarding verified: exec=PASS, shell=PASS, nested-env=PASS"
                + ", forward-disabled=rejected");
    }

    private static void verifyAgentIdentities() throws Exception {
        AgentIdentityRepository repository = JschUtil.initAgentIdentityRepository();
        List<Identity> identities = new ArrayList<>(repository.getIdentities());
        require(!identities.isEmpty(), "ssh-agent contains no identities");
        System.out.println("ssh-agent identities=" + identities.size());
    }

    private static void verifyExecForwarding(ShellSSHClient client, String envName, String envValue,
                                             String targetHost, int targetPort, String user, String marker,
                                             String authMode) {
        String output = client.exec("printf 'EXEC_ENV=%s\\n' \"$" + envName + "\"; "
                + "printf 'EXEC_SECOND=%s\\n' \"$EASYSHELL_TEST_SECOND\"; "
                + "printf 'EXEC_AUTH_SOCK=%s\\n' \"${SSH_AUTH_SOCK:-none}\"; ssh-add -l", 15000);
        require(output != null && output.contains("EXEC_ENV=" + envValue), "Exec environment was not applied");
        require(output.contains("EXEC_SECOND=" + envValue + "-second"), "second Exec environment was not applied");
        require(output.contains("EXEC_AUTH_SOCK=/"), "Exec channel did not expose the forwarded agent socket");
        require(output.contains("ED25519"), "forwarded agent did not expose the Ed25519 identity");

        for (int index = 0; index < 3; index++) {
            String targetMarker = marker + "-target-" + index;
            String targetCommand = "printf 'TARGET_ENV=%s\\n' \"$" + envName + "\"; "
                    + "printf 'TARGET_MARKER=" + targetMarker + "\\n'";
            String sshCommand = "ssh -o BatchMode=yes -o StrictHostKeyChecking=no "
                    + "-o UserKnownHostsFile=/dev/null -o SendEnv=" + envName
                    + " -p " + targetPort + " " + user + "@" + targetHost + " " + quote(targetCommand);
            String forwarded = client.exec(sshCommand + " && printf 'AGENT_FORWARD_OK=" + index + "\\n'", 30000);
            require(forwarded != null && forwarded.contains("TARGET_ENV=" + envValue),
                    "nested SSH did not receive the environment variable");
            require(forwarded.contains("TARGET_MARKER=" + targetMarker),
                    "nested SSH did not reach the target host");
            require(forwarded.contains("AGENT_FORWARD_OK=" + index),
                    "nested SSH did not authenticate through the forwarded agent");
        }
        System.out.println("agent forwarding Exec and nested SSH(" + authMode + ")=3/3");
    }

    private static void verifyShellEnvironment(ShellSSHClient client, String envName, String envValue)
            throws Exception {
        ShellSSHShell shell = client.openShell();
        InputStream input = shell.getInputStream();
        OutputStream output = shell.getOutputStream();
        readAvailable(input, 1200);
        String command = "printf 'SHELL_ENV=%s\\n' \"$" + envName + "\"; "
                + "printf 'SHELL_AUTH_SOCK=%s\\n' \"${SSH_AUTH_SOCK:-none}\"; "
                + "ssh-add -l; printf 'SHELL_DONE=%s\\n' \"$" + envName + "\"\r";
        output.write(command.getBytes(StandardCharsets.UTF_8));
        output.flush();
        // The command line itself contains only the variable reference. Waiting for the expanded
        // value avoids matching a terminal echo before ssh-add has produced its output.
        String text = readUntil(input, "SHELL_DONE=" + envValue, 10000);
        require(text.contains("SHELL_ENV=" + envValue), "Shell environment was not applied");
        require(text.contains("SHELL_AUTH_SOCK=/"), "Shell channel did not expose the forwarded agent socket");
        require(text.contains("ED25519"), "Shell forwarded agent did not expose the Ed25519 identity");
        System.out.println("agent forwarding Shell and environment=PASS");
    }

    private static void verifyForwardingDisabled(String jumpHost, int jumpPort, String user, String environment,
                                                 String targetHost, int targetPort) {
        try (ShellSSHClient client = newAgentClient(jumpHost, jumpPort, user, false, environment)) {
            client.start(15000);
            require(client.isConnected(), "agent connection without forwarding failed");
            String targetCommand = "printf 'NO_FORWARD_TARGET_MARKER\\n'";
            String sshCommand = "ssh -o BatchMode=yes -o StrictHostKeyChecking=no "
                    + "-o UserKnownHostsFile=/dev/null -p " + targetPort + " "
                    + user + "@" + targetHost + " " + quote(targetCommand) + " 2>&1";
            String output = client.exec("printf 'CONTROL_AUTH_SOCK=%s\\n' \"${SSH_AUTH_SOCK:-none}\"; "
                    + sshCommand + "; printf 'CONTROL_RC=%s\\n' \"$?\"", 30000);
            require(output != null && output.contains("CONTROL_AUTH_SOCK=none"),
                    "agent socket was exposed even though forwarding was disabled");
            require(!output.contains("NO_FORWARD_TARGET_MARKER"),
                    "nested SSH unexpectedly succeeded without agent forwarding");
            require(output.contains("CONTROL_RC=255") || output.contains("CONTROL_RC=1"),
                    "nested SSH without forwarding did not fail as expected");
        }
        System.out.println("agent forwarding disabled control=rejected");
    }

    private static AuthorizedKey authorizePublicKey(ShellSSHClient client, Path publicKey, String marker)
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
        System.out.println("agent public key authorized: role=" + marker + ", existed=" + existed);
        return new AuthorizedKey(authorizedKeys, backup, existed);
    }

    private static void restoreAuthorizedKey(ShellSSHClient client, AuthorizedKey key) {
        if (key.existed) {
            String output = client.exec("cp -p " + quote(key.backup) + " " + quote(key.authorizedKeys)
                    + " && cmp -s " + quote(key.backup) + " " + quote(key.authorizedKeys)
                    + " && rm -f " + quote(key.backup) + " && echo RESTORED", 10000);
            require(output != null && output.contains("RESTORED"), "authorized_keys restore failed");
        } else {
            String output = client.exec("rm -f " + quote(key.backup) + " " + quote(key.authorizedKeys)
                    + " && test ! -e " + quote(key.authorizedKeys) + " && echo REMOVED", 10000);
            require(output != null && output.contains("REMOVED"), "temporary authorized_keys cleanup failed");
        }
        System.out.println("authorized_keys cleaned: " + key.authorizedKeys);
    }

    private static String publicKeyLine(Path publicKey, String marker) throws Exception {
        String[] parts = Files.readString(publicKey, StandardCharsets.UTF_8).trim().split("\\s+");
        require(parts.length >= 2, "agent public key format is invalid");
        return parts[0] + " " + parts[1] + " " + marker;
    }

    private static String readAvailable(InputStream input, long maxWaitMillis) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[4096];
        while (System.currentTimeMillis() - start < maxWaitMillis) {
            while (input.available() > 0) {
                int read = input.read(buffer, 0, Math.min(input.available(), buffer.length));
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            }
            Thread.sleep(20L);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    private static String readUntil(InputStream input, String marker, long maxWaitMillis) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[4096];
        String value = "";
        while (System.currentTimeMillis() - start < maxWaitMillis) {
            while (input.available() > 0) {
                int read = input.read(buffer, 0, Math.min(input.available(), buffer.length));
                if (read > 0) {
                    output.write(buffer, 0, read);
                    value = output.toString(StandardCharsets.UTF_8);
                    if (value.contains(marker)) {
                        return value;
                    }
                }
            }
            Thread.sleep(20L);
        }
        return value;
    }

    private static ShellSSHClient newPasswordClient(String host, int port, String user, String password) {
        return newClient(host, port, user, "password", password, false, null);
    }

    private static ShellSSHClient newPasswordForwardClient(String host, int port, String user,
                                                           String password, String environment) {
        return newClient(host, port, user, "password", password, true, environment);
    }

    private static ShellSSHClient newAgentClient(String host, int port, String user,
                                                 boolean forwardAgent, String environment) {
        return newClient(host, port, user, "sshAgent", null, forwardAgent, environment);
    }

    private static ShellSSHClient newClient(String host, int port, String user, String authMethod,
                                            String password, boolean forwardAgent, String environment) {
        ShellConnect connect = new ShellConnect();
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod(authMethod);
        connect.setForwardAgent(forwardAgent);
        connect.setEnvironment(environment);
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

    private record AuthorizedKey(String authorizedKeys, String backup, boolean existed) {
    }
}
