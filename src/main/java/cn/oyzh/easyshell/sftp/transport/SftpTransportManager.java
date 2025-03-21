package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import com.jcraft.jsch.SftpException;
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

    public void createMonitor(SftpFile localFile, String remoteFile, ShellSftp localSftp, ShellSftp remoteSftp) {
        this.tasks.add(new SftpTransportTask(this, localFile, remoteFile, localSftp, remoteSftp));
        this.callback();
    }

    /**
     * 取消
     */
    public void cancel() {
        for (SftpTransportTask task : this.tasks) {
            task.cancel();
        }
    }

    private Runnable taskChangedCallback;

    public void setTaskChangedCallback(Runnable taskChangedCallback) {
        this.taskChangedCallback = taskChangedCallback;
    }

    public List<SftpTransportTask> getTasks() {
        return tasks;
    }

    public void remove(SftpTransportTask task) {
        task.cancel();
        this.tasks.remove(task);
        this.callback();
    }

    public void cancel(SftpTransportTask task) {
        task.cancel();
    }

    protected void callback() {
        if (this.taskChangedCallback != null) {
            this.taskChangedCallback.run();
        }
    }
}
