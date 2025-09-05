package cn.oyzh.easyshell.zk;

import cn.oyzh.common.network.ProxyUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import org.apache.zookeeper.ClientCnxnSocketNIO;
import org.apache.zookeeper.client.ZKClientConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 自定义ClientCnxnSocket实现（基于NIO），通过SOCKS5代理连接
 */
public class ShellZKClientCnxnSocket extends ClientCnxnSocketNIO {

    public static final String PROXY_HOST = "proxyHost";

    public static final String PROXY_PORT = "proxyPort";

    public static final String PROXY_USER = "proxyUser";

    public static final String PROXY_PASSWORD = "proxyPassword";

    public static final String PROXY_PROTOCOL = "proxyProtocol";

    public ShellZKClientCnxnSocket(ZKClientConfig clientConfig) throws IOException {
        super(clientConfig);
    }

    @Override
    protected SocketChannel createSock() throws IOException {
        ShellProxyConfig proxyConfig = ShellZKClientUtil.getProxyConfig(clientConfig);
        if (proxyConfig != null) {
            Proxy proxy = ShellProxyUtil.initProxy1(proxyConfig);
            // 直接创建SocketChannel，而不是从Socket获取
            SocketChannel sock = SocketChannel.open();
            // 配置为非阻塞模式
            sock.configureBlocking(false);
            // 获取代理服务器地址
            SocketAddress proxyAddress = proxy.address();
            // 连接到代理服务器
            boolean connectedImmediately = sock.connect(proxyAddress);
            if (!connectedImmediately) {
                // 等待连接完成
                while (!sock.finishConnect()) {
                    ThreadUtil.sleep(10);
                }
            }
            if (sock.socket() != null) {
                sock.socket().setSoLinger(false, -1);
                sock.socket().setTcpNoDelay(true);
            }
            return sock;
        }
        return super.createSock();
    }

    @Override
    protected void registerAndConnect(SocketChannel sock, InetSocketAddress addr) throws IOException {
        ShellProxyConfig proxyConfig = ShellZKClientUtil.getProxyConfig(clientConfig);
        if (proxyConfig != null) {
            // 执行SOCKS握手协议
            ProxyUtil.socks5Handshake(sock, addr, proxyConfig.getUser(), proxyConfig.getPassword());
            // 注册到选择器，关注读写操作
            super.sockKey = sock.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            // 标记连接已建立
            super.sendThread.primeConnection();
        } else {
            super.registerAndConnect(sock, addr);
        }
    }
}