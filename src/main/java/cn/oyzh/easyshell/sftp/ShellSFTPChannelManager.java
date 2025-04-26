//package cn.oyzh.easyshell.sftp;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * @author oyzh
// * @since 2025-03-07
// */
//public class ShellSFTPChannelManager {
//
//    private final List<ShellSFTPChannel> sftpList = new CopyOnWriteArrayList<>();
//
//    public boolean hasAvailable() {
//        for (ShellSFTPChannel shellSftp : this.sftpList) {
//            if (shellSftp.isClosed() || !shellSftp.isConnected() || shellSftp.isUsing() || shellSftp.isHolding()) {
//                continue;
//            }
//            return true;
//        }
//        return false;
//    }
//
//    public ShellSFTPChannel take() {
//        List<ShellSFTPChannel> removes = new ArrayList<>();
//        try {
//            for (ShellSFTPChannel shellSftp : this.sftpList) {
//                if (shellSftp.isClosed() || !shellSftp.isConnected()) {
//                    removes.add(shellSftp);
//                    continue;
//                }
//                if (shellSftp.isUsing() || shellSftp.isHolding()) {
//                    continue;
//                }
//                return shellSftp;
//            }
//        } finally {
//            this.sftpList.removeAll(removes);
//        }
//        return null;
//    }
//
//    public void push(ShellSFTPChannel sftp) {
//        List<ShellSFTPChannel> removes = new ArrayList<>();
//        for (ShellSFTPChannel shellSftp : this.sftpList) {
//            if (shellSftp.isClosed() || !shellSftp.isConnected()) {
//                removes.add(shellSftp);
//            }
//        }
//        this.sftpList.removeAll(removes);
//        this.sftpList.add(sftp);
//    }
//
//    public void close() {
//        for (ShellSFTPChannel shellSftp : this.sftpList) {
//            shellSftp.close();
//        }
//        this.sftpList.clear();
//    }
//}
