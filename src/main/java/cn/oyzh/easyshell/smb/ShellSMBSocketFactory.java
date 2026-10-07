package cn.oyzh.easyshell.smb;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import com.hierynomus.protocol.commons.socket.ProxySocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * smb socket工厂，负责根据代理配置创建socket
 *
 * @author oyzh
 * @since 2025-09-07
 */
public class ShellSMBSocketFactory extends ProxySocketFactory {

    /**
     * 连接超时时间
     */
    private final int connectTimeout;

    /**
     * 代理配置
     */
    private final ShellProxyConfig proxyConfig;

    /**
     * 构造smb socket工厂
     */
    public ShellSMBSocketFactory() {
        this(null, DEFAULT_CONNECT_TIMEOUT);
    }

    /**
     * 构造smb socket工厂
     *
     * @param proxyConfig    代理配置
     * @param connectTimeout 连接超时时间
     */
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

    /**
     * 创建socket
     *
     * @param address     地址
     * @param bindAddress 绑定地址
     * @return socket
     * @throws IOException 异常
     */
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
