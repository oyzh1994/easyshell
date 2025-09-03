package cn.oyzh.easyshell.redis;

import cn.oyzh.common.log.JulLog;
import redis.clients.jedis.JedisSocketFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

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

    public ShellRedisSocketFactory(String host, int port, Proxy proxy) {
        this.host = host;
        this.port = port;
        this.proxy = proxy;
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
            socket.connect(new InetSocketAddress(this.host, this.port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return socket;
    }
}
