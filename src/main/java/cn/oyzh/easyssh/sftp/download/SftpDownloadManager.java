package cn.oyzh.easyssh.sftp.download;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
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
public class SftpDownloadManager {

    private Queue<SftpDownloadMonitor> monitors = new ArrayDeque<>();

    @Setter
    private Consumer<SftpDownloadEnded> downloadEndedCallback;

    @Setter
    private Consumer<SftpDownloadChanged> downloadChangedCallback;

    @Setter
    private Consumer<SftpDownloadCanceled> downloadCanceledCallback;

    public void createMonitor(File file, String remote, SSHSftp sftp) {
        if (this.monitors == null) {
            this.monitors = new ArrayDeque<>();
        }
        this.monitors.add(new SftpDownloadMonitor(file, remote, this, sftp));
        this.doDownload();
    }

    public SftpDownloadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void downloadEnded(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.downloadEndedCallback != null) {
            SftpDownloadEnded ended = new SftpDownloadEnded();
            ended.setFileCount(this.size());
            ended.setRemote(monitor.getRemote());
            ended.setFileName(monitor.getFileName());
            this.downloadEndedCallback.accept(ended);
        }
    }

    public void downloadCanceled(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.downloadCanceledCallback != null) {
            SftpDownloadCanceled ended = new SftpDownloadCanceled();
            ended.setFileCount(this.size());
            ended.setRemote(monitor.getRemote());
            ended.setFileName(monitor.getFileName());
            this.downloadCanceledCallback.accept(ended);
        }
    }

    public void downloadChanged(SftpDownloadMonitor monitor) {
        if (this.downloadChangedCallback != null) {
            SftpDownloadChanged changed = new SftpDownloadChanged();
            changed.setFileCount(this.size());
            changed.setFileSize(this.count());
            changed.setTotal(monitor.getTotal());
            changed.setRemote(monitor.getRemote());
            changed.setCurrent(monitor.getCurrent());
            changed.setFileName(monitor.getFileName());
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
            cnt += monitor.getFileLength();
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
                        ThreadUtil.sleep(100);
                        continue;
                    }
                    SSHSftp sftp = monitor.getSftp();
                    sftp.setUsing(true);
                    try {
                        sftp.get(monitor.getRemote(), monitor.getFilePath(), monitor, ChannelSftp.OVERWRITE);
                    } catch (SftpException ex) {
                        ex.printStackTrace();
                    } finally {
                        sftp.setUsing(false);
                    }
                    ThreadUtil.sleep(100);
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
