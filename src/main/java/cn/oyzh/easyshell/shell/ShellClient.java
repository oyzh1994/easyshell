package cn.oyzh.easyshell.shell;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.ShellSftpManager;
import cn.oyzh.easyshell.sftp.SftpAttr;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteDeleted;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteEnded;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteManager;
import cn.oyzh.easyshell.sftp.download.SftpDownloadManager;
import cn.oyzh.easyshell.sftp.upload.SftpUploadManager;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.store.ShellSSHConfigStore;
import cn.oyzh.easyshell.x11.X11Manager;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.SSHForwardConfig;
import cn.oyzh.ssh.SSHForwarder;
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
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellClient {

    /**
     * JSch对象
     */
    private static final JSch JSCH = new JSch();

    /**
     * shell信息
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private final ShellConnect shellConnect;

    /**
     * shell会话
     */
    @Getter
    private Session session;

    /**
     * ssh端口转发器
     */
    private SSHForwarder sshForwarder;

    /**
     * x11配置存储
     */
    private final ShellX11ConfigStore x11ConfigStore = ShellX11ConfigStore.INSTANCE;

    /**
     * shell配置存储
     */
    private final ShellSSHConfigStore sshConfigStore = ShellSSHConfigStore.INSTANCE;

    /**
     * 静默关闭标志位
     */
    private boolean closeQuietly;

    public ShellClient(@NonNull ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        // 监听连接状态
        this.stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CLOSED -> {
                    if (!this.closeQuietly) {
                        ShellEventUtil.connectionClosed(this);
                    }
                }
                case CONNECTED -> ShellEventUtil.connectionConnected(this);
                default -> {

                }
            }
        });
    }

    /**
     * 连接状态
     */
    @Getter
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    public ShellConnState state() {
        return this.stateProperty().get();
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    public ReadOnlyObjectProperty<ShellConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    /**
     * 添加连接状态监听器
     *
     * @param stateListener 监听器
     */
    public void addStateListener(ChangeListener<ShellConnState> stateListener) {
        if (stateListener != null) {
            this.stateProperty().addListener(stateListener);
        }
    }

    private String initHost() {
        // 连接地址
        String host;
        // 初始化ssh端口转发
        if (this.shellConnect.isSSHForward()) {
            // 初始化ssh转发器
            ShellSSHConfig sshConfig = this.shellConnect.getSshConfig();
            // 从数据库获取
            if (sshConfig == null) {
                sshConfig = this.sshConfigStore.getByIid(this.shellConnect.getId());
            }
            if (sshConfig != null) {
                if (this.sshForwarder == null) {
                    this.sshForwarder = new SSHForwarder(sshConfig);
                }
                // ssh配置
                SSHForwardConfig forwardConfig = new SSHForwardConfig();
                forwardConfig.setHost(this.shellConnect.hostIp());
                forwardConfig.setPort(this.shellConnect.hostPort());
                // 执行连接
                int localPort = this.sshForwarder.forward(forwardConfig);
                // 连接信息
                host = "127.0.0.1:" + localPort;
            } else {
                JulLog.warn("ssh forward is enable but ssh config is null");
                throw new SSHException("ssh forward is enable but ssh config is null");
            }
        } else {// 直连
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.shellConnect.getUser(), this.shellConnect.getPassword(), this.shellConnect.getHost());
        }
        // 连接信息
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        // 创建会话
        this.session = JSCH.getSession(this.shellConnect.getUser(), hostIp, port);
//        this.session = JSCH.getSession(this.shellConnect.getUser(), this.shellConnect.hostIp(), this.shellConnect.hostPort());
        // 主机密码
        if (StringUtil.isNotBlank(this.shellConnect.getPassword())) {
            this.session.setPassword(this.shellConnect.getPassword());
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
        if (this.shellConnect.isX11forwarding()) {
            // x11配置
            ShellX11Config x11Config = this.shellConnect.getX11Config();
            // 获取x11配置
            if (x11Config == null) {
                x11Config = this.x11ConfigStore.getByIid(this.shellConnect.getId());
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
        this.session.setTimeout(this.shellConnect.connectTimeOutMs());
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
                this.state.set(ShellConnState.CLOSED);
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
            this.state.set(ShellConnState.CONNECTING);
            // 执行连接
            this.session.connect(timeout);
            // 判断连接结果
            if (this.session.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
            } else if (this.state.get() == ShellConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("sshClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
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
            return this.state.get() == ShellConnState.CONNECTING;
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
        return this.shellConnect.getName();
    }

    @Getter
    private ShellShell shell;

    public ShellShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
                if (this.shellConnect.isX11forwarding()) {
                    channel.setXForwarding(true);
                }
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                channel.setPtyType("xterm");
                this.shell = new ShellShell(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

    private final ShellSftpManager sftpManager = new ShellSftpManager();

    private final SftpUploadManager sftpUploadManager = new SftpUploadManager();

    public SftpUploadManager getSftpUploadManager() {
        return sftpUploadManager;
    }

    @Getter
    private final SftpDeleteManager sftpDeleteManager = new SftpDeleteManager();

    private final SftpDownloadManager sftpDownloadManager = new SftpDownloadManager();

    public SftpDownloadManager getSftpDownloadManager() {
        return sftpDownloadManager;
    }

    public ShellSftp openSftp() {
        if (!this.sftpManager.hasAvailable()) {
            try {
                ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
                ShellSftp sftp = new ShellSftp(channel);
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
            if (this.shellConnect.isX11forwarding()) {
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
        return this.shellConnect.connectTimeOutMs();
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

//    public void cancelUpload() {
//        this.sftpUploadManager.cancel();
//    }
//
//    public void setUploadEndedCallback(Consumer<SftpUploadEnded> callback) {
//        this.sftpUploadManager.setUploadEndedCallback(callback);
//    }
//
//    public void setUploadFailedCallback(Consumer<SftpUploadFailed> callback) {
//        this.sftpUploadManager.setUploadFailedCallback(callback);
//    }
//
//    public void setUploadCanceledCallback(Consumer<SftpUploadCanceled> callback) {
//        this.sftpUploadManager.setUploadCanceledCallback(callback);
//    }
//
//    public void setUploadInPreparationCallback(Consumer<SftpUploadInPreparation> callback) {
//        this.sftpUploadManager.setUploadInPreparationCallback(callback);
//    }
//
//    public void setUploadChangedCallback(Consumer<SftpUploadChanged> callback) {
//        this.sftpUploadManager.setUploadChangedCallback(callback);
//    }

    public void download(File localFile, SftpFile remoteFile) throws SftpException {
        this.sftpDownloadManager.createMonitor(localFile, remoteFile, this.openSftp());
    }

//    public void cancelDownload() {
//        this.sftpDownloadManager.cancel();
//    }

//    public void setDownloadEndedCallback(Consumer<SftpDownloadEnded> callback) {
//        this.sftpDownloadManager.setDownloadEndedCallback(callback);
//    }
//
//    public void setDownloadFailedCallback(Consumer<SftpDownloadFailed> callback) {
//        this.sftpDownloadManager.setDownloadFailedCallback(callback);
//    }
//
//    public void setDownloadCanceledCallback(Consumer<SftpDownloadCanceled> callback) {
//        this.sftpDownloadManager.setDownloadCanceledCallback(callback);
//    }
//
//    public void setDownloadChangedCallback(Consumer<SftpDownloadChanged> callback) {
//        this.sftpDownloadManager.setDownloadChangedCallback(callback);
//    }
//
//    public void setDownloadInPreparationCallback(Consumer<SftpDownloadInPreparation> callback) {
//        this.sftpDownloadManager.setDownloadInPreparationCallback(callback);
//    }

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

    private ServerExec serverExec;

    public ServerExec serverExec() {
        if (this.serverExec == null) {
            this.serverExec = new ServerExec(this);
        }
        return this.serverExec;
    }

    private ShellExec shellExec;

    public ShellExec shellExec() {
        if (this.shellExec == null) {
            this.shellExec = new ShellExec(this);
        }
        return this.shellExec;
    }

    //    public boolean isDownloading() {
//        return this.sftpDownloadManager.isDownloading();
//    }
//
//    public boolean isUploading() {
//        return this.sftpUploadManager.isUploading();
//    }
//
    public BooleanProperty uploadingProperty() {
        return this.sftpUploadManager.uploadingProperty();
    }

    public BooleanProperty deletingProperty() {
        return this.sftpDeleteManager.deletingProperty();
    }

    public BooleanProperty downloadingProperty() {
        return this.sftpDownloadManager.downloadingProperty();
    }

    public void delete(SftpFile file) {
        this.sftpDeleteManager.deleteFile(file, this.openSftp());
    }
}
