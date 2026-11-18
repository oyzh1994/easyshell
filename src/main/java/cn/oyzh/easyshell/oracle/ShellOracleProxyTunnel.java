package cn.oyzh.easyshell.oracle;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Oracle本地代理隧道，用于补齐Oracle JDBC代理认证支持。
 */
public final class ShellOracleProxyTunnel implements AutoCloseable {

    private final ShellProxyConfig proxyConfig;

    private final int connectTimeout;

    private final boolean socks;

    private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicBoolean closed = new AtomicBoolean();

    private ServerSocket serverSocket;

    private Thread acceptThread;

    public ShellOracleProxyTunnel(ShellProxyConfig proxyConfig, int connectTimeout) {
        if (proxyConfig == null || !ShellProxyUtil.isNeedProxy(proxyConfig)) {
            throw new IllegalArgumentException("Oracle proxy configuration is required");
        }
        if (!proxyConfig.isHttpProxy() && !proxyConfig.isSocksProxy()) {
            throw new IllegalArgumentException("Unsupported Oracle proxy protocol: " + proxyConfig.getProtocol());
        }
        this.proxyConfig = ShellProxyConfig.clone(proxyConfig);
        this.connectTimeout = Math.max(1, connectTimeout);
        this.socks = proxyConfig.isSocksProxy();
    }

    public synchronized InetSocketAddress start() throws IOException {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            return (InetSocketAddress) this.serverSocket.getLocalSocketAddress();
        }
        if (this.closed.get()) {
            throw new IOException("Oracle proxy tunnel is closed");
        }
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 50);
        this.acceptThread = ThreadUtil.startVirtual(this::acceptLoop);
        return (InetSocketAddress) this.serverSocket.getLocalSocketAddress();
    }

    public boolean isSocks() {
        return this.socks;
    }

    public boolean isRunning() {
        return this.serverSocket != null && !this.serverSocket.isClosed() && !this.closed.get();
    }

    private void acceptLoop() {
        while (this.isRunning()) {
            try {
                Socket socket = this.serverSocket.accept();
                this.sockets.add(socket);
                ThreadUtil.startVirtual(() -> this.handle(socket));
            } catch (SocketException ex) {
                if (this.isRunning()) {
                    this.close();
                }
                return;
            } catch (IOException ex) {
                if (this.isRunning()) {
                    this.close();
                }
                return;
            }
        }
    }

    private void handle(Socket localSocket) {
        Socket remoteSocket = null;
        boolean handshakeCompleted = false;
        try {
            localSocket.setKeepAlive(true);
            localSocket.setTcpNoDelay(true);
            if (this.socks) {
                Target target = this.readSocksTarget(localSocket);
                remoteSocket = this.connectTarget(target.host(), target.port());
                this.writeAll(localSocket, new byte[]{5, 0, 0, 1, 127, 0, 0, 1, 0, 0});
            } else {
                Target target = this.readHttpTarget(localSocket);
                remoteSocket = this.connectTarget(target.host(), target.port());
                this.writeAscii(localSocket, "HTTP/1.1 200 Connection Established\r\n\r\n");
            }
            handshakeCompleted = true;
            Socket finalRemoteSocket = remoteSocket;
            this.sockets.add(remoteSocket);
            Thread reverse = ThreadUtil.startVirtual(() -> this.copy(finalRemoteSocket, localSocket));
            this.copy(localSocket, finalRemoteSocket);
            reverse.join();
        } catch (Exception ex) {
            JulLog.warn("Oracle proxy tunnel failure.", ex);
            if (!handshakeCompleted) {
                this.sendFailure(localSocket);
            }
        } finally {
            this.closeSocket(remoteSocket);
            this.closeSocket(localSocket);
        }
    }

    private Socket connectTarget(String host, int port) throws IOException {
        return ShellProxyUtil.createSocket(this.proxyConfig, host, port, this.connectTimeout);
    }

    private Target readHttpTarget(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        String requestLine = readAsciiLine(input);
        if (requestLine == null || !requestLine.startsWith("CONNECT ")) {
            throw new IOException("Invalid Oracle HTTP proxy request");
        }
        int suffix = requestLine.lastIndexOf(" HTTP/");
        if (suffix <= "CONNECT ".length()) {
            throw new IOException("Invalid Oracle HTTP proxy authority");
        }
        Target target = parseAuthority(requestLine.substring("CONNECT ".length(), suffix));
        String header;
        while ((header = readAsciiLine(input)) != null && !header.isEmpty()) {
        }
        if (header == null) {
            throw new IOException("Oracle HTTP proxy request ended before headers");
        }
        return target;
    }

    private Target readSocksTarget(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        byte[] greeting = readExact(input, 2);
        if ((greeting[0] & 0xff) != 5) {
            throw new IOException("Unsupported Oracle SOCKS version");
        }
        readExact(input, greeting[1] & 0xff);
        this.writeAll(socket, new byte[]{5, 0});
        byte[] request = readExact(input, 4);
        if ((request[0] & 0xff) != 5 || (request[1] & 0xff) != 1) {
            throw new IOException("Unsupported Oracle SOCKS request");
        }
        int addressType = request[3] & 0xff;
        String host;
        if (addressType == 1) {
            byte[] address = readExact(input, 4);
            host = (address[0] & 0xff) + "." + (address[1] & 0xff) + "."
                    + (address[2] & 0xff) + "." + (address[3] & 0xff);
        } else if (addressType == 3) {
            byte[] length = readExact(input, 1);
            host = new String(readExact(input, length[0] & 0xff), StandardCharsets.ISO_8859_1);
        } else if (addressType == 4) {
            byte[] address = readExact(input, 16);
            host = formatIpv6(address);
        } else {
            throw new IOException("Unsupported Oracle SOCKS address type");
        }
        byte[] port = readExact(input, 2);
        return new Target(host, ((port[0] & 0xff) << 8) | (port[1] & 0xff));
    }

    private void sendFailure(Socket socket) {
        try {
            if (this.socks) {
                this.writeAll(socket, new byte[]{5, 5, 0, 1, 0, 0, 0, 0, 0, 0});
            } else {
                this.writeAscii(socket, "HTTP/1.1 502 Proxy Connection Failed\r\n\r\n");
            }
        } catch (IOException ignored) {
        }
    }

    private void copy(Socket source, Socket target) {
        try {
            InputStream input = source.getInputStream();
            OutputStream output = target.getOutputStream();
            byte[] buffer = new byte[32 * 1024];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
                output.flush();
            }
            if (!target.isOutputShutdown()) {
                target.shutdownOutput();
            }
        } catch (IOException ignored) {
        } finally {
            this.closeSocket(source);
            this.closeSocket(target);
        }
    }

    private void writeAscii(Socket socket, String value) throws IOException {
        this.writeAll(socket, value.getBytes(StandardCharsets.US_ASCII));
    }

    private void writeAll(Socket socket, byte[] bytes) throws IOException {
        OutputStream output = socket.getOutputStream();
        output.write(bytes);
        output.flush();
    }

    private void closeSocket(Socket socket) {
        if (socket == null) {
            return;
        }
        this.sockets.remove(socket);
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public synchronized void close() {
        if (!this.closed.compareAndSet(false, true)) {
            return;
        }
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        for (Socket socket : this.sockets) {
            this.closeSocket(socket);
        }
        this.sockets.clear();
        if (this.acceptThread != null) {
            this.acceptThread.interrupt();
        }
    }

    private static Target parseAuthority(String authority) throws IOException {
        String host;
        String portValue;
        if (authority.startsWith("[")) {
            int end = authority.indexOf(']');
            if (end < 0 || end + 1 >= authority.length() || authority.charAt(end + 1) != ':') {
                throw new IOException("Invalid IPv6 proxy authority");
            }
            host = authority.substring(1, end);
            portValue = authority.substring(end + 2);
        } else {
            int separator = authority.lastIndexOf(':');
            if (separator <= 0 || separator == authority.length() - 1) {
                throw new IOException("Invalid proxy authority");
            }
            host = authority.substring(0, separator);
            portValue = authority.substring(separator + 1);
        }
        int port;
        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid proxy target port", ex);
        }
        if (host.isBlank() || port < 1 || port > 65535) {
            throw new IOException("Invalid proxy target");
        }
        return new Target(host, port);
    }

    private static String formatIpv6(byte[] address) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < address.length; index += 2) {
            if (index > 0) {
                result.append(':');
            }
            result.append(Integer.toHexString(((address[index] & 0xff) << 8) | (address[index + 1] & 0xff)));
        }
        return result.toString();
    }

    private static byte[] readExact(InputStream input, int length) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = input.read(bytes, offset, length - offset);
            if (count < 0) {
                throw new IOException("Proxy request ended unexpectedly");
            }
            offset += count;
        }
        return bytes;
    }

    private static String readAsciiLine(InputStream input) throws IOException {
        StringBuilder result = new StringBuilder();
        int previous = -1;
        while (true) {
            int current = input.read();
            if (current < 0) {
                return result.isEmpty() ? null : result.toString();
            }
            if (previous == '\r' && current == '\n') {
                result.setLength(result.length() - 1);
                return result.toString();
            }
            result.append((char) current);
            previous = current;
            if (result.length() > 16 * 1024) {
                throw new IOException("Proxy request line is too long");
            }
        }
    }

    private record Target(String host, int port) {
    }
}
