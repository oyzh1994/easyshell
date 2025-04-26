package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.sftp.ShellSftpUtil;
import com.jcraft.jsch.SftpException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellFTPClient extends FTPClient implements BaseClient {

    private ShellConnect shellConnect;

    public ShellFTPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    @Override
    public void close() {
        try {
            this.logout();
            super.disconnect();
            this.shellConnect = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(int timeout) {
        if (this.isConnected()) {
            return;
        }
        try {
            // 连接信息
            int port = this.shellConnect.hostPort();
            String hostIp = this.shellConnect.hostIp();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            super.setConnectTimeout(timeout);
            super.connect(hostIp, port);
            if (this.isConnected()) {
                super.login(this.shellConnect.getUser(), this.shellConnect.getPassword());
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellSftpClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            JulLog.warn("shellSftpClient start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return !this.isConnected();
    }

    private final ObservableList<ShellFTPDeleteFile> deleteFiles = FXCollections.observableArrayList();

    private final ObservableList<ShellFTPUploadFile> uploadFiles = FXCollections.observableArrayList();

    private final ObservableList<ShellFTPDownloadFile> downloadFiles = FXCollections.observableArrayList();

    public ObservableList<ShellFTPDeleteFile> getDeleteFiles() {
        return deleteFiles;
    }

    public ObservableList<ShellFTPUploadFile> getUploadFiles() {
        return uploadFiles;
    }

    public ObservableList<ShellFTPDownloadFile> getDownloadFiles() {
        return downloadFiles;
    }

    public void delete(ShellFTPFile file) {
        ShellFTPDeleteFile deleteFile = new ShellFTPDeleteFile();
        try {
            deleteFile.setSize(file.getSize());
            deleteFile.setRemotePath(file.getFilePath());
            this.deleteFiles.add(deleteFile);
            if (file.isDirectory()) {
                ShellFTPUtil.deleteDirectory(this, file.getFilePath());
            } else {
                super.deleteFile(file.getFilePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.deleteFiles.remove(deleteFile);
        }
    }

    public void upload(File localFile, String remotePath) throws SftpException {
        String remoteFile = ShellSftpUtil.concat(remotePath, localFile.getName());
        ShellFTPUploadFile uploadFile = new ShellFTPUploadFile();
        uploadFile.setRemotePath(remoteFile);
        uploadFile.setSize(localFile.length());
        uploadFile.setLocalPath(localFile.getAbsolutePath());
        this.uploadFiles.add(uploadFile);
        Thread task = ThreadUtil.startVirtual(() -> {
            try {
                this.setFileType(FTPClient.BINARY_FILE_TYPE);
                if (localFile.isDirectory()) {
                    String remoteDir = ShellSftpUtil.concat(remotePath, localFile.getName());
                    this.mkdir(remoteDir);
                    ShellFTPUtil.uploadFolder(this, localFile, remoteDir);
                } else {
                    super.storeFile(remoteFile, new FileInputStream(localFile));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                this.uploadFiles.remove(uploadFile);
            }
        });
        uploadFile.setTask(task);
    }

    public void download(File localFile, ShellFTPFile remoteFile) throws SftpException {
        ShellFTPDownloadFile downloadFile = new ShellFTPDownloadFile();
        downloadFile.setSize(remoteFile.getSize());
        downloadFile.setRemotePath(remoteFile.getFilePath());
        downloadFile.setLocalPath(localFile.getAbsolutePath());
        this.downloadFiles.add(downloadFile);
        Thread task = ThreadUtil.startVirtual(() -> {
            try {
                this.setFileType(FTPClient.BINARY_FILE_TYPE);
                super.retrieveFile(remoteFile.getFilePath(), new FileOutputStream(localFile));
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                this.downloadFiles.remove(downloadFile);
            }
        });
        downloadFile.setTask(task);
    }

    public List<ShellFTPFile> lsFile(String filePath) {

        List<ShellFTPFile> list = new ArrayList<>();
        try {
            FTPFile[] files = this.listFiles(filePath);
            if (files != null) {
                for (FTPFile file : files) {
                    list.add(new ShellFTPFile(filePath, file));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public String pwdDir() {
        try {
            return super.printWorkingDirectory();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void mkdir(String filePath) {
        try {
            super.makeDirectory(filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ShellFTPFile finfo(String filePath) {
        try {
            FTPFile file = super.mlistFile(filePath);
            String pPath = ShellSftpUtil.parent(filePath);
            return new ShellFTPFile(pPath, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void touch(String filePath) {
        try {
            // 文件不存在，创建文件
            InputStream inputStream = new ByteArrayInputStream(new byte[0]);
            boolean created = super.storeFile(filePath, inputStream);
            inputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean exist(String filePath) {
        try {
            long lastModifiedTime = this.mdtm(filePath);
            if (lastModifiedTime != -1) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void cd(String filePath) {
        try {
            this.changeWorkingDirectory(filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
