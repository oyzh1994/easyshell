package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPUtil;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import com.jcraft.jsch.ChannelSftp;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证目录列举阶段只识别链接类型，不提前解析链接目标。
 */
public final class ShellJSchSftpLinkPerformanceRegression {

    private static final List<String> DEFAULT_PATHS = List.of("/bin", "/sbin", "/usr/bin", "/usr/sbin");

    private ShellJSchSftpLinkPerformanceRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch SFTP link performance regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        long maxMillis = Long.getLong("test.sftp.link.max.ms", 1000L);
        int iterations = Integer.getInteger("test.sftp.link.iterations", 3);
        List<String> paths = System.getProperty("test.sftp.link.paths") == null
                ? DEFAULT_PATHS
                : List.of(System.getProperty("test.sftp.link.paths").split(","));
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("SFTP link performance target: " + host);

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);

            // 首次调用包含 SFTP 通道创建开销，单独记录但不作为链接判定性能门槛。
            long coldStart = System.nanoTime();
            List<ShellSFTPFile> coldFiles = client.sftpClient().lsFile("/bin");
            long coldMillis = elapsedMillis(coldStart);
            System.out.println("/bin cold entries=" + coldFiles.size() + ", links=" + linkCount(coldFiles)
                    + ", elapsed=" + coldMillis + "ms");
            requireLinkEntries("/bin", coldFiles);
            requireLazyLinks("/bin", coldFiles);

            for (String path : paths) {
                if (!directoryExists(client, path)) {
                    System.out.println(path + " skipped: directory is unavailable");
                    continue;
                }
                long raw = measureRawLs(client, path);
                long best = Long.MAX_VALUE;
                long worst = 0L;
                List<ShellSFTPFile> files = List.of();
                for (int i = 0; i < iterations; i++) {
                    long start = System.nanoTime();
                    files = client.sftpClient().lsFile(path);
                    long elapsed = elapsedMillis(start);
                    best = Math.min(best, elapsed);
                    worst = Math.max(worst, elapsed);
                }
                System.out.println(path + " entries=" + files.size() + ", links=" + linkCount(files)
                        + ", raw=" + raw + "ms, best=" + best + "ms, worst=" + worst + "ms");
                requireLazyLinks(path, files);
                if (path.equals("/bin") || path.equals("/sbin")) {
                    requireLinkEntries(path, files);
                }
                if (path.equals("/bin") || path.equals("/sbin")) {
                    require(worst <= maxMillis, path + " directory listing took " + worst
                            + "ms, expected <= " + maxMillis + "ms");
                }
            }

            requireLazyClickResolution(client);
            System.out.println("SFTP link lazy click resolution verified");
        }
    }

    private static boolean directoryExists(ShellSSHClient client, String path) throws Exception {
        String output = client.exec("test -d " + quote(path) + " && echo present || true");
        return output != null && output.contains("present");
    }

    private static long measureRawLs(ShellSSHClient client, String path) throws Exception {
        ChannelSftp channel = (ChannelSftp) client.getSession().openChannel("sftp");
        try {
            channel.setFilenameEncoding(client.getCharset().name());
            channel.connect(15000);
            long start = System.nanoTime();
            channel.ls(path);
            return elapsedMillis(start);
        } finally {
            channel.disconnect();
        }
    }

    private static void requireLazyClickResolution(ShellSSHClient client) throws Exception {
        List<ShellSFTPFile> files = client.sftpClient().lsFile("/bin");
        ShellSFTPFile link = files.stream()
                .filter(ShellSFTPFile::isLink)
                .filter(file -> file.getLinkAttrs() == null)
                .findFirst()
                .orElse(null);
        require(link != null, "no unresolved link found in /bin");
        long start = System.nanoTime();
        ShellSFTPUtil.realpath(link, client.sftpClient());
        long elapsed = elapsedMillis(start);
        require(link.getLinkAttrs() != null, "link target was not resolved on demand: " + link.getFileName());
        System.out.println("link click resolution: name=" + link.getFileName() + ", elapsed=" + elapsed + "ms");
    }

    private static void requireLazyLinks(String path, List<ShellSFTPFile> files) {
        List<String> resolved = new ArrayList<>();
        for (ShellSFTPFile file : files) {
            if (file.isLink() && file.getLinkAttrs() != null) {
                resolved.add(file.getFileName());
            }
        }
        require(resolved.isEmpty(), path + " eagerly resolved links: " + resolved);
    }

    private static void requireLinkEntries(String path, List<ShellSFTPFile> files) {
        require(linkCount(files) > 0, path + " does not contain any symlink entries");
    }

    private static long linkCount(List<ShellSFTPFile> files) {
        return files.stream().filter(ShellSFTPFile::isLink).count();
    }

    private static long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000L;
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
