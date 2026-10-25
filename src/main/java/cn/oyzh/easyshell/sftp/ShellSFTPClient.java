package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
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
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.ssh.ShellBaseSSHClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private final ConcurrentLinkedQueue<ShellSFTPChannel> delayChannels = new ConcurrentLinkedQueue<>();

    private final boolean ownsSession;

    private Supplier<Session> sessionSupplier;

    /**
     * 连接状态
     */
    private final ObjectProperty<ShellConnState> state = new SimpleObjectProperty<>(ShellConnState.NOT_INITIALIZED);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> super.onStateChanged(state3);

    public ShellSFTPClient(ShellConnect shellConnect) {
        this(shellConnect, null);
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session) {
        this(shellConnect, session, session == null);
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session, boolean ownsSession) {
        this(shellConnect, session, ownsSession, new Semaphore(MAX_DYNAMIC_CHANNELS, true));
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session, boolean ownsSession,
                           Semaphore dynamicChannelPermits) {
        super(shellConnect, dynamicChannelPermits);
        this.ownsSession = ownsSession;
        this.session = session;
        this.cache = new ShellSFTPCache();
        this.channelPool = new ShellSFTPChannelPool(this);
        if (this.isConnected()) {
            this.state.set(ShellConnState.CONNECTED);
        }
        super.addStateListener(this.stateListener);
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session, ShellSFTPCache cache, ShellSFTPChannelPool pool) {
        super(shellConnect);
        this.ownsSession = session == null;
        this.cache = cache;
        this.session = session;
        this.channelPool = pool;
        if (this.isConnected()) {
            this.state.set(ShellConnState.CONNECTED);
        }
        super.addStateListener(this.stateListener);
    }

    public void setSessionSupplier(Supplier<Session> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }

    @Override
    protected synchronized void ensureSessionConnected(int timeout) throws Exception {
        if (this.ownsSession) {
            super.ensureSessionConnected(timeout);
            return;
        }
        Session activeSession = this.sessionSupplier == null ? this.session : this.sessionSupplier.get();
        if (activeSession == null || !activeSession.isConnected()) {
            throw new JSchException("ssh session is disconnected");
        }
        this.session = activeSession;
    }

    @Override
    public void close() {
        try {
            this.closeDelayResources();
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
            if (this.ownsSession && this.session != null) {
                this.session.disconnect();
            }
            this.session = null;
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            JulLog.warn("shellSFTPClient close error", ex);
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
            if (this.ownsSession) {
                this.connectSession(timeout);
            } else {
                Session activeSession = this.sessionSupplier == null ? this.session : this.sessionSupplier.get();
                if (activeSession == null || !activeSession.isConnected()) {
                    throw new ShellException("ssh session is disconnected");
                }
                this.session = activeSession;
                this.initSession();
            }
            if (this.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            if (JulLog.isInfoEnabled()) {
                JulLog.info("shellSFTPClient connected used:{}ms.", (endTime - starTime));
            }
        } catch (Throwable ex) {
            if (this.ownsSession && this.session != null) {
                this.session.disconnect();
                this.session = null;
            }
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellSFTPClient start error", ex);
            throw new ShellException(ex);
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    /**
     * 创建sftp通道
     *
     * @return 通道
     */
    protected ShellSFTPChannel newSFTPChannel() {
        return this.newSFTPChannel(3);
    }

    /**
     * 创建sftp通道
     *
     * @param maxRetry 最大重试次数
     * @return 通道
     */
    protected ShellSFTPChannel newSFTPChannel(int maxRetry) {
        int retry = Math.max(0, maxRetry);
        while (true) {
            try {
                return this.openSFTPChannel();
            } catch (Exception ex) {
                if (retry-- > 0 && this.isRecoverableChannelFailure(ex)) {
                    JulLog.warn("create sftp channel failed, retry remaining:{}", retry + 1);
                    ThreadUtil.sleep(50L * (maxRetry - retry));
                    continue;
                }
                throw ex instanceof ShellException ? (ShellException) ex : new ShellException(ex);
            }
        }
    }

    private ShellSFTPChannel openSFTPChannel() throws Exception {
        if (this.channelPool == null) {
            throw new ShellException("sftp client is closed");
        }
        ChannelSftp channel = null;
        ShellSFTPChannel sftpChannel = null;
        boolean permitAcquired = false;
        try {
            Session activeSession = this.sessionSupplier == null ? this.session : this.sessionSupplier.get();
            if (activeSession == null || !activeSession.isConnected()) {
                if (!this.ownsSession) {
                    throw new ShellException("ssh session is disconnected");
                }
                this.ensureSessionConnected(this.connectTimeout());
                activeSession = this.session;
            }
            if (activeSession == null || !activeSession.isConnected()) {
                throw new ShellException("ssh session is disconnected");
            }
            this.session = activeSession;
            this.initSession();
            permitAcquired = this.acquireDynamicChannel();
            if (!permitAcquired) {
                throw new ShellException("sftp channel acquire timeout");
            }
            channel = (ChannelSftp) activeSession.openChannel("sftp");
            if (channel == null) {
                throw new ShellException("create sftp channel returned null");
            }
            channel.setInputStream(null);
            channel.setOutputStream(null);
            // 设置字符集
            channel.setFilenameEncoding(this.getCharset());
            // 创建通道
            sftpChannel = new ShellSFTPChannel(channel, this::releaseDynamicChannel);
            permitAcquired = false;
            // 通道参数必须在连接前设置
            super.initEnvironments(channel);
            // 连接
            sftpChannel.connect(this.connectTimeout());
            return sftpChannel;
        } catch (Exception ex) {
            if (sftpChannel != null) {
                sftpChannel.close();
            } else if (channel != null) {
                channel.disconnect();
            }
            if (permitAcquired) {
                this.releaseDynamicChannel();
            }
            throw ex;
        }
    }

    private boolean isRecoverableChannelFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String value = message.toLowerCase();
                if (value.contains("session is down")
                        || value.contains("session is not connected")
                        || value.contains("session is disconnected")
                        || value.contains("channel is not opened")
                        || value.contains("channel is closed")
                        || value.contains("connection is closed")
                        || value.contains("broken pipe")
                        || value.contains("socket closed")
                        || value.contains("connection reset")
                        || value.contains("end of io stream")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * 获取通道
     *
     * @return 通道
     */
    protected ShellSFTPChannel takeChannel() {
        ShellSFTPChannel channel = this.channelPool == null ? null : this.channelPool.borrowObject();
        if (channel == null) {
            throw new ShellException("sftp channel is unavailable");
        }
        return channel;
    }

    /**
     * 返回通道
     *
     * @param channel 通道
     */
    protected void returnChannel(ShellSFTPChannel channel) {
        if (channel == null) {
            return;
        }
        if (this.channelPool == null) {
            channel.close();
            return;
        }
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
        ShellClientActionUtil.forAction(this.connectName(), "rm " + file);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rm(file);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rmdir " + dir);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rmdir(dir);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public String resolvePath(String file) {
        return ShellSFTPUtil.resolvePath(file, this::workDir);
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rm -r " + dir);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            this.deleteDirRecursive(channel, dir);
        } finally {
            this.returnChannel(channel);
        }
    }

    private void deleteDirRecursive(ShellSFTPChannel channel, String dir) throws SftpException {
        Vector<ChannelSftp.LsEntry> entries = channel.ls(dir);
        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (!ShellFileUtil.isNormal(filename)) {
                continue;
            }
            String fullPath = ShellFileUtil.concat(dir, filename);
            if (entry.getAttrs().isDir()) {
                this.deleteDirRecursive(channel, fullPath);
            } else {
                channel.rm(fullPath);
            }
        }
        channel.rmdir(dir);
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "mkdir " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.mkdir(filePath);
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "mkdir -p " + filePath);
        filePath = this.resolvePath(filePath);
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

    private String pwd;

    @Override
    public String workDir() throws Exception {
        if (this.pwd == null) {
            ShellClientActionUtil.forAction(this.connectName(), "pwd");
            ShellSFTPChannel channel = this.takeChannel();
            try {
                this.pwd = channel.pwd();
            } finally {
                this.returnChannel(channel);
            }
        }
        return this.pwd;
    }

    @Override
    public void cd(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "cd " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.cd(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    public SftpATTRS stat(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "stat " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        if (channel == null) {
            return null;
        }
        try {
            return channel.stat(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
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
        ShellClientActionUtil.forAction(this.connectName(), "realpath " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.realpath(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "touch " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.touch(filePath);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void put(File localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        ShellSFTPChannel channel = this.takeChannel();
        try {
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
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        ShellSFTPChannel channel = this.takeChannel();
        try {
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
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        ShellSFTPChannel channel = this.newSFTPChannel();
        boolean handedOver = false;
        try {
            OutputStream stream = callback == null ? channel.put(remoteFile) : ShellFileProgressMonitor.of(channel.put(remoteFile), callback);
            this.delayChannels.add(channel);
            handedOver = true;
            return stream;
        } finally {
            if (!handedOver) {
                channel.close();
            }
        }
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
        ShellClientActionUtil.forAction(this.connectName(), "get " + fPath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
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
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        ShellSFTPChannel channel = this.newSFTPChannel();
        boolean handedOver = false;
        try {
            String fPath = remoteFile.getFilePath();
            InputStream stream = callback == null ? channel.get(fPath) : ShellFileProgressMonitor.of(channel.get(fPath), callback);
            this.delayChannels.add(channel);
            handedOver = true;
            return stream;
        } finally {
            if (!handedOver) {
                channel.close();
            }
        }
    }

    @Override
    public boolean chmod(int permission, String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "chmod " + filePath + " " + permission);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.chmod(permission, filePath);
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public boolean chmodRecursive(int permissions, ShellSFTPFile file) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "chmod -R " + file.getFilePath() + " " + permissions);
        String filePath = file.getFilePath();
        if (!this.isWindows()) {
            String safePath = ShellFileUtil.fixFilePath(filePath).replace("'", "'\"'\"'");
            String output = this.exec("chmod " + permissions + " -R -- '" + safePath + "'");
            if (StringUtil.isEmpty(output)) {
                return true;
            }
        }
        return ShellFileClient.super.chmodRecursive(permissions, file);
    }

    @Override
    public ShellSFTPFile fileInfo(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            String fixedPath = this.resolvePath(filePath);
            SftpATTRS attrs = channel.stat(fixedPath);
            if (attrs == null) {
                return null;
            }
            String pPath = ShellFileUtil.parent(fixedPath);
            String fName = ShellFileUtil.name(fixedPath);
            ShellSFTPFile file = new ShellSFTPFile(pPath, fName, attrs);
            // 读取链接文件
            this.cache.realpath(file, channel);
            file.setOwner(this.cache.getOwner(file.getUid(), this));
            file.setGroup(this.cache.getGroup(file.getGid(), this));
            return file;
        } finally {
            this.returnChannel(channel);
        }
    }

    // @Override
    // public List<ShellSFTPFile> lsFile(String filePath) throws Exception {
    //     ShellSFTPChannel channel = this.takeChannel();
    //     try {
    //         return channel.lsFile(filePath);
    //         // } catch (Exception ex) {
    //         //     this.closeChannel(channel);
    //         //     throw ex;
    //     } finally {
    //         this.returnChannel(channel);
    //     }
    // }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellSFTPFile> fileCallback) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "ls " + filePath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.lsFile(filePath, fileCallback);
        } finally {
            this.returnChannel(channel);
        }
    }

    public boolean rename(String filePath, String newPath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rename " + filePath + " " + newPath);
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rename(filePath, newPath);
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public boolean rename(ShellSFTPFile file, String newName) throws Exception {
        String filePath = file.getFilePath();
        String newPath = ShellFileUtil.concat(file.getParentPath(), newName);
        return this.rename(filePath, newPath);
    }

    public void destroy() {
        this.close();
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
    private final Competitor transportCompetitor = new Competitor(2);

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

    // @Deprecated
    // private SftpProgressMonitor newMonitor(Function<Long, Boolean> callback) {
    //     return callback == null ? null : new SftpProgressMonitor() {
    //         @Override
    //         public void init(int i, String s, String s1, long l) {
    //
    //         }
    //
    //         @Override
    //         public boolean count(long l) {
    //             return callback.apply(l);
    //         }
    //
    //         @Override
    //         public void end() {
    //
    //         }
    //     };
    // }

    @Override
    public ShellFileClient<ShellSFTPFile> forkClient() {
        // try {
        //     ShellSFTPClient sftpClient = new ShellSFTPClient(this.shellConnect, null) {
        //         // ShellSFTPClient sftpClient = new ShellSFTPClient(this.shellConnect, null, this.realpathCache, this.sftpChannelPool) {
        //         @Override
        //         public boolean isForked() {
        //             return true;
        //         }
        //     };
        //     sftpClient.start();
        //     return sftpClient;
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        // }
        return this;
    }

    @Override
    public boolean isCdSupport() {
        return false;
    }
}
