package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
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
public class SftpDownloadManager extends SftpManager<SftpDownloadMonitor, SftpDownloadTask> {

    public void fileDownload(File localFile, SftpFile remoteFile, ShellClient client) {
        this.tasks.add(new SftpDownloadTask(this, localFile, remoteFile, client));
        this.taskSizeChanged();
        this.doDownload();
    }

    private final BooleanProperty downloadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty downloadingProperty() {
        return this.downloadingProperty;
    }

//    public void updateDownloading() {
//        for (SftpDownloadTask task : this.tasks) {
//            if (task.isDownloading() || task.isInPreparation()) {
//                this.downloadingProperty.set(true);
//                return;
//            }
//        }
//        this.downloadingProperty.set(false);
//    }

    public boolean isDownloading() {
        return this.downloadingProperty.get();
    }

    public void setDownloading(boolean downloading) {
        this.downloadingProperty.set(downloading);
    }

    /**
     * 执行下载
     */
    protected void doDownload() {
        if (this.isDownloading()) {
            return;
        }
        this.setDownloading(true);
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    SftpDownloadTask task = this.tasks.peek();
                    if (task == null) {
                        break;
                    }
                    try {
                        task.download();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JulLog.warn("file:{} download failed", task.getSrcPath(), ex);
                        MessageBox.exception(ex);
                    } finally {
                        this.tasks.remove(task);
                    }
                }
            } finally {
                this.setDownloading(false);
            }
        });
    }
}
