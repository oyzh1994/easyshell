package cn.oyzh.easyshell.sftp;

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
import cn.oyzh.easyshell.ssh.ShellBaseSSHClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.ssh.ShellSSHAuthUserInfo;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

/**
 * sftp客户端
 *
 * @author oyzh
 * @since 2025/04/16
 */
public class ShellSFTPClient extends ShellBaseSSHClient implements ShellFileClient<ShellSFTPFile> {

    /**
     * 通道管理器
     */
    private ShellSFTPChannelManager channelManager;

    /**
     * 链接管理器
     */
    private ShellSFTPRealpathManager realpathManager;

    /**
     * 延迟处理的文件通道
     */
    private final List<ShellSFTPChannel> delayChannels = new ArrayList<>();

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

    public ShellSFTPClient(ShellConnect shellConnect, Session session) {
        this.shellConnect = shellConnect;
        this.session = session;
        super.addStateListener(this.stateListener);
        this.channelManager = new ShellSFTPChannelManager(this);
        this.realpathManager = new ShellSFTPRealpathManager(this);
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
        this.session.setUserInfo(new ShellSSHAuthUserInfo(this.shellConnect.getPassword()));
        // 去掉首次连接确认
        this.session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    public void close() {
        try {
            // 销毁会话
            if (this.session != null && this.session.isConnected()) {
                this.session.disconnect();
                this.session = null;
            }
            // 清除用户、分组信息
            if (this.attr != null) {
                this.attr.clear();
                this.attr = null;
            }
            // 通道管理
            if (this.channelManager != null) {
                IOUtil.close(this.channelManager);
                this.channelManager = null;
            }
            // 链接路径
            if (this.realpathManager != null) {
                IOUtil.close(this.realpathManager);
                this.realpathManager = null;
            }
            this.delayChannels.clear();
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
//            this.shellConnect = null;
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
                this.state.set(ShellConnState.CONNECTING);
                // 连接超时
                this.session.setTimeout(timeout);
                // 连接
                this.session.connect(timeout);
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
        return this.session != null && this.session.isConnected();
    }

    /**
     * 创建新通道
     *
     * @return 通道
     */
    protected ShellSFTPChannel newChannel() {
        try {
            // 如果会话关了，则先启动会话
            if (this.isClosed()) {
                this.session.connect(this.connectTimeout());
            }
            ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
            // 设置字符集
            channel.setFilenameEncoding(this.getCharset());
            ShellSFTPChannel sftpChannel = new ShellSFTPChannel(channel, this.realpathManager);
            sftpChannel.connect(this.connectTimeout());
            return sftpChannel;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取通道
     *
     * @return 通道
     */
    protected ShellSFTPChannel takeChannel() {
        return this.channelManager.borrowChannel();
    }

    /**
     * 返回通道
     *
     * @param channel 通道
     */
    protected void returnChannel(ShellSFTPChannel channel) {
        this.channelManager.returnChannel(channel);
    }

    /**
     * 关闭通道
     *
     * @param channel 通道
     */
    protected void closeChannel(ShellSFTPChannel channel) {
        if (channel != null&& channel.isConnected()) {
            channel.close();
        }
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
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.rm(file);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rm(file);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDir(String dir) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.rmdir(dir);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rmdir(dir);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            Vector<ChannelSftp.LsEntry> entries = channel.ls(dir);
            for (ChannelSftp.LsEntry entry : entries) {
                String filename = entry.getFilename();
                if (ShellFileUtil.isNormal(filename)) {
                    String fullPath = dir + "/" + filename;
                    if (entry.getAttrs().isDir()) {
                        this.deleteDirRecursive(fullPath);
                    } else {
                        this.delete(fullPath);
                    }
                }
            }
            this.deleteDir(dir);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.mkdir(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.mkdir(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        filePath = ShellFileUtil.fixFilePath(filePath);
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
            } catch (SftpException ex) {
                // 创建缺失目录
                if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    this.createDir(currentPath.toString());
                } else {
                    throw ex;
                }
            }
        }
    }

    @Override
    public String workDir() throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            return channel.pwd();
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.pwd();
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void cd(String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.cd(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.cd(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    public SftpATTRS stat(String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            return channel.stat(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.stat(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            return channel.exist(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.exist(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    public String realpath(String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            return channel.realpath(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.realpath(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.touch(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.touch(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        try (ShellSFTPChannel channel = this.newChannel()) {
            channel.put(localFile, remoteFile, this.newMonitor(callback), ChannelSftp.OVERWRITE);
        }
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellSFTPChannel channel = this.newChannel();
        this.delayChannels.add(channel);
        return channel.put(remoteFile, this.newMonitor(callback));
    }

    @Override
    public void get(ShellSFTPFile remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        try (ShellSFTPChannel channel = this.newChannel()) {
            channel.get(remoteFile.getFilePath(), localFile, this.newMonitor(callback), ChannelSftp.OVERWRITE);
        }
    }

    @Override
    public InputStream getStream(ShellSFTPFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellSFTPChannel channel = this.newChannel();
        this.delayChannels.add(channel);
        return channel.get(remoteFile.getFilePath(), this.newMonitor(callback));
    }

    @Override
    public boolean chmod(int permission, String filePath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.chmod(permission, filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.chmod(permission, filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public ShellSFTPFile fileInfo(String filePath) throws Exception {
        SftpATTRS attrs = this.stat(filePath);
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
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            return channel.lsFileNormal(filePath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.lsFileNormal(filePath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
//        try (ShellSFTPChannel channel = this.newChannel()) {
//            channel.rename(filePath, newPath);
//        }
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rename(filePath, newPath);
        } catch (Exception ex) {
            this.closeChannel(channel);
            throw ex;
        } finally {
            this.returnChannel(channel);
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
        for (ShellSFTPChannel delayChannel : this.delayChannels) {
            if (delayChannel != null) {
                delayChannel.close();
            }
        }
        this.delayChannels.clear();
    }

    private SftpProgressMonitor newMonitor(Function<Long, Boolean> callback) {
        return callback == null ? null : new SftpProgressMonitor() {
            @Override
            public void init(int i, String s, String s1, long l) {

            }

            @Override
            public boolean count(long l) {
                return callback.apply(l);
            }

            @Override
            public void end() {

            }
        };
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
