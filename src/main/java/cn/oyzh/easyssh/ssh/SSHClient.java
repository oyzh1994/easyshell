package cn.oyzh.easyssh.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.docker.DockerExec;
import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.domain.SSHX11Config;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SSHSftpManager;
import cn.oyzh.easyssh.sftp.SftpAttr;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.delete.SftpDeleteDeleted;
import cn.oyzh.easyssh.sftp.delete.SftpDeleteEnded;
import cn.oyzh.easyssh.sftp.delete.SftpDeleteManager;
import cn.oyzh.easyssh.sftp.download.SftpDownloadCanceled;
import cn.oyzh.easyssh.sftp.download.SftpDownloadChanged;
import cn.oyzh.easyssh.sftp.download.SftpDownloadEnded;
import cn.oyzh.easyssh.sftp.download.SftpDownloadFailed;
import cn.oyzh.easyssh.sftp.download.SftpDownloadInPreparation;
import cn.oyzh.easyssh.sftp.download.SftpDownloadManager;
import cn.oyzh.easyssh.sftp.upload.SftpUploadCanceled;
import cn.oyzh.easyssh.sftp.upload.SftpUploadChanged;
import cn.oyzh.easyssh.sftp.upload.SftpUploadEnded;
import cn.oyzh.easyssh.sftp.upload.SftpUploadFailed;
import cn.oyzh.easyssh.sftp.upload.SftpUploadInPreparation;
import cn.oyzh.easyssh.sftp.upload.SftpUploadManager;
import cn.oyzh.easyssh.store.SSHX11ConfigStore;
import cn.oyzh.easyssh.x11.X11Manager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * ssh终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class SSHClient {

    /**
     * JSch对象
     */
    private static final JSch JSCH = new JSch();

    /**
     * ssh信息
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private final SSHConnect sshConnect;

    /**
     * ssh会话
     */
    @Getter
    private Session session;

    /**
     * x11配置存储
     */
    private final SSHX11ConfigStore x11ConfigStore = SSHX11ConfigStore.INSTANCE;

    /**
     * 静默关闭标志位
     */
    private boolean closeQuietly;

    public SSHClient(@NonNull SSHConnect sshConnect) {
        this.sshConnect = sshConnect;
        // 监听连接状态
        this.stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CLOSED -> {
                    if (!this.closeQuietly) {
                        SSHEventUtil.connectionClosed(this);
                    }
                }
                case CONNECTED -> SSHEventUtil.connectionConnected(this);
                default -> {

                }
            }
        });
    }

    /**
     * 连接状态
     */
    @Getter
    private final ReadOnlyObjectWrapper<SSHConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    public SSHConnState state() {
        return this.stateProperty().get();
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    public ReadOnlyObjectProperty<SSHConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    /**
     * 添加连接状态监听器
     *
     * @param stateListener 监听器
     */
    public void addStateListener(ChangeListener<SSHConnState> stateListener) {
        if (stateListener != null) {
            this.stateProperty().addListener(stateListener);
        }
    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.sshConnect.getUser(), this.sshConnect.getPassword(), this.sshConnect.getHost());
        }
        // 创建会话
        this.session = JSCH.getSession(this.sshConnect.getUser(), this.sshConnect.hostIp(), this.sshConnect.hostPort());
        // 主机密码
        if (StringUtil.isNotBlank(this.sshConnect.getPassword())) {
            this.session.setPassword(this.sshConnect.getPassword());
        }
        // 配置参数
        Properties config = new Properties();
        // 设置终端类型
        config.put("term", "xterm-256color");
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
        // 设置配置
        this.session.setConfig(config);
        // 启用X11转发
        if (this.sshConnect.isX11forwarding()) {
            // x11配置
            SSHX11Config x11Config = this.sshConnect.getX11Config();
            // 获取x11配置
            if (x11Config == null) {
                x11Config = this.x11ConfigStore.getByIid(this.sshConnect.getId());
            }
            if (x11Config != null) {
                // x11配置
                this.session.setConfig("ForwardX11", "yes");
                this.session.setConfig("ForwardX11Trusted", "yes");
                this.session.setX11Host(x11Config.getHost());
                this.session.setX11Port(x11Config.getPort());
                // 本地转发，启动x11服务
                if (x11Config.isLocal()) {
                    X11Manager.startXServer();
                }
            } else {
                throw new RuntimeException("X11forwarding is enable but x11config is null");
            }
        }
        // 超时连接
        this.session.setTimeout(this.sshConnect.connectTimeOutMs());
    }

    /**
     * 关闭客户端，静默模式
     */
    public void closeQuiet() {
        this.closeQuietly = true;
        this.close();
    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            this.sftpManager.close();
            if (this.shell != null) {
                this.shell.close();
                this.shell = null;
            }
            if (this.session != null) {
                this.session.disconnect();
                this.session = null;
                this.state.set(SSHConnState.CLOSED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    /**
//     * 重置客户端
//     */
//    public void reset() {
//        // 移除监听器
//        if (!this.connStateListeners.isEmpty()) {
//            for (ChangeListener<SSHConnState> listener : connStateListeners) {
//                this.stateProperty().removeListener(listener);
//            }
//            this.connStateListeners.clear();
//        }
//        this.close();
//        this.state.set(SSHConnState.NOT_INITIALIZED);
//    }

    /**
     * 开始连接客户端
     */
    public void start() throws Exception {
        this.start(this.connectTimeout());
    }

    /**
     * 开始连接客户端
     *
     * @param timeout 超时时间
     */
    public void start(int timeout) throws Exception {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        // 初始化客户端
        this.initClient();
        try {
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 初始化连接池
            this.state.set(SSHConnState.CONNECTING);
            // 执行连接
            this.session.connect(timeout);
            // 判断连接结果
            if (this.session.isConnected()) {
                this.state.set(SSHConnState.CONNECTED);
            } else if (this.state.get() == SSHConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(SSHConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("sshClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(SSHConnState.FAILED);
            throw ex;
        }
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        if (!this.isClosed()) {
            return this.state.get() == SSHConnState.CONNECTING;
        }
//        this.state.set(SSHConnState.CLOSED);
        return false;
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        if (!this.isClosed()) {
            return this.state.get().isConnected();
        }
//        this.state.set(SSHConnState.CLOSED);
        return false;
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.session == null || !this.session.isConnected() || !this.state.get().isConnected();
    }

    public Object connectName() {
        return this.sshConnect.getName();
    }

    @Getter
    private SSHShell shell;

    public SSHShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
                if (this.sshConnect.isX11forwarding()) {
                    channel.setXForwarding(true);
                }
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                channel.setPtyType("xterm");
                this.shell = new SSHShell(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

    private final SSHSftpManager sftpManager = new SSHSftpManager();

    private final SftpUploadManager sftpUploadManager = new SftpUploadManager();

    @Getter
    private final SftpDeleteManager sftpDeleteManager = new SftpDeleteManager();

    private final SftpDownloadManager sftpDownloadManager = new SftpDownloadManager();

    public SSHSftp openSftp() {
        if (!this.sftpManager.hasAvailable()) {
            try {
                ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
                SSHSftp sftp = new SSHSftp(channel, this.sftpDeleteManager);
                sftp.connect(this.connectTimeout());
                this.sftpManager.push(sftp);
                return sftp;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.sftpManager.take();
    }

    public String exec(String command) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) this.session.openChannel("exec");
            if (this.sshConnect.isX11forwarding()) {
                channel.setXForwarding(true);
            }
            channel.setCommand(command);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);
            channel.connect();
            while (channel.isConnected()) {
                Thread.sleep(5);
            }
            return stream.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return null;
    }

    public String exec_id_un(int uid) {
        return this.exec("/usr/bin/id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("/usr/bin/id -gn " + gid);
    }

    public int connectTimeout() {
        return this.sshConnect.connectTimeOutMs();
    }

    private SftpAttr attr;

    public SftpAttr getAttr() {
        if (this.attr == null) {
            this.attr = new SftpAttr();
        }
        return this.attr;
    }

    public void upload(File localFile, String remoteFile) throws SftpException {
        this.sftpUploadManager.createMonitor(localFile, remoteFile, this.openSftp());
    }

    public void cancelUpload() {
        this.sftpUploadManager.cancel();
    }

    public void setUploadEndedCallback(Consumer<SftpUploadEnded> callback) {
        this.sftpUploadManager.setUploadEndedCallback(callback);
    }

    public void setUploadFailedCallback(Consumer<SftpUploadFailed> callback) {
        this.sftpUploadManager.setUploadFailedCallback(callback);
    }

    public void setUploadCanceledCallback(Consumer<SftpUploadCanceled> callback) {
        this.sftpUploadManager.setUploadCanceledCallback(callback);
    }

    public void setUploadInPreparationCallback(Consumer<SftpUploadInPreparation> callback) {
        this.sftpUploadManager.setUploadInPreparationCallback(callback);
    }

    public void setUploadChangedCallback(Consumer<SftpUploadChanged> callback) {
        this.sftpUploadManager.setUploadChangedCallback(callback);
    }

    public void download(File localFile, SftpFile remoteFile) throws SftpException {
        this.sftpDownloadManager.createMonitor(localFile, remoteFile, this.openSftp());
    }

    public void cancelDownload() {
        this.sftpDownloadManager.cancel();
    }

    public void setDownloadEndedCallback(Consumer<SftpDownloadEnded> callback) {
        this.sftpDownloadManager.setDownloadEndedCallback(callback);
    }

    public void setDownloadFailedCallback(Consumer<SftpDownloadFailed> callback) {
        this.sftpDownloadManager.setDownloadFailedCallback(callback);
    }

    public void setDownloadCanceledCallback(Consumer<SftpDownloadCanceled> callback) {
        this.sftpDownloadManager.setDownloadCanceledCallback(callback);
    }

    public void setDownloadChangedCallback(Consumer<SftpDownloadChanged> callback) {
        this.sftpDownloadManager.setDownloadChangedCallback(callback);
    }

    public void setDownloadInPreparationCallback(Consumer<SftpDownloadInPreparation> callback) {
        this.sftpDownloadManager.setDownloadInPreparationCallback(callback);
    }

    public void setDeleteEndedCallback(Consumer<SftpDeleteEnded> callback) {
        this.sftpDeleteManager.setDeleteEndedCallback(callback);
    }

    public void setDeleteDeletedCallback(Consumer<SftpDeleteDeleted> callback) {
        this.sftpDeleteManager.setDeleteDeletedCallback(callback);
    }

    private DockerExec dockerExec;

    public DockerExec dockerExec() {
        if (this.dockerExec == null) {
            this.dockerExec = new DockerExec(this);
        }
        return this.dockerExec;
    }

    public boolean isDownloading() {
        return this.sftpDownloadManager.isDownloading();
    }

    public boolean isUploading() {
        return this.sftpUploadManager.isUploading();
    }

    public BooleanProperty uploadingProperty() {
        return this.sftpUploadManager.uploadingProperty();
    }

}
