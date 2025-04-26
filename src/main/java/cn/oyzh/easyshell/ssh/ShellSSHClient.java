package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.docker.ShellDockerExec;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.process.ShellProcessExec;
import cn.oyzh.easyshell.server.ShellServerExec;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.store.ShellJumpConfigStore;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellProxyConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder;
import cn.oyzh.ssh.tunneling.SSHTunnelingForwarder;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import java.util.List;
import java.util.Properties;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellSSHClient extends ShellClient {

    /**
     * ssh跳板转发器
     */
    private SSHJumpForwarder jumpForwarder;

    /**
     * ssh隧道转发器
     */
    private SSHTunnelingForwarder tunnelForwarder;

    /**
     * shell密钥存储
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    /**
     * x11配置存储
     */
    private final ShellX11ConfigStore x11ConfigStore = ShellX11ConfigStore.INSTANCE;

    /**
     * 跳板配置存储
     */
    private final ShellJumpConfigStore jumpConfigStore = ShellJumpConfigStore.INSTANCE;

    /**
     * 代理配置存储
     */
    private final ShellProxyConfigStore proxyConfigStore = ShellProxyConfigStore.INSTANCE;

    public ShellSSHClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    /**
     * 连接状态
     */
    private final ReadOnlyObjectWrapper<ShellSSHConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 获取状态
     *
     * @return 状态
     */
    public ShellSSHConnState getState() {
        return this.state.get();
    }

    /**
     * 更新状态
     */
    public void updateState() {
        ShellSSHConnState state = this.getState();
        if (state == ShellSSHConnState.CONNECTED) {
            if (this.session == null || !this.session.isConnected()) {
                this.state.set(ShellSSHConnState.INTERRUPT);
            }
        }
    }

    private ShellSFTPClient sftpClient;

    public ShellSFTPClient getSftpClient() {
        if (this.sftpClient == null) {
            this.sftpClient = new ShellSFTPClient(this.shellConnect, this.session);
        }
        return this.sftpClient;
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    public ReadOnlyObjectProperty<ShellSSHConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    /**
     * 添加连接状态监听器
     *
     * @param stateListener 监听器
     */
    public void addStateListener(ChangeListener<ShellSSHConnState> stateListener) {
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
        // 初始化ssh转发器
        List<ShellJumpConfig> jumpConfigs = this.shellConnect.getJumpConfigs();
        // 从数据库获取
        if (jumpConfigs == null) {
            jumpConfigs = this.jumpConfigStore.listByIid(this.shellConnect.getId());
        }
        // 初始化ssh端口转发
        if (CollectionUtil.isNotEmpty(jumpConfigs)) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder();
            }
            // 转换为目标连接
            SSHConnect target = ShellUtil.toSSHConnect(this.shellConnect);
            // 执行连接
            int localPort = this.jumpForwarder.forward(jumpConfigs, target);
            // 连接信息
            host = "127.0.0.1:" + localPort;
        } else {// 直连
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

    /**
     * 初始化代理
     */
    private void initProxy() {
        // 开启了代理
        if (this.shellConnect.isEnableProxy()) {
            // 初始化ssh转发器
            ShellProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
            // 从数据库获取
            if (proxyConfig == null) {
                proxyConfig = this.proxyConfigStore.getByIid(this.shellConnect.getId());
            }
            if (proxyConfig == null) {
                JulLog.warn("proxy is enable but proxy config is null");
                throw new SSHException("proxy is enable but proxy config is null");
            }
            // 初始化代理
            Proxy proxy = ShellSSHClientUtil.newProxy(proxyConfig);
            // 设置代理
            this.session.setProxy(proxy);
        }
    }

    /**
     * 初始化隧道
     */
    private void initTunneling() {
        // 开启隧道转发
        if (this.shellConnect.isTunnelingForward()) {
            if (this.tunnelForwarder == null) {
                this.tunnelForwarder = new SSHTunnelingForwarder();
            }
            // 执行转发
            this.tunnelForwarder.forward(this.shellConnect.getTunnelingConfigs(), this.session);
        }
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
            this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setPassword(this.shellConnect.getPassword());
        } else if (this.shellConnect.isCertificateAuth()) {// 证书
            String priKeyFile = this.shellConnect.getCertificate();
            // 检查私钥是否存在
            if (!FileUtil.exist(priKeyFile)) {
                MessageBox.warn("certificate file not exist");
                return;
            }
            SSHHolder.getJsch().addIdentity(priKeyFile);
            // 创建会话
            this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        } else if (this.shellConnect.isManagerAuth()) {// 密钥
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
            // 检查私钥是否存在
            if (key == null) {
                MessageBox.warn("certificate file not exist");
                return;
            }
            String keyName = "key_" + key.getId();
            // 添加认证
            SSHHolder.getJsch().addIdentity(keyName, key.getPrivateKeyBytes(), key.getPublicKeyBytes(), null);
            // 创建会话
            this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        }
        // 配置参数
        Properties config = new Properties();
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
                    ShellX11Manager.startXServer();
                }
            } else {
                throw new RuntimeException("X11forwarding is enable but x11config is null");
            }
        }
        // 设置配置
        this.session.setConfig(config);
        // 初始化代理
        this.initProxy();
    }

    @Override
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
            // 销毁隧道转发器
            if (this.tunnelForwarder != null) {
                this.tunnelForwarder.destroy();
            }
            // 销毁回话
            if (this.session != null) {
                this.session.disconnect();
                this.session = null;
                this.state.set(ShellSSHConnState.CLOSED);
            }
            // 销毁跳板转发器
            if (this.jumpForwarder != null) {
                this.jumpForwarder.destroy();
            }
            // 销毁sftp客户端
            if (this.sftpClient != null) {
                this.sftpClient.close();
            }
            // 从监听器队列移除
            ShellSSHClientChecker.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(int timeout) {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            // 初始化连接池
            this.state.set(ShellSSHConnState.CONNECTING);
            // 初始化客户端
            this.initClient();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 执行连接
            if (this.session != null) {
                // 连接超时
                this.session.setTimeout(timeout);
                // 连接
                this.session.connect(timeout);
            }
            // 判断连接结果
            if (this.session != null && this.session.isConnected()) {
                this.state.set(ShellSSHConnState.CONNECTED);
                // 初始化隧道
                this.initTunneling();
                // 添加到状态监听器队列
                ShellSSHClientChecker.push(this);
            } else if (this.state.get() == ShellSSHConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(ShellSSHConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellSSHClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(ShellSSHConnState.FAILED);
            JulLog.warn("shellSSHClient start error", ex);
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
            return this.state.get() == ShellSSHConnState.CONNECTING;
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

    private ShellSSHShell shell;

    public ShellSSHShell getShell() {
        return shell;
    }

    public ShellSSHShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
                // 客户端转发
                if (this.shellConnect.isJumpForward()) {
                    channel.setAgentForwarding(true);
                }
                // x11转发
                if (this.shellConnect.isX11forwarding()) {
                    channel.setXForwarding(true);
                }
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                // 设置终端类型
                channel.setPty(true);
                channel.setPtyType(this.shellConnect.getTermType());
                this.shell = new ShellSSHShell(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

//    public ShellSFTPChannel openSftp() {
//        return this.getSftpClient().openSftp();
//    }
//
//    public ShellSFTPChannel newSftp() {
//        return this.getSftpClient().newSftp();
//    }

    private ShellDockerExec dockerExec;

    public ShellDockerExec dockerExec() {
        if (this.dockerExec == null) {
            this.dockerExec = new ShellDockerExec(this);
            try {
                if (this.isWindows()) {
                    String output = this.exec("where docker.exe");
                    if (StringUtil.isBlank(output)) {
                        JulLog.warn("docker is not available");
                    } else if (!ShellUtil.isWindowsCommandNotFound(output, "docker")) {
                        String env = output.substring(0, output.lastIndexOf("\\"));
                        this.environment.add(env);
                    }
                } else {
                    String output = this.exec("which docker");
                    if (StringUtil.isBlank(output)) {
                        JulLog.warn("docker is not available");
                    } else if (!ShellUtil.isCommandNotFound(output)) {
                        String env = output.substring(0, output.lastIndexOf("/"));
                        this.environment.add(env);
                    } else if (this.isMacos() && this.getSftpClient().openSftp().exist("/Applications/Docker.app/Contents/Resources/bin/docker")) {
                        this.environment.add("/Applications/Docker.app/Contents/Resources/bin/");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.dockerExec;
    }

    private ShellServerExec serverExec;

    public ShellServerExec serverExec() {
        if (this.serverExec == null) {
            this.serverExec = new ShellServerExec(this);
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

    private ShellProcessExec processExec;

    public ShellProcessExec processExec() {
        if (this.processExec == null) {
            this.processExec = new ShellProcessExec(this);
        }
        return this.processExec;
    }

    private String whoami;

    public String whoami() {
        if (this.whoami == null) {
            this.whoami = this.exec("whoami");
            if (this.isWindows() && this.whoami.contains("\\")) {
                this.whoami = this.whoami.substring(this.whoami.lastIndexOf("\\") + 1).trim();
            }
        }
        return this.whoami;
    }
}

