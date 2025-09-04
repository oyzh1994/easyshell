package cn.oyzh.easyshell.zk;

import cn.oyzh.fx.plus.thread.BackgroundService;

/**
 * zk任务线程
 *
 * @author oyzh
 * @since 2023/4/27
 */
public class ZKThread extends Thread {

    /**
     * 执行业务
     */
    private final Runnable task;

    public ZKThread( Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        BackgroundService.submit(this.task);
    }
}
