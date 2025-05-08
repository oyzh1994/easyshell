package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.file.FileClient;
import cn.oyzh.easyshell.sftp.delete.ShellSFTPDeleteManager;
import cn.oyzh.easyshell.sftp.download.ShellSFTPDownloadManager;
import cn.oyzh.easyshell.sftp.transport.ShellSFTPTransportManager;
import cn.oyzh.easyshell.sftp.upload.ShellSFTPUploadManager;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellSFTPClient extends ShellClient implements FileClient<ShellSFTPFile> {

    public ShellSFTPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session) {
        this.shellConnect = shellConnect;
        this.session = session;
    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.shellConnect.getUser(), this.shellConnect.getPassword(), this.shellConnect.getHost());
        }
        // 连接信息
        int port = this.shellConnect.hostPort();
        String hostIp = this.shellConnect.hostIp();
        // 创建会话
        this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        // 设置密码
        this.session.setPassword(this.shellConnect.getPassword());
        // 配置参数
        Properties config = new Properties();
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
        // 设置配置
        this.session.setConfig(config);
    }

    @Override
    public void close() {
        try {
//            if (this.sftpManager != null) {
//                this.sftpManager.close();
//                this.sftpManager = null;
//            }
            if (this.deleteManager != null) {
                this.deleteManager.close();
                this.deleteManager = null;
            }
            if (this.uploadManager != null) {
                this.uploadManager.close();
                this.uploadManager = null;
            }
            if (this.transportManager != null) {
                this.transportManager.close();
                this.transportManager = null;
            }
            if (this.downloadManager != null) {
                this.downloadManager.close();
                this.downloadManager = null;
            }
            // 销毁回话
            if (this.session != null && this.session.isConnected()) {
                this.session.disconnect();
                this.session = null;
            }
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
            // 初始化客户端
            this.initClient();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 执行连接
            if (this.session != null) {
                // 连接超时
                this.session.setTimeout(timeout);
                // 连接
                this.session.connect(timeout);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellSFTPClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            JulLog.warn("shellSFTPClient start error", ex);
            throw new ShellException(ex);
        }
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.session == null || !this.session.isConnected();
    }

//    private ShellSFTPChannelManager sftpManager;
//
//    public ShellSFTPChannelManager getSftpManager() {
//        if (this.sftpManager == null) {
//            this.sftpManager = new ShellSFTPChannelManager();
//        }
//        return this.sftpManager;
//    }

    private ShellSFTPUploadManager uploadManager;

    public ShellSFTPUploadManager getUploadManager() {
        if (this.uploadManager == null) {
            this.uploadManager = new ShellSFTPUploadManager();
        }
        return uploadManager;
    }

    private ShellSFTPDeleteManager deleteManager;

    public ShellSFTPDeleteManager getDeleteManager() {
        if (this.deleteManager == null) {
            this.deleteManager = new ShellSFTPDeleteManager(this::newSFTP);
        }
        return deleteManager;
    }

    private ShellSFTPDownloadManager downloadManager;

    public ShellSFTPDownloadManager getDownloadManager() {
        if (this.downloadManager == null) {
            this.downloadManager = new ShellSFTPDownloadManager();
        }
        return downloadManager;
    }

    private ShellSFTPTransportManager transportManager;

    public ShellSFTPTransportManager getTransportManager() {
        if (this.transportManager == null) {
            this.transportManager = new ShellSFTPTransportManager();
        }
        return transportManager;
    }
//
//    public ShellSFTPChannel openSFTP() {
//        if (!this.getSftpManager().hasAvailable()) {
//            ShellSFTPChannel sftp = this.newSFTP();
//            if (sftp != null) {
//                this.getSftpManager().push(sftp);
//                return sftp;
//            }
//        }
//        return this.getSftpManager().take();
//    }

    protected ShellSFTPChannel newSFTP() {
        try {
            ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
            ShellSFTPChannel sftp = new ShellSFTPChannel(channel, this.osType);
            sftp.connect(this.connectTimeout());
            return sftp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String exec_id_un(int uid) {
        return this.exec("id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("id -gn " + gid);
    }

    private ShellSFTPAttr attr;

    public ShellSFTPAttr getAttr() {
        if (this.attr == null) {
            this.attr = new ShellSFTPAttr();
        }
        return this.attr;
    }

    @Override
    public void delete(ShellSFTPFile file) {
        this.getDeleteManager().fileDelete(file);
    }

    public void upload(File localFile, String remoteFile) throws Exception {
        this.getUploadManager().fileUpload(localFile, remoteFile, this);
    }

    public void download(File localFile, ShellSFTPFile remoteFile) throws Exception {
        this.getDownloadManager().fileDownload(localFile, remoteFile, this);
    }

    public void transport(ShellSFTPFile localFile, String remoteFile, ShellSFTPClient remoteClient) {
        this.getTransportManager().fileTransport(localFile, remoteFile, this, remoteClient);
    }

    public void rm(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.rm(filePath);
        }
    }

    public void rmdir(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.rmdir(filePath);
        }
    }

    @Override
    public boolean mkdir(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.mkdir(filePath);
        }
        return true;
    }

    @Override
    public void cd(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.cd(filePath);
        }
    }

    public SftpATTRS stat(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.stat(filePath);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.exist(filePath);
        }
    }

    public String realpath(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.realpath(filePath);
        }
    }

    @Override
    public String pwdDir() throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.pwd();
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.touch(filePath);
        }
    }

    public void put(InputStream stream, String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.put(stream, filePath);
        }
    }

    public OutputStream put(String dest, SftpProgressMonitor monitor) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.put(dest, monitor);
        }
    }

    public void put(String src, String dest, SftpProgressMonitor monitor) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.put(src, dest, monitor);
        }
    }

    public void get(String src, String dest, SftpProgressMonitor monitor, int mode) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.get(src, dest, monitor, mode);
        }
    }

    public InputStream get(String src) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.get(src);
        }
    }

    public void get(String src, String dest) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.get(src, dest);
        }
    }

    public void get(String src, String dest, SftpProgressMonitor monitor) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.get(src, dest, monitor);
        }
    }

    public void chmod(int permission, String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.chmod(permission, filePath);
        }
    }

    @Override
    public List<ShellSFTPFile> lsFile(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.lsFile(filePath);
        }
    }

    public void lsFile(String filePath, Consumer<ShellSFTPFile> callback) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.lsFile(filePath, callback);
        }
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.rename(filePath, newPath);
        }
        return true;
    }

    public List<ShellSFTPFile> lsFileNormal(String filePath) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            return channel.lsFileNormal(filePath);
        }
    }

    public void mkdirRecursive(String remoteDir) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            channel.mkdirRecursive(remoteDir);
        }
    }

    private final ObservableList<ShellSFTPUploadTask> uploadTasks = FXCollections.observableArrayList();

    public ObservableList<ShellSFTPUploadTask> getUploadTasks() {
        return uploadTasks;
    }

    public void uploadFile(File localFile, String remotePath) throws Exception {
        ShellSFTPUploadTask task = new ShellSFTPUploadTask(localFile, remotePath, this);
        uploadTasks.add(task);
        Thread thread = ThreadUtil.startVirtual(() -> {
            try {
                task.upload();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                uploadTasks.remove(task);
            }
        });
        task.setWorker(thread);
    }

    private final ObservableList<ShellSFTPDownloadTask> downloadTasks = FXCollections.observableArrayList();

    public ObservableList<ShellSFTPDownloadTask> getDownloadTasks() {
        return downloadTasks;
    }

    public void downloadFile(ShellSFTPFile remoteFile, File localPath) throws Exception {
        ShellSFTPDownloadTask task = new ShellSFTPDownloadTask(remoteFile, localPath, this);
        downloadTasks.add(task);
        Thread thread = ThreadUtil.startVirtual(() -> {
            try {
                task.download();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                downloadTasks.remove(task);
            }
        });
        task.setWorker(thread);
    }

    public boolean isDownloadTaskEmpty() {
        return this.downloadTasks.isEmpty();
    }

    public boolean isUploadTaskEmpty() {
        return this.uploadTasks.isEmpty();
    }

    public boolean isDeleteTaskEmpty() {
        return this.deleteTasks.isEmpty();
    }

    public boolean isTaskEmpty() {
        return this.uploadTasks.isEmpty() && this.downloadTasks.isEmpty();
    }

    public int getTaskSize() {
        return this.uploadTasks.size() + this.downloadTasks.size();
    }

    private final ObservableList<ShellSFTPDeleteTask> deleteTasks = FXCollections.observableArrayList();

    public ObservableList<ShellSFTPDeleteTask> getDeleteTasks() {
        return deleteTasks;
    }

    public void deleteFile(ShellSFTPFile file) throws Exception {
        ShellSFTPDeleteFile deleteFile = new ShellSFTPDeleteFile();
        deleteFile.setFile(file);
        ShellSFTPDeleteTask task = new ShellSFTPDeleteTask(deleteFile, this);
        deleteTasks.add(task);
        Thread thread = ThreadUtil.startVirtual(() -> {
            try {
                task.doDelete();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                deleteTasks.remove(task);
            }
        });
        deleteFile.setTask(thread);
    }

    public void addTaskSizeCallback(Runnable callback) {
        this.getUploadTasks().addListener((ListChangeListener<ShellSFTPUploadTask>) change -> {
            callback.run();
        });
        this.getDownloadTasks().addListener((ListChangeListener<ShellSFTPDownloadTask>) change -> {
            callback.run();
        });
    }

    public void getAllFiles(ShellSFTPFile remoteFile, Consumer<ShellSFTPFile> callback) throws Exception {
        if (remoteFile.isFile()) {
            callback.accept(remoteFile);
        } else {
            List<ShellSFTPFile> files = this.lsFileNormal(remoteFile.getFilePath());
            for (ShellSFTPFile file : files) {
                this.getAllFiles(file, callback);
            }
        }
    }

    public void rmdirRecursive(String path) throws Exception {
        try (ShellSFTPChannel channel = this.newSFTP()) {
            Vector<ChannelSftp.LsEntry> entries = channel.ls(path);
            for (ChannelSftp.LsEntry entry : entries) {
                String filename = entry.getFilename();
                if (ShellFileUtil.isNormal(filename)) {
                    String fullPath = path + "/" + filename;
                    if (entry.getAttrs().isDir()) {
                        this.rmdirRecursive(fullPath);
                    } else {
                        this.rm(fullPath);
                    }
                }
            }
            this.rmdir(path);
        }
    }

}
