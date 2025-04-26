package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    /**
     * 删除文件
     *
     * @param file 文件
     */
    public void delete(ShellFTPFile file) {
        ShellFTPDeleteFile deleteFile = new ShellFTPDeleteFile();
        deleteFile.setSize(file.getSize());
        deleteFile.setRemotePath(file.getFilePath());
        this.deleteFiles.add(deleteFile);
        Thread task = ThreadUtil.startVirtual(() -> {
            try {
                file.startWaiting();
                if (file.isDirectory()) {
                    ShellFTPUtil.deleteDirectory(this, file.getFilePath());
                    super.removeDirectory(file.getFilePath());
                } else {
                    super.deleteFile(file.getFilePath());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            } finally {
                file.stopWaiting();
                this.deleteFiles.remove(deleteFile);
            }
        });
        deleteFile.setTask(task);
    }

    /**
     * 删除文件
     *
     * @param file 文件
     */
    public void doDelete(ShellFTPFile file) {
        try {
            file.startWaiting();
            if (file.isDirectory()) {
                ShellFTPUtil.deleteDirectory(this, file.getFilePath());
                super.removeDirectory(file.getFilePath());
            } else {
                super.deleteFile(file.getFilePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        } finally {
            file.stopWaiting();
        }
    }

    /**
     * 上传文件
     *
     * @param localFile  本地文件
     * @param remotePath 远程目录
     */
    public void upload(File localFile, String remotePath) {
        this.upload(localFile, remotePath, null);
    }

    /**
     * 上传文件
     *
     * @param localFile      本地文件
     * @param remotePath     远程目录
     * @param remoteFileName 远程文件名
     */
    public void upload(File localFile, String remotePath, String remoteFileName) {
        if (remoteFileName == null) {
            remoteFileName = localFile.getName();
        }
        String remoteFile = ShellFileUtil.concat(remotePath, remoteFileName);
        ShellFTPUploadFile uploadFile = new ShellFTPUploadFile();
        uploadFile.setRemotePath(remoteFile);
        uploadFile.setSize(localFile.length());
        uploadFile.setLocalPath(localFile.getAbsolutePath());
        this.uploadFiles.add(uploadFile);
        Thread task = ThreadUtil.startVirtual(() -> {
            try {
                this.doUpload(localFile, remoteFile);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            } finally {
                this.uploadFiles.remove(uploadFile);
            }
        });
        uploadFile.setTask(task);
    }

    /**
     * 执行上传
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    public void doUpload(File localFile, String remoteFile) throws IOException {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        if (localFile.isDirectory()) {
            // 创建根目录
            this.mkdir(remoteFile);
            ShellFTPUtil.uploadFolder(this, localFile, remoteFile);
        } else {
            FileInputStream fis = new FileInputStream(localFile);
            super.storeFile(remoteFile, fis);
            fis.close();
        }
    }

    /**
     * 下载文件
     *
     * @param localPath  本地路径
     * @param remoteFile 远程文件
     */
    public void download(File localPath, ShellFTPFile remoteFile) {
        ShellFTPDownloadFile downloadFile = new ShellFTPDownloadFile();
        downloadFile.setSize(remoteFile.getSize());
        downloadFile.setLocalPath(localPath.getPath());
        downloadFile.setRemotePath(remoteFile.getFilePath());
        this.downloadFiles.add(downloadFile);
        String localFile = ShellFileUtil.concat(localPath.getPath(), remoteFile.getFileName());
        Thread task = ThreadUtil.startVirtual(() -> {
            try {
                this.doDownload(localFile, remoteFile);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            } finally {
                this.downloadFiles.remove(downloadFile);
            }
        });
        downloadFile.setTask(task);
    }

    /**
     * 执行下载
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    public void doDownload(String localFile, ShellFTPFile remoteFile) throws IOException {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        if (remoteFile.isDirectory()) {
            FileUtil.mkdir(localFile);
            ShellFTPUtil.downloadFolder(this, remoteFile.getFilePath(), localFile);
        } else {
            FileOutputStream fos = new FileOutputStream(localFile);
            super.retrieveFile(remoteFile.getFilePath(), fos);
            fos.flush();
            fos.close();
        }
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

    public boolean mkdir(String filePath) {
        try {
            return super.makeDirectory(filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public ShellFTPFile finfo(String filePath) {
        try {
            FTPFile file = super.mlistFile(filePath);
            String pPath = ShellFileUtil.parent(filePath);
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

    public boolean chmod(int permissions, String filePath) {
        try {
            // 构建 SITE CHMOD 命令
            String command = "SITE CHMOD " + Integer.toOctalString(permissions) + " " + filePath;
            // 发送命令到 FTP 服务器
            int replyCode = this.sendCommand(command);
            // 检查命令执行结果
            return FTPReply.isPositiveCompletion(replyCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
