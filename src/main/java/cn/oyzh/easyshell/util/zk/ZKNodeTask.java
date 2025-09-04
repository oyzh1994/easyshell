package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.easyshell.zk.ZKNode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * zk节点任务
 *
 * @author oyzh
 * @since 2025-01-23
 */
public class ZKNodeTask {

    /**
     * 执行任务
     *
     * @param node       zk节点
     * @param client     zk客户端
     * @param path       路径
     * @param properties 属性
     * @return 异常
     */
    public Exception doWorker(ZKNode node, ZKClient client, String path, String properties) {
        List<Runnable> tasks = new ArrayList<>();
        // 异常
        AtomicReference<Exception> exceptionReference = new AtomicReference<>();
        // 状态
        if (properties.contains("s")) {
            tasks.add(() -> {
                try {
                    node.stat(client.checkExists(path));
                } catch (KeeperException.NoAuthException ignored) {
                } catch (Exception ex) {
                    exceptionReference.set(ex);
                }
            });
        }
        // 访问控制
        if (properties.contains("a")) {
            tasks.add(() -> {
                try {
                    node.acl(client.getACL(path));
                } catch (KeeperException.NoAuthException ignored) {
                } catch (Exception ex) {
                    exceptionReference.set(ex);
                }
            });
        }
        // 数据
        if (properties.contains("d")) {
            tasks.add(() -> {
                try {
                    node.setNodeData(client.getData(path));
                } catch (KeeperException.NoAuthException ignored) {
                } catch (Exception ex) {
                    exceptionReference.set(ex);
                }
            });
        }
        ThreadUtil.submitVirtual(tasks);
        return exceptionReference.get();
    }

    /**
     * 创建任务
     *
     * @param node       zk节点
     * @param client     zk客户端
     * @param path       路径
     * @param properties 属性
     * @return 异常
     */
    public static Exception of(ZKNode node, ZKClient client, String path, String properties) {
        return new ZKNodeTask().doWorker(node, client, path, properties);
    }
}
