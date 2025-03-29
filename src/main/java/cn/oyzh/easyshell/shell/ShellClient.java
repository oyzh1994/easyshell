package cn.oyzh.easyshell.shell;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CharsetUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.process.ProcessExec;
import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.sftp.SftpAttr;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.ShellSftpManager;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteManager;
import cn.oyzh.easyshell.sftp.download.SftpDownloadManager;
import cn.oyzh.easyshell.sftp.transport.SftpTransportManager;
import cn.oyzh.easyshell.sftp.upload.SftpUploadManager;
import cn.oyzh.easyshell.store.ShellSSHConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.easyshell.x11.X11Manager;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.SSHForwardConfig;
import cn.oyzh.ssh.SSHForwarder;
import cn.oyzh.ssh.SSHHolder;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellClient {

    /**
     * shell信息
     */
    private final ShellConnect shellConnect;

    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    /**
     * shell会话
     */
    private Session session;

    public Session getSession() {
        return session;
    }

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

    public ShellClient(ShellConnect shellConnect) {
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
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 获取状态
     *
     * @return 状态
     */
    public ShellConnState getState() {
        return this.state.get();
    }

    /**
     * 更新状态
     */
    public void updateState() {
        ShellConnState state = this.getState();
        if (state == ShellConnState.CONNECTED) {
            if (this.session == null || !this.session.isConnected()) {
                this.state.set(ShellConnState.INTERRUPT);
            }
        }
    }

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

    /**
     * 初始化连接
     *
     * @return 连接
     */
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
        // 密码
        if (this.shellConnect.isPasswordAuth()) {
            // 创建会话
            this.session = SSHHolder.JSCH.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setPassword(this.shellConnect.getPassword());
        } else {// 证书
            SSHHolder.JSCH.addIdentity(this.shellConnect.getCertificatePath());
            // 创建会话
            this.session = SSHHolder.JSCH.getSession(this.shellConnect.getUser(), hostIp, port);
        }
        // 配置参数
        Properties config = new Properties();
        // 设置终端类型
        config.put("term", "xterm-256color");
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
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
                config.put("ForwardX11", "yes");
                config.put("ForwardX11Trusted", "yes");
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
        // 设置配置
        this.session.setConfig(config);
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
            if (this.shell != null) {
                this.shell.close();
                this.shell = null;
            }
            if (this.shellExec != null) {
                this.shellExec.close();
                this.shellExec = null;
            }
            if (this.serverExec != null) {
                this.serverExec.close();
                this.serverExec = null;
            }
            if (this.dockerExec != null) {
                this.dockerExec.close();
                this.dockerExec = null;
            }
            if (this.sftpManager != null) {
                this.sftpManager.close();
                this.sftpManager = null;
            }
            if (this.session != null) {
                this.session.disconnect();
                this.session = null;
                this.state.set(ShellConnState.CLOSED);
            }
            // 从监听器队列移除
            ShellClientChecker.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 开始连接客户端
     */
    public void start() {
        this.start(this.connectTimeout());
    }

    /**
     * 开始连接客户端
     *
     * @param timeout 超时时间
     */
    public void start(int timeout) {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            // 初始化客户端
            this.initClient();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 初始化连接池
            this.state.set(ShellConnState.CONNECTING);
            // 执行连接
            this.session.connect(timeout);
            // 判断连接结果
            if (this.session.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
            } else if (this.state.get() == ShellConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellClient start error", ex);
            throw new ShellException(ex);
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

    public String connectName() {
        return this.shellConnect.getName();
    }

    private ShellShell shell;

    public ShellShell getShell() {
        return shell;
    }

    public ShellShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
                // 客户端转发
                if (this.shellConnect.isSSHForward()) {
                    channel.setAgentForwarding(true);
                }
                // x11转发
                if (this.shellConnect.isX11forwarding()) {
                    channel.setXForwarding(true);
                }
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                // todo: 必须设置为这个，不然htop鼠标交互不了
                channel.setPtyType("xterm-color");
                this.shell = new ShellShell(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

    private ShellSftpManager sftpManager;

    public ShellSftpManager getSftpManager() {
        if (this.sftpManager == null) {
            this.sftpManager = new ShellSftpManager();
        }
        return this.sftpManager;
    }

    private SftpUploadManager uploadManager;

    public SftpUploadManager getUploadManager() {
        if (this.uploadManager == null) {
            this.uploadManager = new SftpUploadManager();
        }
        return uploadManager;
    }

    private SftpDeleteManager deleteManager;

    public SftpDeleteManager getDeleteManager() {
        if (this.deleteManager == null) {
            this.deleteManager = new SftpDeleteManager();
        }
        return deleteManager;
    }

    private SftpDownloadManager downloadManager;

    public SftpDownloadManager getDownloadManager() {
        if (this.downloadManager == null) {
            this.downloadManager = new SftpDownloadManager();
        }
        return downloadManager;
    }

    private SftpTransportManager transportManager;

    public SftpTransportManager getTransportManager() {
        if (this.transportManager == null) {
            this.transportManager = new SftpTransportManager();
        }
        return transportManager;
    }

    public ShellSftp openSftp() {
        if (!this.getSftpManager().hasAvailable()) {
            ShellSftp sftp = this.newSftp();
            if (sftp != null) {
                this.getSftpManager().push(sftp);
                return sftp;
            }
        }
        return this.getSftpManager().take();
    }

    public ShellSftp newSftp() {
        try {
            ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
            ShellSftp sftp = new ShellSftp(channel);
            sftp.connect(this.connectTimeout());
            return sftp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String exec(String command) {
        ChannelExec channel = null;
        try {
            String extCommand = null;
            if (StringUtil.startWithAnyIgnoreCase(command, "source", "which")) {
                extCommand = command;
            } else if (StringUtil.startWithAnyIgnoreCase(command, "uname")) {
                extCommand = "/usr/bin/" + command;
            } else if (this.isLinux() || this.isMacos()) {
                String exportPath = this.getExportPath();
                extCommand = "export PATH=$PATH" + exportPath + " && " + command;
            } else if (this.isUnix()) {
                extCommand = command;
            }
            channel = (ChannelExec) this.session.openChannel("exec");
            // 客户端转发
            if (this.shellConnect.isSSHForward()) {
                channel.setAgentForwarding(true);
            }
            // x11转发
            if (this.shellConnect.isX11forwarding()) {
                channel.setXForwarding(true);
            }
            channel.setCommand(extCommand);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);
            channel.setErrStream(stream);
            channel.connect();
            while (channel.isConnected()) {
                Thread.sleep(5);
            }
            String result = stream.toString();
            stream.close();
            if (StringUtil.endsWith(result, "\n")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return null;
    }

    /**
     * 获取path变量
     *
     * @return 结果
     */
    public String getExportPath() {
        StringBuilder builder = new StringBuilder();
        for (String string : this.environment) {
            builder.append(":").append(string);
        }
        return builder.toString();
    }

    public String exec_id_un(int uid) {
        return this.exec("id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("id -gn " + gid);
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

    public void delete(SftpFile file) {
        this.getDeleteManager().deleteFile(file, this.openSftp());
    }

    public void upload(File localFile, String remoteFile) throws SftpException {
        this.getUploadManager().createMonitor(localFile, remoteFile, this.openSftp());
    }

    public void download(File localFile, SftpFile remoteFile) throws SftpException {
        this.getDownloadManager().createMonitor(localFile, remoteFile, this.openSftp());
    }

    public void transport(SftpFile localFile, String remoteFile, ShellClient remoteClient) {
        this.getTransportManager().createMonitor(localFile, remoteFile, this, remoteClient);
    }

    /**
     * 环境变量
     */
    private final List<String> environment = new ArrayList<>();

    {
        this.environment.add("/bin");
        this.environment.add("/sbin");
        this.environment.add("/usr/bin");
        this.environment.add("/usr/sbin");
        this.environment.add("/usr/local/bin");
        this.environment.add("/usr/local/sbin");
    }

    private DockerExec dockerExec;

    public DockerExec dockerExec() {
        if (this.dockerExec == null) {
            this.dockerExec = new DockerExec(this);
//            if (this.isMacos()) {
            try {
                String output = this.exec("which docker");
                if (StringUtil.isBlank(output)) {
                    JulLog.warn("docker is not available");
                } else if (!ShellUtil.isCommandNotFound(output)) {
                    String env = output.substring(0, output.lastIndexOf("/"));
                    this.environment.add(env);
                } else if (this.isMacos() && this.openSftp().exist("/Applications/Docker.app/Contents/Resources/bin/docker")) {
                    this.environment.add("/Applications/Docker.app/Contents/Resources/bin/");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
//            }
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

    private ProcessExec processExec;

    public ProcessExec processExec() {
        if (this.processExec == null) {
            this.processExec = new ProcessExec(this);
        }
        return this.processExec;
    }

    public Charset getCharset() {
        return CharsetUtil.fromName(this.shellConnect.getCharset());
    }

    private String osType;

    public boolean isMacos() {
        return StringUtil.containsIgnoreCase(this.osType(), "darwin");
    }

    public boolean isLinux() {
        return StringUtil.containsIgnoreCase(this.osType(), "Linux");
    }

    public boolean isUnix() {
        return !this.isMacos() && !this.isLinux();
    }

    public boolean isFreeBSD() {
        return this.isUnix() && StringUtil.containsIgnoreCase(this.osType(), "FreeBSD");
    }

    private String osType() {
        if (this.osType == null) {
            this.osType = this.exec("uname");
        }
        return this.osType;
    }

    private String whoami;

    public String whoami() {
        if (this.whoami == null) {
            this.whoami = this.exec("whoami");
        }
        return this.whoami;
    }

    @Deprecated
    public String getUserBase() {
        if (this.isMacos()) {
            return "/Users/" + this.whoami() + "/";
        }
        return "/" + this.whoami() + "/";
    }

    private String userHome;

    public String getUserHome() {
        if (this.userHome == null) {
            this.userHome = this.exec("echo $HOME");
        }
        return this.userHome + "/";
    }
}
