package cn.oyzh.easyshell.test.oracle;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.oracle.ShellOracleProxyTunnel;
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OracleProxyTunnelTest {

    @Test
    public void tunnelsOracleHttpProxyHandshake() throws Exception {
        try (EchoServer target = new EchoServer();
             HttpProxyServer proxy = new HttpProxyServer(target.port());
             ShellOracleProxyTunnel tunnel = new ShellOracleProxyTunnel(
                     proxyConfig("HTTP", proxy.port(), true), 3000
             )) {
            InetSocketAddress endpoint = tunnel.start();
            ShellProxyConfig localConfig = proxyConfig("HTTP", endpoint.getPort(), false);
            try (Socket socket = ShellProxyUtil.createSocket(
                    localConfig, "127.0.0.1", target.port(), 3000
            )) {
                socket.setSoTimeout(3000);
                socket.getOutputStream().write(new byte[]{7, 9});
                socket.getOutputStream().flush();
                assertArrayEquals(new byte[]{9, 7}, readExact(socket.getInputStream(), 2));
            }
            tunnel.close();
            assertFalse(tunnel.isRunning());
        }
    }

    @Test
    public void tunnelsOracleSocksProxyHandshake() throws Exception {
        try (EchoServer target = new EchoServer();
             SocksProxyServer proxy = new SocksProxyServer(target.port());
             ShellOracleProxyTunnel tunnel = new ShellOracleProxyTunnel(
                     proxyConfig("SOCKS5", proxy.port(), true), 3000
             )) {
            InetSocketAddress endpoint = tunnel.start();
            assertTrue(tunnel.isSocks());
            ShellProxyConfig localConfig = proxyConfig("SOCKS5", endpoint.getPort(), false);
            try (Socket socket = ShellProxyUtil.createSocket(
                    localConfig, "127.0.0.1", target.port(), 3000
            )) {
                socket.setSoTimeout(3000);
                socket.getOutputStream().write(new byte[]{7, 9});
                socket.getOutputStream().flush();
                assertArrayEquals(new byte[]{9, 7}, readExact(socket.getInputStream(), 2));
            }
        }
    }

    private static ShellProxyConfig proxyConfig(String protocol, int port, boolean authentication) {
        ShellProxyConfig config = new ShellProxyConfig();
        config.setProtocol(protocol);
        config.setHost("127.0.0.1");
        config.setPort(port);
        config.setAuthType(authentication ? "password" : "none");
        config.setUser(authentication ? "proxy-user" : null);
        config.setPassword(authentication ? "proxy-pass" : null);
        return config;
    }

    private static byte[] readExact(InputStream input, int length) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = input.read(bytes, offset, length - offset);
            if (count < 0) {
                throw new IOException("Unexpected EOF");
            }
            offset += count;
        }
        return bytes;
    }

    private static String readLine(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = input.read();
            if (current < 0) {
                throw new IOException("Unexpected EOF");
            }
            if (previous == '\r' && current == '\n') {
                byte[] bytes = output.toByteArray();
                return new String(bytes, 0, bytes.length - 1, StandardCharsets.US_ASCII);
            }
            output.write(current);
            previous = current;
        }
    }

    private static void bridge(Socket first, Socket second) throws IOException {
        Thread reverse = Thread.startVirtualThread(() -> copy(second, first));
        copy(first, second);
        try {
            reverse.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void copy(Socket source, Socket target) {
        try {
            InputStream input = source.getInputStream();
            OutputStream output = target.getOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
                output.flush();
            }
        } catch (IOException ignored) {
        }
    }

    private static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private abstract static class TestServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private volatile boolean closed;

        private TestServer() throws IOException {
            this.serverSocket = new ServerSocket(0, 20, InetAddress.getLoopbackAddress());
            Thread.startVirtualThread(this::acceptLoop);
        }

        int port() {
            return this.serverSocket.getLocalPort();
        }

        private void acceptLoop() {
            while (!this.closed) {
                try {
                    Socket socket = this.serverSocket.accept();
                    this.sockets.add(socket);
                    Thread.startVirtualThread(() -> this.handleClient(socket));
                } catch (IOException ex) {
                    return;
                }
            }
        }

        abstract void handleClient(Socket socket);

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

    private static final class EchoServer extends TestServer {
        private EchoServer() throws IOException {
        }

        @Override
        void handleClient(Socket socket) {
            try {
                socket.setSoTimeout(3000);
                byte[] bytes = readExact(socket.getInputStream(), 2);
                socket.getOutputStream().write(new byte[]{bytes[1], bytes[0]});
                socket.getOutputStream().flush();
            } catch (IOException ignored) {
            } finally {
                closeSocket(socket);
            }
        }
    }

    private static final class HttpProxyServer extends TestServer {
        private final int targetPort;

        private HttpProxyServer(int targetPort) throws IOException {
            this.targetPort = targetPort;
        }

        @Override
        void handleClient(Socket client) {
            Socket target = null;
            try {
                client.setSoTimeout(3000);
                String request = readLine(client.getInputStream());
                String authorization = null;
                String header;
                while ((header = readLine(client.getInputStream())) != null && !header.isEmpty()) {
                    if (header.toLowerCase().startsWith("proxy-authorization:")) {
                        authorization = header.substring(header.indexOf(':') + 1).trim();
                    }
                }
                String expected = "Basic " + Base64.getEncoder().encodeToString(
                        "proxy-user:proxy-pass".getBytes(StandardCharsets.UTF_8)
                );
                if (!expected.equals(authorization) || !request.contains("127.0.0.1:" + this.targetPort)) {
                    client.getOutputStream().write("HTTP/1.1 407 Proxy Authentication Required\r\n\r\n"
                            .getBytes(StandardCharsets.US_ASCII));
                    return;
                }
                target = new Socket("127.0.0.1", this.targetPort);
                client.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n"
                        .getBytes(StandardCharsets.US_ASCII));
                bridge(client, target);
            } catch (IOException ignored) {
            } finally {
                closeSocket(target);
                closeSocket(client);
            }
        }
    }

    private static final class SocksProxyServer extends TestServer {
        private final int targetPort;

        private SocksProxyServer(int targetPort) throws IOException {
            this.targetPort = targetPort;
        }

        @Override
        void handleClient(Socket client) {
            Socket target = null;
            try {
                client.setSoTimeout(3000);
                InputStream input = client.getInputStream();
                byte[] greeting = readExact(input, 2);
                readExact(input, greeting[1] & 0xff);
                client.getOutputStream().write(new byte[]{5, 2});
                byte[] auth = readExact(input, 2);
                if ((auth[0] & 0xff) != 1) {
                    throw new IOException("Invalid SOCKS authentication version");
                }
                int userLength = auth[1] & 0xff;
                String user = new String(readExact(input, userLength), StandardCharsets.ISO_8859_1);
                int passwordLength = readExact(input, 1)[0] & 0xff;
                String password = new String(readExact(input, passwordLength), StandardCharsets.ISO_8859_1);
                if (!"proxy-user".equals(user) || !"proxy-pass".equals(password)) {
                    client.getOutputStream().write(new byte[]{1, 1});
                    return;
                }
                client.getOutputStream().write(new byte[]{1, 0});
                byte[] request = readExact(input, 4);
                String host;
                int addressType = request[3] & 0xff;
                if (addressType == 1) {
                    byte[] address = readExact(input, 4);
                    host = (address[0] & 0xff) + "." + (address[1] & 0xff) + "."
                            + (address[2] & 0xff) + "." + (address[3] & 0xff);
                } else if (addressType == 3) {
                    host = new String(readExact(input, readExact(input, 1)[0] & 0xff), StandardCharsets.ISO_8859_1);
                } else {
                    throw new IOException("Unsupported address type");
                }
                byte[] portBytes = readExact(input, 2);
                int port = ((portBytes[0] & 0xff) << 8) | (portBytes[1] & 0xff);
                if (!"127.0.0.1".equals(host) || port != this.targetPort) {
                    client.getOutputStream().write(new byte[]{5, 5, 0, 1, 0, 0, 0, 0, 0, 0});
                    return;
                }
                target = new Socket(host, port);
                client.getOutputStream().write(new byte[]{5, 0, 0, 1, 127, 0, 0, 1, 0, 0});
                bridge(client, target);
            } catch (IOException ignored) {
            } finally {
                closeSocket(target);
                closeSocket(client);
            }
        }
    }
}
