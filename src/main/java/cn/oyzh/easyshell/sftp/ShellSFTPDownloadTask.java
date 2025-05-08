package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.fx.plus.controls.FXProgressTextBar;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellSFTPDownloadTask {

    /**
     * 进度条
     */
    private FXProgressTextBar progressBar;

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
    private final ShellSFTPFile remoteFile;

    /**
     * 本地文件
     */
    private final File localPath;

    /**
     * 文件列表
     */
    private List<ShellSFTPFile> fileList;

    /**
     * 客户端
     */
    private final ShellSFTPClient client;

    /**
     * 状态
     */
    private transient ShellSFTPStatus status;

    public ShellSFTPDownloadTask(ShellSFTPFile remoteFile, File localPath, ShellSFTPClient client) {
        this.client = client;
        this.localPath = localPath;
        this.remoteFile = remoteFile;
    }

    /**
     * 执行上传
     *
     * @throws Exception 异常
     */
    public void download() throws Exception {
        this.updateStatus(ShellSFTPStatus.IN_PREPARATION);
        this.initFile();
        this.updateStatus(ShellSFTPStatus.EXECUTE_ING);
        while (!this.fileList.isEmpty()) {
            try {
                // 当前文件
                ShellSFTPFile file = this.fileList.removeFirst();
                // 设置文件
                this.currentFileProperty.set(file.getName());
                // 远程文件目录
                String localFilePath;
//                // 文件
                if (this.remoteFile.isFile()) {
                    localFilePath = new File(this.localPath, file.getFileName()).getPath();
                } else {// 文件夹
                    String pPath = file.getParentPath().replace(this.remoteFile.getFilePath(), "");
                    String localDir = ShellFileUtil.concat(this.localPath.getPath(), pPath);
                    localFilePath = ShellFileUtil.concat(localDir, file.getName());
                    // 创建父目录
                    if (!FileUtil.exist(localDir)) {
                        FileUtil.mkdir(localDir);
                    }
                }
                // 执行上传
                this.client.get(file.getFilePath(), localFilePath, new ShellSFTPProgressMonitor() {
                    @Override
                    public boolean count(long count) {
                        currentSize += count;
                        updateSpeed();
                        updateProgress();
                        updateFileSize();
                        return status != ShellSFTPStatus.CANCELED;
                    }
                });
                this.updateFileCount();
            } catch (Exception ex) {// 其他
                // 忽略中断异常
                if (!ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                    this.error = ex;
                    this.updateStatus(ShellSFTPStatus.FAILED);
                    throw ex;
                }
            }
        }
        if (this.status != ShellSFTPStatus.CANCELED && this.status != ShellSFTPStatus.FAILED) {
            this.updateStatus(ShellSFTPStatus.FINISHED);
        }
    }

    /**
     * 取消
     */
    public void cancel() {
        ThreadUtil.interrupt(this.worker);
        this.updateStatus(ShellSFTPStatus.CANCELED);
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
            this.client.getAllFiles(this.remoteFile, f -> {
                fileList.add(f);
                this.totalSize += f.getFileSize();
                this.updateFileSize();
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
    }

    public StringProperty fileSizeProperty() {
        return fileSizeProperty;
    }

    /**
     * 更新文件数量
     */
    private void updateFileCount() {
        this.fileCountProperty.set(this.fileList.size());
    }

    public LongProperty fileCountProperty() {
        return this.fileCountProperty;
    }

    /**
     * 更新进度
     */
    private void updateProgress() {
        if (this.progressBar == null) {
            this.progressBar = new FXProgressTextBar();
        }
        this.progressBar.setValue(this.currentSize, this.totalSize);
    }

    public FXProgressTextBar getProgress() {
        return progressBar;
    }

    public StringProperty currentFileProperty() {
        return this.currentFileProperty;
    }

    public String getSrcPath() {
        return  this.remoteFile.getFilePath();
    }

    public String getDestPath() {
        return ShellFileUtil.concat(this.localPath.getPath(), this.remoteFile.getFileName());
    }

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(ShellSFTPStatus status) {
        this.status = status;
        switch (status) {
            case FAILED:
                this.statusProperty.set(I18nHelper.uploadFailed());
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
        JulLog.info("status:{}", this.statusProperty.get());
    }

    public StringProperty statusProperty() {
        return this.statusProperty;
    }

    private Thread worker;

    public void setWorker(Thread worker) {
        this.worker = worker;
    }
}
