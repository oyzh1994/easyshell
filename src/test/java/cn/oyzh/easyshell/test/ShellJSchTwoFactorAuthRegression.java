package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证 JSch 的 keyboard-interactive 密码 + TOTP 双因素认证。
 */
public final class ShellJSchTwoFactorAuthRegression {

    private ShellJSchTwoFactorAuthRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch 2FA authentication regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        String host = System.getenv().getOrDefault("EASYSHELL_2FA_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("EASYSHELL_2FA_PORT", "2224"));
        String user = System.getenv().getOrDefault("EASYSHELL_2FA_USER", "root");
        String password = requireEnv("EASYSHELL_2FA_PASSWORD");
        String secret = requireEnv("EASYSHELL_2FA_SECRET");
        System.out.println("2FA auth target: host=" + host + ", port=" + port + ", user=" + user);

        verifySuccessfulAuthentication(host, port, user, password, secret);
        verifyWrongTotpRejected(host, port, user, password, secret);
    }

    private static void verifySuccessfulAuthentication(String host, int port, String user,
                                                        String password, String secret) throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);

        AtomicInteger supplierCalls = new AtomicInteger();
        Path downloaded = Files.createTempFile("easyshell-2fa-", ".txt");
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            client.setVerificationCodeSupplier(() -> {
                supplierCalls.incrementAndGet();
                return totpUnchecked(secret, 0);
            });
            long start = System.currentTimeMillis();
            client.start(15000);
            require(client.isConnected(), "2FA keyboard-interactive authentication failed");

            String marker = "easyshell-2fa-" + UUID.randomUUID().toString().replace("-", "");
            String output = client.exec("printf '%s' " + quote(marker), 10000);
            require(output != null && output.contains(marker), "2FA Exec marker mismatch");
            verifySftp(client, marker, downloaded);
            System.out.println("2FA authentication verified: supplierCalls=" + supplierCalls.get()
                    + ", exec=PASS, sftp=PASS, elapsed=" + (System.currentTimeMillis() - start) + "ms");
        } finally {
            Files.deleteIfExists(downloaded);
        }
    }

    private static void verifyWrongTotpRejected(String host, int port, String user,
                                                String password, String secret) throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);

        String wrongCode = invalidTotp(secret);
        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicInteger authCallbacks = new AtomicInteger();
        long start = System.currentTimeMillis();
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> {
                authCallbacks.incrementAndGet();
                return null;
            });
            client.setVerificationCodeSupplier(() -> {
                supplierCalls.incrementAndGet();
                return wrongCode;
            });
            try {
                client.start(15000);
                throw new IllegalStateException("wrong TOTP unexpectedly authenticated");
            } catch (ShellException expected) {
                long elapsed = System.currentTimeMillis() - start;
                require(!client.isConnected(), "wrong TOTP client is still marked connected");
                require(supplierCalls.get() == 1,
                        "wrong TOTP was submitted more than once: " + supplierCalls.get());
                require(authCallbacks.get() == 1,
                        "wrong TOTP did not trigger the interactive authentication callback: " + authCallbacks.get());
                require(elapsed < 15000, "wrong TOTP rejection was too slow: " + elapsed + "ms");
                System.out.println("wrong TOTP rejected: supplierCalls=" + supplierCalls.get()
                        + ", authCallbacks=" + authCallbacks.get() + ", elapsed=" + elapsed + "ms, failure="
                        + expected.getClass().getSimpleName());
            }
        }
    }

    private static void verifySftp(ShellSSHClient client, String marker, Path downloaded) throws Exception {
        String remote = "/tmp/" + marker + ".txt";
        String content = marker + "-sftp";
        try {
            client.sftpClient().put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    remote, null);
            ShellSFTPFile file = client.sftpClient().fileInfo(remote);
            require(file != null, "2FA SFTP metadata is missing");
            client.sftpClient().get(file, downloaded.toString(), null);
            require(content.equals(Files.readString(downloaded, StandardCharsets.UTF_8)),
                    "2FA SFTP content mismatch");
        } finally {
            try {
                if (client.sftpClient().exist(remote)) {
                    client.sftpClient().delete(remote);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static String totp(String secret, long stepOffset) throws Exception {
        byte[] key = decodeBase32(secret);
        long counter = System.currentTimeMillis() / 1000L / 30L + stepOffset;
        byte[] data = ByteBuffer.allocate(Long.BYTES).putLong(counter).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0x0f;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        return String.format(Locale.ROOT, "%06d", binary % 1_000_000);
    }

    private static String totpUnchecked(String secret, long stepOffset) {
        try {
            return totp(secret, stepOffset);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to generate TOTP", ex);
        }
    }

    private static String invalidTotp(String secret) {
        while (true) {
            String candidate = String.format(Locale.ROOT, "%06d",
                    ThreadLocalRandom.current().nextInt(1_000_000));
            boolean valid = false;
            for (int offset = -5; offset <= 5; offset++) {
                if (candidate.equals(totpUnchecked(secret, offset))) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                return candidate;
            }
        }
    }

    private static byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        int buffer = 0;
        int bitsLeft = 0;
        byte[] output = new byte[normalized.length() * 5 / 8];
        int index = 0;
        for (char character : normalized.toCharArray()) {
            int digit;
            if (character >= 'A' && character <= 'Z') {
                digit = character - 'A';
            } else if (character >= '2' && character <= '7') {
                digit = character - '2' + 26;
            } else {
                throw new IllegalArgumentException("invalid Base32 TOTP secret");
            }
            buffer = (buffer << 5) | digit;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return output;
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
}
