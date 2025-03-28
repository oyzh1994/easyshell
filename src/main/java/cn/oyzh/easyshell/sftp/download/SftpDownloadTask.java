package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpTask;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class SftpDownloadTask extends SftpTask<SftpDownloadMonitor> {

    /**
     * 下载状态
     */
    private SftpDownloadStatus status;

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(SftpDownloadStatus status) {
        this.status = status;
        switch (status) {
            case FAILED -> this.statusProperty.set(I18nHelper.failed());
            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
            case DOWNLOAD_ING -> this.statusProperty.set(I18nHelper.downloadIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
    }

    private final SftpDownloadManager manager;

    public SftpDownloadTask(SftpDownloadManager manager, File localFile, SftpFile remoteFile, ShellSftp sftp) {
        this.manager = manager;
        this.destPath = localFile.getPath();
        this.currentFileProperty().set(remoteFile.getPath());
        // 执行线程
        this.executeThread = ThreadUtil.start(() -> {
            try {
                sftp.setHolding(true);
                this.updateStatus(SftpDownloadStatus.IN_PREPARATION);
                this.addMonitorRecursive(localFile, remoteFile, sftp);
                this.updateStatus(SftpDownloadStatus.DOWNLOAD_ING);
                this.calcTotalSize();
                this.updateTotal();
                this.doDownload();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                sftp.setHolding(false);
                this.updateTotal();
                // 如果是非取消和失败，则设置为结束
                if (!this.isCancelled() && !this.isFailed()) {
                    this.updateStatus(SftpDownloadStatus.FINISHED);
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
    protected void addMonitorRecursive(File localFile, SftpFile remoteFile, ShellSftp sftp) throws SftpException {
        // 文件夹
        if (remoteFile.isDir()) {
            // 列举文件
            List<SftpFile> files = sftp.lsFileNormal(remoteFile.getFilePath());
            // 处理文件
            if (CollectionUtil.isNotEmpty(files)) {
                // 本地文件夹
                File localDir = new File(localFile.getPath(), remoteFile.getFileName());
                FileUtil.mkdir(localDir);
                // 添加文件
                for (SftpFile file : files) {
                    file.setParentPath(remoteFile.getFilePath());
                    if (file.isDir()) {
                        this.addMonitorRecursive(localDir, file, sftp);
                    } else {
                        File localFile1 = new File(localDir, file.getFileName());
                        this.addMonitorRecursive(localFile1, file, sftp);
                    }
                }
            }
        } else {// 文件
            this.updateTotal();
            this.monitors.add(new SftpDownloadMonitor(localFile, remoteFile, this, sftp));
        }
    }

    /**
     * 执行下载
     */
    private void doDownload() {
        while (!this.isEmpty()) {
            SftpDownloadMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            ShellSftp sftp = monitor.getSftp();
            try {
                sftp.get(monitor.getRemoteFilePath(), monitor.getLocalFilePath(), monitor, ChannelSftp.OVERWRITE);
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                    JulLog.warn("download canceled");
                } else {
                    ex.printStackTrace();
                    JulLog.warn("file:{} download failed", monitor.getRemoteFileName(), ex);
                    this.failed(monitor, ex);
                    break;
                }
            }
            ThreadUtil.sleep(5);
        }
    }

    @Override
    protected void updateTotal() {
//        if (this.monitors.isEmpty() && !this.isInPreparation()) {
//            this.updateStatus(SftpDownloadStatus.FINISHED);
//        }
        super.updateTotal();
        this.manager.updateDownloading();
    }

    @Override
    public void cancel() {
        super.cancel();
        this.updateStatus(SftpDownloadStatus.CANCELED);
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

    @Override
    public boolean isFailed() {
        return this.status == SftpDownloadStatus.FAILED;
    }

    @Override
    public boolean isFinished() {
        return this.status == SftpDownloadStatus.FINISHED;
    }

    @Override
    public boolean isCancelled() {
        return this.status == SftpDownloadStatus.CANCELED;
    }

    @Override
    public boolean isInPreparation() {
        return this.status == SftpDownloadStatus.IN_PREPARATION;
    }

    /**
     * 是否下载中
     *
     * @return 结果
     */
    public boolean isDownloading() {
        return this.status == SftpDownloadStatus.DOWNLOAD_ING;
    }

    @Override
    public void ended(SftpDownloadMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor);
        if (this.monitors.isEmpty()) {
            this.updateStatus(SftpDownloadStatus.FINISHED);
        }
    }

    @Override
    public void failed(SftpDownloadMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor, exception);
    }

    @Override
    public void canceled(SftpDownloadMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor);
    }

    @Override
    public void changed(SftpDownloadMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor);
    }
}
