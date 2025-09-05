package cn.oyzh.easyshell.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * zk客户端工具类
 *
 * @author oyzh
 * @since 2023/04/25
 */

public class ShellZKClientUtil {

    /**
     * 构建zk客户端
     *
     * @param connect           连接
     * @param retryPolicy       重试策略
     * @param authInfos         认证信息
     * @param zooKeeperConsumer ZooKeeper接收器
     * @return zk客户端
     */
    public static CuratorFramework build(ShellConnect connect,
                                         RetryPolicy retryPolicy,
                                         List<AuthInfo> authInfos,
                                         Consumer<ZooKeeper> zooKeeperConsumer) {
        ExecutorService service = Executors.newCachedThreadPool();
        String host = connect.getHost();
        int connectionTimeoutMs = connect.connectTimeOutMs();
        int sessionTimeoutMs = connect.sessionTimeOutMs();
        // 构建builder
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(host)
                .maxCloseWaitMs(500)
                .authorization(authInfos)
                .runSafeService(service)
                .retryPolicy(retryPolicy)
                .zk34CompatibilityMode(true)
                .threadFactory(ShellZKThread::new)
                .waitForShutdownTimeoutMs(500)
                // .zk34CompatibilityMode(compatibility)
                .zookeeperFactory(new ShellZKFactory(connect, zooKeeperConsumer))
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs);
        return builder.build();
    }

    public static ShellZKClient newClient(ShellConnect zkConnect) {
        return new ShellZKClient(zkConnect);
    }

    /**
     * 设置代理配置
     *
     * @param config      zk客户端配置
     * @param proxyConfig 代理配置
     */
    public static void setProxyConfig(ZKClientConfig config, ShellProxyConfig proxyConfig) {
        config.setProperty(ShellZKClientCnxnSocket.PROXY_HOST, proxyConfig.getHost());
        config.setProperty(ShellZKClientCnxnSocket.PROXY_USER, proxyConfig.getUser());
        config.setProperty(ShellZKClientCnxnSocket.PROXY_PORT, proxyConfig.getPort() + "");
        config.setProperty(ShellZKClientCnxnSocket.PROXY_PASSWORD, proxyConfig.getPassword());
        config.setProperty(ShellZKClientCnxnSocket.PROXY_PROTOCOL, proxyConfig.getProtocol());
    }

    /**
     * 获取代理配置
     *
     * @param config zk客户端配置
     * @return ShellProxyConfig
     */
    public static ShellProxyConfig getProxyConfig(ZKClientConfig config) {
        String proxyHost = config.getProperty(ShellZKClientCnxnSocket.PROXY_HOST);
        if (proxyHost == null) {
            return null;
        }
        String proxyUser = config.getProperty(ShellZKClientCnxnSocket.PROXY_USER);
        String proxyPort = config.getProperty(ShellZKClientCnxnSocket.PROXY_PORT);
        String proxyPassword = config.getProperty(ShellZKClientCnxnSocket.PROXY_PASSWORD);
        String proxyProtocol = config.getProperty(ShellZKClientCnxnSocket.PROXY_PROTOCOL);
        ShellProxyConfig proxyConfig = new ShellProxyConfig();
        proxyConfig.setHost(proxyHost);
        proxyConfig.setUser(proxyUser);
        proxyConfig.setPassword(proxyPassword);
        proxyConfig.setProtocol(proxyProtocol);
        proxyConfig.setPort(Integer.parseInt(proxyPort));
        return proxyConfig;
    }
}
