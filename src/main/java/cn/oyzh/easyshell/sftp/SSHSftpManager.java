package cn.oyzh.easyshell.sftp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-03-07
 */
public class SSHSftpManager {

    private final List<SSHSftp> sftpList = new CopyOnWriteArrayList<>();

    public boolean hasAvailable() {
        for (SSHSftp sshSftp : this.sftpList) {
            if (sshSftp.isClosed()) {
                continue;
            }
            if (sshSftp.isUsing()) {
                continue;
            }
            return true;
        }
        return false;
    }

    public SSHSftp take() {
        List<SSHSftp> removes = new ArrayList<>();
        try {
            for (SSHSftp sshSftp : this.sftpList) {
                if (sshSftp.isClosed() || !sshSftp.isConnected()) {
                    removes.add(sshSftp);
                    continue;
                }
                if (sshSftp.isUsing() || sshSftp.isHolding()) {
                    continue;
                }
                return sshSftp;
            }
        } finally {
            this.sftpList.removeAll(removes);
        }
        return null;
    }

    public void push(SSHSftp sftp) {
        List<SSHSftp> removes = new ArrayList<>();
        for (SSHSftp sshSftp : this.sftpList) {
            if (sshSftp.isClosed()) {
                removes.add(sshSftp);
            }
        }
        this.sftpList.removeAll(removes);
        this.sftpList.add(sftp);
    }

    public void close() {
        for (SSHSftp sshSftp : this.sftpList) {
            sshSftp.close();
        }
        this.sftpList.clear();
    }
}
