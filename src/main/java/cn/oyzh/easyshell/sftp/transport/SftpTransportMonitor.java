package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.sftp.ShellSftpMonitor;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpTransportMonitor extends ShellSftpMonitor {

    private final ShellSftpFile localFile;

    private final String remoteFile;

    public String getRemoteFile() {
        return remoteFile;
    }

    private final SftpTransportTask task;

    public SftpTransportMonitor(final ShellSftpFile localFile, String remoteFile, SftpTransportTask task) {
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
        JulLog.info("file:{} transport finished, cost:{}" + I18nHelper.seconds(), this.getLocalFilePath(), duration);
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
        this.cancelled = true;
        JulLog.warn("file:{} transport canceled, transport:{} total:{}", this.getLocalFilePath(), this.current, this.total);
        ThreadUtil.start(() -> this.task.canceled(this), 50);
    }

    @Override
    public long getTotal() {
        return this.getLocalFileLength();
    }

    @Override
    public String getFilePath() {
        return this.localFile.getFilePath();
    }
}
