//package cn.oyzh.easyshell.sftp;
//
//import cn.oyzh.common.util.NumberUtil;
//import cn.oyzh.easyshell.file.ShellFileStatus;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.beans.property.StringProperty;
//
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
///**
// * @author oyzh
// * @since 2025-03-21
// */
//public abstract class ShellSFTPTask<M extends ShellSFTPMonitor> {
//
//    /**
//     * 状态
//     */
//    protected ShellFileStatus status;
//
//    /**
//     * 更新状态
//     *
//     * @param status 状态
//     */
//    public void updateStatus(ShellFileStatus status) {
//        this.status = status;
//        switch (status) {
//            case FAILED -> this.statusProperty.set(I18nHelper.failed());
//            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
//            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
//            default -> this.statusProperty.set(I18nHelper.inPreparation());
//        }
//    }
//
//    /**
//     * 传输监听器
//     */
//    protected final Queue<M> monitors = new ConcurrentLinkedQueue<>();
//
//
//    /**
//     * 获取监听器
//     *
//     * @return 监听器
//     */
//    public M takeMonitor() {
//        return this.monitors.peek();
//    }
//
//    /**
//     * 移除监听器
//     *
//     * @param monitor 监听器
//     */
//    public void remove(M monitor) {
//        this.monitors.remove(monitor);
////        this.updateTotal();
//    }
//
//    /**
//     * 任务是否为空
//     *
//     * @return 任务是否为空
//     */
//    public boolean isEmpty() {
//        return this.monitors.isEmpty();
//    }
//
//    /**
//     * 任务大小
//     *
//     * @return 任务大小
//     */
//    public int size() {
//        return this.monitors.size();
//    }
//
//    /**
//     * 源路径
//     */
//    protected String srcPath;
//
//    public String getSrcPath() {
//        return srcPath;
//    }
//
//    /**
//     * 目标路径
//     */
//    protected String destPath;
//
//    public String getDestPath() {
//        return destPath;
//    }
//
//    /**
//     * 执行完成
//     *
//     * @param monitor 监听器
//     */
//    public void ended(M monitor) {
//        this.monitors.remove(monitor);
//        this.updateStatus(ShellFileStatus.FINISHED);
////        this.updateTotal();
//    }
//
//    /**
//     * 执行失败
//     *
//     * @param monitor   监听器
//     * @param exception 异常
//     */
//    public void failed(M monitor, Throwable exception) {
//        this.monitors.remove(monitor);
//        this.updateStatus(ShellFileStatus.FAILED);
////        this.updateTotal();
//    }
//
//    /**
//     * 执行取消
//     *
//     * @param monitor 监听器
//     */
//    public void canceled(M monitor) {
//        this.monitors.remove(monitor);
//        this.updateStatus(ShellFileStatus.CANCELED);
////        this.updateTotal();
//    }
//
//    /**
//     * 执行变化
//     *
//     * @param monitor 监听器
//     */
//    public void changed(M monitor) {
////        this.currentFileProperty.set(monitor.getFilePath());
//        this.calcCurrentSize();
////        if (this.progress != null) {
////            this.progress.setValue(this.currentSize, this.totalSize);
////        }
////        JulLog.debug("current file:{}", this.currentFileProperty.get());
//    }
//
////    protected FXProgressTextBar progress;
////
////    public FXProgressTextBar getProgress() {
////        if (this.progress == null) {
////            this.progress = new FXProgressTextBar();
////            this.progress.setValue(this.currentSize, this.totalSize);
////        }
////        return progress;
////    }
//
//    /**
//     * 状态属性
//     */
//    protected final StringProperty statusProperty = new SimpleStringProperty();
//
//    public StringProperty statusProperty() {
//        return statusProperty;
//    }
//
//    public String getStatus() {
//        return statusProperty.get();
//    }
//
//    /**
//     * 速度属性
//     */
//    private StringProperty speedProperty;
//
//    public StringProperty speedProperty() {
//        if (this.speedProperty == null) {
//            this.speedProperty = new SimpleStringProperty();
//        }
//        return this.speedProperty;
//    }
//
//    public String getSpeed() {
//        return this.speedProperty.get();
//    }
//
////    /**
////     * 文件大小属性、显示用
////     */
////    private StringProperty fileSizeProperty;
////
////    public StringProperty fileSizeProperty() {
////        if (this.fileSizeProperty == null) {
////            this.fileSizeProperty = new SimpleStringProperty(NumberUtil.formatSize(this.totalSize, 2));
////        }
////        return this.fileSizeProperty;
////    }
//
//    /**
//     * 获取文件大小，显示用
//     *
//     * @return 文件大小
//     */
//    public String getFileSize() {
////        return this.fileSizeProperty().get();
//        return NumberUtil.formatSize(this.totalSize, 2);
//    }
//
//    /**
//     * 文件总大小
//     */
//    private long totalSize;
//
//    public long getTotalSize() {
//        return totalSize;
//    }
//
//    /**
//     * 开始时间
//     */
//    private long startTime;
//
//    /**
//     * 当前大小
//     */
//    private long currentSize;
//
//    public long getCurrentSize() {
//        return currentSize;
//    }
//
////    /**
////     * 总数量属性
////     */
////    private final IntegerProperty totalCountProperty = new SimpleIntegerProperty();
////
////    public IntegerProperty totalCountProperty() {
////        return totalCountProperty;
////    }
//
////    /**
////     * 当前文件属性
////     */
////    private final StringProperty currentFileProperty = new SimpleStringProperty();
////
////    public StringProperty currentFileProperty() {
////        return currentFileProperty;
////    }
//
//    /**
//     * 取消
//     */
//    public void cancel() {
////        // 停止线程
////        ThreadUtil.interrupt(this.executeThread);
//        // 取消业务
//        for (M monitor : this.monitors) {
//            try {
//                monitor.cancel();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 移除
//     */
//    public abstract void remove();
//
//    /**
//     * 是否已失败
//     *
//     * @return 结果
//     */
//    public boolean isFailed() {
//        return this.status == ShellFileStatus.FAILED;
//    }
//
//    /**
//     * 是否已结束
//     *
//     * @return 结果
//     */
//    public boolean isFinished() {
//        return this.status == ShellFileStatus.FINISHED;
//    }
//
//    /**
//     * 是否已取消
//     *
//     * @return 结果
//     */
//    public boolean isCancelled() {
//        return this.status == ShellFileStatus.CANCELED;
//    }
//
//    /**
//     * 是否执行中
//     *
//     * @return 结果
//     */
//    public boolean isExecuting() {
//        return this.status == ShellFileStatus.EXECUTE_ING;
//    }
//
//    /**
//     * 是否准备中
//     *
//     * @return 结果
//     */
//    public boolean isInPreparation() {
//        return this.status == ShellFileStatus.IN_PREPARATION;
//    }
//
//    /**
//     * 是否已完成
//     *
//     * @return 结果
//     */
//    public boolean isCompleted() {
//        return this.isCancelled() || this.isFailed() || this.isFinished() || this.monitors.isEmpty();
//    }
//
//    /**
//     * 计算总大小
//     */
//    protected void calcTotalSize() {
//        long totalSize = 0;
//        for (M monitor : this.monitors) {
//            totalSize += monitor.getTotal();
//        }
//        // 总大小
//        this.totalSize = totalSize;
//        // 开始时间
//        this.startTime = System.currentTimeMillis();
////        // 文件大小
////        if (this.fileSizeProperty != null) {
////            this.fileSizeProperty.set(NumberUtil.formatSize(totalSize, 2));
////        }
//    }
//
//    /**
//     * 计算当前大小
//     */
//    protected void calcCurrentSize() {
//        long currentSize = 0;
//        for (M monitor : this.monitors) {
//            currentSize += monitor.getTotal() - monitor.getCurrent();
//        }
//        this.currentSize = this.totalSize - currentSize;
//        // 处理耗时
//        long costTime = System.currentTimeMillis() - this.startTime;
//        // 当前速度
//        long speed = this.currentSize / costTime * 1000;
//        // 速度属性
//        this.speedProperty().set(NumberUtil.formatSize(speed, 2) + "/" + I18nHelper.second());
//    }
//
////    /**
////     * 更新总信息
////     */
////    protected void updateTotal() {
////        this.totalCountProperty.set(this.monitors.size());
////        JulLog.debug("total count:{}", this.totalCountProperty.get());
////    }
//}
