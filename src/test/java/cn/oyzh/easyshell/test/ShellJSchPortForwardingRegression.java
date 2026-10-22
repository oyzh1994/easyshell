package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.domain.SSHTunneling;
import cn.oyzh.ssh.jump.SSHJumpForwarder;
import cn.oyzh.ssh.tunneling.SSHTunnelingForwarder;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 使用多台真实主机验证本地、远程、动态和多层跳板端口转发。
 */
public final class ShellJSchPortForwardingRegression {

    private ShellJSchPortForwardingRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch port forwarding regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int gatewayIndex = Integer.getInteger("test.forward.gateway.index", 1);
        int relayIndex = Integer.getInteger("test.forward.relay.index", 3);
        int windowsRelayIndex = Integer.getInteger("test.forward.windows.relay.index", 2);
        int targetIndex = Integer.getInteger("test.forward.target.index", 6);
        ShellJSchTestHosts.Host gateway = ShellJSchTestHosts.load(gatewayIndex);
        ShellJSchTestHosts.Host relay = ShellJSchTestHosts.load(relayIndex);
        ShellJSchTestHosts.Host windowsRelay = ShellJSchTestHosts.load(windowsRelayIndex);
        ShellJSchTestHosts.Host target = ShellJSchTestHosts.load(targetIndex);
        require(new HashSet<>(List.of(gatewayIndex, relayIndex, windowsRelayIndex, targetIndex)).size() == 4,
                "forwarding hosts must be distinct");
        System.out.println("Forwarding matrix: " + gateway + " -> " + relay + " -> " + windowsRelay + " -> " + target);

        String targetHostname = directHostname(target);
        requireConfiguredLocalForwarding(gateway, relay);
        try (ShellSSHClient gatewayClient = start(gateway)) {
            requireForwarderMatrix(gatewayClient, gateway, relay, target);
        }
        requireMultiHopForwarding(List.of(gateway, relay), target, targetHostname, 2, "two-hop");
        requireMultiHopForwarding(List.of(gateway, relay, windowsRelay), target, targetHostname, 3, "three-hop");
        requireConfiguredMultiHopTunneling(List.of(gateway, relay, windowsRelay), target, 3);
    }

    private static void requireConfiguredLocalForwarding(ShellJSchTestHosts.Host gateway,
                                                         ShellJSchTestHosts.Host relay) throws Exception {
        int localPort = freePort();
        ShellTunnelingConfig tunneling = new ShellTunnelingConfig();
        tunneling.setEnabled(true);
        tunneling.setType("local");
        tunneling.setLocalHost("127.0.0.1");
        tunneling.setLocalPort(localPort);
        tunneling.setRemoteHost(relay.host());
        tunneling.setRemotePort(relay.port());

        ShellConnect connect = gateway.newConnect();
        connect.setTunnelingConfigs(List.of(tunneling));
        try (ShellSSHClient client = start(connect)) {
            requireSshBanner(localPort);
            requireNotBlank(client.exec("hostname", 10000), "configured local forwarding hostname is blank");
        }
        requirePortClosed(localPort);
        System.out.println("Configured cross-host local forwarding verified");
    }

    private static void requireForwarderMatrix(ShellSSHClient gatewayClient,
                                               ShellJSchTestHosts.Host gateway,
                                               ShellJSchTestHosts.Host relay,
                                               ShellJSchTestHosts.Host target) throws Exception {
        int relayLocalPort = freePort();
        int targetLocalPort = freePort();
        int dynamicPort = freePort();
        try (EchoServer echo = new EchoServer();
             SSHJumpForwarder jumpForwarder = new SSHJumpForwarder();
             SSHTunnelingForwarder tunnelForwarder = new SSHTunnelingForwarder()) {
            Session session = jumpForwarder.initSession(toSSHConnect(gateway));

            SSHTunneling relayLocal = tunnel("local", "127.0.0.1", relayLocalPort,
                    relay.host(), relay.port());
            SSHTunneling targetLocal = tunnel("local", "127.0.0.1", targetLocalPort,
                    target.host(), target.port());
            tunnelForwarder.forward(List.of(relayLocal, targetLocal), session);
            requireSshBanner(relayLocalPort);
            requireSshBanner(targetLocalPort);
            requireConcurrentBanners(relayLocalPort, targetLocalPort);

            Set<String> existingRemote = new HashSet<>(List.of(session.getPortForwardingR()));
            SSHTunneling remote = tunnel("remote", "127.0.0.1", echo.port(), "127.0.0.1", 0);
            tunnelForwarder.forward(List.of(remote), session);
            int remotePort = newRemotePort(session, existingRemote);
            String remoteToken = "jsch-remote-forward-" + UUID.randomUUID();
            String remoteOutput = gatewayClient.exec(remoteEchoCommand(remotePort, remoteToken), 15000);
            require(remoteToken.equals(remoteOutput == null ? null : remoteOutput.trim()),
                    "remote forwarding output mismatch");

            SSHTunneling dynamic = tunnel("dynamic", "127.0.0.1", dynamicPort, null, 0);
            tunnelForwarder.forward(List.of(dynamic), session);
            requireSocks5Banner(dynamicPort, relay.host(), relay.port());
            requireSocks5Banner(dynamicPort, target.host(), target.port());
            requireSocks4Banner(dynamicPort, relay.host(), relay.port());
            requireSocks5HalfClose(dynamicPort, remotePort);
            requireMalformedSocksRecovery(dynamicPort, relay.host(), relay.port());
        }
        requirePortClosed(relayLocalPort);
        requirePortClosed(targetLocalPort);
        requirePortClosed(dynamicPort);
        System.out.println("Local, remote and dynamic forwarding verified across hosts");
    }

    private static void requireMultiHopForwarding(List<ShellJSchTestHosts.Host> hops,
                                                  ShellJSchTestHosts.Host target,
                                                  String expectedHostname,
                                                  int expectedSessions,
                                                  String name) throws Exception {
        int localPort;
        try (SSHJumpForwarder forwarder = new SSHJumpForwarder()) {
            localPort = forwarder.forward(hops.stream().map(ShellJSchPortForwardingRegression::toSSHConnect).toList(),
                    toSSHConnect(target));
            require(localPort > 0, name + " forwarding port is invalid");
            require(forwardSessions(forwarder).size() == expectedSessions,
                    name + " forwarding session count mismatch");
            requireSshBanner(localPort);
            requireConcurrentBanners(localPort);

            String hostname = forwardedHostname(localPort, target);
            require(expectedHostname.equals(hostname), name + " reached an unexpected target");
        }
        requirePortClosed(localPort);
        System.out.println(name + " forwarding verified: sessions=" + expectedSessions + ", targetMatched=true");
    }

    private static void requireConfiguredMultiHopTunneling(List<ShellJSchTestHosts.Host> hops,
                                                           ShellJSchTestHosts.Host target,
                                                           int expectedSessions) throws Exception {
        int localPort = freePort();
        ShellTunnelingConfig tunneling = new ShellTunnelingConfig();
        tunneling.setEnabled(true);
        tunneling.setType("local");
        tunneling.setLocalHost("127.0.0.1");
        tunneling.setLocalPort(localPort);
        tunneling.setRemoteHost("127.0.0.1");
        tunneling.setRemotePort(22);

        ShellConnect connect = target.newConnect();
        connect.setJumpConfigs(hops.stream().map(ShellJSchTestHosts.Host::newJumpConfig).toList());
        connect.setTunnelingConfigs(List.of(tunneling));
        try (ShellSSHClient client = start(connect)) {
            requireSshBanner(localPort);
            requireNotBlank(client.exec("hostname", 10000), "multi-hop tunneling hostname is blank");
            SSHJumpForwarder forwarder = shellJumpForwarder(client);
            require(forwardSessions(forwarder).size() == expectedSessions,
                    "multi-hop tunneling session count mismatch");
        }
        requirePortClosed(localPort);
        System.out.println("Configured three-hop local forwarding verified: sessions=" + expectedSessions);
    }

    private static String directHostname(ShellJSchTestHosts.Host host) throws Exception {
        try (ShellSSHClient client = start(host)) {
            return requireNotBlank(client.exec("hostname", 10000), "direct target hostname is blank");
        }
    }

    private static String forwardedHostname(int localPort, ShellJSchTestHosts.Host target) throws Exception {
        SSHConnect connect = toSSHConnect(target);
        connect.setHost("127.0.0.1");
        connect.setPort(localPort);
        try (SSHJumpForwarder holder = new SSHJumpForwarder()) {
            Session session = holder.initSession(connect);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            try {
                channel.setCommand("hostname");
                InputStream input = channel.getInputStream();
                channel.connect(15000);
                return requireNotBlank(new String(input.readAllBytes(), StandardCharsets.UTF_8).trim(),
                        "forwarded target hostname is blank");
            } finally {
                channel.disconnect();
            }
        }
    }

    private static ShellSSHClient start(ShellJSchTestHosts.Host host) throws Exception {
        ShellSSHClient client = new ShellSSHClient(host.newConnect());
        client.setVerifyFailureCallback(current -> null);
        ShellJSchTestHosts.acceptHostKeys(client);
        client.start(15000);
        return client;
    }

    private static ShellSSHClient start(ShellConnect connect) throws Exception {
        ShellSSHClient client = new ShellSSHClient(connect);
        client.setVerifyFailureCallback(current -> null);
        ShellJSchTestHosts.acceptHostKeys(client);
        client.start(15000);
        return client;
    }

    private static SSHConnect toSSHConnect(ShellJSchTestHosts.Host host) {
        SSHConnect connect = new SSHConnect();
        connect.setHost(host.host());
        connect.setPort(host.port());
        connect.setUser(host.user());
        connect.setPassword(host.password());
        connect.setAuthMethod("password");
        connect.setTimeout(15000);
        return connect;
    }

    private static SSHTunneling tunnel(String type, String localHost, int localPort,
                                       String remoteHost, int remotePort) {
        SSHTunneling tunneling = new SSHTunneling();
        tunneling.setType(type);
        tunneling.setLocalHost(localHost);
        tunneling.setLocalPort(localPort);
        tunneling.setRemoteHost(remoteHost);
        tunneling.setRemotePort(remotePort);
        return tunneling;
    }

    private static String remoteEchoCommand(int remotePort, String token) {
        return "exec 3<>/dev/tcp/127.0.0.1/" + remotePort
                + "; printf '%s\\n' '" + token + "' >&3; IFS= read -r line <&3; printf '%s' \"$line\"";
    }

    private static int newRemotePort(Session session, Set<String> existing) throws Exception {
        for (String forwarding : session.getPortForwardingR()) {
            if (!existing.contains(forwarding)) {
                return Integer.parseInt(forwarding.substring(0, forwarding.indexOf(':')));
            }
        }
        throw new IllegalStateException("remote forwarding port was not registered");
    }

    private static Set<?> forwardSessions(SSHJumpForwarder forwarder) throws Exception {
        Field field = SSHJumpForwarder.class.getSuperclass().getDeclaredField("sessions");
        field.setAccessible(true);
        return (Set<?>) field.get(forwarder);
    }

    private static SSHJumpForwarder shellJumpForwarder(ShellSSHClient client) throws Exception {
        Field field = ShellSSHClient.class.getDeclaredField("jumpForwarder");
        field.setAccessible(true);
        Object value = field.get(client);
        require(value instanceof SSHJumpForwarder, "jump forwarder is not initialized");
        return (SSHJumpForwarder) value;
    }

    private static void requireConcurrentBanners(int... ports) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Object>> tasks = new ArrayList<>();
            for (int round = 0; round < 4; round++) {
                for (int port : ports) {
                    tasks.add(() -> {
                        requireSshBanner(port);
                        return null;
                    });
                }
            }
            for (Future<Object> future : executor.invokeAll(tasks)) {
                future.get();
            }
        }
    }

    private static void requireSocks5Banner(int proxyPort, String host, int targetPort) throws Exception {
        try (Socket socket = openSocks5(proxyPort, host, targetPort)) {
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
            require(token.equals(response), "socks5 half-close response mismatch");
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
            require(input.read() == 5 && input.read() == 0, "socks5 negotiation failed");
            byte[] address = InetAddress.getByName(host).getAddress();
            require(address.length == 4, "SOCKS5 test target is not IPv4");
            output.write(new byte[]{5, 1, 0, 1, address[0], address[1], address[2], address[3],
                    (byte) (targetPort >>> 8), (byte) targetPort});
            output.flush();
            require(input.read() == 5 && input.read() == 0, "socks5 connect failed");
            require(input.read() == 0, "socks5 reserved byte is invalid");
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
        require(address.length == 4, "SOCKS4 test target is not IPv4");
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
            require(reply.length == 8 && reply[1] == 90, "SOCKS4 negotiation failed");
            requireSshBanner(socket);
        }
    }

    private static void requireMalformedSocksRecovery(int proxyPort, String host, int targetPort) throws Exception {
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
        requireSocks4Banner(proxyPort, host, targetPort);
    }

    private static void skipSocksAddress(InputStream input, int addressType) throws IOException {
        int length = switch (addressType) {
            case 1 -> 4;
            case 4 -> 16;
            case 3 -> input.read();
            default -> throw new IllegalStateException("socks5 reply address type is invalid");
        };
        for (int i = 0; i < length; i++) {
            require(input.read() >= 0, "socks5 reply address is incomplete");
        }
        require(input.read() >= 0 && input.read() >= 0, "socks5 reply port is incomplete");
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
        require(banner != null && banner.startsWith("SSH-2.0-"), "ssh forwarding banner mismatch");
    }

    private static void requirePortClosed(int port) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 300);
            } catch (IOException expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new IllegalStateException("forwarding port was not released: " + port);
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    private static String requireNotBlank(String value, String message) {
        require(value != null && !value.isBlank(), message);
        return value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class EchoServer implements AutoCloseable {

        private final ServerSocket socket;

        private EchoServer() throws IOException {
            this.socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            Thread.ofVirtual().start(() -> {
                try {
                    while (!this.socket.isClosed()) {
                        try (Socket client = this.socket.accept()) {
                            String line = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)).readLine();
                            client.getOutputStream().write((line + "\n").getBytes(StandardCharsets.UTF_8));
                            client.getOutputStream().flush();
                        }
                    }
                } catch (IOException ignored) {
                }
            });
        }

        private int port() {
            return this.socket.getLocalPort();
        }

        @Override
        public void close() throws IOException {
            this.socket.close();
        }
    }
}
