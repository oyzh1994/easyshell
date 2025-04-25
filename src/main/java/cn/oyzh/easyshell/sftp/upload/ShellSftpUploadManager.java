package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSftpManager;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSftpUploadManager extends ShellSftpManager<ShellSftpUploadMonitor, ShellSftpUploadTask> {

    public void fileUpload(File localFile, String remoteFile, ShellSSHClient client) {
        this.tasks.add(new ShellSftpUploadTask(this, localFile, remoteFile, client));
        this.taskSizeChanged();
        this.doUpload();
    }

    private final BooleanProperty uploadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty uploadingProperty() {
        return this.uploadingProperty;
    }

//    public void updateUploading() {
//        for (ShellSftpUploadTask task : this.tasks) {
//            if (task.isUploading() || task.isInPreparation()) {
//                this.uploadingProperty.set(true);
//                return;
//            }
//        }
//        this.uploadingProperty.set(false);
//    }

    public boolean isUploading() {
        return this.uploadingProperty.get();
    }

    public void setUploading(boolean uploading) {
        this.uploadingProperty.set(uploading);
    }

    /**
     * 执行上传
     */
    protected void doUpload() {
        if (this.isUploading()) {
            return;
        }
        this.setUploading(true);
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    ShellSftpUploadTask task = this.tasks.peek();
                    if (task == null) {
                        break;
                    }
                    try {
                        task.upload();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JulLog.warn("file:{} upload failed", task.getSrcPath(), ex);
                        MessageBox.exception(ex);
                    } finally {
                        this.tasks.remove(task);
                    }
                }
            } finally {
                this.setUploading(false);
            }
        });
    }
}
