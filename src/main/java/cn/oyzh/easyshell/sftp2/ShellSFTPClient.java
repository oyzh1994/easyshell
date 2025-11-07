package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
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
import cn.oyzh.easyshell.ssh2.ShellBaseSSHClient;
import cn.oyzh.easyshell.ssh2.ShellSSHJGitClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * sftp客户端
 *
 * @author oyzh
 * @since 2025/04/16
 */
public class ShellSFTPClient extends ShellBaseSSHClient implements ShellFileClient<ShellSFTPFile> {

    /**
     * 缓存管理器
     */
    private ShellSFTPCache cache;

    /**
     * sftp通道池
     */
    private ShellSFTPChannelPool channelPool;

    /**
     * 延迟处理的文件通道
     */
    private final List<ShellSFTPChannel> delayChannels = new ArrayList<>();

    public ShellSFTPClient(ShellConnect shellConnect) {
        this(shellConnect, null, null);
    }

    public ShellSFTPClient(ShellConnect shellConnect, ShellSSHJGitClient sshClient, ClientSession session) {
        super(shellConnect);
        this.session = session;
        this.sshClient = sshClient;
        this.cache = new ShellSFTPCache();
        this.channelPool = new ShellSFTPChannelPool(this);
        super.addStateListener(this.stateListener);
    }

    @Override
    public void close() {
        try {
            // 通道管理
            if (this.channelPool != null) {
                IOUtil.close(this.channelPool);
                this.channelPool = null;
            }
            // 链接路径
            if (this.cache != null) {
                IOUtil.close(this.cache);
                this.cache = null;
            }
            super.close();
            this.closeDelayResources();
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("SFTP client close error.", ex);
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
            // 初始化客户端
            if (this.sshClient == null) {
                this.state.set(ShellConnState.CONNECTING);
                this.initClient(timeout);
            }
            if (this.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            if (JulLog.isInfoEnabled()) {
                JulLog.info("SFTP client connected used:{}ms.", (endTime - starTime));
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("SFTP client start error", ex);
            throw new ShellException(ex);
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    protected void initClient(int timeout) throws Exception {
        super.initClient(timeout);
        SftpModuleProperties.SFTP_CHANNEL_OPEN_TIMEOUT.set(this.sshClient, Duration.ofMillis(timeout));
    }

    /**
     * 创建sftp通道
     *
     * @return 通道
     */
    protected ShellSFTPChannel newSFTPChannel() throws Exception {
        // 获取会话
        ClientSession session = this.takeSession(this.connectTimeout());
        // 创建客户端
        SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(session);
        // 设置字符集
        sftpClient.setNameDecodingCharset(this.getCharset());
        // 创建通道
        return new ShellSFTPChannel(sftpClient);
    }

    /**
     * 获取通道
     *
     * @return 通道
     */
    protected ShellSFTPChannel takeChannel() {
        return this.channelPool.borrowObject();
    }

    /**
     * 返回通道
     *
     * @param channel 通道
     */
    protected void returnChannel(ShellSFTPChannel channel) {
        this.channelPool.returnObject(channel);
    }


    /**
     * 获取用户名称
     *
     * @param uid 用户id
     * @return 结果
     */
    public String exec_id_un(int uid) {
        return this.exec("id -un " + uid);
    }

    /**
     * 获取分组名称
     *
     * @param gid 分组id
     * @return 结果
     */
    public String exec_id_gn(int gid) {
        return this.exec("id -gn " + gid);
    }

    @Override
    public void delete(String file) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "rm " + file);
            channel.rm(file);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "rmdir " + dir);
            channel.rmdir(dir);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        dir = ShellFileUtil.fixFilePath(dir);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            Iterable<SftpClient.DirEntry> entries = channel.ls(dir);
            for (SftpClient.DirEntry entry : entries) {
                String filename = entry.getFilename();
                if (ShellFileUtil.isNormal(filename)) {
                    String fullPath = dir + "/" + filename;
                    if (entry.getAttributes().isDirectory()) {
                        this.deleteDirRecursive(fullPath);
                    } else {
                        this.delete(fullPath);
                    }
                }
            }
            this.deleteDir(dir);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "mkdir " + filePath);
            channel.mkdir(filePath);
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    // @Override
    // public void createDirRecursive(String filePath) throws Exception {
    //     filePath = ShellFileUtil.fixFilePath(filePath);
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
    //             if (ExceptionUtil.hasMessage(ex, "No such file")) {
    //                 this.createDir(currentPath.toString());
    //             } else {
    //                 throw ex;
    //             }
    //         }
    //     }
    // }

    @Override
    public String workDir() throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "pwd");
            return channel.pwd();
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void cd(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    public SftpClient.Attributes stat(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        if (channel == null) {
            return null;
        }
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "stat " + filePath);
            return channel.stat(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "exist " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.exist(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public String realpath(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "realpath " + filePath);
            return channel.realpath(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "touch " + filePath);
            channel.touch(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void put(File localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellSFTPChannel channel = this.newSFTPChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
            if (callback == null) {
                channel.put(localFile.getPath(), remoteFile);
            } else {
                channel.put(ShellFileProgressMonitor.of(new FileInputStream(localFile), callback), remoteFile);
            }
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellSFTPChannel channel = this.newSFTPChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
            if (callback == null) {
                channel.put(localFile, remoteFile);
            } else {
                channel.put(ShellFileProgressMonitor.of(localFile, callback), remoteFile);
            }
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        ShellSFTPChannel channel = this.newSFTPChannel();
        this.delayChannels.add(channel);
        if (callback == null) {
            return channel.write(remoteFile);
        }
        return ShellFileProgressMonitor.of(channel.write(remoteFile), callback);
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
    public void get(ShellSFTPFile remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        String fPath = remoteFile.getFilePath();
        ShellSFTPChannel channel = this.newSFTPChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "get " + fPath);
            if (callback == null) {
                channel.get(fPath, localFile);
            } else {
                channel.get(fPath, ShellFileProgressMonitor.of(new FileOutputStream(localFile), callback));
            }
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public InputStream getStream(ShellSFTPFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        ShellSFTPChannel channel = this.newSFTPChannel();
        this.delayChannels.add(channel);
        String fPath = remoteFile.getFilePath();
        if (callback == null) {
            return channel.get(fPath);
        }
        return ShellFileProgressMonitor.of(channel.get(fPath), callback);
    }

    @Override
    public boolean chmod(int permissions, String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "chmod " + filePath + " " + permissions);
            channel.chmod(permissions, filePath);
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public boolean chmodRecursive(int permissions, ShellSFTPFile file) throws Exception {
        boolean result = false;
        try {
            String output = this.exec("chmod " + permissions + " -R " + file.getFilePath());
            result = StringUtil.isEmpty(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!result) {
            return ShellFileClient.super.chmodRecursive(permissions, file);
        }
        return result;
    }

    @Override
    public ShellSFTPFile fileInfo(String filePath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "fileInfo " + filePath);
        SftpClient.Attributes attrs = this.stat(filePath);
        if (attrs == null) {
            return null;
        }
        filePath = ShellFileUtil.fixFilePath(filePath);
        String pPath = ShellFileUtil.parent(filePath);
        String fName = ShellFileUtil.name(filePath);
        if (attrs.getOwner() == null) {
            attrs.setOwner(this.cache.getOwner(attrs.getUserId(), this));
        }
        if (attrs.getGroup() == null) {
            attrs.setGroup(this.cache.getGroup(attrs.getGroupId(), this));
        }
        ShellSFTPFile file = new ShellSFTPFile(pPath, fName, attrs);
        //// 读取链接文件
        // this.cache.realpath(file, this::takeChannel);
        return file;
    }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellSFTPFile> fileCallback) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "ls " + filePath);
            channel.lsFile(filePath, fileCallback);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean rename(ShellSFTPFile file, String newName) throws Exception {
        String filePath = file.getFilePath();
        String newPath = ShellFileUtil.concat(file.getParentPath(), newName);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), "rename " + filePath + " " + newPath);
            channel.rename(filePath, newPath);
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    /**
     * 上传竞争器
     */
    private final Competitor uploadCompetitor = new Competitor(2);

    @Override
    public Competitor uploadCompetitor() {
        return this.uploadCompetitor;
    }

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    @Override
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

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return downloadTasks;
    }

    /**
     * 传输竞争器
     */
    private final Competitor transportCompetitor = new Competitor(1);

    @Override
    public Competitor transportCompetitor() {
        return transportCompetitor;
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

    @Override
    public ShellFileClient<ShellSFTPFile> forkClient() {
        return this;
    }

    @Override
    public boolean isCdSupport() {
        return false;
    }
}
