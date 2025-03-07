package cn.oyzh.easyssh.sftp.download;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
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

    private final File file;

    @Getter
    private final String remote;

    private final SftpDownloadManager manager;

    @Getter
    private transient boolean ended;

    @Getter
    private transient boolean cancelled;

    private long startTime;

    @Getter
    private final SSHSftp sftp;

    public SftpDownloadMonitor(final File file, String remote, SftpDownloadManager manager, SSHSftp sftp) {
        this.file = file;
        this.sftp = sftp;
        this.remote = remote;
        this.manager = manager;
    }

    @Override
    public void init(int current, String s, String s1, long total) {
        this.total = total;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean count(long current) {
        this.current += current;
        this.manager.downloadChanged(this);
        return !this.cancelled;
    }

    @Override
    public void end() {
        this.ended = true;
        if (this.cancelled) {
            JulLog.warn("file:{} download cancelled, download:{} total:{}", this.getFilePath(), this.current, this.total);
            this.manager.downloadCanceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} download finished, cost:{}" + I18nHelper.seconds(), this.getFilePath(), duration);
            this.manager.downloadEnded(this);
        }
    }

    public String getFileName() {
        return this.file.getName();
    }

    public String getFilePath() {
        return this.file.getPath();
    }

    public long getFileLength() {
        return this.file.length();
    }

    public synchronized void cancel() {
        if (this.ended) {
            ThreadUtil.start(() -> this.manager.removeMonitor(this), 50);
        } else {
            this.cancelled = true;
            ThreadUtil.start(this::end, 50);
        }
    }

    public synchronized boolean isFinished() {
        return this.ended || this.cancelled;
    }
}
