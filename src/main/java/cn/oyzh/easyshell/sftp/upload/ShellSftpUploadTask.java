package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.sftp.ShellSftpTask;
import cn.oyzh.easyshell.sftp.ShellSftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpException;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellSftpUploadTask extends ShellSftpTask<ShellSftpUploadMonitor> {

    /**
     * 上传状态
     */
    private ShellSftpUploadStatus status;

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(ShellSftpUploadStatus status) {
        this.status = status;
        switch (status) {
            case FAILED -> this.statusProperty.set(I18nHelper.failed());
            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
            case UPLOAD_ING -> this.statusProperty.set(I18nHelper.uploadIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
    }

    private final File localFile;

    private final String remoteFile;

    private final ShellClient client;

    private final ShellSftpUploadManager manager;

    @Override
    public String getSrcPath() {
        return this.localFile.getName();
    }

    @Override
    public String getDestPath() {
        return this.remoteFile;
    }

    public ShellSftpUploadTask(ShellSftpUploadManager manager, File localFile, String remoteFile, ShellClient client) {
        this.client = client;
        this.manager = manager;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
//        this.destPath = remoteFile;
//        this.srcPath = localFile.getName();
//        this.currentFileProperty().set(localFile.getPath());
//        this.executeThread = ThreadUtil.start(() -> {
//            try {
//                this.updateStatus(ShellSftpUploadStatus.IN_PREPARATION);
//                this.addMonitorRecursive(localFile, remoteFile, client);
//                this.updateStatus(ShellSftpUploadStatus.UPLOAD_ING);
//                this.calcTotalSize();
//                this.updateTotal();
//                this.doUpload();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                this.updateTotal();
//                // 如果是非取消和失败，则设置为结束
//                if (!this.isCancelled() && !this.isFailed()) {
//                    this.updateStatus(ShellSftpUploadStatus.FINISHED);
//                }
//            }
//        });
    }

    /**
     * 执行上传
     */
    public void upload() {
        try {
            this.updateStatus(ShellSftpUploadStatus.IN_PREPARATION);
            this.addMonitorRecursive(localFile, remoteFile);
            this.updateStatus(ShellSftpUploadStatus.UPLOAD_ING);
            this.calcTotalSize();
//            this.updateTotal();
            this.doUpload();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            this.updateTotal();
            // 如果是非取消和失败，则设置为结束
            if (!this.isCancelled() && !this.isFailed()) {
                this.updateStatus(ShellSftpUploadStatus.FINISHED);
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
    protected void addMonitorRecursive(File localFile, String remoteFile) throws SftpException {
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
                String remoteDir = ShellSftpUtil.concat(remoteFile, localFile.getName());
                // 递归创建文件夹
                this.client.openSftp().mkdirRecursive(remoteDir);
                // 添加文件
                for (File file : files) {
                    if (file.isDirectory()) {
                        this.addMonitorRecursive(file, remoteDir);
                    } else {
                        String remoteFile1 = ShellSftpUtil.concat(remoteDir, file.getName());
                        this.addMonitorRecursive(file, remoteFile1);
                    }
                }
            }
        } else {// 文件
//            this.updateTotal();
            this.monitors.add(new ShellSftpUploadMonitor(localFile, remoteFile, this));
        }
    }

    /**
     * 执行上传
     */
    private void doUpload() {
        while (!this.isEmpty()) {
            ShellSftpUploadMonitor monitor = this.takeMonitor();
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
            ShellSftp sftp = this.client.newSftp();
            try {
                sftp.put(monitor.getLocalFilePath(), monitor.getRemoteFile(), monitor);
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                    JulLog.warn("upload canceled");
                } else {
                    ex.printStackTrace();
                    JulLog.warn("file:{} upload failed", monitor.getLocalFileName(), ex);
                    this.failed(monitor, ex);
                    break;
                }
            } finally {
                IOUtil.close(sftp);
            }
            ThreadUtil.sleep(5);
        }
    }

//    @Override
//    protected void updateTotal() {
////        if (this.monitors.isEmpty() && !this.isInPreparation()) {
////            this.updateStatus(ShellSftpUploadStatus.FINISHED);
////        }
//        super.updateTotal();
//        this.manager.updateUploading();
//    }

    @Override
    public void cancel() {
        super.cancel();
        this.manager.remove(this);
        this.updateStatus(ShellSftpUploadStatus.CANCELED);
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

    @Override
    public boolean isFailed() {
        return this.status == ShellSftpUploadStatus.FAILED;
    }

    @Override
    public boolean isFinished() {
        return this.status == ShellSftpUploadStatus.FINISHED;
    }

    @Override
    public boolean isCancelled() {
        return this.status == ShellSftpUploadStatus.CANCELED;
    }

    @Override
    public boolean isInPreparation() {
        return this.status == ShellSftpUploadStatus.IN_PREPARATION;
    }

    /**
     * 是否上传中
     *
     * @return 结果
     */
    public boolean isUploading() {
        return this.status == ShellSftpUploadStatus.UPLOAD_ING;
    }

    @Override
    public void remove(ShellSftpUploadMonitor monitor) {
        super.remove(monitor);
        this.manager.remove(this);
    }

    @Override
    public void ended(ShellSftpUploadMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor, this);
        this.manager.remove(this);
        this.updateStatus(ShellSftpUploadStatus.FINISHED);
    }

    @Override
    public void failed(ShellSftpUploadMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor, exception);
        this.manager.remove(this);
        this.updateStatus(ShellSftpUploadStatus.FAILED);
    }

    @Override
    public void canceled(ShellSftpUploadMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor, this);
        this.manager.remove(this);
        this.updateStatus(ShellSftpUploadStatus.CANCELED);
    }

    @Override
    public void changed(ShellSftpUploadMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor, this);
    }
}
