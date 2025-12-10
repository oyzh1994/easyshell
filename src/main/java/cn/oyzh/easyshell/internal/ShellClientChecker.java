package cn.oyzh.easyshell.internal;

import cn.oyzh.common.thread.TaskManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * 客户端监测器
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class ShellClientChecker {

    /**
     * 监测任务
     */
    private static Future<?> taskFuture;

    /**
     * 客户端列表
     */
    private static final List<WeakReference<ShellBaseClient>> CLIENTS = new CopyOnWriteArrayList<>();

    /**
     * 添加客户端
     *
     * @param client 客户端
     */
    public static void push(ShellBaseClient client) {
        if (client != null) {
            CLIENTS.add(new WeakReference<>(client));
            doCheck();
        }
    }

    /**
     * 移除客户端
     *
     * @param client 客户端
     */
    public static void remove(ShellBaseClient client) {
        CLIENTS.removeIf(reference -> reference.get() == client);
//        CLIENTS.remove(client);
    }

    /**
     * 执行监测
     */
    private synchronized static void doCheck() {
        if (taskFuture == null) {
            // 创建任务
            taskFuture = TaskManager.startInterval(() -> {
                List<WeakReference<ShellBaseClient>> closedList = new ArrayList<>();
                for (WeakReference<ShellBaseClient> reference : CLIENTS) {
                    ShellBaseClient client = reference.get();
                    if (client != null) {
                        client.checkState();
                        // 如果客户端已关闭，则从队列里面移除
                        if (client.isClosed()) {
                            closedList.add(reference);
                        }
                    } else {
                        closedList.add(reference);
                    }
                }
                CLIENTS.removeAll(closedList);
            }, 1500, 0);
        }
    }

    /**
     * 停止
     */
    public static void stop() {
        try {
            CLIENTS.clear();
            TaskManager.cancel(taskFuture);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}