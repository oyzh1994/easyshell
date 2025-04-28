package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPManager;
import cn.oyzh.fx.plus.information.MessageBox;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSFTPTransportManager extends ShellSFTPManager<ShellSFTPTransportMonitor, ShellSFTPTransportTask> {

    public void fileTransport(ShellSFTPFile localFile, String remoteFile, ShellSFTPClient localClient, ShellSFTPClient remoteClient) {
        this.tasks.add(new ShellSFTPTransportTask(this, localFile, remoteFile, localClient, remoteClient));
        this.taskSizeChanged();
        this.doTransport();
    }

//    private final BooleanProperty transportingProperty = new SimpleBooleanProperty(false);
//
//    public BooleanProperty transportingProperty() {
//        return this.transportingProperty;
//    }
//
//    public boolean isTransporting() {
//        return this.transportingProperty.get();
//    }
//
//    public void setTransporting(boolean transporting) {
//        this.transportingProperty.set(transporting);
//    }

    private transient boolean transporting = false;

    /**
     * 执行传输
     */
    protected void doTransport() {
        if (this.transporting) {
            return;
        }
        this.transporting = true;
        ThreadUtil.start(() -> {
            try {
                while (!this.isEmpty()) {
                    ShellSFTPTransportTask task = this.tasks.peek();
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
                this.transporting = false;
            }
        });
    }
}
