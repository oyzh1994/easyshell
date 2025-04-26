package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPChannel;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPTask;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellSFTPDownloadTask extends ShellSFTPTask<ShellSFTPDownloadMonitor> {

    /**
     * 下载状态
     */
    private ShellSFTPDownloadStatus status;

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(ShellSFTPDownloadStatus status) {
        this.status = status;
        switch (status) {
            case FAILED -> this.statusProperty.set(I18nHelper.failed());
            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
            case DOWNLOAD_ING -> this.statusProperty.set(I18nHelper.downloadIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
    }

    private final File localFile;

    private final ShellSFTPFile remoteFile;

    private final ShellSFTPClient client;

    private final ShellSFTPDownloadManager manager;

    @Override
    public String getSrcPath() {
        return this.remoteFile.getName();
    }

    @Override
    public String getDestPath() {
        return this.localFile.getName();
    }

    public ShellSFTPDownloadTask(ShellSFTPDownloadManager manager, File localFile, ShellSFTPFile remoteFile, ShellSFTPClient client) {
        this.client = client;
        this.manager = manager;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
//        this.currentFileProperty().set(remoteFile.getPath());
//        // 执行线程
//        this.executeThread = ThreadUtil.start(() -> {
//            try {
//                sftp.setHolding(true);
//                this.updateStatus(ShellSFTPDownloadStatus.IN_PREPARATION);
//                this.addMonitorRecursive(localFile, remoteFile, sftp);
//                this.updateStatus(ShellSFTPDownloadStatus.DOWNLOAD_ING);
//                this.calcTotalSize();
//                this.updateTotal();
//                this.doDownload();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                sftp.setHolding(false);
//                this.updateTotal();
//                // 如果是非取消和失败，则设置为结束
//                if (!this.isCancelled() && !this.isFailed()) {
//                    this.updateStatus(ShellSFTPDownloadStatus.FINISHED);
//                }
//            }
//        });
    }

    /**
     * 执行下载
     */
    public void download() {
        try {
            this.updateStatus(ShellSFTPDownloadStatus.IN_PREPARATION);
            this.addMonitorRecursive(localFile, remoteFile);
            this.updateStatus(ShellSFTPDownloadStatus.DOWNLOAD_ING);
            this.calcTotalSize();
//            this.updateTotal();
            this.doDownload();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            this.updateTotal();
            // 如果是非取消和失败，则设置为结束
            if (!this.isCancelled() && !this.isFailed()) {
                this.updateStatus(ShellSFTPDownloadStatus.FINISHED);
            }
        }
    }

    /**
     * 递归添加监听器
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws SftpException 异常
     */
    protected void addMonitorRecursive(File localFile, ShellSFTPFile remoteFile) throws SftpException {
        // 已取消则跳过
        if (this.isCancelled()) {
            return;
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
        // 文件夹
        if (remoteFile.isDirectory()) {
            // 列举文件
            List<ShellSFTPFile> files = this.client.openSFTP().lsFileNormal(remoteFile.getFilePath());
            // 处理文件
            if (CollectionUtil.isNotEmpty(files)) {
                // 本地文件夹
                File localDir = new File(localFile.getPath(), remoteFile.getFileName());
                FileUtil.mkdir(localDir);
                // 添加文件
                for (ShellSFTPFile file : files) {
                    file.setParentPath(remoteFile.getFilePath());
                    if (file.isDirectory()) {
                        this.addMonitorRecursive(localDir, file);
                    } else {
                        File localFile1 = new File(localDir, file.getFileName());
                        this.addMonitorRecursive(localFile1, file);
                    }
                }
            }
        } else {// 文件
//            this.updateTotal();
            this.monitors.add(new ShellSFTPDownloadMonitor(localFile, remoteFile, this));
        }
    }

    /**
     * 执行下载
     */
    private void doDownload() {
        while (!this.isEmpty()) {
            ShellSFTPDownloadMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isCancelled()) {
                continue;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            ShellSFTPChannel sftp = this.client.newSFTP();
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
            } finally {
                IOUtil.close(sftp);
            }
            ThreadUtil.sleep(5);
        }
    }

//    @Override
//    protected void updateTotal() {
////        if (this.monitors.isEmpty() && !this.isInPreparation()) {
////            this.updateStatus(ShellSFTPDownloadStatus.FINISHED);
////        }
//        super.updateTotal();
//        this.manager.updateDownloading();
//    }

    @Override
    public void cancel() {
        super.cancel();
        this.manager.remove(this);
        this.updateStatus(ShellSFTPDownloadStatus.CANCELED);
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

    @Override
    public boolean isFailed() {
        return this.status == ShellSFTPDownloadStatus.FAILED;
    }

    @Override
    public boolean isFinished() {
        return this.status == ShellSFTPDownloadStatus.FINISHED;
    }

    @Override
    public boolean isCancelled() {
        return this.status == ShellSFTPDownloadStatus.CANCELED;
    }

    @Override
    public boolean isInPreparation() {
        return this.status == ShellSFTPDownloadStatus.IN_PREPARATION;
    }

    /**
     * 是否下载中
     *
     * @return 结果
     */
    public boolean isDownloading() {
        return this.status == ShellSFTPDownloadStatus.DOWNLOAD_ING;
    }

    @Override
    public void remove(ShellSFTPDownloadMonitor monitor) {
        super.remove(monitor);
        this.manager.remove(this);
    }

    @Override
    public void ended(ShellSFTPDownloadMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor, this);
        this.manager.remove(this);
        this.updateStatus(ShellSFTPDownloadStatus.FINISHED);
    }

    @Override
    public void failed(ShellSFTPDownloadMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor, exception);
        this.manager.remove(this);
        this.updateStatus(ShellSFTPDownloadStatus.FAILED);
    }

    @Override
    public void canceled(ShellSFTPDownloadMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor, this);
        this.manager.remove(this);
        this.updateStatus(ShellSFTPDownloadStatus.CANCELED);
    }

    @Override
    public void changed(ShellSFTPDownloadMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor, this);
    }
}
