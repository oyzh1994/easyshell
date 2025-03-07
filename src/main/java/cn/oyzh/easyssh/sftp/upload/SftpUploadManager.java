package cn.oyzh.easyssh.sftp.upload;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SftpUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.Setter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadManager {

    private Queue<SftpUploadMonitor> monitors = new ArrayDeque<>();

    @Setter
    private Consumer<SftpUploadEnded> uploadEndedCallback;

    @Setter
    private Consumer<SftpUploadFailed> uploadFailedCallback;

    @Setter
    private Consumer<SftpUploadChanged> uploadChangedCallback;

    @Setter
    private Consumer<SftpUploadCanceled> uploadCanceledCallback;

    @Setter
    private Consumer<SftpUploadInPreparation> uploadInPreparationCallback;

    public void createMonitor(File localFile, String remoteFile, SSHSftp sftp) throws SftpException {
        if (this.monitors == null) {
            this.monitors = new ArrayDeque<>();
        }
        ThreadUtil.start(() -> {
            try {
                this.uploadInPreparation();
                this.addMonitorRecursive(localFile, remoteFile, sftp);
                this.doUpload();
            } catch (SftpException ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    protected void addMonitorRecursive(File localFile, String remoteFile, SSHSftp sftp) throws SftpException {
        // 文件夹
        if (localFile.isDirectory()) {
            // 列举文件
            File[] files = localFile.listFiles();
            if (ArrayUtil.isNotEmpty(files)) {
                // 远程文件夹
                String remoteDir = SftpUtil.concat(remoteFile, localFile.getName());
                // 递归创建文件夹
                sftp.mkdirRecursive(remoteDir);
                for (File file : files) {
                    // 远程文件
                    String remoteFile1 = SftpUtil.concat(remoteDir, file.getName());
                    this.addMonitorRecursive(file, remoteFile1, sftp);
                }
            }
        } else {// 文件
            this.monitors.add(new SftpUploadMonitor(localFile, remoteFile, this, sftp));
        }
    }

    public SftpUploadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void uploadEnded(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.uploadEndedCallback != null) {
            SftpUploadEnded ended = new SftpUploadEnded();
            ended.setFileCount(this.size());
            ended.setRemoteFile(monitor.getRemoteFile());
            ended.setLocalFileName(monitor.getLocalFileName());
            this.uploadEndedCallback.accept(ended);
        }
    }

    public void uploadFailed(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.uploadFailedCallback != null) {
            SftpUploadFailed changed = new SftpUploadFailed();
            changed.setFileCount(this.size());
            changed.setRemoteFile(monitor.getRemoteFile());
            changed.setLocalFileName(monitor.getLocalFileName());
            this.uploadFailedCallback.accept(changed);
        }
    }

    public void uploadCanceled(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.uploadCanceledCallback != null) {
            SftpUploadCanceled ended = new SftpUploadCanceled();
            ended.setFileCount(this.size());
            ended.setRemoteFile(monitor.getRemoteFile());
            ended.setLocalFileName(monitor.getLocalFileName());
            this.uploadCanceledCallback.accept(ended);
        }
    }

    public void uploadChanged(SftpUploadMonitor monitor) {
        if (this.uploadChangedCallback != null) {
            SftpUploadChanged changed = new SftpUploadChanged();
            changed.setFileCount(this.size());
            changed.setFileSize(this.count());
            changed.setTotal(monitor.getTotal());
            changed.setCurrent(monitor.getCurrent());
            changed.setRemoteFile(monitor.getRemoteFile());
            changed.setLocalFileName(monitor.getLocalFileName());
            this.uploadChangedCallback.accept(changed);
        }
    }

    public void uploadInPreparation() {
        if (this.uploadInPreparationCallback != null) {
            SftpUploadInPreparation inPreparation = new SftpUploadInPreparation();
            this.uploadInPreparationCallback.accept(inPreparation);
        }
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    public void removeMonitor(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
    }

    public int size() {
        return this.monitors.size();
    }

    public long count() {
        long cnt = 0;
        for (SftpUploadMonitor monitor : this.monitors) {
            cnt += monitor.getLocalFileLength();
        }
        return cnt;
    }

    public void cancel() {
        for (SftpUploadMonitor monitor : this.monitors) {
            monitor.cancel();
        }
    }

    private void doUpload() {
        if (this.isUploading()) {
            return;
        }
        this.setUploading(true);
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    SftpUploadMonitor monitor = this.takeMonitor();
                    if (monitor == null) {
                        break;
                    }
                    if (monitor.isFinished()) {
                        ThreadUtil.sleep(5);
                        continue;
                    }
                    SSHSftp sftp = monitor.getSftp();
                    try {
                        sftp.put(monitor.getLocalFilePath(), monitor.getRemoteFile(), monitor, ChannelSftp.OVERWRITE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JulLog.warn("file:{} upload failed", monitor.getLocalFileName(), ex);
                        this.uploadFailed(monitor);
                    }
                    ThreadUtil.sleep(5);
                }
            } finally {
                this.setUploading(false);
            }
        });
    }

    private final AtomicBoolean uploading = new AtomicBoolean(false);

    public void setUploading(boolean uploading) {
        this.uploading.set(uploading);
    }

    public boolean isUploading() {
        return this.uploading.get();
    }
}
