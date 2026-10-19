package cn.oyzh.easyshell.redis;

import cn.oyzh.common.log.JulLog;
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
 * redis的socket工厂
 *
 * @author oyzh
 * @since 2025-09-03
 */
public class ShellRedisSocketFactory implements JedisSocketFactory {

    /**
     * 端口
     */
    private final int port;

    /**
     * 主机
     */
    private final String host;

    /**
     * 代理
     */
    private final Proxy proxy;

    /**
     * socket超时时间
     */
    private final int socketTimeout;

    /**
     * 代理配置
     */
    private final ShellProxyConfig proxyConfig;

    /**
     * ssl socket工厂
     */
    private final SSLSocketFactory sslSocketFactory;

    /**
     * 构造方法
     *
     * @param sslSocketFactory ssl socket工厂
     * @param host             主机
     * @param port             端口
     * @param proxyConfig      代理配置
     * @param socketTimeout    socket超时时间
     */
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
            InetSocketAddress address;
            // 需要代理
            if (ShellProxyUtil.isNeedProxy(this.proxyConfig)) {
                // 创建代理连接
                socket = ShellProxyUtil.createSocket(
                        this.proxyConfig,
                        this.host,
                        this.port,
                        this.socketTimeout
                );
                address = (InetSocketAddress) socket.getRemoteSocketAddress();
                JulLog.info("create socket with proxy");
            } else {
                socket = new Socket();
                socket.setSoTimeout(this.socketTimeout);
                address = new InetSocketAddress(this.host, this.port);
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
