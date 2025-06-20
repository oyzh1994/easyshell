package cn.oyzh.easyshell.sshj.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.sshj.ShellBaseSSHClient;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.LocalSourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * sftp客户端
 *
 * @author oyzh
 * @since 2025/04/16
 */
public class ShellSFTPClient extends ShellBaseSSHClient implements ShellFileClient<ShellSFTPFile> {


    private SFTPClient sftpClient;

    /**
     * 连接状态
     */
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    @Override
    public ReadOnlyObjectProperty<ShellConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> super.onStateChanged(state3);

    public ShellSFTPClient(ShellConnect shellConnect) {
        this(shellConnect, null);
    }

    public ShellSFTPClient(ShellConnect shellConnect, SSHClient sshClient) {
        super(shellConnect);
        this.sshClient = sshClient;
        this.shellConnect = shellConnect;
        super.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    protected void initClient() throws IOException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.shellConnect.getUser(), this.shellConnect.getPassword(), this.shellConnect.getHost());
        }
        // 连接信息
        int port = this.shellConnect.hostPort();
        String hostIp = this.shellConnect.hostIp();
        this.sshClient = new SSHClient();
    }

    @Override
    public void close() {
        try {
            // 销毁会话
            if (this.sshClient != null && this.sshClient.isConnected()) {
                this.sshClient.disconnect();
                this.sshClient = null;
            }
            // 清除用户、分组信息
            if (this.attr != null) {
                this.attr.clear();
                this.attr = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
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
            this.sshClient.setTimeout(timeout);
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 执行连接
            if (this.sftpClient != null) {
                this.state.set(ShellConnState.CONNECTING);
            }
            if (this.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellSFTPClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellSFTPClient start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public boolean isConnected() {
        return this.sshClient != null && this.sshClient.isConnected();
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
    public void delete(String file) throws Exception {
        this.sftpClient.rm(file);
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        this.sftpClient.rmdir(dir);
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        this.sftpClient.rmdir(dir);
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        this.sftpClient.mkdir(filePath);
        return true;
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        this.sftpClient.mkdirs(filePath);
    }

    @Override
    public String workDir() throws Exception {
        return this.getUserHome();
    }

    @Override
    public void cd(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    public FileAttributes stat(String filePath) throws Exception {
        return this.sftpClient.stat(filePath);
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        return this.sftpClient.stat(filePath) != null;
    }

    public String realpath(String filePath) throws Exception {
        return this.sftpClient.readlink(filePath);
    }

    @Override
    public void touch(String filePath) throws Exception {
        LocalSourceFile file = ShellSFTPUtil.emptyFile(filePath);
        this.sftpClient.put(file, filePath);
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        LocalSourceFile file = ShellSFTPUtil.streamFile(localFile, remoteFile);
        this.sftpClient.put(file, remoteFile);
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(ShellSFTPFile remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        this.sftpClient.get(remoteFile.getFilePath(), localFile);
    }

    @Override
    public InputStream getStream(ShellSFTPFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        RemoteFile file = this.sftpClient.open(remoteFile.getFilePath());
        RemoteFile.RemoteFileInputStream inputStream = file.new RemoteFileInputStream();
        return inputStream;
    }

    @Override
    public boolean chmod(int permission, String filePath) throws Exception {
        this.sftpClient.chmod(filePath, permission);
        return true;
    }

    @Override
    public ShellSFTPFile fileInfo(String filePath) throws Exception {
        FileAttributes attrs = this.stat(filePath);
        String pPath = ShellFileUtil.parent(filePath);
        String fName = ShellFileUtil.name(filePath);
        ShellSFTPFile file = new ShellSFTPFile(pPath, fName, attrs);
        // 读取链接文件
        ShellSFTPUtil.realpath(file, this);
        // 拥有者、分组
        if (this.isWindows()) {
            file.setOwner("-");
            file.setGroup("-");
        } else {
            file.setOwner(ShellSFTPUtil.getOwner(file.getUid(), this));
            file.setGroup(ShellSFTPUtil.getGroup(file.getGid(), this));
        }
        return file;
    }

    @Override
    public List<ShellSFTPFile> lsFile(String filePath) throws Exception {
        List<RemoteResourceInfo> infos = this.sftpClient.ls(filePath);
        List<ShellSFTPFile> files = new ArrayList<ShellSFTPFile>();
        for (RemoteResourceInfo info : infos) {
            ShellSFTPFile file = new ShellSFTPFile(filePath, info);
            files.add(file);
        }
        return files;
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        this.sftpClient.rename(filePath, newPath);
        return true;
    }

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return uploadTasks;
    }

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return downloadTasks;
    }

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return deleteTasks;
    }

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return transportTasks;
    }

    @Override
    public void closeDelayResources() {
    }

    @Override
    public ShellFileClient<ShellSFTPFile> forkClient() {
        try {
            ShellSFTPClient sftpClient = new ShellSFTPClient(this.shellConnect) {
                @Override
                public boolean isForked() {
                    return true;
                }
            };
            sftpClient.start();
            return sftpClient;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }
}
