package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileProgressMonitor;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.ssh.ShellBaseSSHClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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
     * sftp通道池
     */
    private ShellSFTPChannelPool channelPool;

    /**
     * 链接管理器
     */
    private ShellSFTPRealpathCache realpathCache;

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
        super(shellConnect);
        this.session = session;
        this.realpathCache = new ShellSFTPRealpathCache();
        this.channelPool = new ShellSFTPChannelPool(this);
        super.addStateListener(this.stateListener);
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session, ShellSFTPRealpathCache realpathCache, ShellSFTPChannelPool pool) {
        super(shellConnect);
        this.session = session;
        this.channelPool = pool;
        this.realpathCache = realpathCache;
        super.addStateListener(this.stateListener);
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
                IOUtil.close(this.attr);
                this.attr = null;
            }
            // 通道管理
            if (this.channelPool != null) {
                IOUtil.close(this.channelPool);
                this.channelPool = null;
            }
            // 链接路径
            if (this.realpathCache != null) {
                IOUtil.close(this.realpathCache);
                this.realpathCache = null;
            }
            this.delayChannels.clear();
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
        } catch (Throwable ex) {
            ex.printStackTrace();
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
        try {
            // 如果会话关了，则重新初始化客户端
            if (this.isClosed()) {
                this.session.connect(this.connectTimeout());
            }
            ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
            channel.setInputStream(null);
            channel.setOutputStream(null);
            // 设置字符集
            channel.setFilenameEncoding(this.getCharset());
            // 创建通道
            ShellSFTPChannel sftpChannel = new ShellSFTPChannel(channel, this.realpathCache);
            // 连接
            sftpChannel.connect(this.connectTimeout());
            // 初始化环境
            super.initEnvironments(channel);
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

    // /**
    //  * 关闭通道
    //  *
    //  * @param channel 通道
    //  */
    // protected void closeChannel(ShellSFTPChannel channel) {
    //     if (channel != null && channel.isConnected()) {
    //         channel.close();
    //     }
    // }

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

    /**
     * 属性
     */
    private ShellSFTPAttr attr;

    public ShellSFTPAttr getAttr() {
        if (this.attr == null) {
            this.attr = new ShellSFTPAttr();
        }
        return this.attr;
    }

    @Override
    public void delete(String file) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rm(file);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rmdir(dir);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
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
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.mkdir(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
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
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.pwd();
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void cd(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.cd(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    public SftpATTRS stat(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        if (channel == null) {
            return null;
        }
        try {
            return channel.stat(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.exist(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public String realpath(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.realpath(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.touch(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void put(File localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellSFTPChannel channel = this.newSFTPChannel();
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
        ShellSFTPChannel channel = this.newSFTPChannel();
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
        ShellSFTPChannel channel = this.newSFTPChannel();
        this.delayChannels.add(channel);
        if (callback == null) {
            return channel.put(remoteFile);
        }
        return ShellFileProgressMonitor.of(channel.put(remoteFile), callback);
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
        ShellSFTPChannel channel = this.newSFTPChannel();
        this.delayChannels.add(channel);
        String fPath = remoteFile.getFilePath();
        if (callback == null) {
            return channel.get(fPath);
        }
        return ShellFileProgressMonitor.of(channel.get(fPath), callback);
    }

    @Override
    public boolean chmod(int permission, String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.chmod(permission, filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
        return true;
    }

    @Override
    public ShellSFTPFile fileInfo(String filePath) throws Exception {
        SftpATTRS attrs = this.stat(filePath);
        if (attrs == null) {
            return null;
        }
        String pPath = ShellFileUtil.parent(filePath);
        String fName = ShellFileUtil.name(filePath);
        ShellSFTPFile file = new ShellSFTPFile(pPath, fName, attrs);
        // 读取链接文件
        this.realpathCache.realpath(file, this::takeChannel);
        // ShellSFTPUtil.realpath(file, this);
        // 拥有者、分组
        // if (this.isWindows()) {
        //     file.setOwner("-");
        //     file.setGroup("-");
        // } else {
        file.setOwner(ShellSFTPUtil.getOwner(file.getUid(), this));
        file.setGroup(ShellSFTPUtil.getGroup(file.getGid(), this));
        // }
        return file;
    }

    @Override
    public List<ShellSFTPFile> lsFile(String filePath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            return channel.lsFile(filePath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellSFTPFile> fileCallback ) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.lsFile(filePath, fileCallback);
        } finally {
            this.returnChannel(channel);
        }
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        ShellSFTPChannel channel = this.takeChannel();
        try {
            channel.rename(filePath, newPath);
            // } catch (Exception ex) {
            //     this.closeChannel(channel);
            //     throw ex;
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
}
