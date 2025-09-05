package cn.oyzh.easyshell.zk;

import cn.oyzh.common.log.JulLog;
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
     * zk连接id
     */
    private final String iid;

    /**
     * zookeeper对象消费器
     */
    private Consumer<ZooKeeper> consumer;

    public ShellZKFactory(String iid, Consumer<ZooKeeper> consumer) {
        this.iid = iid;
        this.consumer = consumer;
    }

    @Override
    public ZooKeeper newZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly) throws Exception {
        ZKClientConfig clientConfig = new ZKClientConfig();
        // 判断是否开始sasl配置
        if (ShellZKSASLUtil.isNeedSasl(this.iid)) {
            JulLog.info("连接:{} 执行sasl认证", this.iid);
            clientConfig.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_KEY, "true");
            clientConfig.setProperty(ZKClientConfig.LOGIN_CONTEXT_NAME_KEY, this.iid);
        } else {// 重置sasl配置
            JulLog.debug("连接:{} 无需sasl认证", this.iid);
            clientConfig.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_KEY, "false");
            clientConfig.setProperty(ZKClientConfig.LOGIN_CONTEXT_NAME_KEY, ZKClientConfig.LOGIN_CONTEXT_NAME_KEY_DEFAULT);
        }
        // clientConfig.setProperty(ZKClientConfig.ZOOKEEPER_CLIENT_CNXN_SOCKET, ClientCnxnSocketNetty.class.getName());
        ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, watcher, canBeReadOnly, clientConfig);
        // 消费此zooKeeper对象
        if (this.consumer != null) {
            this.consumer.accept(zooKeeper);
            this.consumer = null;
        }
        return zooKeeper;
    }
}
