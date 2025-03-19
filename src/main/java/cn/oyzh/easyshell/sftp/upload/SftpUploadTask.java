package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class SftpUploadTask {

    /**
     * 执行线程
     */
    private final Thread executeThread;

    /**
     * 上传监听器
     */
    private final Queue<SftpUploadMonitor> monitors = new ConcurrentLinkedQueue<>();

    public SftpUploadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void removeMonitor(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    /**
     * 上传状态
     */
    private SftpUploadStatus status;

    /**
     * 状态属性
     */
    private final StringProperty statusProperty = new SimpleStringProperty();

    public StringProperty statusProperty() {
        return statusProperty;
    }

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(SftpUploadStatus status) {
        this.status = status;
        switch (status) {
            case FAILED -> this.statusProperty.set(I18nHelper.failed());
            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
            case UPLOADING -> this.statusProperty.set(I18nHelper.uploadIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
    }

    private final String destPath;

    public String getDestPath() {
        return destPath;
    }

    private final SftpUploadManager manager;

    public SftpUploadTask(SftpUploadManager manager, File localFile, String remoteFile, ShellSftp sftp) {
        this.manager = manager;
        this.destPath = remoteFile;
        this.executeThread = ThreadUtil.start(() -> {
            try {
                sftp.setHolding(true);
                this.updateStatus(SftpUploadStatus.IN_PREPARATION);
                this.addMonitorRecursive(localFile, remoteFile, sftp);
                this.updateStatus(SftpUploadStatus.UPLOADING);
                this.updateTotal();
                this.doUpload();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                sftp.setHolding(false);
                // 如果是非取消，则设置为结束
                if (this.status != SftpUploadStatus.CANCELED) {
                    this.updateStatus(SftpUploadStatus.FINISHED);
                }
                this.updateTotal();
            }
        });
    }

    /**
     * 递归添加监听器
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @param sftp       sftp操作器
     * @throws SftpException 异常
     */
    protected void addMonitorRecursive(File localFile, String remoteFile, ShellSftp sftp) throws SftpException {
        // 文件夹
        if (localFile.isDirectory()) {
            // 列举文件
            File[] files = localFile.listFiles();
            // 处理文件
            if (ArrayUtil.isNotEmpty(files)) {
                // 远程文件夹
                String remoteDir = SftpUtil.concat(remoteFile, localFile.getName());
                // 递归创建文件夹
                sftp.mkdirRecursive(remoteDir);
                // 添加文件
                for (File file : files) {
                    if (file.isDirectory()) {
                        this.addMonitorRecursive(file, remoteDir, sftp);
                    } else {
                        String remoteFile1 = SftpUtil.concat(remoteDir, file.getName());
                        this.addMonitorRecursive(file, remoteFile1, sftp);
                    }
                }
            }
        } else {// 文件
            this.updateTotal();
            this.monitors.add(new SftpUploadMonitor(localFile, remoteFile, this, sftp));
        }
    }

    /**
     * 执行上传
     */
    private void doUpload() {
        while (!this.isEmpty()) {
            SftpUploadMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            ShellSftp sftp = monitor.getSftp();
            try {
                sftp.put(monitor.getLocalFilePath(), monitor.getRemoteFile(), monitor, ChannelSftp.OVERWRITE);
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                    JulLog.warn("upload canceled");
                } else {
                    ex.printStackTrace();
                    JulLog.warn("file:{} upload failed", monitor.getLocalFileName(), ex);
                    this.uploadFailed(monitor, ex);
                }
            }
            ThreadUtil.sleep(5);
        }
    }

    /**
     * 上传完成
     *
     * @param monitor 监听器
     */
    public void uploadEnded(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 上传失败
     *
     * @param monitor   监听器
     * @param exception 异常
     */
    public void uploadFailed(SftpUploadMonitor monitor, Exception exception) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 上传取消
     *
     * @param monitor 监听器
     */
    public void uploadCanceled(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 上传变化
     *
     * @param monitor 监听器
     */
    public void uploadChanged(SftpUploadMonitor monitor) {
        this.currentFileProperty.set(monitor.getLocalFilePath());
        this.currentProgressProperty.set(NumberUtil.formatSize(monitor.getCurrent(), 2) + "/" + NumberUtil.formatSize(monitor.getTotal(), 2));
        JulLog.debug("current file:{}", this.currentFileProperty.get());
        JulLog.debug("current progress:{}", this.currentProgressProperty.get());
    }

    /**
     * 总大小属性
     */
    private final StringProperty totalSizeProperty = new SimpleStringProperty();

    public IntegerProperty totalCountProperty() {
        return totalCountProperty;
    }

    /**
     * 总数量属性
     */
    private final IntegerProperty totalCountProperty = new SimpleIntegerProperty();

    public StringProperty totalSizeProperty() {
        return totalSizeProperty;
    }

    /**
     * 更新总信息
     */
    private void updateTotal() {
        this.totalCountProperty.set(this.monitors.size());
        long totalSize = 0;
        for (SftpUploadMonitor monitor : this.monitors) {
            totalSize += monitor.getLocalFileLength();
        }
        this.totalSizeProperty.set(NumberUtil.formatSize(totalSize, 2));
        JulLog.debug("total size:{}", this.totalSizeProperty.get());
        JulLog.debug("total count:{}", this.totalCountProperty.get());
        this.manager.updateUploading();
    }

    /**
     * 当前文件属性
     */
    private final StringProperty currentFileProperty = new SimpleStringProperty();

    public StringProperty currentFileProperty() {
        return currentFileProperty;
    }

    /**
     * 当前进度属性
     */
    private final StringProperty currentProgressProperty = new SimpleStringProperty();

    public StringProperty currentProgressProperty() {
        return currentProgressProperty;
    }

    /**
     * 取消
     */
    public void cancel() {
        // 停止线程
        ThreadUtil.interrupt(this.executeThread);
        // 取消业务
        for (SftpUploadMonitor monitor : this.monitors) {
            try {
                monitor.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ThreadUtil.start(this.monitors::clear, 500);
        this.updateStatus(SftpUploadStatus.CANCELED);
    }

    /**
     * 是否已完成
     *
     * @return 结果
     */
    public boolean isFinished() {
        return this.status == SftpUploadStatus.FINISHED;
    }

    /**
     * 是否上传中
     *
     * @return 结果
     */
    public boolean isUploading() {
        return this.status == SftpUploadStatus.UPLOADING;
    }

    /**
     * 是否准备中
     *
     * @return 结果
     */
    public boolean isInPreparation() {
        return this.status == SftpUploadStatus.IN_PREPARATION;
    }
}
