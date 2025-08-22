package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileProgressMonitor;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ftp客户端
 *
 * @author oyzh
 * @since 2025/04/26
 */
public class ShellFTPClient implements ShellFileClient<ShellFTPFile> {

    /**
     * ftp客户端
     */
    private FTPClient client;

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 是否流模式
     */
    private transient boolean streamMode = false;

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellFileClient.super.onStateChanged(state3);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    public ShellFTPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    @Override
    public void close() {
        try {
            if (this.client != null) {
                this.client.logout();
                this.client.disconnect();
                this.client = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
//            this.shellConnect = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化客户端
     */
    protected void initClient() {
        if (this.shellConnect.isSSLMode()) {
            FTPSClient ftpsClient = new FTPSClient();
            ftpsClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            this.client = ftpsClient;
        } else {
            this.client = new FTPClient();
        }
        // 设置字符集
        if (StringUtil.isNotBlank(this.shellConnect.getCharset())) {
            this.client.setControlEncoding(this.shellConnect.getCharset());
        }
        // 代理处理
        if (this.shellConnect.isEnableProxy()) {
            this.client.setProxy(ShellProxyUtil.initProxy1(this.shellConnect.getProxyConfig()));
        }
    }

    @Override
    public void start(int timeout) throws Exception {
        if (this.isConnected()) {
            return;
        }
        try {
            this.initClient();
            this.state.set(ShellConnState.CONNECTING);
            // 连接信息
            int port = this.shellConnect.hostPort();
            String hostIp = this.shellConnect.hostIp();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            this.client.setConnectTimeout(timeout);
            this.client.setDataTimeout(Duration.of(timeout, ChronoUnit.MILLIS));
            this.client.connect(hostIp, port);
            // 连接失败
            if (!this.isConnected()) {
                this.state.set(ShellConnState.FAILED);
                JulLog.warn("ftp connect fail.");
                return;
            }
            // 登陆
            if (StringUtil.isNotBlank(this.shellConnect.getUser())) {
                // 密码
                String pwd = StringUtil.isNotBlank(this.shellConnect.getPassword()) ? this.shellConnect.getPassword() : null;
                // 登陆失败
                if (!this.client.login(this.shellConnect.getUser(), pwd)) {
                    this.state.set(ShellConnState.FAILED);
                    JulLog.warn("ftp login fail.");
                    return;
                }
            }
            // 启用 TLS 加密
            if (this.shellConnect.isSSLMode()) {
                FTPSClient ftpsClient = (FTPSClient) this.client;
                ftpsClient.execPBSZ(0);
                ftpsClient.execPROT("P");
            }
            // 被动模式
            if (this.shellConnect.isFtpPassiveMode()) {
                this.client.enterLocalPassiveMode();
            } else {
                this.client.enterLocalActiveMode();
            }
            // 保持连接
            this.client.setKeepAlive(true);
            // 设置so超时
            this.client.setSoTimeout(timeout);
            this.lsFile("/");
            this.state.set(ShellConnState.CONNECTED);
            long endTime = System.currentTimeMillis();
            // 添加到状态监听器队列
            ShellClientChecker.push(this);
            JulLog.info("shellFTPClient connected used:{}ms.", (endTime - starTime));
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellFTPClient start error", ex);
            throw ex;
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return deleteTasks;
    }

    /**
     * 上传竞争器
     */
    private final Competitor uploadCompetitor = new Competitor(2);

    @Override
    public Competitor uploadCompetitor() {
        return this.uploadCompetitor;
    }

    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return uploadTasks;
    }

    /**
     * 下载竞争器
     */
    private final Competitor downloadCompetitor = new Competitor(2);

    @Override
    public Competitor downloadCompetitor() {
        return this.downloadCompetitor;
    }

    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return downloadTasks;
    }

    /**
     * 传输竞争器
     */
    private final Competitor transportCompetitor = new Competitor(2);

    @Override
    public Competitor transportCompetitor() {
        return transportCompetitor;
    }

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return this.transportTasks;
    }

    @Override
    public void closeDelayResources() {
        try {
            if (this.streamMode) {
                this.streamMode = false;
                this.client.completePendingCommand();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(String file) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "rm " + file);
        this.client.deleteFile(file);
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "rmdir " + dir);
        if (this.client.removeDirectory(dir)) {
            return;
        }
        if (this.client.sendSiteCommand("RMTDIR " + dir)) {
            return;
        }
        if (this.client.sendSiteCommand("RMDA " + dir)) {
            return;
        }
        if (this.client.sendCommand("RMD", dir) == 250) {
            return;
        }
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        FTPFile[] files = this.client.listFiles(dir);
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
    public boolean rename(ShellFTPFile file, String newName) throws Exception {
        String filePath = file.getFilePath();
        String newPath = ShellFileUtil.concat(file.getParentPath(), newName);
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "rename " + filePath + " " + newPath);
        return this.client.rename(filePath, newPath);
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws IOException {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        this.client.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        InputStream in;
        if (callback != null) {
            in = ShellFileProgressMonitor.of(localFile, callback);
        } else {
            in = localFile;
        }
        this.client.storeFile(remoteFile, in);
        IOUtil.close(in);
        // 更新修改时间
        String mtime = DateUtil.format("yyyyMMddHHmmss");
        this.client.setModificationTime(remoteFile, mtime);
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        this.client.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        OutputStream out = this.client.storeFileStream(remoteFile);
        OutputStream stream;
        if (callback != null) {
            stream = ShellFileProgressMonitor.of(out, callback);
        } else {
            stream = out;
        }
        return stream;
    }

    /**
     * 删除竞争器
     */
    private final Competitor deleteCompetitor = new Competitor(5);

    @Override
    public Competitor deleteCompetitor() {
        return this.deleteCompetitor;
    }

    @Override
    public void get(ShellFTPFile remoteFile, String localFile, Function<Long, Boolean> callback) throws IOException {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        this.client.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        OutputStream out;
        if (callback != null) {
            out = ShellFileProgressMonitor.of(new FileOutputStream(localFile), callback);
        } else {
            out = new FileOutputStream(localFile);
        }
        this.client.retrieveFile(remoteFile.getFilePath(), out);
        IOUtil.close(out);
    }

    @Override
    public InputStream getStream(ShellFTPFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        this.client.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.streamMode = true;
        InputStream in = this.client.retrieveFileStream(remoteFile.getFilePath());
        InputStream stream;
        if (callback != null) {
            stream = ShellFileProgressMonitor.of(in, callback);
        } else {
            stream = in;
        }
        return stream;
    }

    // @Override
    // public List<ShellFTPFile> lsFile(String filePath) throws Exception {
    //     List<ShellFTPFile> list = new ArrayList<>();
    //     FTPFile[] files = this.client.listFiles(filePath);
    //     if (files != null) {
    //         for (FTPFile file : files) {
    //             FTPFile linkFile = null;
    //             if (file.isSymbolicLink()) {
    //                 linkFile = this.getFile("/" + file.getLink());
    //             }
    //             list.add(new ShellFTPFile(filePath, file, linkFile));
    //         }
    //     }
    //     return list;
    // }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellFTPFile> fileCallback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "ls " + filePath);
        FTPFile[] files = this.client.listFiles(filePath);
        if (files != null) {
            for (FTPFile file : files) {
                FTPFile linkFile = null;
                if (file.isSymbolicLink()) {
                    linkFile = this.getFile("/" + file.getLink());
                }
                ShellFTPFile ftpFile = new ShellFTPFile(filePath, file, linkFile);
                fileCallback.accept(ftpFile);
            }
        }
    }

    @Override
    public boolean createDir(String filePath) throws IOException {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "mkdir " + filePath);
        return this.client.makeDirectory(filePath);
    }

    // @Override
    // public void createDirRecursive(String filePath) throws Exception {
    //     String[] dirs = filePath.split("/");
    //     StringBuilder currentPath = new StringBuilder();
    //     for (String dir : dirs) {
    //         if (dir.isEmpty()) {
    //             continue;
    //         }
    //         currentPath.append("/").append(dir);
    //         try {
    //             // 创建缺失目录
    //             if (!this.exist(currentPath.toString())) {
    //                 this.createDir(currentPath.toString());
    //             }
    //         } catch (Exception ex) {
    //             // 创建缺失目录
    //             this.createDir(currentPath.toString());
    //         }
    //     }
    // }

    @Override
    public String workDir() throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "pwd");
        return this.client.printWorkingDirectory();
    }

    @Override
    public void cd(String filePath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "cd " + filePath);
        this.client.changeWorkingDirectory(filePath);
    }

    private FTPFile getFile(String filePath) throws IOException {
        FTPFile file = this.client.mlistFile(filePath);
        if (file != null) {
            return file;
        }
        String pPath = ShellFileUtil.parent(filePath);
        String fName = ShellFileUtil.name(filePath);
        FTPFile[] files = this.client.listFiles(pPath);
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
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "touch " + filePath);
            try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
                this.client.storeFile(filePath, inputStream);
            }
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "exist " + filePath);
            // 获取文件大小
            long size = this.client.size(filePath);
            if (size != 550 && size >= 0) {
                return true;
            }
            // 如果是550，则要继续判断
            if (size == 550) {
                // 获取文件本身
                FTPFile file = this.client.mlistFile(filePath);
                if (file != null) {
                    return true;
                }
            }
            // 列举文件，可能是文件夹
            FTPFile[] files = this.client.listFiles(filePath);
            if (ArrayUtil.isNotEmpty(files)) {
                return true;
            }
            return false;
        } catch (IndexOutOfBoundsException ignored) {
            return false;
        }
    }

    @Override
    public String realpath(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean chmod(int permissions, String filePath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "chmod " + filePath + " " + permissions);
        // 发送命令到 FTP 服务器
        return this.client.sendSiteCommand("CHMOD " + permissions + " " + filePath);
    }

    @Override
    public ShellFTPFile fileInfo(String filePath) throws IOException {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "fileInfo " + filePath);
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
            ShellFTPClient client = new ShellFTPClient(this.shellConnect) {
                @Override
                public boolean isForked() {
                    return true;
                }
            };
            client.start();
            return client;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return this;
    }

    @Override
    public boolean isRealpathSupport() {
        return false;
    }
}
