package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadManager {

    //    private final Queue<SftpUploadMonitor> monitors = new ArrayDeque<>();
    private final List<SftpUploadTask> tasks = new CopyOnWriteArrayList<>();

//    @Setter
//    private Consumer<SftpUploadEnded> uploadEndedCallback;
//
//    @Setter
//    private Consumer<SftpUploadFailed> uploadFailedCallback;
//
//    @Setter
//    private Consumer<SftpUploadChanged> uploadChangedCallback;
//
//    @Setter
//    private Consumer<SftpUploadCanceled> uploadCanceledCallback;
//
//    @Setter
//    private Consumer<SftpUploadInPreparation> uploadInPreparationCallback;

//    /**
//     * 执行线程
//     */
//    private Thread executeThread;

    public void createMonitor(File localFile, String remoteFile, ShellSftp sftp) {
//        this.executeThread = ThreadUtil.start(() -> {
//            try {
//                sftp.setHolding(true);
//                this.setUploading(true);
//                this.uploadInPreparation();
//                this.addMonitorRecursive(localFile, remoteFile, sftp);
//                this.doUpload();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                sftp.setHolding(false);
//                this.setUploading(false);
//            }
//        });
        this.tasks.add(new SftpUploadTask(this, localFile, remoteFile, sftp));
    }

//    protected void addMonitorRecursive(File localFile, String remoteFile, SSHSftp sftp) throws SftpException {
//        this.uploadInPreparation(localFile.getPath());
//        // 文件夹
//        if (localFile.isDirectory()) {
//            // 列举文件
//            File[] files = localFile.listFiles();
//            // 处理文件
//            if (ArrayUtil.isNotEmpty(files)) {
//                // 远程文件夹
//                String remoteDir = SftpUtil.concat(remoteFile, localFile.getName());
//                // 递归创建文件夹
//                sftp.mkdirRecursive(remoteDir);
//                // 添加文件
//                for (File file : files) {
//                    if (file.isDirectory()) {
//                        this.addMonitorRecursive(file, remoteDir, sftp);
//                    } else {
//                        String remoteFile1 = SftpUtil.concat(remoteDir, file.getName());
//                        this.addMonitorRecursive(file, remoteFile1, sftp);
//                    }
//                }
//            }
//        } else {// 文件
//            this.monitors.add(new SftpUploadMonitor(localFile, remoteFile, this, sftp));
//        }
//    }

//    public SftpUploadMonitor takeMonitor() {
//        return this.monitors.peek();
//    }

//    public void uploadEnded(SftpUploadMonitor monitor) {
//        this.monitors.remove(monitor);
//        if (this.uploadEndedCallback != null) {
//            SftpUploadEnded ended = new SftpUploadEnded();
//            ended.setFileCount(this.size());
//            ended.setRemoteFile(monitor.getRemoteFile());
//            ended.setLocalFileName(monitor.getLocalFileName());
//            this.uploadEndedCallback.accept(ended);
//        }
//    }

//    public void uploadFailed(SftpUploadMonitor monitor, Exception exception) {
//        this.monitors.remove(monitor);
//        if (this.uploadFailedCallback != null) {
//            SftpUploadFailed changed = new SftpUploadFailed();
//            changed.setException(exception);
//            changed.setFileCount(this.size());
//            changed.setRemoteFile(monitor.getRemoteFile());
//            changed.setLocalFileName(monitor.getLocalFileName());
//            this.uploadFailedCallback.accept(changed);
//        }
//    }

//    public void uploadCanceled(SftpUploadMonitor monitor) {
//        this.monitors.remove(monitor);
//        if (this.uploadCanceledCallback != null) {
//            SftpUploadCanceled ended = new SftpUploadCanceled();
//            ended.setFileCount(this.size());
//            ended.setRemoteFile(monitor.getRemoteFile());
//            ended.setLocalFileName(monitor.getLocalFileName());
//            this.uploadCanceledCallback.accept(ended);
//        }
//    }

//    public void uploadChanged(SftpUploadMonitor monitor) {
//        if (this.uploadChangedCallback != null) {
//            SftpUploadChanged changed = new SftpUploadChanged();
//            changed.setFileCount(this.size());
//            changed.setFileSize(this.count());
//            changed.setTotal(monitor.getTotal());
//            changed.setCurrent(monitor.getCurrent());
//            changed.setRemoteFile(monitor.getRemoteFile());
//            changed.setLocalFileName(monitor.getLocalFileName());
//            this.uploadChangedCallback.accept(changed);
//        }
//    }

//    public void uploadInPreparation() {
//        this.uploadInPreparation(null);
//    }

//    public void uploadInPreparation(String fileName) {
//        if (this.uploadInPreparationCallback != null) {
//            SftpUploadInPreparation inPreparation = new SftpUploadInPreparation();
//            inPreparation.setFileName(fileName);
//            this.uploadInPreparationCallback.accept(inPreparation);
//        }
//    }

//    public boolean isEmpty() {
//        return this.monitors.isEmpty();
//    }
//
//    public void removeMonitor(SftpUploadMonitor monitor) {
//        this.monitors.remove(monitor);
//    }
//
//    public int size() {
//        return this.monitors.size();
//    }
//
//    public long count() {
//        long cnt = 0;
//        for (SftpUploadMonitor monitor : this.monitors) {
//            cnt += monitor.getLocalFileLength();
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
//        for (SftpUploadMonitor monitor : this.monitors) {
//            try {
//                monitor.cancel();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        ThreadUtil.start(() -> this.monitors.clear(), 500);
        for (SftpUploadTask task : this.tasks) {
            task.cancel();
        }
        ThreadUtil.start(this.tasks::clear, 500);
    }

//    private void doUpload() {
//        while (!this.isEmpty()) {
//            SftpUploadMonitor monitor = this.takeMonitor();
//            if (monitor == null) {
//                break;
//            }
//            if (monitor.isFinished()) {
//                ThreadUtil.sleep(5);
//                continue;
//            }
//            SSHSftp sftp = monitor.getSftp();
//            try {
//                sftp.put(monitor.getLocalFilePath(), monitor.getRemoteFile(), monitor, ChannelSftp.OVERWRITE);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JulLog.warn("file:{} upload failed", monitor.getLocalFileName(), ex);
//                this.uploadFailed(monitor, ex);
//            }
//            ThreadUtil.sleep(5);
//        }
//    }

    private final BooleanProperty uploadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty uploadingProperty() {
        return this.uploadingProperty;
    }

    public void updateUploading() {
        for (SftpUploadTask task : this.tasks) {
            if (task.isUploading()) {
                this.uploadingProperty.set(true);
                return;
            }
        }
        this.uploadingProperty.set(false);
    }

//    public void setUploading(boolean uploading) {
//        this.uploadingProperty.set(uploading);
//    }
//
//    public boolean isUploading() {
//        return this.uploadingProperty.get();
//    }

    public List<SftpUploadTask> getTasks() {
        return tasks;
    }

    public void remove(SftpUploadTask task) {
        task.cancel();
        this.tasks.remove(task);
    }

    public void cancel(SftpUploadTask task) {
        task.cancel();
    }
}
