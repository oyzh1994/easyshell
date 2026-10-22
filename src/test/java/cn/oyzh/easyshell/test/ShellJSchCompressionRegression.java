package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public final class ShellJSchCompressionRegression {

    private ShellJSchCompressionRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch compression regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Compression target: " + host);

        requireTransportCompression(host, false);
        requireTransportCompression(host, true);

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            requireFileCompression(client);
            requireSftpTransfer(client);
        }
    }

    private static void requireTransportCompression(ShellJSchTestHosts.Host host, boolean enabled) throws Exception {
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.getShellConnect().setEnableCompress(enabled);
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            Session session = client.getSession();
            require(session != null && session.isConnected(), "ssh session is not connected");
            String c2s = session.getCompressionAlgorithmC2S();
            String s2c = session.getCompressionAlgorithmS2C();
            requireNotBlank(c2s, "client-to-server compression algorithm is blank");
            requireNotBlank(s2c, "server-to-client compression algorithm is blank");
            if (enabled) {
                require(!"none".equalsIgnoreCase(c2s), "compression negotiation failed for c2s: " + c2s);
                require(!"none".equalsIgnoreCase(s2c), "compression negotiation failed for s2c: " + s2c);
            } else {
                require("none".equalsIgnoreCase(c2s), "compression should be disabled for c2s: " + c2s);
                require("none".equalsIgnoreCase(s2c), "compression should be disabled for s2c: " + s2c);
            }

            String pattern = "0123456789abcdef";
            int repeat = enabled ? 262_144 : 32_768;
            String output = client.exec("python3 -c \"import sys;sys.stdout.write('" + pattern + "'*" + repeat + ")\"", 60000);
            byte[] expected = pattern.repeat(repeat).getBytes(StandardCharsets.UTF_8);
            require(output != null, "large exec output is null");
            require(output.getBytes(StandardCharsets.UTF_8).length == expected.length,
                    "large exec output was truncated under compression=" + enabled);
            require(sha256(expected).equals(sha256(output.getBytes(StandardCharsets.UTF_8))),
                    "large exec output checksum mismatch under compression=" + enabled);
            System.out.println("Transport compression=" + enabled + " verified: c2s=" + c2s + ", s2c=" + s2c
                    + ", bytes=" + expected.length);
        }
    }

    private static void requireFileCompression(ShellSSHClient client) throws Exception {
        String token = UUID.randomUUID().toString();
        Path local = Files.createTempFile("easyshell-compress-", ".txt");
        List<String> roots = new ArrayList<>();
        try {
            String content = "compression-content-" + token + "\n" + "line\n".repeat(2000);
            Files.writeString(local, content, StandardCharsets.UTF_8);
            for (String type : List.of("tgz", "xz", "bz2")) {
                String root = "/tmp/easyshell-jsch-compress-" + token + "-" + type;
                String source = root + "/source dir with space";
                roots.add(root);
                client.exec("mkdir -p " + quote(source + "/nested"));
                ShellSFTPClient sftp = client.sftpClient();
                sftp.put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), source + "/file one.txt", null);
                sftp.put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), source + "/nested/it's file.txt", null);

                String output = client.serverExec().compress(source, type);
                String archive = source + (type.equals("xz") || type.equals("bz2") ? ".tar." + type : "." + type);
                require(!client.exec("test -s " + quote(archive) + " && echo present").isBlank(),
                        "compressed archive is missing or empty: " + type + ", output=" + output);
                client.exec("rm -rf " + quote(source));
                client.serverExec().uncompress(archive);
                require(!client.exec("test -f " + quote(source + "/file one.txt") + " && echo present").isBlank(),
                        "uncompressed file is missing for type " + type);
                require(!client.exec("test -f " + quote(source + "/nested/it's file.txt") + " && echo present").isBlank(),
                        "uncompressed nested file is missing for type " + type);

                Path downloaded = Files.createTempFile("easyshell-uncompress-", ".txt");
                try {
                    ShellSFTPFile file = client.sftpClient().fileInfo(source + "/file one.txt");
                    require(file != null, "uncompressed file metadata is missing for type " + type);
                    client.sftpClient().get(file, downloaded.toString());
                    require(content.equals(Files.readString(downloaded, StandardCharsets.UTF_8)),
                            "uncompressed content mismatch for type " + type);
                } finally {
                    Files.deleteIfExists(downloaded);
                }
                System.out.println("File compression verified: " + type);
            }
            require(client.serverExec().compress("/tmp/unsupported", "unsupported-type") == null,
                    "unsupported compression type should return null");
        } finally {
            for (String root : roots) {
                client.exec("rm -rf " + quote(root));
            }
            Files.deleteIfExists(local);
        }
    }

    private static void requireSftpTransfer(ShellSSHClient client) throws Exception {
        String token = UUID.randomUUID().toString();
        String remote = "/tmp/easyshell-jsch-compress-sftp-" + token + ".bin";
        Path downloaded = Files.createTempFile("easyshell-compress-sftp-", ".bin");
        byte[] content = new byte[512 * 1024];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) ('a' + i % 23);
        }
        try {
            client.sftpClient().put(new ByteArrayInputStream(content), remote, null);
            ShellSFTPFile file = client.sftpClient().fileInfo(remote);
            require(file != null, "sftp file metadata is missing");
            client.sftpClient().get(file, downloaded.toString());
            require(sha256(content).equals(sha256(Files.readAllBytes(downloaded))),
                    "sftp transfer checksum mismatch under compression");
            System.out.println("SFTP transfer under compression verified: bytes=" + content.length);
        } finally {
            client.exec("rm -f " + quote(remote));
            Files.deleteIfExists(downloaded);
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void requireNotBlank(String value, String message) {
        require(value != null && !value.isBlank(), message);
    }
}
