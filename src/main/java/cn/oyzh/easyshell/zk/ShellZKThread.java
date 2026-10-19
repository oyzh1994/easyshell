package cn.oyzh.easyshell.zk;

import cn.oyzh.fx.plus.thread.BackgroundService;

/**
 * zk任务线程
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKThread extends Thread {

    /**
     * 执行业务
     */
    private final Runnable task;

    /**
     * 构造函数
     *
     * @param task 执行任务
     */
    public ShellZKThread(Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        BackgroundService.submit(this.task);
    }
}
