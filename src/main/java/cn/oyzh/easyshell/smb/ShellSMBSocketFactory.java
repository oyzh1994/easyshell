package cn.oyzh.easyshell.smb;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import com.hierynomus.protocol.commons.socket.ProxySocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ShellSMBSocketFactory extends ProxySocketFactory {

    private final int connectTimeout;

    private final ShellProxyConfig proxyConfig;

    public ShellSMBSocketFactory() {
        this(null, DEFAULT_CONNECT_TIMEOUT);
    }

    public ShellSMBSocketFactory(ShellProxyConfig proxyConfig, int connectTimeout) {
        this.proxyConfig = proxyConfig;
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Socket createSocket(String address, int port) throws IOException {
        return createSocket(new InetSocketAddress(address, port), null);
    }

    @Override
    public Socket createSocket(String address, int port, InetAddress localAddress, int localPort) throws IOException {
        return createSocket(new InetSocketAddress(address, port), new InetSocketAddress(localAddress, localPort));
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return createSocket(new InetSocketAddress(address, port), null);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return createSocket(new InetSocketAddress(address, port), new InetSocketAddress(localAddress, localPort));
    }

    private Socket createSocket(InetSocketAddress address, InetSocketAddress bindAddress) throws IOException {
        // 代理
        Socket socket;
        if (ShellProxyUtil.isNeedProxy(this.proxyConfig)) {
            socket = ShellProxyUtil.createSocket(
                    this.proxyConfig,
                    address.getHostString(),
                    address.getPort(),
                    this.connectTimeout
            );
            if (bindAddress != null) {
                socket.bind(bindAddress);
            }
        } else { // 直连
            socket = new Socket();
            if (bindAddress != null) {
                socket.bind(bindAddress);
            }
            socket.connect(address, this.connectTimeout);
        }
        JulLog.debug("Connecting to {}", address);
        return socket;
    }
}
