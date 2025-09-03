package cn.oyzh.easyshell.redis;

import cn.oyzh.common.log.JulLog;
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

    private final SSLSocketFactory sslSocketFactory;

    public ShellRedisSocketFactory(SSLSocketFactory sslSocketFactory, String host, int port, Proxy proxy, int socketTimeout) {
        this.host = host;
        this.port = port;
        this.proxy = proxy;
        this.socketTimeout = socketTimeout;
        this.sslSocketFactory = sslSocketFactory;
    }

    @Override
    public Socket createSocket() throws JedisConnectionException {
        Socket socket;
        if (this.proxy != null) {
            socket = new Socket(this.proxy);
            JulLog.info("create socket with proxy");
        } else {
            socket = new Socket();
            JulLog.info("create socket without proxy");
        }
        try {
            InetSocketAddress localAddress = new InetSocketAddress(this.host, this.port);
            socket.connect(localAddress, this.socketTimeout);
            socket.setSoTimeout(this.socketTimeout);
            // 创建SSL socket
            if (this.sslSocketFactory != null) {
                SSLSocket sslSocket;
                if (this.proxy != null) {
                    InetSocketAddress proxyAddress = (InetSocketAddress) this.proxy.address();
                    sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(socket, proxyAddress.getHostName(), proxyAddress.getPort(), true);
                } else {
                    sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(socket, localAddress.getHostName(), localAddress.getPort(), true);
                }
                // 启动SSL握手
                sslSocket.startHandshake();
                return sslSocket;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return socket;
    }
}
