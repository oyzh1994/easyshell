package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.FXProgressTextBar;
import cn.oyzh.i18n.I18nHelper;
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


    private StringProperty speedProperty = new SimpleStringProperty();

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
        this.taskStatusChanged(ShellSFTPStatus.IN_PREPARATION);
        this.calcTotalSize();
        this.taskStatusChanged(ShellSFTPStatus.EXECUTE_ING);
        for (File file : fileList) {
            String remoteFilePath = remotePath + file.getPath().replace(this.localFile.getPath(), "");
            try {
                this.currentFileProperty.set(file.getName());
                this.client.put(file.getPath(), remoteFilePath, new ShellSFTPProgressMonitor() {
                    @Override
                    public boolean count(long count) {
                        currentSize += count;
                        updateSpeed();
                        updateProgress();
                        return status != ShellSFTPStatus.CANCELED;
                    }
                });
            } catch (Exception ex) {
                this.error = ex;
                this.taskStatusChanged(ShellSFTPStatus.FAILED);
                throw ex;
            }
        }
        this.taskStatusChanged(ShellSFTPStatus.FINISHED);
    }

    public void cancel() {
        this.uploadFile.getTask().interrupt();
        this.taskStatusChanged(ShellSFTPStatus.CANCELED);
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
            FileUtil.getAllFiles(this.localFile, f -> {
                fileList.add(f);
                this.totalSize += this.localFile.length();
                this.updateFileSize();
            });
        }
        this.startTime = System.currentTimeMillis();
    }

    public void taskSizeChanged() {

    }

    public void taskCurrentChanged() {

    }

    public void taskStatusChanged(ShellSFTPStatus status) {

    }

    public void taskProgressChanged() {

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

    public File getLocalFile() {
        return localFile;
    }

    public ShellSFTPStatus getStatus() {
        return status;
    }

    private void updateFileSize() {
        this.fileSizeProperty.set(NumberUtil.formatSize(this.totalSize, 4));
    }

    public StringProperty fileSize() {
        return fileSizeProperty;
    }

    public long getTotalCount() {
        return this.fileList.size();
    }

    private FXProgressTextBar progressBar;

    private void updateProgress() {
        if (progressBar == null) {
            progressBar = new FXProgressTextBar();
        }
        progressBar.setValue(this.currentSize,this.totalSize);
    }

    public FXProgressTextBar getProgress() {
        return progressBar;
    }

    public StringProperty currentFileProperty() {
        return this.currentFileProperty;
    }

    public String getDestPath() {
        return this.remotePath;
    }
}
