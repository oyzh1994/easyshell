package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.exec.ShellSSHExec;
import cn.oyzh.easyshell.ssh2.process.ShellProcessExec;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.easyshell.store.ShellTunnelingConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder2;
import cn.oyzh.ssh.tunneling.SSHTunnelingForwarder2;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.core.CoreModuleProperties;

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
    private SSHJumpForwarder2 jumpForwarder;

    /**
     * ssh隧道转发器
     */
    private SSHTunnelingForwarder2 tunnelForwarder;

    /**
     * x11配置存储
     */
    private final ShellX11ConfigStore x11ConfigStore = ShellX11ConfigStore.INSTANCE;

    /**
     * 隧道转发存储
     */
    private final ShellTunnelingConfigStore tunnelingConfigStore = ShellTunnelingConfigStore.INSTANCE;

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
            if (this.sshClient == null || !this.sshClient.isOpen()) {
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
                // sftp客户端跟ssh共享回话，免得需要输入二次验证码
                this.sftpClient = new ShellSFTPClient(this.shellConnect, this.sshClient, this.session);
                // this.sftpClient = new ShellSFTPClient(this.shellConnect);
                // this.sftpClient.start();
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
                this.jumpForwarder = new SSHJumpForwarder2();
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
                IOUtil.close(this.jumpForwarder);
                this.jumpForwarder = null;
            }
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

    /**
     * 初始化隧道
     */
    private void initTunneling(ClientSession session) {
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
            // 初始化转发器
            if (this.tunnelForwarder == null) {
                this.tunnelForwarder = new SSHTunnelingForwarder2();
            }
            // 执行转发
            this.tunnelForwarder.forward(tunnelingConfigs, session);
        }
    }

    /**
     * 初始化x11配置
     *
     * @param session 会话
     * @param channel 通道
     */
    private void initX11(ClientSession session, ChannelShell channel) {
        // 启用x11转发
        if (this.shellConnect.isX11forwarding()) {
            // x11配置
            ShellX11Config x11Config = this.shellConnect.getX11Config();
            // 获取x11配置
            if (x11Config == null) {
                x11Config = this.x11ConfigStore.getByIid(this.shellConnect.getId());
            }
            if (x11Config != null) {
                // 设置地址和端口
                CoreModuleProperties.X11_BIND_HOST.set(session, x11Config.getHost());
                CoreModuleProperties.X11_BASE_PORT.set(session, x11Config.getPort());
                // 开启转发
                channel.setXForwarding(true);
                // 设置cookie
                String cookie = x11Config.getCookie();
                if (StringUtil.isNotBlank(cookie)) {
                    channel.setXCookie(cookie);
                }
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
                IOUtil.close(this.tunnelForwarder);
                this.tunnelForwarder = null;
            }
            // 销毁回话
            if (this.session != null) {
                this.session.close();
                this.session = null;
            }
            // 销毁跳板转发器
            if (this.jumpForwarder != null) {
                IOUtil.close(this.jumpForwarder);
                this.jumpForwarder = null;
            }
            // 销毁sftp客户端
            if (this.sftpClient != null) {
                this.sftpClient.close();
                this.sftpClient = null;
            }
            this.state.set(ShellConnState.CLOSED);
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
            if (this.sshClient != null && this.sshClient.isOpen()) {
                this.state.set(ShellConnState.CONNECTED);
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
        return this.sshClient != null && this.sshClient.isStarted() && this.state.get().isConnected();
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
    @Deprecated
    public ChannelShell reopenShell() throws Exception {
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
    public ChannelShell openShell() throws Exception {
        if (this.shell == null || this.shell.isClosed()) {
            // 获取会话
            ClientSession session = this.takeSession(this.connectTimeout());
            // 创建shell
            ChannelShell channel = session.createShellChannel(null, this.initEnvironments());
            // 初始化x11转发
            this.initX11(session, channel);
            // 初始化隧道转发
            this.initTunneling(session);
            channel.setIn(null);
            channel.setOut(null);
            channel.setUsePty(true);
            channel.setPtyType(this.shellConnect.getTermType());
            channel.open().verify(this.connectTimeout());
            this.shell = channel;
        }
        return this.shell;
    }

    /**
     * 等待shell就绪
     *
     * @param maxWait 最大等待时间
     * @throws IOException 异常
     */
    public void waitShellReady(int maxWait) throws IOException {
        int wait = 0;
        while (this.shell.getInvertedOut() == null) {
            ThreadUtil.sleep(5);
            wait += 5;
            if (wait > maxWait) {
                throw new IOException("wait shell read timeout by maxWait:" + maxWait);
            }
        }
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

