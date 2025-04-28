package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPManager;
import cn.oyzh.fx.plus.information.MessageBox;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSFTPDownloadManager extends ShellSFTPManager<ShellSFTPDownloadMonitor, ShellSFTPDownloadTask> {

    public void fileDownload(File localFile, ShellSFTPFile remoteFile, ShellSFTPClient client) {
        this.tasks.add(new ShellSFTPDownloadTask(this, localFile, remoteFile, client));
        this.taskSizeChanged();
        this.doDownload();
    }

//    private final BooleanProperty downloadingProperty = new SimpleBooleanProperty(false);
//
//    public BooleanProperty downloadingProperty() {
//        return this.downloadingProperty;
//    }

//    public void updateDownloading() {
//        for (ShellSFTPDownloadTask task : this.tasks) {
//            if (task.isDownloading() || task.isInPreparation()) {
//                this.downloadingProperty.set(true);
//                return;
//            }
//        }
//        this.downloadingProperty.set(false);
//    }

//    public boolean isDownloading() {
//        return this.downloadingProperty.get();
//    }
//
//    public void setDownloading(boolean downloading) {
//        this.downloadingProperty.set(downloading);
//    }


    private transient boolean downloading = false;

    /**
     * 执行下载
     */
    protected void doDownload() {
        if (downloading) {
            return;
        }
        this.downloading = true;
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    ShellSFTPDownloadTask task = this.tasks.peek();
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
                this.downloading = false;
            }
        });
    }
}
