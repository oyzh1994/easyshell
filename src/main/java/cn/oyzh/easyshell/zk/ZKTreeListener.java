package cn.oyzh.easyshell.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.event.zk.ZKEventUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.data.Stat;

/**
 * zk节点事件监听器
 *
 * @author oyzh
 * @since 2020/4/17
 */
public class ZKTreeListener implements TreeCacheListener {

    /**
     * 监听路径
     */
    private String path;

    /**
     * zk客户端
     */
    private ZKClient zkClient;

    /**
     * 消息有效期
     */
    private Integer maxTimeEffect = 10 * 1000;

    public ZKTreeListener( ZKClient zkClient) {
        this("/", zkClient);
    }

    public ZKTreeListener( String path,  ZKClient zkClient) {
        this.path = path;
        this.zkClient = zkClient;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) {
        try {
            // 获取数据
            TreeCacheEvent.Type type = event.getType();
            ChildData data = event.getData();
            Stat stat = null;
            // byte[] nodeData = null;
            String nodePath = null;
            if (data != null) {
                stat = data.getStat();
                nodePath = data.getPath();
                // nodeData = data.getData();
            }
            if (stat != null) {
                long nowTime = System.currentTimeMillis();
                if (type == TreeCacheEvent.Type.NODE_UPDATED) {
                    long mtime = stat.getMtime();
                    if (nowTime - mtime >= this.maxTimeEffect) {
                        JulLog.debug("Update消息已过时.");
                        return;
                    }
                }
                if (type == TreeCacheEvent.Type.NODE_ADDED) {
                    long ctime = stat.getCtime();
                    if (nowTime - ctime >= this.maxTimeEffect) {
                        JulLog.debug("Add消息已过时.");
                        return;
                    }
                }
            }
            // switch (type) {
            //     case NODE_ADDED -> ZKEventUtil.nodeAdded(this.zkClient, stat, nodeData, nodePath);
            //     case NODE_UPDATED -> ZKEventUtil.nodeUpdated(this.zkClient, stat, nodeData, nodePath);
            //     case NODE_REMOVED -> ZKEventUtil.nodeDeleted(this.zkClient, stat, nodePath);
            // }
            switch (type) {
                case NODE_ADDED -> ZKEventUtil.nodeCreated(this.zkClient, nodePath);
                case NODE_UPDATED -> ZKEventUtil.nodeChanged(this.zkClient, nodePath);
                case NODE_REMOVED -> ZKEventUtil.nodeRemoved(this.zkClient, nodePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        this.path = null;
        this.zkClient = null;
        this.maxTimeEffect = null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ZKClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZKClient zkClient) {
        this.zkClient = zkClient;
    }

    public Integer getMaxTimeEffect() {
        return maxTimeEffect;
    }

    public void setMaxTimeEffect(Integer maxTimeEffect) {
        this.maxTimeEffect = maxTimeEffect;
    }
}
