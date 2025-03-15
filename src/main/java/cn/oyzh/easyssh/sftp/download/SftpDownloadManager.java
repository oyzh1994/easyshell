package cn.oyzh.easyssh.sftp.download;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.SftpUtil;
import cn.oyzh.easyssh.sftp.upload.SftpUploadTask;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Setter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDownloadManager {

    //    private final Queue<SftpDownloadMonitor> monitors = new ArrayDeque<>();
    private final List<SftpDownloadTask> tasks = new CopyOnWriteArrayList<>();

//    @Setter
//    private Consumer<SftpDownloadEnded> downloadEndedCallback;
//
//    @Setter
//    private Consumer<SftpDownloadFailed> downloadFailedCallback;
//
//    @Setter
//    private Consumer<SftpDownloadChanged> downloadChangedCallback;
//
//    @Setter
//    private Consumer<SftpDownloadCanceled> downloadCanceledCallback;
//
//    @Setter
//    private Consumer<SftpDownloadInPreparation> downloadInPreparationCallback;

//    /**
//     * 执行线程
//     */
//    private Thread executeThread;

    public void createMonitor(File localFile, SftpFile remoteFile, SSHSftp sftp) {
//        // 执行线程
//        this.executeThread = ThreadUtil.start(() -> {
//            try {
//                sftp.setHolding(true);
//                this.setDownloading(true);
//                this.downloadInPreparation();
//                this.addMonitorRecursive(localFile, remoteFile, sftp);
//                this.doDownload();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                sftp.setHolding(false);
//                this.setDownloading(false);
//            }
//        });
        this.tasks.add(new SftpDownloadTask(this, localFile, remoteFile, sftp));
    }

//    protected void addMonitorRecursive(File localFile, SftpFile remoteFile, SSHSftp sftp) throws SftpException {
//        String filePath = SftpUtil.concat(remoteFile.getFilePath(), remoteFile.getFileName());
//        this.downloadInPreparation(filePath);
//        // 文件夹
//        if (remoteFile.isDir()) {
//            // 列举文件
//            List<SftpFile> files = sftp.lsFileNormal(remoteFile.getFilePath());
//            // 处理文件
//            if (CollectionUtil.isNotEmpty(files)) {
//                // 本地文件夹
//                File localDir = new File(localFile.getPath(), remoteFile.getFileName());
//                FileUtil.mkdir(localDir);
//                // 添加文件
//                for (SftpFile file : files) {
//                    file.setParentPath(remoteFile.getFilePath());
//                    if (file.isDir()) {
//                        this.addMonitorRecursive(localDir, file, sftp);
//                    } else {
//                        File localFile1 = new File(localDir, file.getFileName());
//                        this.addMonitorRecursive(localFile1, file, sftp);
//                    }
//                }
//            }
//        } else {// 文件
//            this.monitors.add(new SftpDownloadMonitor(localFile, remoteFile, this, sftp));
//        }
//    }

//    public SftpDownloadMonitor takeMonitor() {
//        return this.monitors.peek();
//    }
//
//    public void downloadEnded(SftpDownloadMonitor monitor) {
//        this.monitors.remove(monitor);
//        if (this.downloadEndedCallback != null) {
//            SftpDownloadEnded ended = new SftpDownloadEnded();
//            ended.setFileCount(this.size());
//            ended.setRemoteFile(monitor.getRemoteFilePath());
//            ended.setLocalFileName(monitor.getLocalFilePath());
//            this.downloadEndedCallback.accept(ended);
//        }
//    }
//
//    public void downloadFailed(SftpDownloadMonitor monitor, Exception exception) {
//        this.monitors.remove(monitor);
//        if (this.downloadEndedCallback != null) {
//            SftpDownloadFailed failed = new SftpDownloadFailed();
//            failed.setFileCount(this.size());
//            failed.setException(exception);
//            failed.setRemoteFile(monitor.getRemoteFilePath());
//            failed.setLocalFileName(monitor.getLocalFilePath());
//            this.downloadFailedCallback.accept(failed);
//        }
//    }
//
//    public void downloadCanceled(SftpDownloadMonitor monitor) {
//        this.monitors.remove(monitor);
//        if (this.downloadCanceledCallback != null) {
//            SftpDownloadCanceled ended = new SftpDownloadCanceled();
//            ended.setFileCount(this.size());
//            ended.setRemoteFile(monitor.getRemoteFilePath());
//            ended.setLocalFileName(monitor.getLocalFilePath());
//            this.downloadCanceledCallback.accept(ended);
//        }
//    }
//
//    public void downloadChanged(SftpDownloadMonitor monitor) {
//        if (this.downloadChangedCallback != null) {
//            SftpDownloadChanged changed = new SftpDownloadChanged();
//            changed.setFileCount(this.size());
//            changed.setFileSize(this.count());
//            changed.setTotal(monitor.getTotal());
//            changed.setCurrent(monitor.getCurrent());
//            changed.setRemoteFile(monitor.getRemoteFilePath());
//            changed.setLocalFileName(monitor.getLocalFilePath());
//            this.downloadChangedCallback.accept(changed);
//        }
//    }
//
//    public void downloadInPreparation() {
//        this.downloadInPreparation(null);
//    }
//
//    public void downloadInPreparation(String fileName) {
//        if (this.downloadInPreparationCallback != null) {
//            SftpDownloadInPreparation inPreparation = new SftpDownloadInPreparation();
//            inPreparation.setFileName(fileName);
//            this.downloadInPreparationCallback.accept(inPreparation);
//        }
//    }
//
//    public boolean isEmpty() {
//        return this.monitors.isEmpty();
//    }
//
//    public void removeMonitor(SftpDownloadMonitor monitor) {
//        this.monitors.remove(monitor);
//    }

//    public int size() {
//        return this.monitors.size();
//    }
//
//    public long count() {
//        long cnt = 0;
//        for (SftpDownloadMonitor monitor : this.monitors) {
//            cnt += monitor.getRemoteLength();
//        }
//        return cnt;
//    }

    /**
     * 取消
     */
    public void cancel() {
//        // 停止线程
//        ThreadUtil.interrupt(this.executeThread);
//        // 取消业务
//        for (SftpDownloadMonitor monitor : this.monitors) {
//            try {
//                monitor.cancel();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        ThreadUtil.start(() -> this.monitors.clear(), 500);
        // 取消业务
        for (SftpDownloadTask task : this.tasks) {
            try {
                task.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ThreadUtil.start(this.tasks::clear, 500);
    }

//    private void doDownload() {
//        while (!this.isEmpty()) {
//            SftpDownloadMonitor monitor = this.takeMonitor();
//            if (monitor == null) {
//                break;
//            }
//            if (monitor.isFinished()) {
//                ThreadUtil.sleep(5);
//                continue;
//            }
//            SSHSftp sftp = monitor.getSftp();
//            try {
//                sftp.get(monitor.getRemoteFilePath(), monitor.getLocalFilePath(), monitor, ChannelSftp.OVERWRITE);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JulLog.warn("file:{} download failed", monitor.getRemoteFileName(), ex);
//                this.downloadFailed(monitor, ex);
//            }
//            ThreadUtil.sleep(5);
//        }
//    }

    private final BooleanProperty downloadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty downloadingProperty() {
        return this.downloadingProperty;
    }

    public void updateDownloading() {
        for (SftpDownloadTask task : this.tasks) {
            if (task.isDownloading()) {
                this.downloadingProperty.set(true);
                return;
            }
        }
        this.downloadingProperty.set(false);
    }
//
//    public void setDownloading(boolean downloading) {
//        this.downloadingProperty.set(downloading);
//    }
//
//    public boolean isDownloading() {
//        return this.downloadingProperty.get();
//    }

    public List<SftpDownloadTask> getTasks() {
        return tasks;
    }

    public void remove(SftpDownloadTask task) {
        task.cancel();
        this.tasks.remove(task);
    }

    public void cancel(SftpDownloadTask task) {
        task.cancel();
    }
}
