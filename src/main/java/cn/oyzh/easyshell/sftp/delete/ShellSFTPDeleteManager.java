//package cn.oyzh.easyshell.sftp.delete;
//
//import cn.oyzh.common.exception.ExceptionUtil;
//import cn.oyzh.common.function.WeakBiConsumer;
//import cn.oyzh.common.function.WeakConsumer;
//import cn.oyzh.common.function.WeakRunnable;
//import cn.oyzh.common.log.JulLog;
//import cn.oyzh.easyshell.sftp.ShellSFTPChannel;
//import cn.oyzh.easyshell.sftp.ShellSFTPFile;
//import cn.oyzh.easyshell.util.ShellFileUtil;
//import cn.oyzh.fx.plus.information.MessageBox;
//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.SftpException;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
//import java.util.Vector;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
///**
// * @author oyzh
// * @since 2025-03-06
// */
//public class ShellSFTPDeleteManager implements AutoCloseable {
//
//    private Supplier<ShellSFTPChannel> sftpSupplier;
//
//    public ShellSFTPDeleteManager(Supplier<ShellSFTPChannel> sftpSupplier) {
//        this.sftpSupplier = sftpSupplier;
//    }
//
//    private final Queue<ShellSFTPFile> files = new ArrayDeque<>();
//
//    private final List<WeakRunnable> deleteEndedCallbacks = new ArrayList<>();
//
//    private final List<WeakConsumer<String>> deleteDeletedCallbacks = new ArrayList<>();
//
//    private final List<WeakBiConsumer<ShellSFTPFile, Throwable>> deleteFailedCallbacks = new ArrayList<>();
//
//    public void addDeleteEndedCallback(Object obj, Runnable deleteEndedCallback) {
//        if (deleteEndedCallback != null) {
//            this.deleteEndedCallbacks.add(new WeakRunnable(obj, deleteEndedCallback));
//        }
//    }
//
//    public void addDeleteDeletedCallback(Object obj, Consumer<String> deleteDeletedCallback) {
//        if (deleteDeletedCallback != null) {
//            this.deleteDeletedCallbacks.add(new WeakConsumer<>(obj, deleteDeletedCallback));
//        }
//    }
//
//    public void addDeleteFailedCallback(Object obj, BiConsumer<ShellSFTPFile, Throwable> deleteFailedCallback) {
//        if (deleteFailedCallback != null) {
//            this.deleteFailedCallbacks.add(new WeakBiConsumer<>(obj, deleteFailedCallback));
//        }
//    }
//
//    public void fileDelete(ShellSFTPFile file) {
//        this.files.add(file);
//        this.doDelete();
//    }
//
//    public void deleteEnded() {
//        if (!this.deleteEndedCallbacks.isEmpty()) {
//            for (WeakRunnable action : this.deleteEndedCallbacks) {
//                action.run();
//            }
//        }
//    }
//
//    public void deleteDeleted(String path) {
//        if (!this.deleteDeletedCallbacks.isEmpty()) {
//            for (WeakConsumer<String> consumer : this.deleteDeletedCallbacks) {
//                consumer.accept(path);
//            }
//        }
//    }
//
//    public void deleteFailed(ShellSFTPFile file, Throwable exception) {
//        if (!this.deleteFailedCallbacks.isEmpty()) {
//            for (WeakBiConsumer<ShellSFTPFile, Throwable> consumer : this.deleteFailedCallbacks) {
//                consumer.accept(file, exception);
//            }
//        }
//    }
//
//    public boolean isEmpty() {
//        return this.files.isEmpty();
//    }
//
//
//    private transient boolean deleting = false;
//
//    private void doDelete() {
//        if (this.deleting) {
//            return;
//        }
//        try {
//            this.deleting = true;
//            while (!this.isEmpty()) {
//                ShellSFTPFile deleteFile = this.files.peek();
//                if (deleteFile == null) {
//                    break;
//                }
//                try (ShellSFTPChannel sftp = this.sftpSupplier.get()) {
//                    deleteFile.startWaiting();
//                    if (deleteFile.isDirectory()) {
//                        this.rmdirRecursive(deleteFile.getFilePath(), sftp);
//                    } else {
//                        this.rm(deleteFile.getFilePath(), sftp);
//                    }
//                } catch (Exception ex) {
//                    if (!ExceptionUtil.hasMessage(ex, "no such file")) {
//                        ex.printStackTrace();
//                        JulLog.warn("file:{} delete failed", deleteFile.getFilePath(), ex);
//                        MessageBox.exception(ex);
//                        this.deleteFailed(deleteFile, ex);
//                        break;
//                    }
//                } finally {
//                    deleteFile.stopWaiting();
//                    this.files.remove(deleteFile);
//                }
//            }
//        } finally {
//            // 删除文件结束
//            this.deleteEnded();
//            this.deleting = false;
//        }
//    }
//
//    private void rmdirRecursive(String path, ShellSFTPChannel sftp) throws SftpException {
//        Vector<ChannelSftp.LsEntry> entries = sftp.ls(path);
//        for (ChannelSftp.LsEntry entry : entries) {
//            String filename = entry.getFilename();
//            if (ShellFileUtil.isNormal(filename)) {
//                String fullPath = path + "/" + filename;
//                if (entry.getAttrs().isDir()) {
//                    this.rmdirRecursive(fullPath, sftp);
//                } else {
//                    this.rm(fullPath, sftp);
//                }
//            }
//        }
//        this.rmdir(path, sftp);
//    }
//
//    private void rm(String path, ShellSFTPChannel sftp) throws SftpException {
//        sftp.rm(path);
//        this.deleteDeleted(path);
//    }
//
//    private void rmdir(String path, ShellSFTPChannel sftp) throws SftpException {
//        sftp.rmdir(path);
//        this.deleteDeleted(path);
//    }
//
////    private final BooleanProperty deletingProperty = new SimpleBooleanProperty(false);
////
////    public BooleanProperty deletingProperty() {
////        return this.deletingProperty;
////    }
////
////    public void setDeleting(boolean deleting) {
////        this.deletingProperty.set(deleting);
////    }
////
////    public boolean isDeleting() {
////        return this.deletingProperty.get();
////    }
//
//    @Override
//    public void close() throws Exception {
//        this.files.clear();
//        this.sftpSupplier = null;
//        this.deleteFailedCallbacks.clear();
//        this.deleteEndedCallbacks.clear();
//        this.deleteDeletedCallbacks.clear();
//    }
//}
