package cn.oyzh.easyshell.sftp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-03-07
 */
public class SSHSftpManager {

    private final List<ShellSftp> sftpList = new CopyOnWriteArrayList<>();

    public boolean hasAvailable() {
        for (ShellSftp shellSftp : this.sftpList) {
            if (shellSftp.isClosed() || !shellSftp.isConnected() || shellSftp.isUsing() || shellSftp.isHolding()) {
                continue;
            }
            return true;
        }
        return false;
    }

    public ShellSftp take() {
        List<ShellSftp> removes = new ArrayList<>();
        try {
            for (ShellSftp shellSftp : this.sftpList) {
                if (shellSftp.isClosed() || !shellSftp.isConnected()) {
                    removes.add(shellSftp);
                    continue;
                }
                if (shellSftp.isUsing() || shellSftp.isHolding()) {
                    continue;
                }
                return shellSftp;
            }
        } finally {
            this.sftpList.removeAll(removes);
        }
        return null;
    }

    public void push(ShellSftp sftp) {
        List<ShellSftp> removes = new ArrayList<>();
        for (ShellSftp shellSftp : this.sftpList) {
            if (shellSftp.isClosed() || !shellSftp.isConnected()) {
                removes.add(shellSftp);
            }
        }
        this.sftpList.removeAll(removes);
        this.sftpList.add(sftp);
    }

    public void close() {
        for (ShellSftp shellSftp : this.sftpList) {
            shellSftp.close();
        }
        this.sftpList.clear();
    }
}
