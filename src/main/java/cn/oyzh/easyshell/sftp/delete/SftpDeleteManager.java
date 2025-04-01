package cn.oyzh.easyshell.sftp.delete;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.plus.information.MessageBox;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDeleteManager implements AutoCloseable {

    private ShellClient client;

    public SftpDeleteManager(ShellClient client) {
        this.client = client;
    }

    private final Queue<SftpFile> files = new ArrayDeque<>();

    private final List<Runnable> deleteEndedCallbacks = new ArrayList<>();

    private final List<Consumer<String>> deleteDeletedCallbacks = new ArrayList<>();


    public void addDeleteEndedCallback(Runnable deleteEndedCallback) {
        this.deleteEndedCallbacks.add(deleteEndedCallback);
    }

    public void addDeleteDeletedCallback(Consumer<String> deleteDeletedCallback) {
        this.deleteDeletedCallbacks.add(deleteDeletedCallback);
    }

    public void fileDelete(SftpFile file) {
        this.files.add(file);
        this.doDelete();
    }

    public void deleteEnded() {
        if (!this.deleteEndedCallbacks.isEmpty()) {
            for (Runnable deleteEndedCallback : this.deleteEndedCallbacks) {
                deleteEndedCallback.run();
            }
        }
    }

    public void deleteDeleted(String path) {
        if (!this.deleteDeletedCallbacks.isEmpty()) {
            for (Consumer<String> deleteDeletedCallback : this.deleteDeletedCallbacks) {
                deleteDeletedCallback.accept(path);
            }
        }
    }

    public boolean isEmpty() {
        return this.files.isEmpty();
    }

    private void doDelete() {
        if (this.isDeleting()) {
            return;
        }
        try {
            this.setDeleting(true);
            while (!this.isEmpty()) {
                SftpFile deleteFile = this.files.peek();
                if (deleteFile == null) {
                    break;
                }
                ShellSftp sftp = this.client.openSftp();
                try {
                    deleteFile.startWaiting();
                    if (deleteFile.isDir()) {
                        this.rmdirRecursive(deleteFile.getPath(), sftp);
                    } else {
                        this.rm(deleteFile.getPath(), sftp);
                    }
                } catch (Exception ex) {
                    if (!ExceptionUtil.hasMessage(ex, "no such file")) {
                        ex.printStackTrace();
                        JulLog.warn("file:{} delete failed", deleteFile.getPath(), ex);
                        MessageBox.exception(ex);
                    }
                } finally {
                    deleteFile.stopWaiting();
                    this.files.remove(deleteFile);
                }
            }
        } finally {
            // 删除文件结束
            this.deleteEnded();
            this.setDeleting(false);
        }
    }

    private void rmdirRecursive(String path, ShellSftp sftp) throws SftpException {
        Vector<ChannelSftp.LsEntry> entries = sftp.ls(path);
        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (!filename.equals(".") && !filename.equals("..")) {
                String fullPath = path + "/" + filename;
                if (entry.getAttrs().isDir()) {
                    this.rmdirRecursive(fullPath, sftp);
                } else {
                    this.rm(fullPath, sftp);
                }
            }
        }
        this.rmdir(path, sftp);
    }

    private void rm(String path, ShellSftp sftp) throws SftpException {
        sftp.rm(path);
        this.deleteDeleted(path);
    }

    private void rmdir(String path, ShellSftp sftp) throws SftpException {
        sftp.rmdir(path);
        this.deleteDeleted(path);
    }

    private final BooleanProperty deletingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty deletingProperty() {
        return this.deletingProperty;
    }

    public void setDeleting(boolean deleting) {
        this.deletingProperty.set(deleting);
    }

    public boolean isDeleting() {
        return this.deletingProperty.get();
    }

    @Override
    public void close() throws Exception {
        this.client = null;
    }
}
