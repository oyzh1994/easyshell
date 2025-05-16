package cn.oyzh.easyshell.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 文件下载任务
 *
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFileDownloadTask {

    /**
     * 取消回调
     */
    private Consumer<ShellFileDownloadTask> cancelCallback;

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
     * 错误
     */
    private Throwable error;

    /**
     * 当前大小
     */
    private long currentSize;

    /**
     * 远程路径
     */
    private final ShellFile remoteFile;

    /**
     * 本地文件
     */
    private final String localPath;

    /**
     * 文件列表
     */
    private List<ShellFile> fileList;

    /**
     * 客户端
     */
    private final ShellFileClient client;

    /**
     * 状态
     */
    private transient ShellFileStatus status;

    public ShellFileDownloadTask(ShellFile remoteFile, String localPath, ShellFileClient client) {
        this.client = client.forkClient();
        this.localPath = localPath;
        this.remoteFile = remoteFile;
        this.updateStatus(ShellFileStatus.IN_PREPARATION);
    }

    /**
     * 执行下载
     *
     * @throws Exception 异常
     */
    public void doDownload() throws Exception {
        this.updateStatus(ShellFileStatus.IN_PREPARATION);
        this.initFile();
        this.updateStatus(ShellFileStatus.EXECUTE_ING);
        while (!this.fileList.isEmpty()) {
            try {
                // 当前文件
                ShellFile file = this.fileList.removeFirst();
                // 设置当前文件
                this.currentFileProperty.set(file.getFileName());
                // 本地文件目录
                String localFilePath;
                // 文件
                if (this.remoteFile.isFile()) {
                    localFilePath = this.getDestPath();
                } else {// 文件夹
                    String pPath = file.getParentPath().replace(this.remoteFile.getFilePath(), "");
                    String localDir = ShellFileUtil.concat(this.getDestPath(), pPath);
                    localFilePath = ShellFileUtil.concat(localDir, file.getFileName());
                    // 创建父目录
                    if (!FileUtil.exist(localDir)) {
                        FileUtil.mkdir(localDir);
                    }
                }
                // 执行下载
                this.client.get(file, localFilePath, (Function<Long, Boolean>) count -> {
                    this.currentSize += count;
                    // 更新速度
                    this.updateSpeed();
                    // 更新进度
                    this.updateProgress();
                    // 更新文件大小
                    this.updateFileSize();
                    // 判断是否继续
                    return this.status != ShellFileStatus.CANCELED;
                });
                // 更新文件总数
                this.updateFileCount();
            } catch (Exception ex) {
                // 忽略中断异常
                if (!ExceptionUtil.hasMessage(ex, "InterruptedException", "InterruptedIOException")) {
                    this.error = ex;
                    // 更新为失败
                    this.updateStatus(ShellFileStatus.FAILED);
                }
                throw ex;
            }
        }
        // 更新为结束
        if (this.status != ShellFileStatus.CANCELED && this.status != ShellFileStatus.FAILED) {
            this.updateStatus(ShellFileStatus.FINISHED);
        }
        // 关闭子客户端
        if (this.client.isForked()) {
            IOUtil.close(this.client);
        }
    }

    /**
     * 取消
     */
    public void cancel() {
        this.updateStatus(ShellFileStatus.CANCELED);
        if (this.cancelCallback != null) {
            this.cancelCallback.accept(this);
        }
    }

    /**
     * 初始化文件
     */
    protected void initFile() throws Exception {
        if (this.remoteFile.isFile()) {
            this.fileList = new ArrayList<>();
            this.fileList.add(this.remoteFile);
            this.totalSize = this.remoteFile.getFileSize();
            this.updateFileSize();
        } else {
            this.fileList = new ArrayList<>();
            this.client.lsFileRecursive(this.remoteFile, f -> {
                if (f instanceof ShellFile f1) {
                    this.fileList.add(f1);
                    this.totalSize += f1.getFileSize();
                    this.updateFileSize();
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
        return this.remoteFile.getFilePath();
    }

    /**
     * 目标文件路径
     *
     * @return 目标文件路径
     */
    public String getDestPath() {
        return ShellFileUtil.concat(this.localPath, this.remoteFile.getFileName());
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
                this.statusProperty.set(I18nHelper.downloadFailed());
                break;
            case CANCELED:
                this.statusProperty.set(I18nHelper.cancel());
                break;
            case FINISHED:
                this.statusProperty.set(I18nHelper.finished());
                break;
            case EXECUTE_ING:
                this.statusProperty.set(I18nHelper.downloadIng());
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
     * 设置取消回调
     *
     * @param cancelCallback 取消回调
     */
    public void setCancelCallback(Consumer<ShellFileDownloadTask> cancelCallback) {
        this.cancelCallback = cancelCallback;
    }
}
