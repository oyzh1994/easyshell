package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import com.jcraft.jsch.Session;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 复现同一 SSH 会话上 exec、sftp、监控与进程信息并发时的通道故障。
 */
public final class ShellJSchExecStressRegression {

    private ShellJSchExecStressRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch exec stress regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        ShellJSchTestHosts.Host testHost = ShellJSchTestHosts.load(Integer.getInteger("test.host.index"));
        ShellConnect connect = testHost.newConnect();
        connect.setConnectTimeOut(15000);

        AtomicInteger failures = new AtomicInteger();
        List<String> failureMessages = new ArrayList<>();
        AtomicBoolean stopped = new AtomicBoolean();

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> current);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            if (Boolean.getBoolean("test.warmup")) {
                client.getUserHome();
                client.getRemoteCharset();
                client.getExportPath();
            }
            ShellSSHShell shell = client.openShell();
            ShellSFTPClient sftp = client.sftpClient();
            requireSftpList(sftp, "/root");

            int execWorkers = Integer.getInteger("test.exec.workers", 12);
            int execRounds = Integer.getInteger("test.exec.rounds", 80);
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();
                for (int worker = 0; worker < execWorkers; worker++) {
                    int workerId = worker;
                    futures.add(executor.submit(() -> {
                        for (int round = 0; round < execRounds && !stopped.get(); round++) {
                            String command = command(client, workerId, round);
                            long started = System.currentTimeMillis();
                            String output = client.exec(command, 5000);
                            long elapsed = System.currentTimeMillis() - started;
                            if (output == null) {
                                record(failures, failureMessages, client, "exec", command, elapsed);
                                stopped.set(true);
                                return;
                            }
                        }
                    }));
                }
                futures.add(executor.submit(() -> {
                    for (int round = 0; round < 40 && !stopped.get(); round++) {
                        long started = System.currentTimeMillis();
                        if (client.serverExec().monitor(true) == null) {
                            record(failures, failureMessages, client, "monitor", "", System.currentTimeMillis() - started);
                            stopped.set(true);
                            return;
                        }
                        sleep(100);
                    }
                }));
                futures.add(executor.submit(() -> {
                    for (int round = 0; round < 40 && !stopped.get(); round++) {
                        long started = System.currentTimeMillis();
                        if (client.processExec().ps() == null) {
                            record(failures, failureMessages, client, "process", "", System.currentTimeMillis() - started);
                            stopped.set(true);
                            return;
                        }
                        sleep(100);
                    }
                }));
                for (int worker = 0; worker < 4; worker++) {
                    futures.add(executor.submit(() -> {
                        for (int round = 0; round < 80 && !stopped.get(); round++) {
                            long started = System.currentTimeMillis();
                            try {
                                requireSftpList(sftp, "/root");
                            } catch (Exception ex) {
                                record(failures, failureMessages, client, "sftp", "ls /root",
                                        System.currentTimeMillis() - started);
                                stopped.set(true);
                                return;
                            }
                        }
                    }));
                }
                futures.add(executor.submit(() -> {
                    for (int round = 0; round < 3 && !stopped.get(); round++) {
                        long started = System.currentTimeMillis();
                        try {
                            requireSftpTransfer(sftp);
                        } catch (Exception ex) {
                            record(failures, failureMessages, client, "sftp-transfer", "", System.currentTimeMillis() - started);
                            stopped.set(true);
                            return;
                        }
                    }
                }));
                for (Future<?> future : futures) {
                    future.get(5, TimeUnit.MINUTES);
                }
            }

            shell.close();
            if (!failureMessages.isEmpty()) {
                System.out.println("FAILURES " + failures.get());
                failureMessages.forEach(System.out::println);
                throw new IllegalStateException("exec stress failures: " + failures.get());
            }
        }
    }

    private static String command(ShellSSHClient client, int worker, int round) {
        if (client.isWindows()) {
            return switch (round % 3) {
                case 1 -> "ver";
                case 2 -> "set";
                default -> "echo stress-" + worker + "-" + round;
            };
        }
        return switch (round % 5) {
            case 1 -> "ps -A | awk 'END {print NR}'";
            case 2 -> "df -k /tmp | awk 'END {print $2}'";
            case 3 -> "ls -la /tmp | awk 'END {print NR}'";
            case 4 -> "uname -a";
            default -> "echo stress-" + worker + "-" + round;
        };
    }

    private static void requireSftpList(ShellSFTPClient sftp, String path) throws Exception {
        if (sftp == null) {
            throw new IllegalStateException("sftp client is null");
        }
        if (sftp.lsFile(path) == null) {
            throw new IllegalStateException("sftp list is null");
        }
    }

    private static void requireSftpTransfer(ShellSFTPClient sftp) throws Exception {
        String suffix = UUID.randomUUID().toString();
        Path localSource = Files.createTempFile("easyshell-stress-", ".bin");
        Path localTarget = Files.createTempFile("easyshell-stress-", ".bin");
        String remote = "/tmp/easyshell-stress-" + suffix + ".bin";
        byte[] expected = new byte[64 * 1024];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = (byte) (i * 31);
        }
        try {
            Files.write(localSource, expected);
            sftp.put(localSource.toFile(), remote, null);
            if (sftp.fileInfo(remote) == null) {
                throw new IllegalStateException("sftp file info is null after upload");
            }
            sftp.get(sftp.fileInfo(remote), localTarget.toString(), null);
            if (!Arrays.equals(expected, Files.readAllBytes(localTarget))) {
                throw new IllegalStateException("sftp upload/download content mismatch");
            }
        } finally {
            try {
                sftp.delete(remote);
            } catch (Exception ignored) {
            }
            Files.deleteIfExists(localSource);
            Files.deleteIfExists(localTarget);
        }
    }

    private static void record(AtomicInteger failures, List<String> messages, ShellSSHClient client,
                               String operation, String command, long elapsed) {
        failures.incrementAndGet();
        boolean connected = client.getSession() != null && client.getSession().isConnected();
        String message = String.format("operation=%s elapsed=%dms sessionConnected=%s channels=%d [%s] command=%s",
                operation, elapsed, connected, channelCount(client.getSession()),
                channelSummary(client.getSession()), command);
        messages.add(message);
        System.out.println(message);
    }

    private static int channelCount(Session session) {
        if (session == null) {
            return -1;
        }
        try {
            Field field = Session.class.getDeclaredField("channels");
            field.setAccessible(true);
            return ((List<?>) field.get(session)).size();
        } catch (ReflectiveOperationException ex) {
            return -1;
        }
    }

    private static String channelSummary(Session session) {
        if (session == null) {
            return "no-session";
        }
        try {
            Field field = Session.class.getDeclaredField("channels");
            field.setAccessible(true);
            List<?> channels = (List<?>) field.get(session);
            List<String> summaries = new ArrayList<>();
            for (Object channel : channels) {
                if (channel == null) {
                    continue;
                }
                String type = channel.getClass().getSimpleName();
                try {
                    Field connected = channel.getClass().getSuperclass().getDeclaredField("connected");
                    connected.setAccessible(true);
                    Field closed = channel.getClass().getSuperclass().getDeclaredField("closed");
                    closed.setAccessible(true);
                    summaries.add(type + ":connected=" + connected.getBoolean(channel)
                            + ",closed=" + closed.getBoolean(channel));
                } catch (ReflectiveOperationException ex) {
                    summaries.add(type);
                }
            }
            return String.join(",", summaries);
        } catch (ReflectiveOperationException ex) {
            return "channelSummaryFailed=" + ex.getClass().getSimpleName();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
