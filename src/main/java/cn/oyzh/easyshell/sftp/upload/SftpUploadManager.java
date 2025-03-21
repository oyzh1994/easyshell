package cn.oyzh.easyshell.sftp.upload;

import cn.oyzh.easyshell.sftp.SftpManager;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.download.SftpDownloadMonitor;
import cn.oyzh.easyshell.sftp.transport.SftpTransportMonitor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadManager extends SftpManager<SftpUploadMonitor,SftpUploadTask> {

    public void createMonitor(File localFile, String remoteFile, ShellSftp sftp) {
        this.tasks.add(new SftpUploadTask(this, localFile, remoteFile, sftp));
        this.taskChanged();
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
}
