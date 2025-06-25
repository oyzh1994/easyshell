package cn.oyzh.easyshell.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 文件传输任务
 *
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellFileTransportTask {

    /**
     * 工作线程
     */
    private Thread worker;

    /**
     * 错误
     */
    private Exception error;

    /**
     * 任务结束时的回调
     */
    private Runnable finishCallback;

    /**
     * 进度属性
     */
    private final StringProperty progressProperty = new SimpleStringProperty();

    /**
     * 文件总数
     */
    private final LongProperty fileCountProperty = new SimpleLongProperty();

    /**
     * 速度
     */
    private final StringProperty speedProperty = new SimpleStringProperty();

    /**
     * 状态
     */
    private final StringProperty statusProperty = new SimpleStringProperty();

    /**
     * 文件大小
     * 剩余大小/总大小
     */
    private final StringProperty fileSizeProperty = new SimpleStringProperty();

    /**
     * 当前文件
     */
    private final StringProperty currentFileProperty = new SimpleStringProperty();

    /**
     * 总大小
     */
    private long totalSize;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 当前大小
     */
    private long currentSize;

    /**
     * 远程路径
     */
    private String remotePath;

    /**
     * 本地文件
     */
    private final ShellFile localFile;

    /**
     * 文件列表
     */
    private List<ShellFile> fileList;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 本地客户端
     */
    private ShellFileClient localClient;

    /**
     * 远程客户端
     */
    private ShellFileClient remoteClient;

    /**
     * 状态
     */
    private transient ShellFileStatus status;

    /**
     * 竞争器
     */
    private final Competitor competitor;

    public ShellFileTransportTask(Competitor competitor, String remotePath, ShellFile localFile, ShellFileClient remoteClient, ShellFileClient localClient) {
        this.localFile = localFile;
        this.remotePath = remotePath;
        this.competitor = competitor;
        this.localClient = localClient;
        this.remoteClient = remoteClient;
        this.clientName = localClient.connectName();
        this.updateStatus(ShellFileStatus.IN_PREPARATION);
    }

    /**
     * 结束传输
     */
    private void finishTransport() {
        if (this.error == null && this.finishCallback != null) {
            this.finishCallback.run();
            this.finishCallback = null;
            // 关闭子客户端
            if (this.localClient.isForked()) {
                IOUtil.close(this.localClient);
            }
            if (this.remoteClient.isForked()) {
                IOUtil.close(this.remoteClient);
            }
        }
    }

    /**
     * 执行传输
     *
     * @param finishCallback 结束回调
     */
    public void doTransport(Runnable finishCallback) {
        this.finishCallback = finishCallback;
        this.worker = ThreadUtil.start(() -> {
            try {
                // 尝试锁定
                if (!this.competitor.tryLock(this)) {
                    this.updateStatus(ShellFileStatus.FAILED);
                    return;
                }
                this.localClient = this.localClient.forkClient();
                this.remoteClient = this.remoteClient.forkClient();
                this.updateStatus(ShellFileStatus.IN_PREPARATION);
                // 初始化文件
                this.initFile();
                this.updateStatus(ShellFileStatus.EXECUTE_ING);
                // 执行传输
                this.doTransport();
                this.finishTransport();
            } catch (Exception ex) {
                // 忽略中断、取消异常
                if (this.status != ShellFileStatus.CANCELED && !ExceptionUtil.isInterrupt(ex)) {
                    this.error = ex;
                    this.updateStatus(this.status);
                }
            } finally {
                // 释放
                this.competitor.release(this);
            }
        });
    }

    /**
     * 执行传输
     */
    private void doTransport() throws Exception {
        InputStream in = null;
        OutputStream out = null;
        // 当前文件传输大小
        AtomicLong currSize = new AtomicLong();
        try {
            while (!this.fileList.isEmpty()) {
                // 取消
                if (this.status == ShellFileStatus.CANCELED) {
                    break;
                }
                // 获取首个文件
                ShellFile file = this.fileList.getFirst();
                // 设置当前文件
                this.currentFileProperty.set(file.getFileName());
                // 远程文件目录
                String remoteFilePath;
                // 文件
                if (this.localFile.isFile()) {
                    remoteFilePath = this.remotePath;
                } else {// 文件夹
                    String pPath = file.getParentPath().replace(this.localFile.getFilePath(), "");
                    String remoteDir = ShellFileUtil.concat(this.remotePath, pPath);
                    remoteFilePath = ShellFileUtil.concat(remoteDir, file.getFileName());
                    // 创建父目录
                    if (!this.remoteClient.exist(remoteDir) && this.remoteClient.isCreateDirRecursiveSupport()) {
                        this.remoteClient.createDirRecursive(remoteDir);
                    }
                }
                // 执行传输
                in = this.localClient.getStream(file, null);
                // 执行函数
                Function<Long, Boolean> func = count -> {
                    currSize.addAndGet(count);
                    this.currentSize += count;
                    // 更新速度
                    this.updateSpeed();
                    // 更新进度
                    this.updateProgress();
                    // 更新文件大小
                    this.updateFileSize();
                    // 判断是否继续
                    return this.status != ShellFileStatus.CANCELED;
                };
                // 流复制
                if (this.remoteClient.isPutStreamSupport()) {
                    out = this.remoteClient.putStream(remoteFilePath, func);
                    in.transferTo(out);
                } else {// 正常处理
                    this.remoteClient.put(in, remoteFilePath, func);
                }
                // 关闭流
                IOUtil.close(in);
                IOUtil.close(out);
                // 关闭部分延迟资源
                this.localClient.closeDelayResources();
                this.remoteClient.closeDelayResources();
                // 更新文件总数
                this.updateFileCount();
                // 移除首个文件
                this.fileList.removeFirst();
                // 重置当前文件大小
                currSize.set(0);
            }
        } catch (Exception ex) {
            // 减去失败部分
            this.currentSize -= currSize.get();
            // 忽略中断、取消异常
            if (this.status != ShellFileStatus.CANCELED && !ExceptionUtil.isInterrupt(ex)) {
                // 更新为失败
                this.updateStatus(ShellFileStatus.FAILED);
                // 抛出异常
                throw ex;
            }
        } finally {
            // 确保关闭流
            IOUtil.close(in);
            IOUtil.close(out);
            // 关闭部分延迟资源
            this.localClient.closeDelayResources();
            this.remoteClient.closeDelayResources();
        }
        // 更新为结束
        if (this.status != ShellFileStatus.CANCELED && this.status != ShellFileStatus.FAILED) {
            this.updateStatus(ShellFileStatus.FINISHED);
        }
    }

    /**
     * 取消
     */
    public void cancel() {
        this.error = null;
        this.competitor.release(this);
        this.updateStatus(ShellFileStatus.CANCELED);
        ThreadUtil.interrupt(this.worker);
        this.finishTransport();
    }

    /**
     * 重试
     */
    public void retry() {
        this.error = null;
        this.updateFileSize();
        this.updateStatus(ShellFileStatus.EXECUTE_ING);
        this.worker = ThreadUtil.start(() -> {
            try {
                // 执行传输
                this.doTransport();
                this.finishTransport();
            } catch (Exception ex) {
                this.error = ex;
                this.updateStatus(this.status);
            }
        });
    }

    /**
     * 初始化文件
     */
    protected void initFile() throws Exception {
        this.remotePath = ShellFileUtil.concat(this.remotePath, this.localFile.getFileName());
        if (this.localFile.isFile()) {
            this.fileList = new ArrayList<>();
            this.fileList.add(this.localFile);
            this.totalSize = this.localFile.getFileSize();
            this.updateFileSize();
        } else {
            this.fileList = new ArrayList<>();
            this.localClient.lsFileRecursive(this.localFile, f -> {
                if (this.status == ShellFileStatus.CANCELED) {
                    throw new InterruptedException();
                }
                if (f instanceof ShellFile f1) {
                    this.fileList.add(f1);
                    this.totalSize += f1.getFileSize();
                    this.updateFileSize();
                    this.updateFileCount();
                }
            });
        }
        this.updateFileCount();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 更新速度
     */
    protected void updateSpeed() {
        // 处理耗时
        long costTime = System.currentTimeMillis() - this.startTime;
        // 当前速度
        long speed = this.currentSize / costTime * 1000;
        // 速度属性
        this.speedProperty.set(NumberUtil.formatSize(speed, 2) + "/" + I18nHelper.second());
    }

    /**
     * 速度属性
     *
     * @return 速度属性
     */
    public StringProperty speedProperty() {
        return speedProperty;
    }

    /**
     * 更新文件大小
     */
    private void updateFileSize() {
        String total = NumberUtil.formatSize(this.totalSize, 2);
        String current = NumberUtil.formatSize(this.currentSize, 2);
        this.fileSizeProperty.set(current + "/" + total);
        JulLog.debug("fileSize: {}", this.fileSizeProperty.getValue());
    }

    /**
     * 文件大小属性
     *
     * @return 文件大小属性
     */
    public StringProperty fileSizeProperty() {
        return fileSizeProperty;
    }

    /**
     * 更新文件数量
     */
    private void updateFileCount() {
        this.fileCountProperty.set(this.fileList.size());
    }

    /**
     * 文件数量属性
     *
     * @return 文件数量属性
     */
    public LongProperty fileCountProperty() {
        return this.fileCountProperty;
    }

    /**
     * 更新进度
     */
    private void updateProgress() {
        this.progressProperty.setValue(NumberUtil.scale(this.currentSize * 1D / this.totalSize * 100D, 2) + "%");
        JulLog.debug("progress: {}", this.progressProperty.getValue());
    }

    /**
     * 当前进度
     *
     * @return 当前进度
     */
    public StringProperty progressProperty() {
        return progressProperty;
    }

    /**
     * 当前文件属性
     *
     * @return 当前文件属性
     */
    public StringProperty currentFileProperty() {
        return this.currentFileProperty;
    }

    /**
     * 源文件路径
     *
     * @return 源文件路径
     */
    public String getSrcPath() {
        return this.localFile.getFilePath();
    }

    /**
     * 目标文件路径
     *
     * @return 目标文件路径
     */
    public String getDestPath() {
        return this.remotePath;
    }

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(ShellFileStatus status) {
        this.status = status;
        switch (status) {
            case FAILED:
                if (this.error != null) {
                    this.statusProperty.set(I18nHelper.transportFailed() + ": " + this.error.getMessage());
                } else {
                    this.statusProperty.set(I18nHelper.transportFailed());
                }
                break;
            case CANCELED:
                this.statusProperty.set(I18nHelper.cancel());
                break;
            case FINISHED:
                this.statusProperty.set(I18nHelper.finished());
                break;
            case EXECUTE_ING:
                this.statusProperty.set(I18nHelper.transportIng());
                break;
            case IN_PREPARATION:
                this.statusProperty.set(I18nHelper.inPreparation());
                break;
        }
        JulLog.debug("status: {}", this.statusProperty.get());
    }

    /**
     * 状态属性
     *
     * @return 状态属性
     */
    public StringProperty statusProperty() {
        return this.statusProperty;
    }

    /**
     * 获取客户端名称
     *
     * @return 客户端名称
     */
    public String getClientName() {
        return this.clientName;
    }

    /**
     * 是否失败
     *
     * @return 结果
     */
    public boolean isFailed() {
        return this.status == ShellFileStatus.FAILED;
    }

    /**
     * 是否取消
     *
     * @return 结果
     */
    public boolean isCanceled() {
        return this.status == ShellFileStatus.CANCELED;
    }
}
