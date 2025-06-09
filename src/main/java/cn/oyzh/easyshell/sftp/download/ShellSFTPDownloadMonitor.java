//package cn.oyzh.easyshell.sftp.download;
//
//import cn.oyzh.common.log.JulLog;
//import cn.oyzh.common.thread.ThreadUtil;
//import cn.oyzh.easyshell.sftp.ShellSFTPFile;
//import cn.oyzh.easyshell.sftp.ShellSFTPMonitor;
//import cn.oyzh.i18n.I18nHelper;
//
//import java.io.File;
//
///**
// * @author oyzh
// * @since 2025-03-06
// */
//public class ShellSFTPDownloadMonitor extends ShellSFTPMonitor {
//
//    private final File localFile;
//
//    private final ShellSFTPFile remoteFile;
//
//    private final ShellSFTPDownloadTask task;
//
//    public ShellSFTPDownloadMonitor(final File localFile, ShellSFTPFile remoteFile, ShellSFTPDownloadTask task) {
//        this.task = task;
//        this.localFile = localFile;
//        this.remoteFile = remoteFile;
//    }
//
//    @Override
//    public boolean count(long current) {
//        this.current += current;
//        this.task.changed(this);
//        return !this.cancelled;
//    }
//
//    @Override
//    public void end() {
//        this.ended = true;
//        long endTime = System.currentTimeMillis();
//        long duration = (endTime - this.startTime) / 1000;
//        if (duration == 0) {
//            duration = 1;
//        }
//        JulLog.info("file:{} download finished, cost:{}" + I18nHelper.seconds(), this.getRemoteFileName(), duration);
//        this.task.ended(this);
//    }
//
//    public String getRemoteFileName() {
//        return this.remoteFile.getName();
//    }
//
//    public String getRemoteFilePath() {
//        return this.remoteFile.getFilePath();
//    }
//
//    public String getLocalFileName() {
//        return this.localFile.getName();
//    }
//
//    public String getLocalFilePath() {
//        return this.localFile.getPath();
//    }
//
//    @Override
//    public long getLocalFileLength() {
//        return this.localFile.length();
//    }
//
//    @Override
//    public String getFilePath() {
//        return this.remoteFile.getFilePath();
//    }
//
//    public long getRemoteLength() {
//        return this.remoteFile.size();
//    }
//
//    @Override
//    public synchronized void cancel() {
//        this.cancelled = true;
//        JulLog.warn("file:{} download canceled, download:{} total:{}", this.getRemoteFileName(), this.current, this.total);
//        ThreadUtil.start(() -> this.task.canceled(this), 50);
//    }
//
//    @Override
//    public long getTotal() {
//        return this.getRemoteLength();
//    }
//}
