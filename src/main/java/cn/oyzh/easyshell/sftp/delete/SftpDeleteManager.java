package cn.oyzh.easyshell.sftp.delete;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDeleteManager {

    private final Queue<DeleteFile> files = new ArrayDeque<>();

    private Consumer<SftpDeleteEnded> deleteEndedCallback;

    public Consumer<SftpDeleteEnded> getDeleteEndedCallback() {
        return deleteEndedCallback;
    }

    public void setDeleteEndedCallback(Consumer<SftpDeleteEnded> deleteEndedCallback) {
        this.deleteEndedCallback = deleteEndedCallback;
    }

    public Consumer<SftpDeleteDeleted> getDeleteDeletedCallback() {
        return deleteDeletedCallback;
    }

    public void setDeleteDeletedCallback(Consumer<SftpDeleteDeleted> deleteDeletedCallback) {
        this.deleteDeletedCallback = deleteDeletedCallback;
    }

    private Consumer<SftpDeleteDeleted> deleteDeletedCallback;

    public void deleteFile(SftpFile file, ShellSftp sftp) {
        this.files.add(new DeleteFile(file, sftp));
        this.doDelete();
    }

    public void deleteEnded() {
        if (this.deleteEndedCallback != null) {
            SftpDeleteEnded deleteEnded = new SftpDeleteEnded();
            this.deleteEndedCallback.accept(deleteEnded);
        }
    }

    public void deleteDeleted(SftpFile file) {
        if (this.deleteDeletedCallback != null) {
            String path = SftpUtil.concat(file.getFilePath(), file.getFileName());
            SftpDeleteDeleted deleteDeleted = new SftpDeleteDeleted();
            deleteDeleted.setRemoteFile(path);
            this.deleteDeletedCallback.accept(deleteDeleted);
        }
    }

    public void deleteDeleted(String path) {
        if (this.deleteDeletedCallback != null) {
            SftpDeleteDeleted deleteDeleted = new SftpDeleteDeleted();
            deleteDeleted.setRemoteFile(path);
            this.deleteDeletedCallback.accept(deleteDeleted);
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
                DeleteFile deleteFile = this.files.poll();
                if (deleteFile == null) {
                    break;
                }
                ShellSftp sftp = deleteFile.getSftp();
                try {
                    if (deleteFile.isDir()) {
                        this.rmdirRecursive(deleteFile.getPath(), sftp);
                    } else {
                        this.rm(deleteFile.getPath(), sftp);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JulLog.warn("file:{} delete failed", deleteFile.getPath(), ex);
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

    public static class DeleteFile {

        private SftpFile file;

        private ShellSftp sftp;

        public ShellSftp getSftp() {
            return sftp;
        }

        public void setSftp(ShellSftp sftp) {
            this.sftp = sftp;
        }

        public SftpFile getFile() {
            return file;
        }

        public void setFile(SftpFile file) {
            this.file = file;
        }

        public String getPath() {
            return file.getFilePath();
        }

        public boolean isDir() {
            return file.isDir();
        }

        public DeleteFile(SftpFile file, ShellSftp sftp) {
            this.file = file;
            this.sftp = sftp;
        }
    }
}
