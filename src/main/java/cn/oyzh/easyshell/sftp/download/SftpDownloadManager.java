package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpManager;
import cn.oyzh.easyshell.sftp.ShellSftp;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDownloadManager extends SftpManager<SftpDownloadMonitor, SftpDownloadTask> {

    public void createMonitor(File localFile, SftpFile remoteFile, ShellSftp sftp) {
        this.tasks.add(new SftpDownloadTask(this, localFile, remoteFile, sftp));
        this.taskChanged();
    }

    private final BooleanProperty downloadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty downloadingProperty() {
        return this.downloadingProperty;
    }

    public void updateDownloading() {
        for (SftpDownloadTask task : this.tasks) {
            if (task.isDownloading() || task.isInPreparation()) {
                this.downloadingProperty.set(true);
                return;
            }
        }
        this.downloadingProperty.set(false);
    }
}
