package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.file.FileClient;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ftp客户端
 *
 * @author oyzh
 * @since 2025/04/26
 */
public class ShellFTPClient extends FTPClient implements FileClient<ShellFTPFile>, BaseClient {

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

    /**
     * 初始化客户端
     */
    protected void initClient() {
        // 设置字符集
        if (StringUtil.isNotBlank(this.shellConnect.getCharset())) {
            this.setControlEncoding(this.shellConnect.getCharset());
        }
    }

    @Override
    public void start(int timeout) {
        if (this.isConnected()) {
            return;
        }
        try {
            this.initClient();
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
            this.lsFile("/");
            long endTime = System.currentTimeMillis();
            JulLog.info("shellFTPClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            JulLog.warn("shellFTPClient start error", ex);
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

    private final ObservableList<ShellFTPDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFTPUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFTPDownloadTask> downloadTasks = FXCollections.observableArrayList();

    public ObservableList<ShellFTPDeleteTask> deleteTasks() {
        return deleteTasks;
    }

    public ObservableList<ShellFTPUploadTask> uploadTasks() {
        return uploadTasks;
    }

    public ObservableList<ShellFTPDownloadTask> downloadTasks() {
        return downloadTasks;
    }

    @Override
    public void delete(String file) throws Exception {
        super.deleteFile(file);
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        if (!super.removeDirectory(dir)) {
            this.sendCommand("SITE RMTDIR " + dir);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        FTPFile[] files = this.listFiles(dir);
        if (files != null) {
            for (FTPFile file : files) {
                String filePath = dir + "/" + file.getName();
                if (file.isDirectory()) {
                    // 递归删除子文件夹
                    deleteDirRecursive(filePath);
                } else {
                    // 删除文件
                    this.deleteFile(filePath);
                }
            }
        }
        // 删除空文件夹
        this.deleteDir(dir);
    }

    @Override
    public void doDelete(ShellFTPFile file) {
        ShellFTPDeleteTask deleteTask = new ShellFTPDeleteTask(file, this);
        Thread worker = ThreadUtil.startVirtual(() -> {
            try {
                deleteTask.doDelete();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                this.deleteTasks.remove(deleteTask);
            }
        });
        deleteTask.setWorker(worker);
        this.deleteTasks.add(deleteTask);
    }

//    /**
//     * 上传文件
//     *
//     * @param localFile  本地文件
//     * @param remotePath 远程目录
//     */
//    public void upload(File localFile, String remotePath) {
//        this.upload(localFile, remotePath, null);
//    }

//    /**
//     * 上传文件
//     *
//     * @param localFile      本地文件
//     * @param remotePath     远程目录
//     * @param remoteFileName 远程文件名
//     */
//    public void upload(File localFile, String remotePath, String remoteFileName) {
//        if (remoteFileName == null) {
//            remoteFileName = localFile.getName();
//        }
//        String remoteFile = ShellFileUtil.concat(remotePath, remoteFileName);
//        ShellFTPUploadFile uploadFile = new ShellFTPUploadFile();
//        uploadFile.setRemotePath(remoteFile);
//        uploadFile.setSize(localFile.length());
//        uploadFile.setLocalPath(localFile.getAbsolutePath());
//        this.uploadFiles.add(uploadFile);
//        Thread task = ThreadUtil.startVirtual(() -> {
//            try {
//                this.doUpload(localFile, remoteFile);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                MessageBox.exception(ex);
//            } finally {
//                this.uploadFiles.remove(uploadFile);
//            }
//        });
//        uploadFile.setTask(task);
//    }

    @Override
    public void doUpload(File localFile, String remoteFile) {
        ShellFTPUploadTask uploadTask = new ShellFTPUploadTask(localFile, remoteFile, this);
        Thread worker = ThreadUtil.startVirtual(() -> {
            try {
                uploadTask.doUpload();
            } catch (InterruptedException | InterruptedIOException ex) {
                JulLog.warn("upload interrupted");
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                this.uploadTasks.remove(uploadTask);
            }
        });
        uploadTask.setWorker(worker);
        this.uploadTasks.add(uploadTask);
    }

    @Override
    public void doDownload(ShellFTPFile remoteFile, String localPath) {
        ShellFTPDownloadTask downloadTask = new ShellFTPDownloadTask(remoteFile, localPath, this);
        Thread worker = ThreadUtil.startVirtual(() -> {
            try {
                downloadTask.doDownload();
            } catch (InterruptedException | InterruptedIOException ex) {
                JulLog.warn("upload interrupted");
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                this.downloadTasks.remove(downloadTask);
            }
        });
        downloadTask.setWorker(worker);
        this.downloadTasks.add(downloadTask);
    }

    /**
     * 执行上传
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    public void put(File localFile, String remoteFile) throws IOException {
        this.put(localFile, remoteFile, null);
    }

    /**
     * 执行上传
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    public void put(File localFile, String remoteFile, ShellFTPProgressMonitor monitor) throws IOException {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        InputStream in;
        if (monitor != null) {
            in = monitor.init(new FileInputStream(localFile));
        } else {
            in = new FileInputStream(localFile);
        }
        super.storeFile(remoteFile, in);
        IOUtil.close(in);
    }

    /**
     * 执行下载
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    public void get(ShellFTPFile remoteFile, String localFile) throws IOException {
        this.get(remoteFile, localFile, null);
    }

    /**
     * 执行下载
     *
     * @param remoteFile 远程文件
     * @param localFile  本地文件
     * @throws IOException 异常
     */
    public void get(ShellFTPFile remoteFile, String localFile, ShellFTPProgressMonitor monitor) throws IOException {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        OutputStream out;
        if (monitor != null) {
            out = monitor.init(new FileOutputStream(localFile));
        } else {
            out = new FileOutputStream(localFile);
        }
        super.retrieveFile(remoteFile.getFilePath(), out);
        IOUtil.close(out);
    }

//    /**
//     * 执行上传
//     *
//     * @param localFile  本地文件
//     * @param remoteFile 远程文件
//     * @throws IOException 异常
//     */
//    public void put1(File localFile, String remoteFile) throws IOException {
//        this.setFileType(FTPClient.BINARY_FILE_TYPE);
//        if (localFile.isDirectory()) {
//            // 创建根目录
//            this.mkdir(remoteFile);
//            ShellFTPUtil.uploadFolder(this, localFile, remoteFile);
//        } else {
//            FileInputStream fis = new FileInputStream(localFile);
//            super.storeFile(remoteFile, fis);
//            fis.close();
//        }
//    }

//    /**
//     * 下载文件
//     *
//     * @param localPath  本地路径
//     * @param remoteFile 远程文件
//     */
//    public void download(File localPath, ShellFTPFile remoteFile) {
//        ShellFTPDownloadFile downloadFile = new ShellFTPDownloadFile();
//        downloadFile.setSize(remoteFile.getSize());
//        downloadFile.setLocalPath(localPath.getPath());
//        downloadFile.setRemotePath(remoteFile.getFilePath());
//        this.downloadFiles.add(downloadFile);
//        String localFile = ShellFileUtil.concat(localPath.getPath(), remoteFile.getFileName());
//        Thread task = ThreadUtil.startVirtual(() -> {
//            try {
//                this.doDownload(localFile, remoteFile);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                MessageBox.exception(ex);
//            } finally {
//                this.downloadFiles.remove(downloadFile);
//            }
//        });
//        downloadFile.setTask(task);
//    }
//
//    /**
//     * 执行下载
//     *
//     * @param localFile  本地文件
//     * @param remoteFile 远程文件
//     * @throws IOException 异常
//     */
//    public void doDownload(String localFile, ShellFTPFile remoteFile) throws IOException {
//        this.setFileType(FTPClient.BINARY_FILE_TYPE);
//        if (remoteFile.isDirectory()) {
//            FileUtil.mkdir(localFile);
//            ShellFTPUtil.downloadFolder(this, remoteFile.getFilePath(), localFile);
//        } else {
//            FileOutputStream fos = new FileOutputStream(localFile);
//            super.retrieveFile(remoteFile.getFilePath(), fos);
//            fos.flush();
//            fos.close();
//        }
//    }

    @Override
    public List<ShellFTPFile> lsFile(String filePath) throws Exception {
        List<ShellFTPFile> list = new ArrayList<>();
        FTPFile[] files = this.listFiles(filePath);
        if (files != null) {
            for (FTPFile file : files) {
                list.add(new ShellFTPFile(filePath, file));
            }
        }
        return list;
    }

    @Override
    public String pwdDir() throws Exception {
        return super.printWorkingDirectory();
    }

    @Override
    public boolean mkdir(String filePath) throws IOException {
        return super.makeDirectory(filePath);
    }

    public ShellFTPFile finfo(String filePath) throws IOException {
        FTPFile file = super.mlistFile(filePath);
        String pPath = ShellFileUtil.parent(filePath);
        return new ShellFTPFile(pPath, file);
    }

    @Override
    public void touch(String filePath) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            // 文件不存在，创建文件
            super.storeFile(filePath, inputStream);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        try {
            FTPFile file = this.mlistFile(filePath);
            return file != null;
        } catch (IndexOutOfBoundsException ignored) {
            return false;
        }
    }

    @Override
    public void cd(String filePath) throws Exception {
        this.changeWorkingDirectory(filePath);
    }

    public boolean chmod(int permissions, String filePath) throws IOException {
        // 构建 SITE CHMOD 命令
        String command = "SITE CHMOD " + Integer.toOctalString(permissions) + " " + filePath;
        // 发送命令到 FTP 服务器
        int replyCode = this.sendCommand(command);
        // 检查命令执行结果
        return FTPReply.isPositiveCompletion(replyCode);
    }

    public int getTaskSize() {
        return this.uploadTasks.size() + this.downloadTasks.size();
    }

    public boolean isUploadTaskEmpty() {
        return this.uploadTasks.isEmpty();
    }

    public boolean isTaskEmpty() {
        return this.uploadTasks.isEmpty() && this.downloadTasks.isEmpty();
    }

    public void addTaskSizeCallback(Runnable callback) {
        this.uploadTasks().addListener((ListChangeListener<ShellFTPUploadTask>) change -> {
            callback.run();
        });
        this.downloadTasks().addListener((ListChangeListener<ShellFTPDownloadTask>) change -> {
            callback.run();
        });
    }
}
