package cn.oyzh.easyssh.sftp.delete;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.SftpUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Setter;

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

    @Setter
    private Consumer<SftpDeleteEnded> deleteEndedCallback;

    @Setter
    private Consumer<SftpDeleteDeleted> deleteDeletedCallback;

    public void deleteFile(SftpFile file, SSHSftp sftp) {
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
                SSHSftp sftp = deleteFile.getSftp();
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

    private void rmdirRecursive(String path, SSHSftp sftp) throws SftpException {
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

    private void rm(String path, SSHSftp sftp) throws SftpException {
        sftp.rm(path);
        this.deleteDeleted(path);
    }

    private void rmdir(String path, SSHSftp sftp) throws SftpException {
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

        private SSHSftp sftp;

        public SSHSftp getSftp() {
            return sftp;
        }

        public void setSftp(SSHSftp sftp) {
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

        public DeleteFile(SftpFile file, SSHSftp sftp) {
            this.file = file;
            this.sftp = sftp;
        }
    }
}
