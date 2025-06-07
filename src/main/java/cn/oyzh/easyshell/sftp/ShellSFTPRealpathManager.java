package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * sftp链接文件管理器
 *
 * @author oyzh
 * @since 2025-06-07
 */
public class ShellSFTPRealpathManager {

    /**
     * sftp客户端
     */
    private final ShellSFTPClient client;

    /**
     * 文件队列
     */
    private final List<ShellSFTPFile> QUEUE = new ArrayList<>();

    /**
     * 运行中标志位
     */
    private final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public ShellSFTPRealpathManager(ShellSFTPClient client) {
        this.client = client;
    }

    /**
     * 添加文件
     *
     * @param files 文件列表
     */
    public void put(List<ShellSFTPFile> files) {
        QUEUE.addAll(files);
        this.doRealpath();
    }

    /**
     * 读取链接
     */
    private void doRealpath() {
        if (RUNNING.get()) {
            return;
        }
        RUNNING.set(true);
        List<Runnable> tasks = new ArrayList<>();
        // 按指定份数切割
        List<List<ShellSFTPFile>> list = CollectionUtil.splitIntoParts(QUEUE, 3);
        // 提交到任务
        for (List<ShellSFTPFile> files : list) {
            Runnable task = () -> {
                for (ShellSFTPFile file : files) {
                    if (this.client.isClosed()) {
                        break;
                    }
                    try {
                        ShellSFTPUtil.realpath(file, this.client);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            tasks.add(task);
        }
        // 执行任务
        ThreadUtil.submitVirtual(tasks);
        // 清除队列
        QUEUE.clear();
        RUNNING.set(false);
    }

    /**
     * 等待完成
     */
    public void waitComplete() {
        while (this.RUNNING.get()) {
            ThreadUtil.sleep(5);
        }
    }
}
