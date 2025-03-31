package cn.oyzh.easyshell.sftp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-21
 */
public class SftpManager<M extends SftpMonitor, T extends SftpTask<M>> {

    /**
     * 任务列表
     */
    protected final Queue<T> tasks = new ArrayDeque<>();

    /**
     * 获取任务列表
     *
     * @return 任务列表
     */
    public List<T> getTasks() {
        return new ArrayList<>(this.tasks);
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

    /**
     * 是否已完成
     *
     * @return 结果
     */
    public boolean isCompleted() {
        if (!this.tasks.isEmpty()) {
            for (T task : this.tasks) {
                if (!task.isCompleted()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 移除单个任务
     *
     * @param task 任务
     */
    public void remove(T task) {
//        task.cancel();
        this.tasks.remove(task);
        this.taskSizeChanged();
    }

    /**
     * 取消单个任务
     *
     * @param task 任务
     */
    public void cancel(T task) {
        task.cancel();
        this.tasks.remove(task);
        this.taskSizeChanged();
    }

    /**
     * 取消
     */
    public void cancel() {
        for (T task : this.tasks) {
            task.cancel();
        }
    }

    public int getTaskSize() {
        return this.tasks.size();
    }

    private Runnable taskSizeChangedCallback;

    public void setTaskChangedCallback(Runnable taskChangedCallback) {
        this.taskSizeChangedCallback = taskChangedCallback;
    }

    public void taskSizeChanged() {
        if (this.taskSizeChangedCallback != null) {
            this.taskSizeChangedCallback.run();
        }
    }

    private BiConsumer<String, T> taskStatusChangedCallback;

    public void taskStatusChanged(String status, T task) {
        if (this.taskStatusChangedCallback != null) {
            this.taskStatusChangedCallback.accept(status, task);
        }
    }

    public void setTaskStatusChangedCallback(BiConsumer<String, T> taskStatusChangedCallback) {
        this.taskStatusChangedCallback = taskStatusChangedCallback;
    }

    private BiConsumer<M, T> monitorChangedCallback;

    public void setMonitorChangedCallback(BiConsumer<M, T> monitorChangedCallback) {
        this.monitorChangedCallback = monitorChangedCallback;
    }

    public void monitorChanged(M monitor, T task) {
        if (this.monitorChangedCallback != null) {
            this.monitorChangedCallback.accept(monitor, task);
        }
    }

    private Consumer<M> monitorCanceledCallback;

    public void setMonitorCanceledCallback(Consumer<M> monitorCanceledCallback) {
        this.monitorCanceledCallback = monitorCanceledCallback;
    }

    public void monitorCanceled(M monitor) {
        if (this.monitorCanceledCallback != null) {
            this.monitorCanceledCallback.accept(monitor);
        }
    }

    private Consumer<M> monitorEndedCallback;

    public void setMonitorEndedCallback(Consumer<M> monitorEndedCallback) {
        this.monitorEndedCallback = monitorEndedCallback;
    }

    public void monitorEnded(M monitor) {
        if (this.monitorEndedCallback != null) {
            this.monitorEndedCallback.accept(monitor);
        }
    }

    private BiConsumer<M, Throwable> monitorFailedCallback;

    public void setMonitorFailedCallback(BiConsumer<M, Throwable> monitorFailedCallback) {
        this.monitorFailedCallback = monitorFailedCallback;
    }

    public void monitorFailed(M monitor, Throwable ex) {
        if (this.monitorFailedCallback != null) {
            this.monitorFailedCallback.accept(monitor, ex);
        }
    }
}
