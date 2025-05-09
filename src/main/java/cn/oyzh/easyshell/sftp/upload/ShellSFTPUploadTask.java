package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.file.ShellFileStatus;
import cn.oyzh.easyshell.sftp.ShellSFTPTask;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpException;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellSFTPUploadTask extends ShellSFTPTask<ShellSFTPUploadMonitor> {

    @Override
    public void updateStatus(ShellFileStatus status) {
        super.updateStatus(status);;
        if (status == ShellFileStatus.EXECUTE_ING) {
            this.statusProperty.set(I18nHelper.uploadIng());
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
    }

    private final File localFile;

    private final String remoteFile;

    private final ShellSFTPClient client;

    private final ShellSFTPUploadManager manager;

    @Override
    public String getSrcPath() {
        return this.localFile.getName();
    }

    @Override
    public String getDestPath() {
        return this.remoteFile;
    }

    public ShellSFTPUploadTask(ShellSFTPUploadManager manager, File localFile, String remoteFile, ShellSFTPClient client) {
        this.client = client;
        this.manager = manager;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
//        this.destPath = remoteFile;
//        this.srcPath = localFile.getName();
//        this.currentFileProperty().set(localFile.getPath());
//        this.executeThread = ThreadUtil.start(() -> {
//            try {
//                this.updateStatus(ShellSFTPStatus.IN_PREPARATION);
//                this.addMonitorRecursive(localFile, remoteFile, client);
//                this.updateStatus(ShellSFTPStatus.UPLOAD_ING);
//                this.calcTotalSize();
//                this.updateTotal();
//                this.doUpload();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                this.updateTotal();
//                // 如果是非取消和失败，则设置为结束
//                if (!this.isCancelled() && !this.isFailed()) {
//                    this.updateStatus(ShellSFTPStatus.FINISHED);
//                }
//            }
//        });
    }

    /**
     * 执行上传
     */
    public void upload() {
        try {
            this.updateStatus(ShellFileStatus.IN_PREPARATION);
            this.addMonitorRecursive(localFile, remoteFile);
            this.updateStatus(ShellFileStatus.EXECUTE_ING);
            this.calcTotalSize();
//            this.updateTotal();
            this.doUpload();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            this.updateTotal();
            // 如果是非取消和失败，则设置为结束
            if (!this.isCancelled() && !this.isFailed()) {
                this.updateStatus(ShellFileStatus.FINISHED);
            }
        }
    }

    /**
     * 递归添加监听器
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws SftpException 异常
     */
    protected void addMonitorRecursive(File localFile, String remoteFile) throws Exception {
        // 已取消则跳过
        if (this.isCancelled()) {
            return;
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
        // 文件夹
        if (localFile.isDirectory()) {
            // 列举文件
            File[] files = localFile.listFiles();
            // 处理文件
            if (ArrayUtil.isNotEmpty(files)) {
                // 远程文件夹
                String remoteDir = ShellFileUtil.concat(remoteFile, localFile.getName());
                // 递归创建文件夹
                this.client.mkdirRecursive(remoteDir);
                // 添加文件
                for (File file : files) {
                    if (file.isDirectory()) {
                        this.addMonitorRecursive(file, remoteDir);
                    } else {
                        String remoteFile1 = ShellFileUtil.concat(remoteDir, file.getName());
                        this.addMonitorRecursive(file, remoteFile1);
                    }
                }
            }
        } else {// 文件
//            this.updateTotal();
            this.monitors.add(new ShellSFTPUploadMonitor(localFile, remoteFile, this));
        }
    }

    /**
     * 执行上传
     */
    private void doUpload() {
        while (!this.isEmpty()) {
            ShellSFTPUploadMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isCancelled()) {
                continue;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            try {
                this.client.put(monitor.getLocalFilePath(), monitor.getRemoteFile(), monitor);
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                    JulLog.warn("upload canceled");
                } else {
                    ex.printStackTrace();
                    JulLog.warn("file:{} upload failed", monitor.getLocalFileName(), ex);
                    this.failed(monitor, ex);
                    break;
                }
            }
            ThreadUtil.sleep(5);
        }
    }

//    @Override
//    protected void updateTotal() {
////        if (this.monitors.isEmpty() && !this.isInPreparation()) {
////            this.updateStatus(ShellSFTPStatus.FINISHED);
////        }
//        super.updateTotal();
//        this.manager.updateUploading();
//    }

    @Override
    public void cancel() {
        super.cancel();
        this.manager.remove(this);
        this.updateStatus(ShellFileStatus.CANCELED);
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

    @Override
    public void remove(ShellSFTPUploadMonitor monitor) {
        super.remove(monitor);
        this.manager.remove(this);
    }

    @Override
    public void ended(ShellSFTPUploadMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor, this);
        this.manager.remove(this);
//        this.updateStatus(ShellSFTPStatus.FINISHED);
    }

    @Override
    public void failed(ShellSFTPUploadMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor, exception);
        this.manager.remove(this);
//        this.updateStatus(ShellSFTPStatus.FAILED);
    }

    @Override
    public void canceled(ShellSFTPUploadMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor, this);
        this.manager.remove(this);
//        this.updateStatus(ShellSFTPStatus.CANCELED);
    }

    @Override
    public void changed(ShellSFTPUploadMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor, this);
    }
}
