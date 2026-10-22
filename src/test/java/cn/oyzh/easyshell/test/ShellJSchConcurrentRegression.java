package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.sftp.ShellSFTPChannel;
import cn.oyzh.easyshell.sftp.ShellSFTPChannelPool;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellBaseSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.process.ShellProcessInfo;
import cn.oyzh.easyshell.ssh.server.ShellServerInfo;
import cn.oyzh.easyshell.ssh.server.ShellServerMonitor;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.domain.SSHTunneling;
import cn.oyzh.ssh.jump.SSHJumpForwarder;
import cn.oyzh.ssh.tunneling.SSHTunnelingForwarder;
import com.jcraft.jsch.Session;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class ShellJSchConcurrentRegression {

    private ShellJSchConcurrentRegression() {
    }

    public static void main(String[] args) {
        try {
            run(args);
            System.out.println("JSch integration regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {
        String host = value("test.host", args, 0);
        String user = value("test.user", args, 1);
        String password = value("test.password", args, 2);

        ShellConnect connect = new ShellConnect();
        connect.setHost(host.contains(":") ? host : host + ":22");
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> current);
            client.start(15000);
            requireOsTypeMatching();
            requirePathInjection(client);
            requireForwarding(client, host, user, password);
            requireShell(client);
            requireExecTimeout(client);
            requireSftp(client);
            requireSftpPoolReuse(client);
            requireSftpPoolConcurrency(client);
            requireSftpBorrowedCleanup(client);
            requireDockerCommand(client);
            requireProcessInfo(client.processExec().ps());
            requireProcessNetworkSpeed(client.processExec().ps());
            requireMonitorConcurrency(client);
            requireFailedStartCleanup(host, user, password);
            requireTunnelReconnect(host, user, password);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int round = 0; round < 50; round++) {
                    int currentRound = round;
                    if (round == 10 || round == 25 || round == 40) {
                        ShellClientChecker.remove(client);
                        client.getSession().disconnect();
                        requireSftpReconnect(client, round);
                    }
                    List<Callable<Object>> tasks = new ArrayList<>();
                    tasks.add(() -> requireServerInfo(client.serverExec().info()));
                    tasks.add(() -> requireProcessInfo(client.processExec().ps()));
                    tasks.add(() -> requireExec(client, currentRound));
                    tasks.add(() -> requireExec(client, currentRound));

                    List<Future<Object>> futures = executor.invokeAll(tasks);
                    for (Future<Object> future : futures) {
                        await(future);
                    }
                }
            }
        }
    }

    private static void requireForwarding(ShellSSHClient client, String host, String user, String password) throws Exception {
        SSHConnect connect = new SSHConnect();
        connect.setHost(host.contains(":") ? host.substring(0, host.lastIndexOf(':')) : host);
        connect.setPort(host.contains(":") ? Integer.parseInt(host.substring(host.lastIndexOf(':') + 1)) : 22);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setTimeout(15000);

        try (EchoServer echo = new EchoServer();
             SSHJumpForwarder jumpForwarder = new SSHJumpForwarder();
             SSHTunnelingForwarder tunnelForwarder = new SSHTunnelingForwarder()) {
            requireSocketEcho(echo.port, "jsch-echo-sanity");
            Session session = jumpForwarder.initSession(connect);

            SSHTunneling local = tunnel("local", "127.0.0.1", freePort(), "127.0.0.1", 22);
            tunnelForwarder.forward(List.of(local), session);
            requireSshBanner(local.getLocalPort());

            Set<String> oldRemote = new HashSet<>(List.of(session.getPortForwardingR()));
            SSHTunneling remote = tunnel("remote", "127.0.0.1", echo.port, "127.0.0.1", 0);
            tunnelForwarder.forward(List.of(remote), session);
            int remotePort = newRemotePort(session, oldRemote);
            String remoteToken = "jsch-remote-forward";
            String command = "exec 3<>/dev/tcp/127.0.0.1/" + remotePort
                    + "; printf '%s\\n' '" + remoteToken + "' >&3; IFS= read -r line <&3; printf '%s' \"$line\"";
            String remoteOutput = client.exec(command);
            if (!remoteToken.equals(remoteOutput)) {
                throw new IllegalStateException("remote forwarding output mismatch: " + remoteOutput);
            }

            int dynamicPort = freePort();
            SSHTunneling dynamic = tunnel("dynamic", "127.0.0.1", dynamicPort, null, 0);
            tunnelForwarder.forward(List.of(dynamic), session);
            requireSocks5Banner(dynamicPort);
            requireSocks5HalfClose(dynamicPort, remotePort);
            requireSocks4Banner(dynamicPort, "127.0.0.1", 22);
            requireMalformedSocksRecovery(dynamicPort);

            SSHConnect target = new SSHConnect();
            target.setHost("127.0.0.1");
            target.setPort(22);
            int jumpPort = jumpForwarder.forward(List.of(connect), target);
            requireSshBanner(jumpPort);
            int multiHopPort = jumpForwarder.forward(List.of(connect, connect), target);
            requireSshBanner(multiHopPort);
        }
    }

    private static SSHTunneling tunnel(String type, String localHost, int localPort, String remoteHost, int remotePort) {
        SSHTunneling tunneling = new SSHTunneling();
        tunneling.setType(type);
        tunneling.setLocalHost(localHost);
        tunneling.setLocalPort(localPort);
        tunneling.setRemoteHost(remoteHost);
        tunneling.setRemotePort(remotePort);
        return tunneling;
    }

    private static int newRemotePort(Session session, Set<String> existing) throws Exception {
        for (String forwarding : session.getPortForwardingR()) {
            if (!existing.contains(forwarding)) {
                return Integer.parseInt(forwarding.substring(0, forwarding.indexOf(':')));
            }
        }
        throw new IllegalStateException("remote forwarding port was not registered");
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    private static void requireSocketEcho(int port, String token) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 15000);
            socket.setSoTimeout(15000);
            socket.getOutputStream().write((token + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            String response = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
            if (!token.equals(response)) {
                throw new IllegalStateException("local echo server output mismatch");
            }
        }
    }

    private static void requireSocks5Banner(int port) throws Exception {
        try (Socket socket = openSocks5(port, "127.0.0.1", 22)) {
            requireSshBanner(socket);
        }
    }

    private static void requireSocks5HalfClose(int proxyPort, int echoPort) throws Exception {
        String token = "jsch-socks5-half-close-" + UUID.randomUUID();
        try (Socket socket = openSocks5(proxyPort, "127.0.0.1", echoPort)) {
            socket.getOutputStream().write((token + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            socket.shutdownOutput();
            String response = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
            if (!token.equals(response)) {
                throw new IllegalStateException("socks5 half-close response mismatch");
            }
        }
    }

    private static Socket openSocks5(int proxyPort, String host, int targetPort) throws Exception {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), proxyPort), 15000);
            socket.setSoTimeout(15000);
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            output.write(new byte[]{5, 1, 0});
            output.flush();
            if (input.read() != 5 || input.read() != 0) {
                throw new IllegalStateException("socks5 negotiation failed");
            }
            byte[] address = InetAddress.getByName(host).getAddress();
            output.write(new byte[]{5, 1, 0, 1, address[0], address[1], address[2], address[3],
                    (byte) (targetPort >>> 8), (byte) targetPort});
            output.flush();
            if (input.read() != 5 || input.read() != 0) {
                throw new IllegalStateException("socks5 connect failed");
            }
            if (input.read() != 0) {
                throw new IllegalStateException("socks5 reserved byte is invalid");
            }
            int addressType = input.read();
            skipSocksAddress(input, addressType);
            return socket;
        } catch (Exception ex) {
            socket.close();
            throw ex;
        }
    }

    private static void requireSocks4Banner(int proxyPort, String host, int targetPort) throws Exception {
        byte[] address = InetAddress.getByName(host).getAddress();
        if (address.length != 4) {
            throw new IllegalStateException("SOCKS4 test target is not IPv4");
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), proxyPort), 15000);
            socket.setSoTimeout(15000);
            OutputStream output = socket.getOutputStream();
            output.write(4);
            output.write(1);
            output.write(targetPort >>> 8);
            output.write(targetPort);
            output.write(address);
            output.write(0);
            output.flush();
            InputStream input = socket.getInputStream();
            byte[] reply = input.readNBytes(8);
            if (reply.length != 8 || reply[1] != 90) {
                throw new IllegalStateException("SOCKS4 negotiation failed");
            }
            String banner = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).readLine();
            if (banner == null || !banner.startsWith("SSH-")) {
                throw new IllegalStateException("SOCKS4 forwarding banner mismatch");
            }
        }
    }

    private static void requireMalformedSocksRecovery(int proxyPort) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), proxyPort), 15000);
            socket.setSoTimeout(2000);
            socket.getOutputStream().write(9);
            socket.getOutputStream().flush();
            try {
                socket.getInputStream().read();
            } catch (IOException ignored) {
            }
        }
        requireSocks4Banner(proxyPort, "127.0.0.1", 22);
    }

    private static void skipSocksAddress(InputStream input, int addressType) throws IOException {
        int length = switch (addressType) {
            case 1 -> 4;
            case 4 -> 16;
            case 3 -> input.read();
            default -> throw new IllegalStateException("socks5 reply address type is invalid");
        };
        for (int i = 0; i < length; i++) {
            input.read();
        }
        if (input.read() < 0 || input.read() < 0) {
            throw new IllegalStateException("socks5 reply port is incomplete");
        }
    }

    private static void requireSshBanner(int port) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 15000);
            requireSshBanner(socket);
        }
    }

    private static void requireSshBanner(Socket socket) throws IOException {
        socket.setSoTimeout(15000);
        String banner = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
        if (banner == null || !banner.startsWith("SSH-2.0-")) {
            throw new IllegalStateException("ssh forwarding banner mismatch");
        }
    }

    private static final class EchoServer implements AutoCloseable {

        private final ServerSocket socket;
        private final int port;

        private EchoServer() throws IOException {
            this.socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            this.port = this.socket.getLocalPort();
            Thread.ofVirtual().start(() -> {
                try {
                    while (!this.socket.isClosed()) {
                        try (Socket client = this.socket.accept()) {
                            String line = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)).readLine();
                            client.getOutputStream().write((line + "\n").getBytes(StandardCharsets.UTF_8));
                            client.getOutputStream().flush();
                        }
                    }
                } catch (IOException ignoredException) {
                }
            });
        }

        @Override
        public void close() throws IOException {
            this.socket.close();
        }
    }

    private static void requireShell(ShellSSHClient client) throws Exception {
        ShellSSHShell shell = client.openShell();
        String token = "jsch-shell-" + UUID.randomUUID();
        InputStream input = shell.getInputStream();
        OutputStream output = shell.getOutputStream();
        output.write(("stty -echo; echo " + token + "\n").getBytes(StandardCharsets.UTF_8));
        output.flush();

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        long deadline = System.currentTimeMillis() + 15000;
        while (!result.toString(StandardCharsets.UTF_8).contains(token) && System.currentTimeMillis() < deadline) {
            if (input.available() > 0) {
                int length = input.read(buffer);
                if (length < 0) {
                    break;
                }
                result.write(buffer, 0, length);
            } else {
                Thread.sleep(20);
            }
        }
        if (!result.toString(StandardCharsets.UTF_8).contains(token)) {
            throw new IllegalStateException("interactive shell output was not received");
        }
        client.setPtySize(120, 40, 800, 600);
        shell.close();
        ShellSSHShell reopened = client.reopenShell();
        if (!reopened.isConnected()) {
            throw new IllegalStateException("interactive shell reopen failed");
        }
        reopened.close();
    }

    private static void requireSftp(ShellSSHClient client) throws Exception {
        String suffix = UUID.randomUUID().toString();
        byte[] content = ("jsch-sftp-" + suffix).getBytes(StandardCharsets.UTF_8);
        Path upload = Files.createTempFile("easyshell-jsch-upload-", ".bin");
        Path download = Files.createTempFile("easyshell-jsch-download-", ".bin");
        String remote = "/tmp/easyshell-jsch-" + suffix;
        String renamed = remote + "-renamed";
        String stream = remote + "-stream";
        String tree = "/tmp/easyshell-jsch-tree-" + suffix;
        ShellSFTPClient sftp = client.sftpClient();
        if (sftp == null) {
            throw new IllegalStateException("sftp client is null");
        }
        try {
            Files.write(upload, content);
            AtomicLong uploadProgress = new AtomicLong();
            sftp.put(upload.toFile(), remote, length -> {
                uploadProgress.addAndGet(length);
                return true;
            });
            if (uploadProgress.get() <= 0) {
                throw new IllegalStateException("sftp upload progress was not reported");
            }
            if (!sftp.exist(remote)) {
                throw new IllegalStateException("sftp upload was not found");
            }
            ShellSFTPFile remoteFile = sftp.fileInfo(remote);
            if (remoteFile == null) {
                throw new IllegalStateException("sftp file info is null");
            }
            requireFileInfoPooling(sftp, remote);
            sftp.chmod(0600, remote);
            if (!sftp.rename(remote, renamed)) {
                throw new IllegalStateException("sftp rename failed");
            }
            remoteFile = sftp.fileInfo(renamed);
            AtomicLong downloadProgress = new AtomicLong();
            sftp.get(remoteFile, download.toString(), length -> {
                downloadProgress.addAndGet(length);
                return true;
            });
            if (!java.util.Arrays.equals(content, Files.readAllBytes(download))) {
                throw new IllegalStateException("sftp download content mismatch");
            }
            if (downloadProgress.get() <= 0) {
                throw new IllegalStateException("sftp download progress was not reported");
            }
            requireSftpStreams(sftp, stream, content);
            requireSftpDeepDelete(sftp, tree, content);
            sftp.delete(renamed);
        } finally {
            cleanupRemoteFile(sftp, remote);
            cleanupRemoteFile(sftp, renamed);
            cleanupRemoteFile(sftp, stream);
            cleanupRemoteTree(sftp, tree);
            try {
                sftp.closeDelayResources();
            } catch (Exception ignored) {
            }
            Files.deleteIfExists(upload);
            Files.deleteIfExists(download);
        }
    }

    private static void requireSftpStreams(ShellSFTPClient sftp, String remote, byte[] content) throws Exception {
        AtomicLong uploadProgress = new AtomicLong();
        try (OutputStream output = sftp.putStream(remote, length -> {
            uploadProgress.addAndGet(length);
            return true;
        })) {
            output.write(content);
        } finally {
            sftp.closeDelayResources();
        }
        if (uploadProgress.get() <= 0) {
            throw new IllegalStateException("sftp stream upload progress was not reported");
        }
        ShellSFTPFile file = sftp.fileInfo(remote);
        if (file == null) {
            throw new IllegalStateException("sftp stream upload was not found");
        }
        AtomicLong downloadProgress = new AtomicLong();
        try (InputStream input = sftp.getStream(file, length -> {
            downloadProgress.addAndGet(length);
            return true;
        })) {
            if (!java.util.Arrays.equals(content, input.readAllBytes())) {
                throw new IllegalStateException("sftp stream download content mismatch");
            }
        } finally {
            sftp.closeDelayResources();
        }
        if (downloadProgress.get() <= 0) {
            throw new IllegalStateException("sftp stream download progress was not reported");
        }
        sftp.delete(remote);
    }

    private static void requireSftpDeepDelete(ShellSFTPClient sftp, String tree, byte[] content) throws Exception {
        sftp.createDirRecursive(tree);
        String current = tree;
        for (int depth = 0; depth < 12; depth++) {
            current = current + "/d" + depth;
            sftp.createDir(current);
        }
        String remoteFile = current + "/payload.bin";
        sftp.put(new java.io.ByteArrayInputStream(content), remoteFile, null);
        if (!sftp.exist(remoteFile)) {
            throw new IllegalStateException("sftp deep tree upload was not found");
        }
        sftp.deleteDirRecursive(tree);
        if (sftp.exist(tree)) {
            throw new IllegalStateException("sftp deep tree delete failed");
        }
    }

    private static void cleanupRemoteFile(ShellSFTPClient sftp, String remote) {
        try {
            if (sftp.exist(remote)) {
                sftp.delete(remote);
            }
        } catch (Exception ignored) {
        }
    }

    private static void cleanupRemoteTree(ShellSFTPClient sftp, String remote) {
        try {
            if (sftp.exist(remote)) {
                sftp.deleteDirRecursive(remote);
            }
        } catch (Exception ignored) {
        }
    }

    private static void requireSftpReconnect(ShellSSHClient client, int round) throws Exception {
        String remote = "/tmp/easyshell-jsch-reconnect-" + round + "-" + UUID.randomUUID();
        ShellSFTPClient sftp = client.sftpClient();
        try {
            sftp.put(new java.io.ByteArrayInputStream(("reconnect-" + round).getBytes(StandardCharsets.UTF_8)), remote, null);
            if (!sftp.exist(remote)) {
                throw new IllegalStateException("sftp reconnect upload failed");
            }
        } finally {
            cleanupRemoteFile(sftp, remote);
        }
    }

    private static void requireFailedStartCleanup(String host, String user, String password) throws Exception {
        try (ServerSocket occupied = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ShellTunnelingConfig tunneling = new ShellTunnelingConfig();
            tunneling.setEnabled(true);
            tunneling.setType("local");
            tunneling.setLocalHost("127.0.0.1");
            tunneling.setLocalPort(occupied.getLocalPort());
            tunneling.setRemoteHost("127.0.0.1");
            tunneling.setRemotePort(22);

            ShellConnect connect = new ShellConnect();
            connect.setHost(host.contains(":") ? host : host + ":22");
            connect.setUser(user);
            connect.setPassword(password);
            connect.setAuthMethod("password");
            connect.setCharset("UTF-8");
            connect.setConnectTimeOut(15000);
            connect.setTunnelingConfigs(List.of(tunneling));

            ShellSSHClient failedClient = new ShellSSHClient(connect);
            try {
                failedClient.start(15000);
                throw new IllegalStateException("start should fail when tunnel binding is unavailable");
            } catch (ShellException expected) {
            } finally {
                failedClient.close();
            }
            if (failedClient.getSession() != null) {
                throw new IllegalStateException("failed start left an SSH session open");
            }
        }
    }

    private static void requireTunnelReconnect(String host, String user, String password) throws Exception {
        int localPort = freePort();
        ShellTunnelingConfig tunneling = new ShellTunnelingConfig();
        tunneling.setEnabled(true);
        tunneling.setType("local");
        tunneling.setLocalHost("127.0.0.1");
        tunneling.setLocalPort(localPort);
        tunneling.setRemoteHost("127.0.0.1");
        tunneling.setRemotePort(22);

        ShellConnect connect = new ShellConnect();
        connect.setHost(host.contains(":") ? host : host + ":22");
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);
        connect.setTunnelingConfigs(List.of(tunneling));

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> current);
            client.start(15000);
            requireSshBanner(localPort);
            client.getSession().disconnect();
            String token = "jsch-tunnel-reconnect";
            if (!token.equals(client.exec("echo " + token))) {
                throw new IllegalStateException("session reconnect failed");
            }
            requireSshBanner(localPort);
        }
    }

    private static void requireMonitorConcurrency(ShellSSHClient client) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Object>> tasks = new ArrayList<>();
            for (int index = 0; index < 4; index++) {
                tasks.add(() -> requireMonitor(client.serverExec().monitor()));
            }
            for (Future<Object> future : executor.invokeAll(tasks)) {
                await(future);
            }
        }
    }

    private static ShellServerMonitor requireMonitor(ShellServerMonitor monitor) {
        if (monitor == null) {
            throw new IllegalStateException("server monitor is null");
        }
        return monitor;
    }

    private static void requireDockerCommand(ShellSSHClient client) {
        String output = client.dockerExec().docker_version();
        if (output == null) {
            throw new IllegalStateException("docker command output is null");
        }
    }

    private static ShellServerInfo requireServerInfo(ShellServerInfo info) {
        if (info == null) {
            throw new IllegalStateException("server info is null");
        }
        return info;
    }

    private static List<ShellProcessInfo> requireProcessInfo(List<ShellProcessInfo> processes) {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalStateException("process info is empty");
        }
        return processes;
    }

    private static void requireProcessNetworkSpeed(List<ShellProcessInfo> processes) {
        requireProcessInfo(processes);
        boolean networkParsed = processes.stream()
                .anyMatch(process -> process.getNetworkSend() >= 0 || process.getNetworkRecv() >= 0);
        if (!networkParsed) {
            throw new IllegalStateException("process network speed was not parsed");
        }
    }

    private static void requireExecTimeout(ShellSSHClient client) throws Exception {
        long start = System.currentTimeMillis();
        client.exec("sleep 3; echo jsch-timeout-marker", 300);
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 2500) {
            throw new IllegalStateException("ssh exec timeout was not enforced: " + elapsed);
        }
        if (!"jsch-exec-after-timeout".equals(client.exec("echo jsch-exec-after-timeout"))) {
            throw new IllegalStateException("ssh exec did not recover after timeout");
        }
    }

    private static String requireExec(ShellSSHClient client, int round) {
        String token = "jsch-concurrent-" + round;
        String output = client.exec("echo " + token);
        if (!token.equals(output)) {
            throw new IllegalStateException("unexpected exec output length: " + (output == null ? -1 : output.length()));
        }
        return output;
    }

    private static void requirePathInjection(ShellSSHClient client) {
        if (client.isWindows()) {
            return;
        }
        String exportPath = client.getExportPath();
        int pathIndex = exportPath.lastIndexOf(":$PATH");
        if (pathIndex < 0) {
            throw new IllegalStateException("export path does not preserve remote PATH");
        }
        String resolved = client.exec("printf '%s' \"$PATH\"");
        if (resolved == null || !resolved.startsWith(exportPath.substring(0, pathIndex + 1))) {
            throw new IllegalStateException("exec command did not apply the configured PATH");
        }
        if (resolved.length() <= pathIndex + 1) {
            throw new IllegalStateException("exec command replaced the remote PATH instead of extending it");
        }
    }

    private static void requireOsTypeMatching() {
        requireOsType("Darwin", true, false, false, false);
        requireOsType("Macos", true, false, false, false);
        requireOsType("OS X", true, false, false, false);
        requireOsType("Linux", false, true, false, false);
        requireOsType("FreeBSD", false, false, true, false);
        requireOsType("AIX", false, false, true, false);
        requireOsType("HP-UX", false, false, true, false);
        requireOsType("SunOS", false, false, true, false);
        requireOsType("Microsoft Windows Server", false, false, false, true);
        requireOsType("MINGW64_NT-10.0", false, false, false, true);
        requireOsType("MSYS_NT-10.0", false, false, false, true);
        requireOsType("CYGWIN_NT-10.0", false, false, false, true);
    }

    private static void requireOsType(String osType, boolean macos, boolean linux, boolean unix, boolean windows) {
        OsTypeClient client = new OsTypeClient(osType);
        if (client.isMacos() != macos || client.isLinux() != linux || client.isUnix() != unix || client.isWindows() != windows) {
            throw new IllegalStateException("os type matching failed: " + osType);
        }
    }

    private static void requireFileInfoPooling(ShellSFTPClient sftp, String remote) throws Exception {
        ShellSFTPChannelPool pool = sftpChannelPool(sftp);
        for (int index = 0; index < 100; index++) {
            ShellSFTPFile info = sftp.fileInfo(remote);
            if (info == null || !remote.equals(info.getFilePath())) {
                throw new IllegalStateException("sftp file info pooling returned invalid metadata");
            }
        }
        if (pool.size() != 1) {
            throw new IllegalStateException("sftp file info leaked or discarded pool channels: " + pool.size());
        }
    }

    private static void requireSftpPoolConcurrency(ShellSSHClient client) throws Exception {
        try (ShellSFTPClient shared = new ShellSFTPClient(client.getShellConnect(), client.getSession(), false)) {
            shared.start(15000);
            ShellSFTPChannelPool pool = sftpChannelPool(shared);
            int workers = 16;
            int rounds = 40;
            AtomicInteger active = new AtomicInteger();
            AtomicInteger maxActive = new AtomicInteger();
            CountDownLatch ready = new CountDownLatch(workers);
            CountDownLatch start = new CountDownLatch(1);
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<Object>> futures = new ArrayList<>();
                for (int worker = 0; worker < workers; worker++) {
                    futures.add(executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        for (int round = 0; round < rounds; round++) {
                            ShellSFTPChannel channel = pool.borrowObject();
                            if (channel == null) {
                                throw new IllegalStateException("sftp channel pool returned null under contention");
                            }
                            int current = active.incrementAndGet();
                            maxActive.accumulateAndGet(current, Math::max);
                            try {
                                channel.stat("/");
                                Thread.sleep(1);
                            } finally {
                                active.decrementAndGet();
                                pool.returnObject(channel);
                            }
                        }
                        return null;
                    }));
                }
                ready.await(10, TimeUnit.SECONDS);
                start.countDown();
                for (Future<Object> future : futures) {
                    future.get(60, TimeUnit.SECONDS);
                }
            }
            if (maxActive.get() > pool.getMaxSize()) {
                throw new IllegalStateException("sftp channel pool exceeded its maximum size");
            }
            ShellSFTPChannel reused = pool.borrowObject();
            try {
                if (reused == null || !reused.isConnected()) {
                    throw new IllegalStateException("sftp channel pool has no reusable channel after contention");
                }
            } finally {
                pool.returnObject(reused);
            }

            List<ShellSFTPChannel> held = new ArrayList<>();
            for (int index = 0; index < pool.getMaxSize(); index++) {
                ShellSFTPChannel channel = pool.borrowObject();
                if (channel == null) {
                    throw new IllegalStateException("sftp channel pool exhausted before maximum size");
                }
                held.add(channel);
            }
            CountDownLatch waiterStarted = new CountDownLatch(1);
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                Future<ShellSFTPChannel> waiter = executor.submit(() -> {
                    waiterStarted.countDown();
                    return pool.borrowObject();
                });
                waiterStarted.await(5, TimeUnit.SECONDS);
                Thread.sleep(100);
                pool.close();
                if (waiter.get(5, TimeUnit.SECONDS) != null) {
                    throw new IllegalStateException("closing sftp channel pool did not wake a waiting borrower");
                }
            }
            for (ShellSFTPChannel channel : held) {
                if (!channel.isClosed()) {
                    throw new IllegalStateException("closing sftp channel pool leaked a borrowed channel");
                }
            }
        }
    }

    private static void requireSftpPoolReuse(ShellSSHClient client) throws Exception {
        try (ShellSFTPClient shared = new ShellSFTPClient(client.getShellConnect(), client.getSession(), false)) {
            shared.start(15000);
            ShellSFTPChannelPool pool = sftpChannelPool(shared);
            ShellSFTPChannel first = pool.borrowObject();
            if (first == null || !first.isConnected()) {
                throw new IllegalStateException("sftp channel pool did not create a usable channel");
            }
            pool.returnObject(first);
            ShellSFTPChannel second = pool.borrowObject();
            try {
                if (second != first) {
                    throw new IllegalStateException("sftp channel pool did not reuse an idle channel");
                }
            } finally {
                pool.returnObject(second);
            }

            ShellSFTPChannel closed = pool.borrowObject();
            if (closed == null) {
                throw new IllegalStateException("sftp channel pool did not provide a channel for invalidation test");
            }
            closed.close();
            pool.returnObject(closed);
            ShellSFTPChannel replacement = pool.borrowObject();
            try {
                if (replacement == null || replacement == closed || !replacement.isConnected()) {
                    throw new IllegalStateException("sftp channel pool reused a closed channel");
                }
            } finally {
                pool.returnObject(replacement);
            }
        }
    }

    private static void requireSftpBorrowedCleanup(ShellSSHClient client) throws Exception {
        ShellSFTPClient shared = new ShellSFTPClient(client.getShellConnect(), client.getSession(), false);
        ShellSFTPChannel borrowed = null;
        try {
            shared.start(15000);
            ShellSFTPChannelPool pool = sftpChannelPool(shared);
            borrowed = pool.borrowObject();
            if (borrowed == null || !borrowed.isConnected()) {
                throw new IllegalStateException("shared sftp client did not create a usable channel");
            }
        } finally {
            shared.close();
        }
        if (borrowed == null || !borrowed.isClosed()) {
            throw new IllegalStateException("closing the sftp pool leaked a borrowed channel");
        }
        if (!client.getSession().isConnected()) {
            throw new IllegalStateException("closing a shared sftp client disconnected the owner session");
        }
    }

    private static ShellSFTPChannelPool sftpChannelPool(ShellSFTPClient client) throws Exception {
        Field field = ShellSFTPClient.class.getDeclaredField("channelPool");
        field.setAccessible(true);
        ShellSFTPChannelPool pool = (ShellSFTPChannelPool) field.get(client);
        if (pool == null) {
            throw new IllegalStateException("sftp channel pool is null");
        }
        return pool;
    }

    private static void await(Future<Object> future) throws Exception {
        try {
            future.get();
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw ex;
        }
    }

    private static String value(String property, String[] args, int index) {
        String result = System.getProperty(property);
        if (result == null && args.length > index) {
            result = args[index];
        }
        if (result == null) {
            String name = property.substring("test.".length()).toUpperCase().replace('.', '_');
            result = System.getenv("EASYSHELL_TEST_" + name);
        }
        if (result == null || result.isBlank()) {
            throw new IllegalArgumentException("missing integration test value: " + property);
        }
        return result;
    }

    private static final class OsTypeClient extends ShellBaseSSHClient {

        private final ObjectProperty<ShellConnState> state = new SimpleObjectProperty<>(ShellConnState.CLOSED);

        private OsTypeClient(String osType) {
            super(new ShellConnect());
            this.osType = osType;
        }

        @Override
        public void start(int timeout) {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public ObjectProperty<ShellConnState> stateProperty() {
            return this.state;
        }
    }
}
