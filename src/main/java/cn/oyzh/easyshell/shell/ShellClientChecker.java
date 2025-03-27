package cn.oyzh.easyshell.shell;

import cn.oyzh.common.thread.TaskManager;

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
    private static final List<ShellClient> CLIENTS = new CopyOnWriteArrayList<>();

    /**
     * 添加客户端
     *
     * @param client 客户端
     */
    public static void push(ShellClient client) {
        CLIENTS.add(client);
        doCheck();
    }

    /**
     * 移除客户端
     * @param client 客户端
     */
    public static void remove(ShellClient client) {
        CLIENTS.remove(client);
    }

    /**
     * 执行监测
     */
    private synchronized static void doCheck() {
        if (taskFuture == null) {
            // 创建任务
            taskFuture = TaskManager.startInterval("client:check", () -> {
                List<ShellClient> closedList = null;
                for (ShellClient client : CLIENTS) {
                    client.updateState();
                    // 如果客户端已关闭，则从队列里面移除
                    if (client.isClosed()) {
                        if (closedList == null) {
                            closedList = new ArrayList<>();
                        }
                        closedList.add(client);
                    }
                }
                if (closedList != null) {
                    CLIENTS.removeAll(closedList);
                }
            }, 1500, 1500);
        }
    }
}