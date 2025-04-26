package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.function.WeakBiConsumer;
import cn.oyzh.common.function.WeakRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellSFTPManager<M extends ShellSFTPMonitor, T extends ShellSFTPTask<M>> implements AutoCloseable {

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

    private final List<WeakRunnable> taskSizeChangedCallbacks = new ArrayList<>();

    public void addTaskSizeChangedCallback(Object obj, Runnable taskChangedCallback) {
        if (taskChangedCallback != null) {
            this.taskSizeChangedCallbacks.add(new WeakRunnable(obj, taskChangedCallback));
        }
    }

    public void taskSizeChanged() {
        for (WeakRunnable runnable : this.taskSizeChangedCallbacks) {
            runnable.run();
        }
    }

    private final List<WeakBiConsumer<String, T>> taskStatusChangedCallbacks = new ArrayList<>();

    public void taskStatusChanged(String status, T task) {
        for (WeakBiConsumer<String, T> consumer : this.taskStatusChangedCallbacks) {
            consumer.accept(status, task);
        }
    }

    public void addTaskStatusChangedCallback(Object obj, BiConsumer<String, T> taskStatusChangedCallback) {
        this.taskStatusChangedCallbacks.add(new WeakBiConsumer<>(obj, taskStatusChangedCallback));
    }

    private final List<WeakBiConsumer<M, T>> monitorChangedCallbacks = new ArrayList<>();

    public void addMonitorChangedCallback(Object obj, BiConsumer<M, T> monitorChangedCallback) {
        this.monitorChangedCallbacks.add(new WeakBiConsumer<>(obj, monitorChangedCallback));
    }

    public void monitorChanged(M monitor, T task) {
        for (WeakBiConsumer<M, T> consumer : this.monitorChangedCallbacks) {
            consumer.accept(monitor, task);
        }
    }

    private final List<WeakBiConsumer<M, T>> monitorCanceledCallbacks = new ArrayList<>();

    public void addMonitorCanceledCallback(Object obj, BiConsumer<M, T> monitorCanceledCallback) {
        this.monitorCanceledCallbacks.add(new WeakBiConsumer<>(obj, monitorCanceledCallback));
    }

    public void monitorCanceled(M monitor, T task) {
        for (WeakBiConsumer<M, T> consumer : this.monitorCanceledCallbacks) {
            consumer.accept(monitor, task);
        }
    }

    private final List<WeakBiConsumer<M, T>> monitorEndedCallbacks = new ArrayList<>();

    public void addMonitorEndedCallback(Object obj, BiConsumer<M, T> monitorEndedCallback) {
        this.monitorEndedCallbacks.add(new WeakBiConsumer<>(obj, monitorEndedCallback));
    }

    public void monitorEnded(M monitor, T task) {
        for (WeakBiConsumer<M, T> consumer : this.monitorEndedCallbacks) {
            consumer.accept(monitor, task);
        }
    }

    private final List<WeakBiConsumer<M, Throwable>> monitorFailedCallbacks = new ArrayList<>();

    public void addMonitorFailedCallback(Object obj, BiConsumer<M, Throwable> monitorFailedCallback) {
        this.monitorFailedCallbacks.add(new WeakBiConsumer<>(obj, monitorFailedCallback));
    }

    public void monitorFailed(M monitor, Throwable ex) {
        for (WeakBiConsumer<M, Throwable> consumer : this.monitorFailedCallbacks) {
            consumer.accept(monitor, ex);
        }
    }

    @Override
    public void close() throws Exception {
        this.tasks.clear();
        this.monitorEndedCallbacks.clear();
        this.monitorFailedCallbacks.clear();
        this.monitorChangedCallbacks.clear();
        this.monitorCanceledCallbacks.clear();
        this.taskStatusChangedCallbacks.clear();
    }
}
