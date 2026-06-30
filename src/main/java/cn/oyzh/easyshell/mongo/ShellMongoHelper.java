package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;

import javax.net.SocketFactory;

/**
 * @author oyzh
 * @since 2024/7/1
 */
public class ShellMongoHelper {

    /**
     * 初始化代理
     *
     * @param timeoutMs 超时时间
     */
    public static SocketFactory initProxySocketFactory(ShellProxyConfig proxyConfig, int timeoutMs, String host, int port) {

        return new SocketFactory() {
            @Override
            public java.net.Socket createSocket(String host, int port) throws java.io.IOException {
                return ShellProxyUtil.createSocket(proxyConfig, host, port, timeoutMs);
            }

            @Override
            public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
                return createSocket(host, port);
            }

            @Override
            public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException {
                return createSocket(host.getHostAddress(), port);
            }

            @Override
            public java.net.Socket createSocket(java.net.InetAddress host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
                return createSocket(host.getHostAddress(), port);
            }

            @Override
            public java.net.Socket createSocket() throws java.io.IOException {
                return createSocket(host, port);
            }
        };
    }

}
