package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpTransportMonitor implements SftpProgressMonitor {

    private long total;

    private long current;

    private final File localFile;

    private final String remoteFile;

    public String getRemoteFile() {
        return remoteFile;
    }

    private final SftpTransportTask task;

    private transient boolean ended;

    private transient boolean cancelled;

    private long startTime;

    public long getTotal() {
        return total;
    }

    public ShellSftp getSftp() {
        return sftp;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    private final ShellSftp sftp;

    public SftpTransportMonitor(final File localFile, String remoteFile, SftpTransportTask task, ShellSftp sftp) {
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.sftp = sftp;
        this.task = task;
    }

    @Override
    public void init(int current, String s, String s1, long total) {
        this.total = total;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean count(long current) {
        this.current += current;
//        this.manager.uploadChanged(this);
        this.task.uploadChanged(this);
        return !this.cancelled;
    }

    @Override
    public void end() {
        this.ended = true;
        if (this.cancelled) {
            JulLog.warn("file:{} upload cancelled, upload:{} total:{}", this.getLocalFilePath(), this.current, this.total);
            this.task.uploadCanceled(this);
//            this.manager.uploadCanceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} upload finished, cost:{}" + I18nHelper.seconds(), this.getLocalFilePath(), duration);
            this.task.uploadEnded(this);
//            this.manager.uploadEnded(this);
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
            ThreadUtil.start(() -> this.task.removeMonitor(this), 50);
//            ThreadUtil.start(() -> this.manager.removeMonitor(this), 50);
        } else {
            this.cancelled = true;
            ThreadUtil.start(this::end, 50);
        }
    }

    public synchronized boolean isFinished() {
        return this.ended || this.cancelled;
    }


}
