package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.file.FileUtil;
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
public class ShellSFTPUploadTask {

    private File localFile;

    private long totalSize;

    private long currentSize;

    private LongProperty fileCountProperty = new SimpleLongProperty();

    private StringProperty speedProperty = new SimpleStringProperty();

    private StringProperty statusProperty = new SimpleStringProperty();

    private StringProperty fileSizeProperty = new SimpleStringProperty();

    private StringProperty currentFileProperty = new SimpleStringProperty();

    private String remotePath;

    private ShellSFTPClient client;

    private ShellSFTPStatus status;

    private List<File> fileList;

    private Throwable error;

    private long startTime;

    private ShellSFTPUploadFile uploadFile;

    public ShellSFTPUploadTask(ShellSFTPUploadFile uploadFile, ShellSFTPClient client) {
        this.client = client;
        this.localFile = uploadFile.getLocalFile();
        this.remotePath = uploadFile.getRemotePath();
    }

    private void setStatus(ShellSFTPStatus status) {
        this.status = status;
    }

    public void upload() throws Exception {
        this.updateStatus(ShellSFTPStatus.IN_PREPARATION);
        this.calcTotalSize();
        this.updateStatus(ShellSFTPStatus.EXECUTE_ING);
        String localPath = localFile.getPath();
        for (File file : fileList) {
            String pPath = file.getParent().replace(localPath, "");
            String remoteDir = ShellFileUtil.concat(remotePath, pPath);
            String remoteFilePath = ShellFileUtil.concat(remoteDir, file.getName());
            try {
                this.currentFileProperty.set(file.getName());
                // 创建父目录
                if (!this.client.exist(remoteDir)) {
                    this.client.mkdirRecursive(remoteDir);
                }
                this.client.put(file.getPath(), remoteFilePath, new ShellSFTPProgressMonitor() {
                    @Override
                    public boolean count(long count) {
                        currentSize += count;
                        updateSpeed();
                        updateProgress();
                        return status != ShellSFTPStatus.CANCELED;
                    }
                });
                this.updateFileCount();
            } catch (Exception ex) {
                this.error = ex;
                this.updateStatus(ShellSFTPStatus.FAILED);
                throw ex;
            }
        }
        this.updateStatus(ShellSFTPStatus.FINISHED);
    }

    public void cancel() {
        this.uploadFile.getTask().interrupt();
        this.updateStatus(ShellSFTPStatus.CANCELED);
    }

    /**
     * 计算总大小
     */
    protected void calcTotalSize() {
        if (this.localFile.isFile()) {
            this.fileList = new ArrayList<>();
            this.fileList.add(this.localFile);
            this.totalSize = this.localFile.length();
            this.updateFileSize();
        } else {
            this.fileList = new ArrayList<>();
            this.remotePath = ShellFileUtil.concat(this.remotePath, this.localFile.getName());
            FileUtil.getAllFiles(this.localFile, f -> {
                fileList.add(f);
                this.totalSize += f.length();
                this.updateFileSize();
            });
        }
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 计算当前大小
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

    public StringProperty fileSizeProperty() {
        return fileSizeProperty;
    }

    private void updateFileSize() {
        this.fileSizeProperty.set(NumberUtil.formatSize(this.totalSize, 4));
    }

    public StringProperty fileSize() {
        return fileSizeProperty;
    }

    private void updateFileCount() {
        this.fileCountProperty.set(this.fileList.size());
    }

    public LongProperty fileCountProperty() {
        return this.fileCountProperty;
    }

    private FXProgressTextBar progressBar;

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
        return localFile.getPath();
    }

    public String getDestPath() {
        return this.remotePath;
    }

    private void updateStatus(ShellSFTPStatus status) {
        this.status = status;
        switch (status) {
            case CANCELED:
                this.statusProperty.set(I18nHelper.cancel());
            case FINISHED:
                this.statusProperty.set(I18nHelper.finished());
            case FAILED:
                this.statusProperty.set(I18nHelper.downloadFailed());
            case IN_PREPARATION:
                this.statusProperty.set(I18nHelper.inPreparation());
            case EXECUTE_ING:
                this.statusProperty.set(I18nHelper.downloadIng());
        }
    }

    public StringProperty statusProperty() {
        return this.statusProperty;
    }
}
