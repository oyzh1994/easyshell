package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPMonitor;
import cn.oyzh.i18n.I18nHelper;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSFTPUploadMonitor extends ShellSFTPMonitor {

    private final File localFile;

    private final String remoteFile;

    public String getRemoteFile() {
        return remoteFile;
    }

    private final ShellSFTPUploadTask task;

    public ShellSFTPUploadMonitor(final File localFile, String remoteFile, ShellSFTPUploadTask task) {
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.task = task;
    }

    @Override
    public boolean count(long current) {
        this.current += current;
        this.task.changed(this);
        return !this.cancelled;
    }

    @Override
    public void end() {
        this.ended = true;
        long endTime = System.currentTimeMillis();
        long duration = (endTime - this.startTime) / 1000;
        if (duration == 0) {
            duration = 1;
        }
        JulLog.info("file:{} upload finished, cost:{}" + I18nHelper.seconds(), this.getLocalFilePath(), duration);
        this.task.ended(this);
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

    @Override
    public synchronized void cancel() {
        JulLog.warn("file:{} upload canceled, upload:{} total:{}", this.getLocalFilePath(), this.current, this.total);
        this.cancelled = true;
        ThreadUtil.start(() -> this.task.canceled(this), 50);
    }

    @Override
    public long getTotal() {
        return this.getLocalFileLength();
    }

    @Override
    public String getFilePath() {
        return this.getLocalFilePath();
    }
}
