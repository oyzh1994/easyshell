package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyshell.sftp.SftpTask;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class SftpUploadTask extends SftpTask<SftpUploadMonitor> {

    /**
     * 上传状态
     */
    private SftpUploadStatus status;

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
            case UPLOAD_ING -> this.statusProperty.set(I18nHelper.uploadIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
    }

    private final SftpUploadManager manager;

    public SftpUploadTask(SftpUploadManager manager, File localFile, String remoteFile, ShellSftp sftp) {
        this.manager = manager;
        this.destPath = remoteFile;
        this.currentFileProperty().set(localFile.getPath());
        this.executeThread = ThreadUtil.start(() -> {
            try {
                sftp.setHolding(true);
                this.updateStatus(SftpUploadStatus.IN_PREPARATION);
                this.addMonitorRecursive(localFile, remoteFile, sftp);
                this.updateStatus(SftpUploadStatus.UPLOAD_ING);
                this.calcTotalSize();
                this.updateTotal();
                this.doUpload();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                sftp.setHolding(false);
                this.updateTotal();
                // 如果是非取消和失败，则设置为结束
                if (!this.isCancelled() && !this.isFailed()) {
                    this.updateStatus(SftpUploadStatus.FINISHED);
                }
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
                    this.failed(monitor, ex);
                }
            }
            ThreadUtil.sleep(5);
        }
    }

    @Override
    protected void updateTotal() {
//        if (this.monitors.isEmpty() && !this.isInPreparation()) {
//            this.updateStatus(SftpUploadStatus.FINISHED);
//        }
        super.updateTotal();
        this.manager.updateUploading();
    }

    @Override
    public void cancel() {
        super.cancel();
        this.updateStatus(SftpUploadStatus.CANCELED);
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

    @Override
    public boolean isFailed() {
        return this.status == SftpUploadStatus.FAILED;
    }

    @Override
    public boolean isFinished() {
        return this.status == SftpUploadStatus.FINISHED;
    }

    @Override
    public boolean isCancelled() {
        return this.status == SftpUploadStatus.CANCELED;
    }

    @Override
    public boolean isInPreparation() {
        return this.status == SftpUploadStatus.IN_PREPARATION;
    }

    /**
     * 是否上传中
     *
     * @return 结果
     */
    public boolean isUploading() {
        return this.status == SftpUploadStatus.UPLOAD_ING;
    }

    @Override
    public void ended(SftpUploadMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor);
        if (this.monitors.isEmpty()) {
            this.updateStatus(SftpUploadStatus.FINISHED);
        }
    }

    @Override
    public void failed(SftpUploadMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor, exception);
    }

    @Override
    public void canceled(SftpUploadMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor);
    }

    @Override
    public void changed(SftpUploadMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor);
    }
}
