package cn.oyzh.easyshell.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import org.apache.curator.utils.ZookeeperFactory;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;

import java.util.function.Consumer;

/**
 * zk工厂
 *
 * @author oyzh
 * @since 2023/9/27
 */
public class ShellZKFactory implements ZookeeperFactory {

    /**
     * 连接
     */
    private final ShellConnect connect;

    /**
     * zookeeper对象回调
     */
    private Consumer<ZooKeeper> callback;

    public ShellZKFactory(ShellConnect connect, Consumer<ZooKeeper> callback) {
        this.connect = connect;
        this.callback = callback;
    }

    @Override
    public ZooKeeper newZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly) throws Exception {
        ZKClientConfig clientConfig = new ZKClientConfig();
        String iid = this.connect.getId();
        // 判断是否开始sasl配置
        if (ShellZKSASLUtil.isNeedSasl(iid)) {
            JulLog.info("连接:{} 执行sasl认证", iid);
            clientConfig.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_KEY, "true");
            clientConfig.setProperty(ZKClientConfig.LOGIN_CONTEXT_NAME_KEY, iid);
        } else {// 重置sasl配置
            JulLog.debug("连接:{} 无需sasl认证", iid);
            clientConfig.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_KEY, "false");
            clientConfig.setProperty(ZKClientConfig.LOGIN_CONTEXT_NAME_KEY, ZKClientConfig.LOGIN_CONTEXT_NAME_KEY_DEFAULT);
        }
        // 设置socket实现
        clientConfig.setProperty(ZKClientConfig.ZOOKEEPER_CLIENT_CNXN_SOCKET, ShellZKClientCnxnSocket.class.getName());
        // 设置代理
        ShellProxyConfig proxyConfig = this.connect.getProxyConfig();
        if (this.connect.isEnableProxy() && proxyConfig != null) {
            ShellZKClientUtil.setProxyConfig(clientConfig, proxyConfig);
        }
        ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, watcher, canBeReadOnly, clientConfig);
        // 此zooKeeper对象回调
        if (this.callback != null) {
            this.callback.accept(zooKeeper);
            this.callback = null;
        }
        return zooKeeper;
    }
}
