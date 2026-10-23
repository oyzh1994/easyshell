package cn.oyzh.easyshell.rdp;

import cn.oyzh.common.network.NetworkUtil;
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
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RDP本地代理隧道
 *
 * @author oyzh
 * @since 2026-10-23
 */
public final class RDPProxyTunnel implements AutoCloseable {

    private final ShellProxyConfig proxyConfig;

    private final String targetHost;

    private final int targetPort;

    private final int connectTimeout;

    private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicBoolean closed = new AtomicBoolean();

    private ServerSocket serverSocket;

    private Thread acceptThread;

    public RDPProxyTunnel(ShellProxyConfig proxyConfig, String targetHost, int targetPort, int connectTimeout) {
        this.proxyConfig = ShellProxyConfig.clone(proxyConfig);
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.connectTimeout = connectTimeout;
    }

    public synchronized InetSocketAddress start() throws IOException {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            return (InetSocketAddress) this.serverSocket.getLocalSocketAddress();
        }
        if (this.closed.get()) {
            throw new IOException("RDP proxy tunnel is closed");
        }
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        this.acceptThread = ThreadUtil.startVirtual(this::acceptLoop);
        return (InetSocketAddress) this.serverSocket.getLocalSocketAddress();
    }

    public boolean isRunning() {
        return this.serverSocket != null && !this.serverSocket.isClosed() && !this.closed.get();
    }

    private void acceptLoop() {
        while (this.isRunning()) {
            try {
                Socket localSocket = this.serverSocket.accept();
                this.sockets.add(localSocket);
                ThreadUtil.startVirtual(() -> this.handle(localSocket));
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
        try {
            localSocket.setKeepAlive(true);
            localSocket.setTcpNoDelay(true);
            remoteSocket = ShellProxyUtil.createSocket(
                    this.proxyConfig,
                    this.targetHost,
                    this.targetPort,
                    this.connectTimeout
            );
            this.sockets.add(remoteSocket);
            Socket finalRemoteSocket = remoteSocket;
            ThreadUtil.startVirtual(() -> copy(localSocket, finalRemoteSocket));
            copy(remoteSocket, localSocket);
        } catch (Exception ignored) {
        } finally {
            this.closeSocket(remoteSocket);
            this.closeSocket(localSocket);
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
        }
    }

    public static void verifyConnectivity(ShellProxyConfig proxyConfig, String targetHost, int targetPort, int timeout) throws IOException {
        try (Socket socket = ShellProxyUtil.createSocket(proxyConfig, targetHost, targetPort, timeout)) {
            socket.setSoTimeout(timeout);
            verifyRdpEndpoint(socket);
        }
    }

    public static void verifyRdpEndpoint(Socket socket) throws IOException {
        OutputStream output = socket.getOutputStream();
        output.write(new byte[]{
                3, 0, 0, 19, 14, (byte) 0xe0, 0, 0, 0, 0, 0,
                1, 0, 8, 0, 3, 0, 0, 0
        });
        output.flush();

        InputStream input = socket.getInputStream();
        int version = input.read();
        int reserved = input.read();
        int lengthHigh = input.read();
        int lengthLow = input.read();
        if (version < 0 || reserved < 0 || lengthHigh < 0 || lengthLow < 0) {
            throw new IOException("RDP endpoint closed before X.224 response");
        }
        int length = (lengthHigh << 8) | lengthLow;
        if (version != 3 || reserved != 0 || length < 7) {
            throw new IOException("Invalid RDP TPKT response header");
        }
        int lengthIndicator = input.read();
        int code = input.read();
        if (lengthIndicator < 0 || code < 0) {
            throw new IOException("RDP endpoint closed during X.224 response");
        }
        for (int index = 6; index < length; index++) {
            if (input.read() < 0) {
                throw new IOException("RDP endpoint closed during X.224 response");
            }
        }
        if (code != 0xd0) {
            throw new IOException(String.format("Unexpected X.224 response code: 0x%02x", code));
        }
    }

    public static boolean isReachableDirectly(String targetHost, int targetPort, int timeout) {
        return NetworkUtil.reachable(targetHost, targetPort, timeout);
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
}
