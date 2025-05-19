package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * ftp客户端
 *
 * @author oyzh
 * @since 2025/04/26
 */
public class ShellFTPClient extends FTPClient implements ShellFileClient<ShellFTPFile> {

    private ShellConnect shellConnect;

    /**
     * 是否流模式
     */
    private transient boolean streamMode = false;

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
            super.setDataTimeout(Duration.of(timeout, ChronoUnit.MILLIS));
            super.connect(hostIp, port);
            super.enterLocalPassiveMode();
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

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return deleteTasks;
    }

    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return uploadTasks;
    }

    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return downloadTasks;
    }

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return this.transportTasks;
    }

    @Override
    public void closeDelayResources() {
        try {
            if (this.streamMode) {
                this.streamMode = false;
                this.completePendingCommand();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(String file) throws Exception {
        super.deleteFile(file);
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        if (super.removeDirectory(dir)) {
            return;
        }
        if (this.sendSiteCommand("RMTDIR " + dir)) {
            return;
        }
        if (this.sendSiteCommand("RMDA " + dir)) {
            return;
        }
        if (this.sendCommand("RMD", dir) == 250) {
            return;
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
                    this.deleteDirRecursive(filePath);
                } else {
                    // 删除文件
                    this.delete(filePath);
                }
            }
        }
        // 删除空文件夹
        this.deleteDir(dir);
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws IOException {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        InputStream in;
        if (callback != null) {
            in = ShellFTPProgressMonitor.of(localFile, callback);
        } else {
            in = localFile;
        }
        super.storeFile(remoteFile, in);
        IOUtil.close(in);
        // 更新修改时间
        String mtime = DateUtil.format("yyyyMMddHHmmss");
        super.setModificationTime(remoteFile, mtime);
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        OutputStream out = super.storeFileStream(remoteFile);
        OutputStream stream;
        if (callback != null) {
            stream = ShellFTPProgressMonitor.of(out, callback);
        } else {
            stream = out;
        }
        return stream;
    }

    @Override
    public void get(ShellFTPFile remoteFile, String localFile, Function<Long, Boolean> callback) throws IOException {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        OutputStream out;
        if (callback != null) {
            out = ShellFTPProgressMonitor.of(new FileOutputStream(localFile), callback);
        } else {
            out = new FileOutputStream(localFile);
        }
        super.retrieveFile(remoteFile.getFilePath(), out);
        IOUtil.close(out);
    }

    @Override
    public InputStream getStream(ShellFTPFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        this.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        InputStream in = super.retrieveFileStream(remoteFile.getFilePath());
        InputStream stream;
        if (callback != null) {
            stream = ShellFTPProgressMonitor.of(in, callback);
        } else {
            stream = in;
        }
        return stream;
    }

    @Override
    public List<ShellFTPFile> lsFile(String filePath) throws Exception {
        List<ShellFTPFile> list = new ArrayList<>();
        FTPFile[] files = this.listFiles(filePath);
        if (files != null) {
            for (FTPFile file : files) {
                FTPFile linkFile = null;
                if (file.isSymbolicLink()) {
                    linkFile = this.getFile("/" + file.getLink());
                }
                list.add(new ShellFTPFile(filePath, file, linkFile));
            }
        }
        return list;
    }

    @Override
    public boolean createDir(String filePath) throws IOException {
        return super.makeDirectory(filePath);
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        String[] dirs = filePath.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            currentPath.append("/").append(dir);
            try {
                // 创建缺失目录
                if (!this.exist(currentPath.toString())) {
                    this.createDir(currentPath.toString());
                }
            } catch (Exception ex) {
                // 创建缺失目录
                this.createDir(currentPath.toString());
            }
        }
    }

    @Override
    public String workDir() throws Exception {
        return super.printWorkingDirectory();
    }

    @Override
    public void cd(String filePath) throws Exception {
        this.changeWorkingDirectory(filePath);
    }

    private FTPFile getFile(String filePath) throws IOException {
        FTPFile file = super.mlistFile(filePath);
        if (file != null) {
            return file;
        }
        String pPath = ShellFileUtil.parent(filePath);
        String fName = ShellFileUtil.name(filePath);
        FTPFile[] files = super.listFiles(pPath);
        if (files != null) {
            for (FTPFile ftpFile : files) {
                if (ftpFile.getName().equals(fName)) {
                    return ftpFile;
                }
            }
        }
        return null;
    }

    @Override
    public void touch(String filePath) throws Exception {
        // 文件不存在，创建文件
        if (!this.exist(filePath)) {
            try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
                super.storeFile(filePath, inputStream);
            }
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        try {
            long size = this.size(filePath);
            // 如果是550，则要继续判断
            if (size == 550) {
                FTPFile file = this.mlistFile(filePath);
                return file != null;
            }
            return true;
        } catch (IndexOutOfBoundsException ignored) {
            return false;
        }
    }

    @Override
    public boolean chmod(int permissions, String filePath) throws Exception {
        // 构建 SITE CHMOD 命令
        String command = "CHMOD " + Integer.toOctalString(permissions) + " " + filePath;
        // 发送命令到 FTP 服务器
        return this.sendSiteCommand(command);
    }

    @Override
    public ShellFTPFile fileInfo(String filePath) throws IOException {
        FTPFile file = this.getFile(filePath);
        if (file != null) {
            String pPath = ShellFileUtil.parent(filePath);
            FTPFile linkFile = null;
            if (file.isSymbolicLink()) {
                linkFile = this.getFile("/" + file.getLink());
            }
            return new ShellFTPFile(pPath, file, linkFile);
        }
        return null;
    }

    @Override
    public ShellFTPClient forkClient() {
        try {
            ShellFTPClient ftpClient = new ShellFTPClient(this.shellConnect) {
                @Override
                public boolean isForked() {
                    return true;
                }
            };
            ftpClient.start();
            return ftpClient;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

}
