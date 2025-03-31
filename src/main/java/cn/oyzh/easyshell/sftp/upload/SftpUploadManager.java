package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.SftpManager;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadManager extends SftpManager<SftpUploadMonitor, SftpUploadTask> {

    public void createMonitor(File localFile, String remoteFile, ShellClient client) {
        this.tasks.add(new SftpUploadTask(this, localFile, remoteFile, client));
        this.taskSizeChanged();
        this.doUpload();
    }

    private final BooleanProperty uploadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty uploadingProperty() {
        return this.uploadingProperty;
    }

    public void updateUploading() {
        for (SftpUploadTask task : this.tasks) {
            if (task.isUploading() || task.isInPreparation()) {
                this.uploadingProperty.set(true);
                return;
            }
        }
        this.uploadingProperty.set(false);
    }

    public boolean isUploading() {
        return this.uploadingProperty.get();
    }

    public void setUploading(boolean uploading) {
        this.uploadingProperty.set(uploading);
    }

    private void doUpload() {
        if (this.isUploading()) {
            return;
        }
        this.setUploading(true);
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    SftpUploadTask task = this.tasks.peek();
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
