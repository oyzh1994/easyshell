package cn.oyzh.easyshell.test.rdp;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.rdp.RDPProxyTunnel;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RDPProxyTunnelTest {

    @Test
    public void httpConnectWithoutAuthentication() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("http", false, target.port())) {
            RDPProxyTunnel.verifyConnectivity(config("HTTP", false, null, null), "127.0.0.1", target.port(), 3000);
        }
    }

    @Test
    public void httpConnectWithAuthentication() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("http", true, target.port())) {
            RDPProxyTunnel.verifyConnectivity(config("HTTP", true, "proxyuser", "proxy-pass"), "127.0.0.1", target.port(), 3000);
        }
    }

    @Test
    public void httpConnectRejectsMissingOrWrongAuthentication() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("http", true, target.port())) {
            assertRejected(config("HTTP", false, null, null), target.port());
            assertRejected(config("HTTP", true, "proxyuser", "wrong"), target.port());
        }
    }

    @Test
    public void socks5WithoutAuthentication() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("socks", false, target.port())) {
            RDPProxyTunnel.verifyConnectivity(config("SOCKS5", false, null, null), "127.0.0.1", target.port(), 3000);
        }
    }

    @Test
    public void socks5WithAuthentication() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("socks", true, target.port())) {
            RDPProxyTunnel.verifyConnectivity(config("SOCKS5", true, "proxyuser", "proxy-pass"), "127.0.0.1", target.port(), 3000);
        }
    }

    @Test
    public void socks5RejectsMissingOrWrongAuthentication() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("socks", true, target.port())) {
            assertRejected(config("SOCKS5", false, null, null), target.port());
            assertRejected(config("SOCKS5", true, "proxyuser", "wrong"), target.port());
        }
    }

    @Test
    public void localTunnelForwardsRdpHandshake() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer();
             ProxyServer proxy = new ProxyServer("http", true, target.port());
             RDPProxyTunnel tunnel = new RDPProxyTunnel(
                     config("HTTP", true, "proxyuser", "proxy-pass"), "127.0.0.1", target.port(), 3000
             )) {
            InetSocketAddress endpoint = tunnel.start();
            assertTrue(tunnel.isRunning());
            try (Socket socket = new Socket()) {
                socket.connect(endpoint, 3000);
                socket.setSoTimeout(3000);
                RDPProxyTunnel.verifyRdpEndpoint(socket);
            }
            tunnel.close();
            assertFalse(tunnel.isRunning());
        }
    }

    @Test
    public void directConnectWorksWithoutProxy() throws Exception {
        try (RdpTargetServer target = new RdpTargetServer()) {
            RDPProxyTunnel.verifyConnectivity(null, "127.0.0.1", target.port(), 3000);
        }
    }

    private static void assertRejected(ShellProxyConfig config, int targetPort) {
        try {
            RDPProxyTunnel.verifyConnectivity(config, "127.0.0.1", targetPort, 3000);
            fail("proxy authentication should have been rejected");
        } catch (IOException expected) {
        }
    }

    private static ShellProxyConfig config(String protocol, boolean auth, String user, String password) {
        ShellProxyConfig config = new ShellProxyConfig();
        config.setHost("127.0.0.1");
        config.setPort("HTTP".equals(protocol) ? currentHttpPort() : currentSocksPort());
        config.setProtocol(protocol);
        config.setAuthType(auth ? "password" : "none");
        config.setUser(user);
        config.setPassword(password);
        return config;
    }

    private static int currentHttpPort() {
        return TestPorts.HTTP;
    }

    private static int currentSocksPort() {
        return TestPorts.SOCKS;
    }

    private static final class TestPorts {
        private static int HTTP;
        private static int SOCKS;
    }

    private static final class RdpTargetServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private volatile boolean closed;

        private RdpTargetServer() throws IOException {
            this.serverSocket = new ServerSocket(0, 20, InetAddress.getLoopbackAddress());
            Thread.startVirtualThread(this::acceptLoop);
        }

        private int port() {
            return this.serverSocket.getLocalPort();
        }

        private void acceptLoop() {
            while (!this.closed) {
                try {
                    Socket socket = this.serverSocket.accept();
                    this.sockets.add(socket);
                    Thread.startVirtualThread(() -> this.handle(socket));
                } catch (IOException ex) {
                    return;
                }
            }
        }

        private void handle(Socket socket) {
            try {
                socket.setSoTimeout(3000);
                InputStream input = socket.getInputStream();
                readExact(input, 19);
                OutputStream output = socket.getOutputStream();
                output.write(new byte[]{
                        3, 0, 0, 19, 14, (byte) 0xd0, 0, 0, 0x12, 0x34, 0, 3,
                        1, 0, 8, 0, 5, 0, 0, 0
                });
                output.flush();
                Thread.sleep(20);
            } catch (Exception ignored) {
            } finally {
                closeSocket(socket);
            }
        }

        @Override
        public void close() {
            this.closed = true;
            try {
                this.serverSocket.close();
            } catch (IOException ignored) {
            }
            for (Socket socket : this.sockets) {
                closeSocket(socket);
            }
        }
    }

    private static final class ProxyServer implements AutoCloseable {
        private final String protocol;
        private final boolean authentication;
        private final int targetPort;
        private final ServerSocket serverSocket;
        private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private volatile boolean closed;

        private ProxyServer(String protocol, boolean authentication, int targetPort) throws IOException {
            this.protocol = protocol;
            this.authentication = authentication;
            this.targetPort = targetPort;
            this.serverSocket = new ServerSocket(0, 20, InetAddress.getLoopbackAddress());
            if ("http".equals(protocol)) {
                TestPorts.HTTP = this.serverSocket.getLocalPort();
            } else {
                TestPorts.SOCKS = this.serverSocket.getLocalPort();
            }
            Thread.startVirtualThread(this::acceptLoop);
        }

        private int port() {
            return this.serverSocket.getLocalPort();
        }

        private void acceptLoop() {
            while (!this.closed) {
                try {
                    Socket socket = this.serverSocket.accept();
                    this.sockets.add(socket);
                    Thread.startVirtualThread(() -> this.handle(socket));
                } catch (IOException ex) {
                    return;
                }
            }
        }

        private void handle(Socket client) {
            Socket target = null;
            try {
                client.setSoTimeout(3000);
                if ("http".equals(this.protocol)) {
                    target = this.handleHttp(client);
                } else {
                    target = this.handleSocks(client);
                }
                bridge(client, target);
            } catch (Exception ignored) {
            } finally {
                closeSocket(target);
                closeSocket(client);
            }
        }

        private Socket handleHttp(Socket client) throws IOException {
            InputStream input = client.getInputStream();
            String requestLine = readLine(input);
            String authorization = null;
            String header;
            while ((header = readLine(input)) != null && !header.isEmpty()) {
                if (header.toLowerCase().startsWith("proxy-authorization:")) {
                    authorization = header.substring(header.indexOf(':') + 1).trim();
                }
            }
            if (this.authentication) {
                String expected = "Basic " + Base64.getEncoder().encodeToString("proxyuser:proxy-pass".getBytes(StandardCharsets.UTF_8));
                if (!expected.equals(authorization)) {
                    writeAscii(client, "HTTP/1.1 407 Proxy Authentication Required\r\nProxy-Authenticate: Basic realm=\"test\"\r\n\r\n");
                    throw new IOException("authentication rejected");
                }
            }
            if (requestLine == null || !requestLine.startsWith("CONNECT ")) {
                writeAscii(client, "HTTP/1.1 405 Method Not Allowed\r\n\r\n");
                throw new IOException("invalid CONNECT request");
            }
            Socket target = new Socket("127.0.0.1", this.targetPort);
            this.sockets.add(target);
            writeAscii(client, "HTTP/1.1 200 Connection established\r\n\r\n");
            return target;
        }

        private Socket handleSocks(Socket client) throws IOException {
            InputStream input = client.getInputStream();
            byte[] greeting = readExact(input, 2);
            byte[] methods = readExact(input, greeting[1] & 0xff);
            boolean supportsPassword = contains(methods, (byte) 2);
            if (this.authentication && supportsPassword) {
                writeBytes(client, new byte[]{5, 2});
                byte[] auth = readExact(input, 2);
                if (auth[0] != 1) {
                    throw new IOException("invalid SOCKS auth version");
                }
                byte[] user = readExact(input, auth[1]);
                byte[] passwordLength = readExact(input, 1);
                byte[] password = readExact(input, passwordLength[0]);
                if (!"proxyuser".equals(new String(user, StandardCharsets.UTF_8))
                        || !"proxy-pass".equals(new String(password, StandardCharsets.UTF_8))) {
                    writeBytes(client, new byte[]{1, 1});
                    throw new IOException("authentication rejected");
                }
                writeBytes(client, new byte[]{1, 0});
            } else if (!this.authentication && contains(methods, (byte) 0)) {
                writeBytes(client, new byte[]{5, 0});
            } else {
                writeBytes(client, new byte[]{5, (byte) 0xff});
                throw new IOException("authentication method rejected");
            }
            byte[] request = readExact(input, 4);
            int addressType = request[3] & 0xff;
            if (addressType == 1) {
                readExact(input, 4);
            } else if (addressType == 4) {
                readExact(input, 16);
            } else if (addressType == 3) {
                readExact(input, readExact(input, 1)[0]);
            } else {
                throw new IOException("invalid SOCKS address type");
            }
            readExact(input, 2);
            Socket target = new Socket("127.0.0.1", this.targetPort);
            this.sockets.add(target);
            writeBytes(client, new byte[]{5, 0, 0, 1, 127, 0, 0, 1, 0, 0});
            return target;
        }

        @Override
        public void close() {
            this.closed = true;
            try {
                this.serverSocket.close();
            } catch (IOException ignored) {
            }
            for (Socket socket : this.sockets) {
                closeSocket(socket);
            }
        }
    }

    private static void bridge(Socket first, Socket second) {
        Thread.startVirtualThread(() -> copy(first, second));
        copy(second, first);
    }

    private static void copy(Socket source, Socket target) {
        try {
            InputStream input = source.getInputStream();
            OutputStream output = target.getOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
                output.flush();
            }
        } catch (IOException ignored) {
        }
    }

    private static byte[] readExact(InputStream input, int length) throws IOException {
        byte[] data = new byte[length];
        int offset = 0;
        while (offset < length) {
            int read = input.read(data, offset, length - offset);
            if (read < 0) {
                throw new IOException("unexpected EOF");
            }
            offset += read;
        }
        return data;
    }

    private static String readLine(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int previous = -1;
        int current;
        while ((current = input.read()) >= 0) {
            if (previous == '\r' && current == '\n') {
                byte[] data = output.toByteArray();
                return new String(data, 0, data.length - 1, StandardCharsets.US_ASCII);
            }
            output.write(current);
            previous = current;
        }
        return output.size() == 0 ? null : output.toString(StandardCharsets.US_ASCII);
    }

    private static void writeAscii(Socket socket, String text) throws IOException {
        writeBytes(socket, text.getBytes(StandardCharsets.US_ASCII));
    }

    private static void writeBytes(Socket socket, byte[] data) throws IOException {
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
    }

    private static boolean contains(byte[] data, byte value) {
        for (byte item : data) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }

    private static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
