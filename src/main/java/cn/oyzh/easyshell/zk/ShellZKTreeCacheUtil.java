package cn.oyzh.easyshell.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheSelector;

/**
 * zk缓存工具类
 *
 * @author oyzh
 * @since 202411/29
 */

public class ShellZKTreeCacheUtil {

    /**
     * 构建zk监听器
     *
     * @param framework zk客户端
     * @param selector  缓存选择器
     * @return zk监听器
     */
    public static TreeCache build( CuratorFramework framework, TreeCacheSelector selector) {
        return build(framework, "/", selector);
    }

    /**
     * 构建zk监听器
     *
     * @param framework zk客户端
     * @param path      监听路径
     * @param selector  缓存选择器
     * @return zk监听器
     */
    public static TreeCache build( CuratorFramework framework,  String path, TreeCacheSelector selector) {
        // 构建builder
        TreeCache.Builder builder = TreeCache.newBuilder(framework, path)
                .setCacheData(false)
                .setSelector(selector)
                .setExecutor(ShellZKThread::new)
                .setCreateParentNodes(true);
        return builder.build();
    }
}
