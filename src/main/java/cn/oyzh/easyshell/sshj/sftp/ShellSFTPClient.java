package cn.oyzh.easyshell.sshj.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
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
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
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

    private ShellSFTPClintPool clintPool = new ShellSFTPClintPool(this);

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

    @Override
    protected void initClient() throws Exception {
        super.initClient();
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
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            this.state.set(ShellConnState.CONNECTING);
            if (this.sshClient == null) {
                this.initClient();
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

    /**
     * 获取通道
     *
     * @return 通道
     */
    protected SFTPClient takeSFTPClient() {
        return this.clintPool.borrowChannel();
    }

    /**
     * 返回通道
     *
     * @param client 通道
     */
    protected void returnSFTPClient(SFTPClient client) {
        this.clintPool.returnChannel(client);
    }

    /**
     * 关闭通道
     *
     * @param client 通道
     */
    protected void closeClient(SFTPClient client) {
        IOUtil.closeQuietly(client);
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
        SFTPClient client = this.takeSFTPClient();
        try {
            client.rm(file);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.rmdir(dir);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.rmdir(dir);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.mkdir(filePath);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
        return true;
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.mkdirs(filePath);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
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
        SFTPClient client = this.takeSFTPClient();
        try {
            return client.stat(filePath);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.stat(filePath);
        } catch (SFTPException ex) {
            if (ex.getStatusCode() == Response.StatusCode.NO_SUCH_FILE || ex.getStatusCode() == Response.StatusCode.NO_SUCH_PATH) {
                return false;
            }
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
        return true;
    }

    public String realpath(String filePath) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            return client.readlink(filePath);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
        LocalSourceFile file = ShellSFTPUtil.emptyFile(filePath);
        SFTPClient client = this.takeSFTPClient();
        try {
            client.put(file, filePath);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        LocalSourceFile file = ShellSFTPUtil.streamFile(localFile, remoteFile);
        SFTPClient client = this.takeSFTPClient();
        try {
            client.put(file, remoteFile);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(ShellSFTPFile remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.get(remoteFile.getFilePath(), localFile);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public InputStream getStream(ShellSFTPFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        try (SFTPClient client = this.takeSFTPClient()) {
            RemoteFile file = client.open(remoteFile.getFilePath());
            RemoteFile.RemoteFileInputStream inputStream = file.new RemoteFileInputStream();
            return inputStream;
        }
    }

    @Override
    public boolean chmod(int permission, String filePath) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.chmod(filePath, permission);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
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
        SFTPClient client = this.takeSFTPClient();
        try {
            List<RemoteResourceInfo> infos = client.ls(filePath);
            List<ShellSFTPFile> files = new ArrayList<>();
            for (RemoteResourceInfo info : infos) {
                ShellSFTPFile file = new ShellSFTPFile(filePath, info);
                files.add(file);
            }
            return files;
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        SFTPClient client = this.takeSFTPClient();
        try {
            client.rename(filePath, newPath);
        } catch (Exception ex) {
            this.closeClient(client);
            throw ex;
        } finally {
            this.returnSFTPClient(client);
        }
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

    public SFTPClient newSFTPClient() throws IOException {
        return this.sshClient.newSFTPClient();
    }
}
