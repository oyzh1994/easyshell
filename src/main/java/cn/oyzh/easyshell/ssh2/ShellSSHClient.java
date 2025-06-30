package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.exec.ShellSSHExec;
import cn.oyzh.easyshell.ssh2.process.ShellProcessExec;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellProxyConfigStore;
import cn.oyzh.easyshell.store.ShellTunnelingConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder;
import cn.oyzh.ssh.tunneling.SSHTunnelingForwarder;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ssh客户端
 *
 * @author oyzh
 * @since 2025/06/30
 */
public class ShellSSHClient extends ShellBaseSSHClient {

    /**
     * shell类型
     */
    private String shellType;

    /**
     * 最后一次输出
     */
    private String lastOutput;

    private SshClient sshClient;

    /**
     * 解析工作目录
     */
    private boolean resolveWorkerDir;

    /**
     * 工作目录属性
     */
    private StringProperty workDirProperty;

    /**
     * 是否解析工作目录
     *
     * @return 结果
     */
    public boolean isResolveWorkerDir() {
        return resolveWorkerDir;
    }

    /**
     * 设置是否解析工作目录
     *
     * @param resolveWorkerDir 是否解析工作目录
     */
    public void setResolveWorkerDir(boolean resolveWorkerDir) {
        this.resolveWorkerDir = resolveWorkerDir;
        if (resolveWorkerDir) {
            this.doResolveWorkerDir(this.lastOutput);
        }
    }

    /**
     * 解析工作目录
     *
     * @param output 输出
     */
    public void resolveWorkerDir(String output) {
        if (this.resolveWorkerDir) {
            this.doResolveWorkerDir(output);
        }
        this.lastOutput = output;
    }

    /**
     * 解析工作目录
     *
     * @param output 输出
     */
    private void doResolveWorkerDir(String output) {
        String workDir = ShellSSHUtil.resolveWorkerDir(output, this.userHome);
        // 跟随目录
        if (StringUtil.isNotBlank(workDir)) {
            this.workDirProperty().set(workDir);
        }
    }

    /**
     * 获取工作目录属性
     *
     * @return 工作目录属性
     */
    public StringProperty workDirProperty() {
        if (this.workDirProperty == null) {
            this.workDirProperty = new SimpleStringProperty();
        }
        return this.workDirProperty;
    }

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
     * 隧道转发存储
     */
    private final ShellTunnelingConfigStore tunnelingConfigStore = ShellTunnelingConfigStore.INSTANCE;

    /**
     * 代理配置存储
     */
    private final ShellProxyConfigStore proxyConfigStore = ShellProxyConfigStore.INSTANCE;

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

    public ShellSSHClient(ShellConnect shellConnect) {
        super(shellConnect);
        this.addStateListener(this.stateListener);
    }

    /**
     * 更新状态
     */
    public void updateState() {
        ShellConnState state = this.getState();
        if (state == ShellConnState.CONNECTED) {
            if (this.session == null || this.session.isClosed()) {
                this.state.set(ShellConnState.INTERRUPT);
            }
        }
    }

    /**
     * sftp客户端
     */
    private ShellSFTPClient sftpClient;

    public ShellSFTPClient sftpClient() {
        try {
            if (this.sftpClient == null) {
                // sftp客户端使用独立会话，不要跟shell共用会话，免得互相影响
                this.sftpClient = new ShellSFTPClient(this.shellConnect);
                this.sftpClient.start();
                // this.sftpClient = new ShellSFTPClient(this.shellConnect, this.session);
            }
            return this.sftpClient;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected String initHost() {
        // 连接地址
        String host;
        // 初始化跳板转发
        if (this.shellConnect.isEnableJump()) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder();
            }
            // 初始化跳板配置
            List<ShellJumpConfig> jumpConfigs = this.shellConnect.getJumpConfigs();
            // 转换为目标连接
            SSHConnect target = ShellUtil.toSSHConnect(this.shellConnect);
            // 执行连接
            int localPort = this.jumpForwarder.forward(jumpConfigs, target);
            // 连接信息
            host = "127.0.0.1:" + localPort;
        } else {// 直连
            if (this.jumpForwarder != null) {
                this.jumpForwarder.destroy();
                this.jumpForwarder = null;
            }
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
        }
    }

    /**
     * 初始化隧道
     */
    private void initTunneling() {
        // 加载隧道转发配置
        List<ShellTunnelingConfig> tunnelingConfigs = this.shellConnect.getTunnelingConfigs();
        // 从数据库获取
        if (tunnelingConfigs == null) {
            tunnelingConfigs = this.tunnelingConfigStore.loadByIid(this.shellConnect.getId());
        }
        // 过滤配置
        tunnelingConfigs = tunnelingConfigs == null ? Collections.emptyList() : tunnelingConfigs.stream().filter(ShellTunnelingConfig::isEnabled).collect(Collectors.toList());
        // 开启隧道转发
        if (CollectionUtil.isNotEmpty(tunnelingConfigs)) {
        }
    }

    /**
     * 初始化x11配置
     */
    private void initX11() {
        // 启用X11转发
        if (this.shellConnect.isX11forwarding()) {
            // x11配置
            ShellX11Config x11Config = this.shellConnect.getX11Config();
            // 获取x11配置
            if (x11Config == null) {
                x11Config = this.x11ConfigStore.getByIid(this.shellConnect.getId());
            }
            if (x11Config != null) {
                // 本地转发，启动x11服务
                if (x11Config.isLocal()) {
                    ShellX11Manager.startXServer();
                }
            } else {
                throw new RuntimeException("X11forwarding is enable but x11config is null");
            }
        }
    }

    @Override
    protected void initClient(int timeout) throws IOException {
        // 执行初始化
        super.initClient(timeout);
        // 初始化x11
        this.initX11();
        // 初始化代理
        this.initProxy();
    }

    @Override
    public void close() {
        try {
            // 从监听器队列移除
            ShellSSHClientChecker.remove(this);
            if (this.shell != null) {
                this.shell.close();
                this.shell = null;
            }
            if (this.sshExec != null) {
                this.sshExec.close();
                this.sshExec = null;
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
                this.session.close();
                this.session = null;
                this.state.set(ShellConnState.CLOSED);
            }
            // 销毁跳板转发器
            if (this.jumpForwarder != null) {
                this.jumpForwarder.destroy();
            }
            // 销毁sftp客户端
            if (this.sftpClient != null) {
                this.sftpClient.close();
            }
            this.removeStateListener(this.stateListener);
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
            this.state.set(ShellConnState.CONNECTING);
            // 初始化客户端
            this.initClient(timeout);
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 执行连接
            if (this.session != null && this.session.isOpen()) {
                this.state.set(ShellConnState.CONNECTED);
                // 初始化隧道
                this.initTunneling();
                // 添加到状态监听器队列
                ShellSSHClientChecker.push(this);
            } else if (this.state.get() == ShellConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            if (JulLog.isInfoEnabled()) {
                JulLog.info("shellSSHClient connected used:{}ms.", (endTime - starTime));
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellSSHClient start error", ex);
            throw new ShellException(ex);
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
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

    @Override
    public boolean isConnected() {
        return this.session != null && this.session.isOpen() && this.state.get().isConnected();
    }

    /**
     * shell通道
     */
    private ChannelShell shell;

    public ChannelShell getShell() {
        return shell;
    }

    /**
     * 重新打开shell
     *
     * @return 新shell
     */
    public ChannelShell reopenShell() {
        if (this.shell != null && this.shell.isOpen()) {
            IOUtil.closeQuietly(this.shell);
        }
        this.shell = null;
        return this.openShell();
    }

    /**
     * 打开shell通道
     *
     * @return ShellSSHShell
     */
    public ChannelShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = this.session.createShellChannel(null, this.initEnvironments());
                channel.setUsePty(true);
                channel.setPtyType(this.shellConnect.getTermType());
                channel.open().verify(this.connectTimeout());
                channel.setIn(null);
                channel.setOut(null);
                this.shell = channel;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

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
                    } else if (this.isMacos() && this.sftpClient().exist("/Applications/Docker.app/Contents/Resources/bin/docker")) {
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

    private ShellSSHExec sshExec;

    public ShellSSHExec sshExec() {
        if (this.sshExec == null) {
            this.sshExec = new ShellSSHExec(this);
        }
        return this.sshExec;
    }

    private ShellProcessExec processExec;

    public ShellProcessExec processExec() {
        if (this.processExec == null) {
            this.processExec = new ShellProcessExec(this);
        }
        return this.processExec;
    }

    private String whoami;

    /**
     * 我是谁
     *
     * @return 结果
     */
    public String whoami() {
        if (this.whoami == null) {
            this.whoami = this.exec("whoami");
            if (this.isWindows() && this.whoami.contains("\\")) {
                this.whoami = this.whoami.substring(this.whoami.lastIndexOf("\\") + 1).trim();
            }
        }
        return this.whoami;
    }

    /**
     * 获取终端类型
     *
     * @return 终端类型
     */
    public String getShellType() {
        if (this.shellType == null) {
            this.shellType = this.serverExec().getShellType();
        }
        return this.shellType;
    }

    /**
     * 获取shell名称
     *
     * @return 结果
     */
    public String getShellName() {
        String shellType = this.getShellType();
        if (shellType == null) {
            return null;
        }
        if (shellType.contains("/")) {
            shellType = shellType.substring(shellType.lastIndexOf("/") + 1);
        } else if (shellType.contains("\\")) {
            shellType = shellType.substring(shellType.lastIndexOf("\\") + 1);
        }
        return shellType;
    }

    /**
     * 是否zsh的shell
     *
     * @return 结果
     */
    public boolean isZshType() {
        String shellName = this.getShellName();
        return StringUtil.endWithIgnoreCase(shellName, "zsh");
    }

    /**
     * 是否bash的shell
     *
     * @return 结果
     */
    public boolean isBashType() {
        String shellName = this.getShellName();
        return StringUtil.endWithIgnoreCase(shellName, "bash");
    }

    /**
     * 设置pty大小
     *
     * @param columns 列
     * @param rows    行
     * @param sizeW   宽
     * @param sizeH   高
     */
    public synchronized void setPtySize(int columns, int rows, int sizeW, int sizeH) {
        try {
            if (this.shell != null && this.shell.isOpen()) {
                this.shell.sendWindowChange(columns, rows, sizeW, sizeH);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

