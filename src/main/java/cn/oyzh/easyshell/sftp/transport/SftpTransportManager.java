package cn.oyzh.easyshell.sftp.transport;

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
public class SftpTransportManager {

    private final List<SftpTransportTask> tasks = new CopyOnWriteArrayList<>();

    public void createMonitor(File localFile, String remoteFile, ShellSftp sftp) {
        this.tasks.add(new SftpTransportTask(this, localFile, remoteFile, sftp));
    }

    /**
     * 取消
     */
    public void cancel() {
        for (SftpTransportTask task : this.tasks) {
            task.cancel();
        }
        ThreadUtil.start(this.tasks::clear, 500);
    }

    private final BooleanProperty uploadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty uploadingProperty() {
        return this.uploadingProperty;
    }

    public void updateUploading() {
        for (SftpTransportTask task : this.tasks) {
            if (task.isTransporting() || task.isInPreparation()) {
                this.uploadingProperty.set(true);
                return;
            }
        }
        this.uploadingProperty.set(false);
    }

    public List<SftpTransportTask> getTasks() {
        return tasks;
    }

    public void remove(SftpTransportTask task) {
        task.cancel();
        this.tasks.remove(task);
    }

    public void cancel(SftpTransportTask task) {
        task.cancel();
    }
}
