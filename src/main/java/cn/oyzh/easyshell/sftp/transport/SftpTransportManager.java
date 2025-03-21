package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpManager;
import cn.oyzh.easyshell.shell.ShellClient;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpTransportManager extends SftpManager<SftpTransportMonitor, SftpTransportTask> {

    public void createMonitor(SftpFile localFile, String remoteFile, ShellClient localClient, ShellClient remoteClient) {
        this.tasks.add(new SftpTransportTask(this, localFile, remoteFile, localClient, remoteClient));
        this.taskChanged();
    }
}
