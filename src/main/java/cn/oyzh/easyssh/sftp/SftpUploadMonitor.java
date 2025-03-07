package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
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

    private final File file;

    @Getter
    private final String dest;

    private final SftpUploadManager manager;

    @Getter
    private transient boolean ended;

    @Getter
    private transient boolean cancelled;

    private long startTime;

    @Getter
    private final SSHSftp sftp;

    public SftpUploadMonitor(final File file, String dest, SftpUploadManager manager, SSHSftp sftp) {
        this.file = file;
        this.dest = dest;
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
            JulLog.warn("file:{} upload cancelled, upload:{} total:{}", this.getFilePath(), this.current, this.total);
            this.manager.uploadCanceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} upload finished, cost:{}" + I18nHelper.seconds(), this.getFilePath(), duration);
            this.manager.uploadEnded(this);
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
