package cn.oyzh.easyssh.sftp.download;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.upload.SftpUploadFailed;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.Setter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDownloadManager {

    private Queue<SftpDownloadMonitor> monitors = new ArrayDeque<>();

    @Setter
    private Consumer<SftpDownloadEnded> downloadEndedCallback;

    @Setter
    private Consumer<SftpDownloadFailed> downloadFailedCallback;

    @Setter
    private Consumer<SftpDownloadChanged> downloadChangedCallback;

    @Setter
    private Consumer<SftpDownloadCanceled> downloadCanceledCallback;

    public void createMonitor(File localFile, SftpFile remoteFile, SSHSftp sftp) throws SftpException {
        if (this.monitors == null) {
            this.monitors = new ArrayDeque<>();
        }
//        this.monitors.add(new SftpDownloadMonitor(localFile, remoteFile, this, sftp));
        this.addMonitorRecursive(localFile, remoteFile, sftp);
        this.doDownload();
    }

    protected void addMonitorRecursive(File localFile, SftpFile remoteFile, SSHSftp sftp) throws SftpException {
        // 文件夹
        if (remoteFile.isDir()) {
            // 列举文件
            List<SftpFile> files = sftp.lsFileNormal(remoteFile.getFilePath());
            if (CollectionUtil.isNotEmpty(files)) {
                // 本地文件夹
                File localDir = new File(localFile.getPath(), remoteFile.getFileName());
                FileUtil.mkdir(localDir);
                for (SftpFile file : files) {
                    file.setParentPath(remoteFile.getFilePath());
                    // 本地文件
                    File localFile1 = new File(localDir, file.getFileName());
                    this.addMonitorRecursive(localFile1, file, sftp);
                }
            }
        } else {// 文件
            this.monitors.add(new SftpDownloadMonitor(localFile, remoteFile, this, sftp));
        }
    }

    public SftpDownloadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void downloadEnded(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.downloadEndedCallback != null) {
            SftpDownloadEnded ended = new SftpDownloadEnded();
            ended.setFileCount(this.size());
            ended.setRemoteFile(monitor.getRemoteFileName());
            ended.setLocalFileName(monitor.getLocalFileName());
            this.downloadEndedCallback.accept(ended);
        }
    }

    public void downloadFailed(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.downloadEndedCallback != null) {
            SftpDownloadFailed failed = new SftpDownloadFailed();
            failed.setFileCount(this.size());
            failed.setRemoteFile(monitor.getRemoteFileName());
            failed.setLocalFileName(monitor.getLocalFileName());
            this.downloadFailedCallback.accept(failed);
        }
    }

    public void downloadCanceled(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.downloadCanceledCallback != null) {
            SftpDownloadCanceled ended = new SftpDownloadCanceled();
            ended.setFileCount(this.size());
            ended.setRemoteFile(monitor.getRemoteFileName());
            ended.setLocalFileName(monitor.getLocalFileName());
            this.downloadCanceledCallback.accept(ended);
        }
    }

    public void downloadChanged(SftpDownloadMonitor monitor) {
        if (this.downloadChangedCallback != null) {
            SftpDownloadChanged changed = new SftpDownloadChanged();
            changed.setFileCount(this.size());
            changed.setFileSize(this.count());
            changed.setTotal(monitor.getTotal());
            changed.setCurrent(monitor.getCurrent());
            changed.setRemoteFile(monitor.getRemoteFileName());
            changed.setLocalFileName(monitor.getLocalFileName());
            this.downloadChangedCallback.accept(changed);
        }
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    public void removeMonitor(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
    }

    public int size() {
        return this.monitors.size();
    }

    public long count() {
        long cnt = 0;
        for (SftpDownloadMonitor monitor : this.monitors) {
            cnt += monitor.getRemoteLength();
        }
        return cnt;
    }

    public void cancel() {
        for (SftpDownloadMonitor monitor : this.monitors) {
            monitor.cancel();
        }
    }

    private void doDownload() {
        if (this.isDownloading()) {
            return;
        }
        this.setDownloading(true);
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    SftpDownloadMonitor monitor = this.takeMonitor();
                    if (monitor == null) {
                        break;
                    }
                    if (monitor.isFinished()) {
                        ThreadUtil.sleep(10);
                        continue;
                    }
                    SSHSftp sftp = monitor.getSftp();
                    try {
                        sftp.get(monitor.getRemoteFilePath(), monitor.getLocalFilePath(), monitor, ChannelSftp.OVERWRITE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JulLog.warn("file:{} download failed", monitor.getRemoteFileName(), ex);
                        this.downloadFailed(monitor);
                    }
                    ThreadUtil.sleep(10);
                }
            } finally {
                this.setDownloading(false);
            }
        });
    }

    private final AtomicBoolean downloading = new AtomicBoolean(false);

    public void setDownloading(boolean downloading) {
        this.downloading.set(downloading);
    }

    public boolean isDownloading() {
        return this.downloading.get();
    }
}
