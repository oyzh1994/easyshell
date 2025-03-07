package cn.oyzh.easyssh.sftp.upload;

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
public class SftpUploadMonitor implements SftpProgressMonitor {

    @Getter
    private long total;

    @Getter
    private long current;

    private final File localFile;

    @Getter
    private final String remoteFile;

    private final SftpUploadManager manager;

    @Getter
    private transient boolean ended;

    @Getter
    private transient boolean cancelled;

    private long startTime;

    @Getter
    private final SSHSftp sftp;

    public SftpUploadMonitor(final File localFile, String remoteFile, SftpUploadManager manager, SSHSftp sftp) {
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.sftp = sftp;
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
        this.manager.uploadChanged(this);
        return !this.cancelled;
    }

    @Override
    public void end() {
        this.ended = true;
        if (this.cancelled) {
            JulLog.warn("file:{} upload cancelled, upload:{} total:{}", this.getLocalFilePath(), this.current, this.total);
            this.manager.uploadCanceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} upload finished, cost:{}" + I18nHelper.seconds(), this.getLocalFilePath(), duration);
            this.manager.uploadEnded(this);
        }
    }

    public String getLocalFileName() {
        return this.localFile.getName();
    }

    public String getLocalFilePath() {
        return this.localFile.getPath();
    }

    public long getLocalFileLength() {
        return this.localFile.length();
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
