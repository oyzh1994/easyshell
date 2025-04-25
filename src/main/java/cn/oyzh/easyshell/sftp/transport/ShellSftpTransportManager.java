package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.sftp.ShellSftpManager;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSftpTransportManager extends ShellSftpManager<ShellSftpTransportMonitor, ShellSftpTransportTask> {

    public void fileTransport(ShellSftpFile localFile, String remoteFile, ShellSSHClient localClient, ShellSSHClient remoteClient) {
        this.tasks.add(new ShellSftpTransportTask(this, localFile, remoteFile, localClient, remoteClient));
        this.taskSizeChanged();
        this.doTransport();
    }

    private final BooleanProperty transportingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty transportingProperty() {
        return this.transportingProperty;
    }

    public boolean isTransporting() {
        return this.transportingProperty.get();
    }

    public void setTransporting(boolean transporting) {
        this.transportingProperty.set(transporting);
    }

    /**
     * 执行传输
     */
    protected void doTransport() {
        if (this.isTransporting()) {
            return;
        }
        this.setTransporting(true);
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    ShellSftpTransportTask task = this.tasks.peek();
                    if (task == null) {
                        break;
                    }
                    try {
                        task.transport();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JulLog.warn("file:{} transport failed", task.getSrcPath(), ex);
                        MessageBox.exception(ex);
                    } finally {
                        this.tasks.remove(task);
                    }
                }
            } finally {
                this.setTransporting(false);
            }
        });
    }
}
