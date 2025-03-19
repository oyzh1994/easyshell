package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpProgressMonitor;
import lombok.Getter;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDownloadMonitor implements SftpProgressMonitor {

    @Getter
    private long total;

    @Getter
    private long current;

    private final File localFile;

    @Getter
    private final SftpFile remoteFile;

    private final SftpDownloadTask task;
//    private final SftpDownloadManager manager;

    @Getter
    private transient boolean ended;

    @Getter
    private transient boolean cancelled;

    private long startTime;

    @Getter
    private final ShellSftp sftp;

    public SftpDownloadMonitor(final File localFile, SftpFile remoteFile, SftpDownloadTask task, ShellSftp sftp) {
        this.sftp = sftp;
        this.task = task;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
    }

    @Override
    public void init(int current, String s, String s1, long total) {
        this.total = total;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean count(long current) {
        this.current += current;
        this.task.downloadChanged(this);
        return !this.cancelled;
    }

    @Override
    public void end() {
        this.ended = true;
        if (this.cancelled) {
            JulLog.warn("file:{} download cancelled, download:{} total:{}", this.getRemoteFileName(), this.current, this.total);
            this.task.downloadCanceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} download finished, cost:{}" + I18nHelper.seconds(), this.getRemoteFileName(), duration);
            this.task.downloadEnded(this);
        }
    }

    public String getRemoteFileName() {
        return this.remoteFile.getName();
    }

    public String getRemoteFilePath() {
        return this.remoteFile.getFilePath();
    }

    public String getLocalFileName() {
        return this.localFile.getName();
    }

    public String getLocalFilePath() {
        return this.localFile.getPath();
    }

    public long getRemoteLength() {
        return this.remoteFile.size();
    }

    public synchronized void cancel() {
        if (this.ended) {
            ThreadUtil.start(() -> this.task.removeMonitor(this), 50);
        } else {
            this.cancelled = true;
            ThreadUtil.start(this::end, 50);
        }
    }

    public synchronized boolean isFinished() {
        return this.ended || this.cancelled;
    }
}
