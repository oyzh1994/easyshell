package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpMonitor;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpTransportMonitor extends SftpMonitor {

    private final SftpFile localFile;

    private final String remoteFile;

    public String getRemoteFile() {
        return remoteFile;
    }

    private final SftpTransportTask task;

    private final ShellSftp localSftp;

    public ShellSftp getLocalSftp() {
        return localSftp;
    }

    private final ShellSftp remoteSftp;

    public ShellSftp getRemoteSftp() {
        return remoteSftp;
    }

    public SftpTransportMonitor(final SftpFile localFile, String remoteFile, SftpTransportTask task, ShellSftp localSftp, ShellSftp remoteSftp) {
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.localSftp = localSftp;
        this.remoteSftp = remoteSftp;
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
            JulLog.warn("file:{} transport cancelled, upload:{} total:{}", this.getLocalFilePath(), this.current, this.total);
            this.task.canceled(this);
        } else {
            long endTime = System.currentTimeMillis();
            long duration = (endTime - this.startTime) / 1000;
            if (duration == 0) {
                duration = 1;
            }
            JulLog.info("file:{} transport finished, cost:{}" + I18nHelper.seconds(), this.getLocalFilePath(), duration);
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
}
