package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.controls.FXProgressTextBar;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author oyzh
 * @since 2025-03-21
 */
public abstract class SftpTask<M extends SftpMonitor> {

    /**
     * 执行线程
     */
    protected Thread executeThread;

    /**
     * 传输监听器
     */
    protected final Queue<M> monitors = new ConcurrentLinkedQueue<>();

    public M takeMonitor() {
        return this.monitors.peek();
    }

    public void removeMonitor(M monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    protected String destPath;

    public String getDestPath() {
        return destPath;
    }

    /**
     * 执行完成
     *
     * @param monitor 监听器
     */
    public void ended(M monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 执行失败
     *
     * @param monitor   监听器
     * @param exception 异常
     */
    public void failed(M monitor, Throwable exception) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 执行取消
     *
     * @param monitor 监听器
     */
    public void canceled(M monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 执行变化
     *
     * @param monitor 监听器
     */
    public void changed(M monitor) {
//        this.updateTotal();
        this.currentFileProperty.set(monitor.getFilePath());
        if (this.progress != null) {
            this.calcCurrentSize();
            this.progress.setValue(this.currentSize, this.totalSize);
        }
//        this.currentProgressProperty.set(NumberUtil.formatSize(monitor.getCurrent(), 2) + "/" + NumberUtil.formatSize(monitor.getTotal(), 2));
        JulLog.debug("current file:{}", this.currentFileProperty.get());
//        JulLog.debug("current progress:{}", this.currentProgressProperty.get());
    }

    protected FXProgressTextBar progress;

    public FXProgressTextBar getProgress() {
        if (this.progress == null) {
            this.progress = new FXProgressTextBar();
        }
        return progress;
    }

    /**
     * 状态属性
     */
    protected final StringProperty statusProperty = new SimpleStringProperty();

    public StringProperty statusProperty() {
        return statusProperty;
    }

//    /**
//     * 总大小属性
//     */
//    private final StringProperty totalSizeProperty = new SimpleStringProperty();
//
//    public StringProperty totalSizeProperty() {
//        return totalSizeProperty;
//    }

    private long totalSize;

    private long currentSize;

    public IntegerProperty totalCountProperty() {
        return totalCountProperty;
    }

    /**
     * 总数量属性
     */
    private final IntegerProperty totalCountProperty = new SimpleIntegerProperty();

    /**
     * 当前文件属性
     */
    private final StringProperty currentFileProperty = new SimpleStringProperty();

    public StringProperty currentFileProperty() {
        return currentFileProperty;
    }
//
//    /**
//     * 当前进度属性
//     */
//    private final StringProperty currentProgressProperty = new SimpleStringProperty();
//
//    public StringProperty currentProgressProperty() {
//        return currentProgressProperty;
//    }

    /**
     * 取消
     */
    public void cancel() {
        // 停止线程
        ThreadUtil.interrupt(this.executeThread);
        // 取消业务
        for (M monitor : this.monitors) {
            try {
                monitor.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 移除
     */
    public abstract void remove();

    /**
     * 是否已失败
     *
     * @return 结果
     */
    public abstract boolean isFailed();

    /**
     * 是否已结束
     *
     * @return 结果
     */
    public abstract boolean isFinished();

    /**
     * 是否已取消
     *
     * @return 结果
     */
    public abstract boolean isCancelled();

    /**
     * 是否准备中
     *
     * @return 结果
     */
    public abstract boolean isInPreparation();

    /**
     * 是否已完成
     *
     * @return 结果
     */
    public boolean isCompleted() {
        return this.isCancelled() || this.isFailed() || this.isFinished() || this.monitors.isEmpty();
    }

    protected void calcTotalSize() {
        long totalSize = 0;
        for (M monitor : this.monitors) {
            totalSize += monitor.getTotal();
        }
        this.totalSize = totalSize;
    }

    protected void calcCurrentSize() {
        long currentSize = 0;
        for (M monitor : this.monitors) {
            currentSize += monitor.getTotal() - monitor.getCurrent();
        }
        this.currentSize = this.totalSize - currentSize;
    }

    /**
     * 更新总信息
     */
    protected void updateTotal() {
        this.totalCountProperty.set(this.monitors.size());
//        this.totalSizeProperty.set(NumberUtil.formatSize(totalSize, 2));
//        JulLog.debug("total size:{}", this.totalSizeProperty.get());
        JulLog.debug("total count:{}", this.totalCountProperty.get());
    }
}
