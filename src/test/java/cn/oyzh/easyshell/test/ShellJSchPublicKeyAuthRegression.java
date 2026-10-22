package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellKeyUtil;
import cn.oyzh.ssh.jsch.JSchKeyUtil;
import cn.oyzh.store.jdbc.JdbcConn;
import cn.oyzh.store.jdbc.JdbcConst;
import cn.oyzh.store.jdbc.JdbcDialect;
import cn.oyzh.store.jdbc.JdbcManager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 公钥认证实机回归。先通过密码会话上传公钥，再分别验证密钥管理器和私钥文件认证。
 */
public final class ShellJSchPublicKeyAuthRegression {

    private static final String KEY_STORE_ID_PREFIX = "jsch-pubkey-test-";

    private ShellJSchPublicKeyAuthRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch public key authentication regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.pubkey.host.index", 1);
        long authTimeout = Long.getLong("test.pubkey.auth.timeout.millis", 15000L);
        long maxFailureMillis = Long.getLong("test.pubkey.max.failure.millis", 15000L);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Public key auth target: " + host);

        Path localDir = Files.createTempDirectory("easyshell-jsch-pubkey-");
        Path storeDir = localDir.resolve("store");
        Files.createDirectories(storeDir);
        List<String> keyIds = new ArrayList<>();
        List<GeneratedKey> supportedKeys = new ArrayList<>();
        ShellSSHClient passwordClient = null;
        boolean remotePrepared = false;
        boolean authorizedKeysExisted = false;
        Path authorizedKeysBackup = localDir.resolve("authorized_keys.backup");
        String authorizedKeys = null;

        initializeStore(storeDir);
        try {
            List<GeneratedKey> generatedKeys = generateKeys();
            String marker = "easyshell-jsch-pubkey-" + UUID.randomUUID().toString().replace("-", "");

            passwordClient = new ShellSSHClient(host.newConnect());
            passwordClient.setVerifyFailureCallback(current -> null);
            passwordClient.start((int) authTimeout);
            require(passwordClient.isConnected(), "password authentication failed");
            require(!passwordClient.isWindows(), "public key regression requires a Unix test host");

            String home = passwordClient.getUserHome();
            String sshDir = home + ".ssh";
            authorizedKeys = sshDir + "/authorized_keys";
            String chmodCommand = "mkdir -p " + quote(sshDir)
                    + " && chmod 700 " + quote(sshDir)
                    + " && touch " + quote(authorizedKeys)
                    + " && chmod 600 " + quote(authorizedKeys);
            passwordClient.exec(chmodCommand, (int) authTimeout);
            authorizeServerKeyAlgorithms(passwordClient, generatedKeys);
            for (GeneratedKey key : generatedKeys) {
                if (key.serverEnabled()) {
                    supportedKeys.add(key);
                }
            }
            require(!supportedKeys.isEmpty(), "server does not accept any generated public key algorithm");

            authorizedKeysExisted = passwordClient.sftpClient().exist(authorizedKeys);
            if (authorizedKeysExisted) {
                ShellSFTPFile backupFile = passwordClient.sftpClient().fileInfo(authorizedKeys);
                require(backupFile != null, "authorized_keys metadata is missing");
                passwordClient.sftpClient().get(backupFile, authorizedKeysBackup.toString(), null);
            }
            remotePrepared = true;

            List<ShellKey> uploadKeys = new ArrayList<>();
            for (GeneratedKey key : supportedKeys) {
                ShellKey uploadKey = new ShellKey();
                uploadKey.setId(UUID.randomUUID().toString());
                uploadKey.setPublicKey(key.publicKey().trim() + " " + marker + "-" + key.algorithm());
                uploadKeys.add(uploadKey);
            }
            require(ShellKeyUtil.sshCopyId(uploadKeys, passwordClient), "sshCopyId upload failed");
            String count = passwordClient.exec("grep -F -c " + quote(marker) + " " + quote(authorizedKeys),
                    (int) authTimeout);
            require(count != null && Integer.parseInt(count.trim()) == supportedKeys.size(),
                    "uploaded public key count mismatch: " + count);
            System.out.println("password upload verified: generated=" + generatedKeys.size()
                    + ", serverEnabled=" + supportedKeys.size());

            for (GeneratedKey key : supportedKeys) {
                verifyManagerAuth(host, key, keyIds, authTimeout, marker, localDir);
            }
            verifyCertificateAuth(host, find(supportedKeys, "RSA-2048"), authTimeout, marker, localDir);
            verifyCertificateAuth(host, find(supportedKeys, "RSA-2048-PASSPHRASE"), authTimeout, marker, localDir);
            verifyCertificateAuth(host, find(supportedKeys, "ED25519-256"), authTimeout, marker, localDir);
            verifyUnauthorizedKeyFailure(host, authTimeout, maxFailureMillis, keyIds);
            System.out.println("public key auth matrix completed: manager=" + supportedKeys.size()
                    + ", certificate=3, unauthorized=rejected");
        } finally {
            Throwable cleanupFailure = null;
            try {
                if (passwordClient != null && remotePrepared) {
                    restoreAuthorizedKeys(passwordClient, authorizedKeys, authorizedKeysExisted,
                            authorizedKeysBackup);
                }
            } catch (Throwable ex) {
                cleanupFailure = ex;
            }
            if (passwordClient != null) {
                try {
                    passwordClient.close();
                } catch (Throwable ex) {
                    if (cleanupFailure == null) {
                        cleanupFailure = ex;
                    }
                }
            }
            ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
            for (String keyId : keyIds) {
                keyStore.delete(keyId);
            }
            JdbcManager.destroy();
            deleteTree(storeDir);
            deleteTree(localDir);
            if (cleanupFailure != null) {
                throw new IllegalStateException("public key authorization cleanup failed", cleanupFailure);
            }
        }
    }

    private static List<GeneratedKey> generateKeys() throws Exception {
        List<GeneratedKey> keys = new ArrayList<>();
        keys.add(generate("RSA-2048", JSchKeyUtil.generateRsa(2048, null), null));
        keys.add(generate("RSA-2048-PASSPHRASE", JSchKeyUtil.generateRsa(2048, "jsch-pubkey-passphrase"),
                "jsch-pubkey-passphrase"));
        keys.add(generate("ECDSA-256", JSchKeyUtil.generateEcdsa(256, null), null));
        keys.add(generate("ECDSA-384", JSchKeyUtil.generateEcdsa(384, null), null));
        keys.add(generate("ECDSA-521", JSchKeyUtil.generateEcdsa(521, null), null));
        keys.add(generate("ED25519-256", JSchKeyUtil.generateEd25519(256, null), null));
        keys.add(generate("DSA-1024", JSchKeyUtil.generateDsa(1024, null), null));
        for (GeneratedKey key : keys) {
            System.out.println("generated key: algorithm=" + key.algorithm() + ", type=" + key.type()
                    + ", size=" + key.size() + ", encrypted=" + key.hasPassphrase());
        }
        return keys;
    }

    private static GeneratedKey generate(String algorithm, String[] key, String passphrase) throws Exception {
        require(key != null && key.length == 2, algorithm + " key generation returned no key pair");
        String type = JSchKeyUtil.getKeyType(key[0], null);
        int size = JSchKeyUtil.getKeySize(key[0], null);
        require(type != null && size > 0, algorithm + " public key could not be parsed");
        return new GeneratedKey(algorithm, type, size, passphrase, key[0], key[1], true);
    }

    private static void authorizeServerKeyAlgorithms(ShellSSHClient client, List<GeneratedKey> keys) {
        Set<String> accepted = acceptedServerKeyAlgorithms(client);
        if (accepted == null) {
            System.out.println("server accepted-key query unavailable; all generated algorithms will be attempted");
            return;
        }
        for (int i = 0; i < keys.size(); i++) {
            GeneratedKey key = keys.get(i);
            boolean enabled = switch (key.type()) {
                case "ssh-rsa" -> accepted.contains("rsa-sha2-256")
                        || accepted.contains("rsa-sha2-512") || accepted.contains("ssh-rsa");
                case "ssh-dss" -> accepted.contains("ssh-dss");
                default -> accepted.contains(key.type());
            };
            keys.set(i, key.withServerEnabled(enabled));
            if (!enabled) {
                System.out.println("server policy disabled key algorithm: " + key.algorithm() + " (" + key.type() + ")");
            }
        }
    }

    private static Set<String> acceptedServerKeyAlgorithms(ShellSSHClient client) {
        String output = client.exec("sshd -T 2>&1 | grep -i '^pubkeyacceptedalgorithms'", 10000);
        if (output == null || (!output.contains("ssh-ed25519") && !output.contains("rsa-sha2-256"))) {
            output = client.exec("ssh -Q PubkeyAcceptedAlgorithms", 10000);
            if (output == null || (!output.contains("ssh-ed25519") && !output.contains("rsa-sha2-256"))) {
                return null;
            }
        }
        Set<String> algorithms = new LinkedHashSet<>();
        for (String line : output.replace(',', '\n').split("\\R")) {
            String value = line.trim();
            if (!value.isEmpty() && !value.toLowerCase().contains("pubkeyacceptedalgorithms")) {
                algorithms.add(value);
            }
        }
        return algorithms;
    }

    private static void verifyManagerAuth(ShellJSchTestHosts.Host host, GeneratedKey key, List<String> keyIds,
                                          long timeout, String marker, Path localDir) throws Exception {
        String keyId = KEY_STORE_ID_PREFIX + UUID.randomUUID();
        ShellKey shellKey = new ShellKey();
        shellKey.setId(keyId);
        shellKey.setName("pubkey-" + key.algorithm());
        shellKey.setType(key.type());
        shellKey.setLength(key.size());
        shellKey.setPassword(key.passphrase());
        shellKey.setPublicKey(key.publicKey());
        shellKey.setPrivateKey(key.privateKey());
        require(ShellKeyStore.INSTANCE.replace(shellKey), "failed to persist manager key: " + key.algorithm());
        keyIds.add(keyId);

        ShellConnect connect = host.newConnect();
        connect.setPassword(null);
        connect.setAuthMethod("manager");
        connect.setKeyId(keyId);
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        long start = System.currentTimeMillis();
        try {
            client.start((int) timeout);
            require(client.isConnected(), key.algorithm() + " manager authentication failed");
            String output = client.exec("printf '%s' " + quote(marker + "-" + key.algorithm()), (int) timeout);
            require(output != null && output.contains(marker + "-" + key.algorithm()),
                    key.algorithm() + " manager exec marker mismatch");
            verifySftp(client, key.algorithm(), marker, localDir);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("manager auth verified: algorithm=" + key.algorithm()
                    + ", exec=PASS, sftp=PASS, elapsed=" + elapsed + "ms");
        } finally {
            client.close();
        }
    }

    private static void verifyCertificateAuth(ShellJSchTestHosts.Host host, GeneratedKey key, long timeout,
                                              String marker, Path localDir) throws Exception {
        require(key != null, "certificate authentication key is unavailable");
        Path privateKey = localDir.resolve("certificate-" + key.algorithm() + "-" + UUID.randomUUID() + ".key");
        Files.writeString(privateKey, key.privateKey(), StandardCharsets.UTF_8);
        try {
            Files.setPosixFilePermissions(privateKey, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
        }

        ShellConnect connect = host.newConnect();
        connect.setPassword(null);
        connect.setAuthMethod("certificate");
        connect.setCertificate(privateKey.toString());
        connect.setCertificatePwd(key.passphrase());
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            client.start((int) timeout);
            require(client.isConnected(), key.algorithm() + " certificate authentication failed");
            String output = client.exec("printf '%s' " + quote(marker + "-cert"), (int) timeout);
            require(output != null && output.contains(marker + "-cert"),
                    key.algorithm() + " certificate exec marker mismatch");
            System.out.println("certificate auth verified: algorithm=" + key.algorithm()
                    + ", encrypted=" + key.hasPassphrase() + ", exec=PASS");
        } finally {
            Files.deleteIfExists(privateKey);
        }
    }

    private static void verifyUnauthorizedKeyFailure(ShellJSchTestHosts.Host host, long timeout,
                                                     long maxFailureMillis, List<String> keyIds)
            throws Exception {
        GeneratedKey key = generate("UNAUTHORIZED-RSA-2048", JSchKeyUtil.generateRsa(2048, null), null);
        String keyId = KEY_STORE_ID_PREFIX + UUID.randomUUID();
        ShellKey shellKey = new ShellKey();
        shellKey.setId(keyId);
        shellKey.setName("unauthorized-" + key.algorithm());
        shellKey.setType(key.type());
        shellKey.setLength(key.size());
        shellKey.setPublicKey(key.publicKey());
        shellKey.setPrivateKey(key.privateKey());
        require(ShellKeyStore.INSTANCE.replace(shellKey), "failed to persist unauthorized key");
        keyIds.add(keyId);

        ShellConnect connect = host.newConnect();
        connect.setPassword(null);
        connect.setAuthMethod("manager");
        connect.setKeyId(keyId);
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        long start = System.currentTimeMillis();
        try {
            try {
                client.start((int) timeout);
                throw new IllegalStateException("unauthorized public key unexpectedly authenticated");
            } catch (ShellException expected) {
                long elapsed = System.currentTimeMillis() - start;
                require(!client.isConnected(), "unauthorized client is still marked connected");
                require(elapsed < maxFailureMillis,
                        "unauthorized key rejection was too slow: " + elapsed + "ms");
                System.out.println("unauthorized key rejected: elapsed=" + elapsed + "ms, failure="
                        + expected.getClass().getSimpleName());
            }
        } finally {
            client.close();
        }
    }

    private static void verifySftp(ShellSSHClient client, String algorithm, String marker, Path localDir)
            throws Exception {
        String token = UUID.randomUUID().toString().replace("-", "");
        String remote = "/tmp/easyshell-jsch-pubkey-" + token + ".txt";
        Path downloaded = localDir.resolve("sftp-" + token + ".txt");
        String content = marker + "-sftp-" + algorithm;
        try {
            client.sftpClient().put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    remote, null);
            ShellSFTPFile file = client.sftpClient().fileInfo(remote);
            require(file != null, algorithm + " SFTP metadata is missing");
            client.sftpClient().get(file, downloaded.toString(), null);
            require(content.equals(Files.readString(downloaded, StandardCharsets.UTF_8)),
                    algorithm + " SFTP content mismatch");
        } finally {
            try {
                if (client.sftpClient().exist(remote)) {
                    client.sftpClient().delete(remote);
                }
            } catch (Exception ignored) {
            }
            Files.deleteIfExists(downloaded);
        }
    }

    private static void restoreAuthorizedKeys(ShellSSHClient client, String authorizedKeys, boolean existed,
                                              Path backup) throws Exception {
        if (existed && Files.isRegularFile(backup)) {
            client.sftpClient().put(backup.toFile(), authorizedKeys, null);
            client.exec("chmod 600 " + quote(authorizedKeys), 10000);
            Path verification = backup.resolveSibling("authorized_keys.verify");
            try {
                ShellSFTPFile restored = client.sftpClient().fileInfo(authorizedKeys);
                require(restored != null, "restored authorized_keys metadata is missing");
                client.sftpClient().get(restored, verification.toString(), null);
                require(Arrays.equals(Files.readAllBytes(backup), Files.readAllBytes(verification)),
                        "authorized_keys content was not restored exactly");
            } finally {
                Files.deleteIfExists(verification);
            }
            System.out.println("authorized_keys restored and byte-compared with backup");
        } else {
            client.exec("rm -f " + quote(authorizedKeys), 10000);
            require(!client.sftpClient().exist(authorizedKeys), "temporary authorized_keys was not removed");
            System.out.println("temporary authorized_keys removed and absence verified");
        }
    }

    private static void initializeStore(Path storeDir) throws Exception {
        JdbcConst.dbDialect(JdbcDialect.H2);
        JdbcConst.dbFile(storeDir.resolve("db").toString());
        JdbcConn connection = JdbcManager.takeoff();
        JdbcManager.giveback(connection);
    }

    private static GeneratedKey find(List<GeneratedKey> keys, String algorithm) {
        return keys.stream().filter(key -> algorithm.equals(key.algorithm())).findFirst().orElse(null);
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static void deleteTree(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record GeneratedKey(String algorithm, String type, int size, String passphrase,
                                String publicKey, String privateKey, boolean serverEnabled) {

        private boolean hasPassphrase() {
            return this.passphrase != null && !this.passphrase.isEmpty();
        }

        private GeneratedKey withServerEnabled(boolean enabled) {
            return new GeneratedKey(this.algorithm, this.type, this.size, this.passphrase,
                    this.publicKey, this.privateKey, enabled);
        }
    }
}
