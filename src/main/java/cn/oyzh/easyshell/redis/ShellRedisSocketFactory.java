package cn.oyzh.easyshell.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.network.ProxyUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import redis.clients.jedis.JedisSocketFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * @author oyzh
 * @since 2025-09-03
 */
public class ShellRedisSocketFactory implements JedisSocketFactory {

    private final int port;

    private final String host;

    private final Proxy proxy;

    private final int socketTimeout;

    private final ShellProxyConfig proxyConfig;

    private final SSLSocketFactory sslSocketFactory;

    public ShellRedisSocketFactory(SSLSocketFactory sslSocketFactory,
                                   String host,
                                   int port,
                                   ShellProxyConfig proxyConfig,
                                   int socketTimeout) {
        this.host = host;
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.sslSocketFactory = sslSocketFactory;
        this.proxyConfig = proxyConfig;
        this.proxy = ShellProxyUtil.initProxy1(this.proxyConfig);
    }

    @Override
    public Socket createSocket() throws JedisConnectionException {
        Socket socket;
        try {
            socket = new Socket();
            socket.setSoTimeout(this.socketTimeout);
            // 连接地址
            InetSocketAddress address;
            // 执行代理
            if (ProxyUtil.isNeedProxy(this.proxy)) {
                socket.connect(this.proxy.address(), this.socketTimeout);
                address = (InetSocketAddress) this.proxy.address();
                ProxyUtil.socks5Handshake(
                        socket,
                        this.host,
                        this.port,
                        this.proxyConfig.getUser(),
                        this.proxyConfig.getPassword()
                );
                JulLog.info("create socket with proxy");
            } else {// 直连
                address = new InetSocketAddress(this.host, this.port);
                socket.setSoTimeout(this.socketTimeout);
                socket.connect(address, this.socketTimeout);
                JulLog.info("create socket without proxy");
            }
            // 创建SSL socket
            if (this.sslSocketFactory != null) {
                SSLSocket sslSocket;
                if (this.proxy != null) {
                    sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(socket, address.getHostName(), address.getPort(), true);
                } else {
                    sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(socket, address.getHostName(), address.getPort(), true);
                }
                // 启动SSL握手
                sslSocket.startHandshake();
                // 设置为ssl连接
                socket = sslSocket;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return socket;
    }
}
