package cn.oyzh.easyshell.sftp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-03-07
 */
public class ShellSftpChannelManager {

    private final List<ShellSftpChannel> sftpList = new CopyOnWriteArrayList<>();

    public boolean hasAvailable() {
        for (ShellSftpChannel shellSftp : this.sftpList) {
            if (shellSftp.isClosed() || !shellSftp.isConnected() || shellSftp.isUsing() || shellSftp.isHolding()) {
                continue;
            }
            return true;
        }
        return false;
    }

    public ShellSftpChannel take() {
        List<ShellSftpChannel> removes = new ArrayList<>();
        try {
            for (ShellSftpChannel shellSftp : this.sftpList) {
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

    public void push(ShellSftpChannel sftp) {
        List<ShellSftpChannel> removes = new ArrayList<>();
        for (ShellSftpChannel shellSftp : this.sftpList) {
            if (shellSftp.isClosed() || !shellSftp.isConnected()) {
                removes.add(shellSftp);
            }
        }
        this.sftpList.removeAll(removes);
        this.sftpList.add(sftp);
    }

    public void close() {
        for (ShellSftpChannel shellSftp : this.sftpList) {
            shellSftp.close();
        }
        this.sftpList.clear();
    }
}
