package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.SftpMonitor;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadMonitor extends SftpMonitor {

    private final File localFile;

    private final String remoteFile;

    public String getRemoteFile() {
        return remoteFile;
    }

    private final SftpUploadTask task;

    private final ShellSftp sftp;

    public ShellSftp getSftp() {
        return sftp;
    }

    public SftpUploadMonitor(final File localFile, String remoteFile, SftpUploadTask task, ShellSftp sftp) {
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.sftp = sftp;
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
        if (this.cancelled) {
            JulLog.warn("file:{} upload cancelled, upload:{} total:{}", this.getLocalFilePath(), this.current, this.total);
            this.task.canceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} upload finished, cost:{}" + I18nHelper.seconds(), this.getLocalFilePath(), duration);
            this.task.ended(this);
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

    @Override
    public synchronized void cancel() {
        if (this.ended) {
            ThreadUtil.start(() -> this.task.removeMonitor(this), 50);
        } else {
            this.cancelled = true;
            ThreadUtil.start(this::end, 50);
        }
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
