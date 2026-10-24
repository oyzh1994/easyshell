package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh.exec.ShellSSHExec;
import cn.oyzh.easyshell.ssh.process.ShellProcessExec;
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
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * ssh客户端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellSSHClient extends ShellBaseSSHClient {

    /**
     * 终端尺寸变化合并窗口。窗口最大化/还原会连续产生多个网格尺寸，
     * JSch 每次 setPtySize 都会发送 window-change，必须只发送最终尺寸。
     */
    private static final int PTY_RESIZE_DEBOUNCE_MILLIS = 100;

    /**
     * 终端输出安静后再安装 OSC hook，避免登录脚本和首屏输出期间注入命令。
     */
    private static final int OSC_INTEGRATION_QUIET_MILLIS = 600;

    /**
     * 最后一次输出
     */
    private String lastOutput;

    /**
     * 解析工作目录
     */
    private boolean resolveWorkerDir;

    /**
     * 增量工作目录解析器，保留跨输出块的 OSC 与提示符状态
     */
    private final ShellWorkerDirResolver workerDirResolver = new ShellWorkerDirResolver();

    /**
     * 当前 shell 会话的 OSC 目录上报安装状态。不支持或安装失败后不再重复探测。
     */
    private volatile OscIntegrationState oscWorkerDirIntegrationState = OscIntegrationState.PENDING;

    /**
     * 延迟安装 OSC hook 的任务
     */
    private ScheduledFuture<?> oscWorkerDirIntegrationFuture;

    /**
     * 工作目录属性
     */
    private StringProperty workDirProperty;

    //    /**
    //     * 终端历史存储
    //     */
    //    private final ShellTermHistoryStore termHistoryStore = ShellTermHistoryStore.INSTANCE;

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
            this.scheduleOscWorkerDirIntegration();
            this.doResolveWorkerDir(this.lastOutput);
        } else {
            this.cancelOscWorkerDirIntegration();
        }
    }

    /**
     * 解析工作目录
     *
     * @param output 输出
     */
    public void resolveWorkerDir(String output) {
        // if (StringUtil.equals(this.lastOutput, output)) {
        //     return;
        // }
        if (this.resolveWorkerDir) {
            this.doResolveWorkerDir(output);
            this.scheduleOscWorkerDirIntegration();
        }
        this.lastOutput = output;
    }

    /**
     * 解析工作目录
     *
     * @param output 输出
     */
    private void doResolveWorkerDir(String output) {
        String workDir = this.workerDirResolver.resolve(output, this.userHome);
        // 跟随目录
        if (StringUtil.isNotBlank(workDir)) {
            this.workDirProperty().set(workDir);
        }
    }

    /**
     * 重置工作目录解析状态
     */
    void resetWorkerDirResolver() {
        this.workerDirResolver.reset();
    }

    /**
     * 在终端输出安静后安排安装 OSC hook。持续输出会重置计时。
     */
    synchronized void scheduleOscWorkerDirIntegration() {
        if (!this.resolveWorkerDir || !ShellSSHUtil.isEnableOscWorkerDir(this.shellConnect)
                || this.oscWorkerDirIntegrationState != OscIntegrationState.PENDING
                || this.shell == null || this.shell.isClosed()) {
            return;
        }
        if (this.oscWorkerDirIntegrationFuture != null) {
            this.oscWorkerDirIntegrationFuture.cancel(false);
        }
        this.oscWorkerDirIntegrationFuture = ExecutorUtil.start(
                this::installOscWorkerDirIntegration, OSC_INTEGRATION_QUIET_MILLIS);
    }

    /**
     * 取消尚未执行的 OSC hook 安装任务
     */
    private synchronized void cancelOscWorkerDirIntegration() {
        if (this.oscWorkerDirIntegrationFuture != null) {
            this.oscWorkerDirIntegrationFuture.cancel(false);
            this.oscWorkerDirIntegrationFuture = null;
        }
    }

    /**
     * 安装 OSC 工作目录上报 hook。同一次 shell 会话只安装一次。
     */
    void installOscWorkerDirIntegration() {
        ShellSSHShell currentShell;
        synchronized (this) {
            this.oscWorkerDirIntegrationFuture = null;
            if (!this.resolveWorkerDir || !ShellSSHUtil.isEnableOscWorkerDir(this.shellConnect)
                    || this.oscWorkerDirIntegrationState != OscIntegrationState.PENDING
                    || this.shell == null || this.shell.isClosed()) {
                return;
            }
            // 先占位，避免终端读取线程在安装期间等待本对象的锁。
            this.oscWorkerDirIntegrationState = OscIntegrationState.INSTALLING;
            currentShell = this.shell;
        }
        boolean installed = false;
        try {
            String shellType = this.getShellType();
            ShellOscShellIntegration.Platform platform;
            if (this.isWindows()) {
                platform = ShellOscShellIntegration.Platform.WINDOWS;
            } else if (this.isFreeBSD()) {
                platform = ShellOscShellIntegration.Platform.FREEBSD;
            } else {
                platform = ShellOscShellIntegration.Platform.UNIX;
            }
            if (!ShellOscShellIntegration.isSupported(shellType, platform)) {
                return;
            }
            installed = ShellOscShellIntegration.install(
                    currentShell.getOutputStream(), shellType, platform);
        } catch (Exception ex) {
            JulLog.warn("install osc worker dir integration failed", ex);
        } finally {
            synchronized (this) {
                // shell 在安装期间关闭并重建时，openShell 会重置状态，旧任务不能覆盖新会话。
                if (this.oscWorkerDirIntegrationState == OscIntegrationState.INSTALLING
                        && currentShell == this.shell) {
                    this.oscWorkerDirIntegrationState = installed
                            ? OscIntegrationState.INSTALLED : OscIntegrationState.UNAVAILABLE;
                }
            }
        }
    }

    /**
     * OSC 目录上报的安装状态
     */
    private enum OscIntegrationState {
        PENDING,
        INSTALLING,
        INSTALLED,
        UNAVAILABLE
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

    //    /**
    //     * 保存终端历史
    //     *
    //     * @param output 输出
    //     */
    //    public void saveTermHistory(String output) {
    //        String command = ShellSSHUtil.resolveCommand(output);
    //        if (StringUtil.isNotBlank(command)) {
    //            JulLog.error("command: " + command);
    //            ShellTermHistory history = new ShellTermHistory();
    //            history.setContent(command);
    //            history.setIid(this.shellConnect.getId());
    //            history.setSaveTime(System.currentTimeMillis());
    //            this.termHistoryStore.save(history);
    //        }
    //    }

    /**
     * ssh跳板转发器
     */
    private SSHJumpForwarder jumpForwarder;

    /**
     * ssh隧道转发器
     */
    private SSHTunnelingForwarder tunnelForwarder;

    /**
     * 当前隧道绑定所属的会话
     */
    private Session tunnelSession;

    private final Object tunnelLock = new Object();

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

    private String connectHost;

    /**
     * 本次连接使用的代理配置。直连时作用于主会话，跳板时作用于第一跳。
     */
    private ShellProxyConfig connectProxyConfig;

    /**
     * 待发送的 PTY 尺寸。
     */
    private ScheduledFuture<?> pendingPtyResizeFuture;

    private int pendingPtyColumns;

    private int pendingPtyRows;

    private int pendingPtyWidth;

    private int pendingPtyHeight;

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
            if (this.session == null || !this.session.isConnected()) {
                this.state.set(ShellConnState.INTERRUPTED);
            }
        }
    }

    /**
     * sftp客户端
     */
    private ShellSFTPClient sftpClient;

    public synchronized ShellSFTPClient sftpClient() {
        try {
            if (this.sftpClient == null) {
                this.sftpClient = new ShellSFTPClient(this.shellConnect, this.session, false, this.dynamicChannelPermits);
                this.sftpClient.osType = this.osType;
                this.sftpClient.userHome = this.userHome;
                this.sftpClient.environment = this.environment;
                this.sftpClient.remoteCharset = this.remoteCharset;
                this.sftpClient.setSessionSupplier(() -> {
                    try {
                        this.ensureSessionConnected(this.connectTimeout());
                        return this.session;
                    } catch (Exception ex) {
                        throw new ShellException(ex);
                    }
                });
            }
            return this.sftpClient;
        } catch (Throwable ex) {
            JulLog.warn("create sftp client failed", ex);
        }
        return null;
    }

    @Override
    protected String initHost() {
        if (this.connectHost != null) {
            if (!this.shellConnect.isEnableJump()
                    || (this.jumpForwarder != null && this.jumpForwarder.isAlive())) {
                return this.connectHost;
            }
            if (this.jumpForwarder != null) {
                this.jumpForwarder.destroy();
                this.jumpForwarder = null;
            }
            this.connectHost = null;
        }
        // 连接地址
        String host;
        // 初始化跳板转发
        if (this.shellConnect.isEnableJump()) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder();
            }
            // 初始化跳板配置
            List<ShellJumpConfig> jumpConfigs = this.shellConnect.getEnableJumpConfigs();
            // 转换为目标连接
            SSHConnect target = ShellUtil.toSSHConnect(this.shellConnect);
            // 执行连接
            int localPort = this.jumpForwarder.forward(jumpConfigs, target, this.connectProxyConfig);
            // 连接信息
            host = "127.0.0.1:" + localPort;
        } else {// 直连
            if (this.jumpForwarder != null) {
                this.jumpForwarder.destroy();
                this.jumpForwarder = null;
            }
            // 连接信息
            host = this.shellConnect.getHost();
        }
        this.connectHost = host;
        return this.connectHost;
    }

    /**
     * 初始化代理
     */
    private void initProxy() {
        // 跳板模式下代理已用于第一跳，主会话连接的是本地临时转发端口。
        if (this.connectProxyConfig == null || this.shellConnect.isEnableJump()) {
            return;
        }
        Proxy proxy = ShellSSHUtil.newProxy(this.connectProxyConfig);
        this.session.setProxy(proxy);
    }

    /**
     * 加载代理配置。代理启用但配置缺失时保持原有失败语义。
     */
    private ShellProxyConfig resolveProxyConfig() {
        if (!this.shellConnect.isEnableProxy()) {
            return null;
        }
        ShellProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
        if (proxyConfig == null) {
            proxyConfig = this.proxyConfigStore.getByIid(this.shellConnect.getId());
        }
        if (proxyConfig == null) {
            JulLog.warn("proxy is enable but proxy config is null");
            throw new SSHException("proxy is enable but proxy config is null");
        }
        return proxyConfig;
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
            if (this.tunnelForwarder == null) {
                this.tunnelForwarder = new SSHTunnelingForwarder();
            }
            // 执行转发
            this.tunnelForwarder.forward(tunnelingConfigs, this.session);
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
                // x11配置
                this.session.setConfig("ForwardX11", "yes");
                this.session.setConfig("ForwardX11Trusted", "yes");
                this.session.setX11Host(x11Config.getHost());
                this.session.setX11Port(x11Config.getPort());
                if (StringUtil.isNotBlank(x11Config.getCookie())) {
                    this.session.setX11Cookie(x11Config.getCookie());
                }
                // 本地转发，启动x11服务
                if (ShellX11Manager.isBuilt_in(x11Config)) {
                    try {
                        ShellX11Manager.startXServer_built_in();
                    } catch (Exception ex) {
                        throw new ShellException(ex);
                    }
                }
            } else {
                throw new RuntimeException("X11forwarding is enable but x11config is null");
            }
        }
    }

    @Override
    protected void initClient() throws JSchException, AgentProxyException {
        // 跳板转发需要先拿到代理配置，代理应作用于第一跳而不是本地转发端口。
        this.connectProxyConfig = this.resolveProxyConfig();
        // 执行初始化
        super.initClient();
        // 初始化x11
        this.initX11();
        // 初始化代理
        this.initProxy();
    }

    @Override
    public void close() {
        try {
            this.releaseResources();
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            JulLog.warn("shellSSHClient close error", ex);
        }
    }

    private void releaseResources() {
        this.cancelPendingPtyResize();
        this.cancelOscWorkerDirIntegration();
        ShellClientChecker.remove(this);
        this.closeQuietly(this.sftpClient, "sftp client");
        this.sftpClient = null;
        this.closeQuietly(this.shell, "shell channel");
        this.shell = null;
        this.closeQuietly(this.sshExec, "ssh exec");
        this.sshExec = null;
        this.closeQuietly(this.processExec, "process exec");
        this.processExec = null;
        this.closeQuietly(this.dockerExec, "docker exec");
        this.dockerExec = null;
        synchronized (this.tunnelLock) {
            if (this.tunnelForwarder != null) {
                this.tunnelForwarder.destroy();
                this.tunnelForwarder = null;
            }
            this.tunnelSession = null;
        }
        if (this.session != null) {
            this.session.disconnect();
            this.session = null;
        }
        if (this.jumpForwarder != null) {
            this.jumpForwarder.destroy();
            this.jumpForwarder = null;
        }
        this.connectHost = null;
    }

    private void closeQuietly(AutoCloseable resource, String name) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ex) {
            JulLog.warn("shellSSHClient close {} error", name, ex);
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
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            this.connectSession(timeout);
            // 判断连接结果
            if (this.session != null && this.session.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
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
            this.releaseResources();
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellSSHClient start error", ex);
            throw new ShellException(ex);
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    protected void onSessionConnected() {
        synchronized (this.tunnelLock) {
            if (this.tunnelSession != this.session) {
                if (this.tunnelForwarder != null) {
                    this.tunnelForwarder.destroy();
                    this.tunnelForwarder = null;
                }
                this.tunnelSession = this.session;
                this.initTunneling();
            }
        }
        this.state.set(ShellConnState.CONNECTED);
    }

    @Override
    public boolean isConnected() {
        return this.session != null && this.session.isConnected()
                && this.state.get() != null && this.state.get().isConnected();
    }

    /**
     * shell通道
     */
    private ShellSSHShell shell;

    public ShellSSHShell getShell() {
        return shell;
    }

    /**
     * 重新打开shell
     *
     * @return 新shell
     */
    public synchronized ShellSSHShell reopenShell() throws Exception {
        if (this.shell != null) {
            this.shell.close();
        }
        this.shell = null;
        return this.openShell();
    }

    /**
     * 打开shell通道
     *
     * @return ShellSSHShell
     */
    public synchronized ShellSSHShell openShell() throws Exception {
        if (this.shell == null || this.shell.isClosed()) {
            this.cancelOscWorkerDirIntegration();
            this.resetWorkerDirResolver();
            this.oscWorkerDirIntegrationState = OscIntegrationState.PENDING;
            ChannelShell channel = null;
            try {
                this.ensureSessionConnected(this.connectTimeout());
                channel = (ChannelShell) this.session.openChannel("shell");
                //// 用户环境
                // Map<String, String> userEnvs = this.shellConnect.environments();
                // if (CollectionUtil.isNotEmpty(userEnvs)) {
                //    for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                //        channel.setEnv(entry.getKey(), entry.getValue());
                //    }
                //}
                //// 初始化环境变量
                // if (this.osType != null) {
                //    channel.setEnv("PATH", this.getExportPath());
                //}
                //// 初始化字符集
                // channel.setEnv("LANG", "en_US." + this.getCharset().displayName());
                super.initEnvironments(channel);
                //// 客户端转发
                // if (this.shellConnect.isJumpForward()) {
                //    channel.setAgentForwarding(true);
                //}
                //// x11转发
                // if (this.shellConnect.isX11forwarding()) {
                //    channel.setXForwarding(true);
                //}
                // byte[] modes = new byte[]{
                //        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // ISPEED=38400
                //        (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // OSPEED=38400
                //        (byte)0x1F, (byte)0x00, (byte)0x00, (byte)0x50, (byte)0x00, // 行数80
                //        (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x19, (byte)0x00, // 列数25
                //        (byte)0x00 // TTY_OP_END
                //};
                // channel.setTerminalMode(modes);
                // 设置终端类型
                channel.setPty(true);
                channel.setPtyType(this.shellConnect.getTermType());
                // JSch 必须在 connect 前取得输入输出流，否则连接建立后到达的 MOTD、
                // 登录信息和首批提示符会写入空输出流而丢失。
                channel.setInputStream(null);
                InputStream inputStream = channel.getInputStream();
                OutputStream outputStream = channel.getOutputStream();
                this.shell = new ShellSSHShell(channel, inputStream, outputStream);
                this.shell.connect(this.connectTimeout());
            } catch (Exception ex) {
                if (this.shell != null) {
                    this.shell.close();
                    this.shell = null;
                } else if (channel != null) {
                    channel.disconnect();
                }
                throw new ShellException(ex);
            }
        }
        return this.shell;
    }

    public synchronized void waitShellReady(int timeout) throws IOException {
        try {
            ShellSSHShell currentShell = this.openShell();
            if (currentShell == null) {
                throw new ShellException("ssh shell is null");
            }
            currentShell.connect(timeout);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public synchronized void setPtySize(int columns, int rows, int width, int height) {
        if (columns <= 0 || rows <= 0) {
            return;
        }
        this.pendingPtyColumns = columns;
        this.pendingPtyRows = rows;
        this.pendingPtyWidth = width;
        this.pendingPtyHeight = height;
        if (this.pendingPtyResizeFuture != null) {
            this.pendingPtyResizeFuture.cancel(false);
        }
        this.pendingPtyResizeFuture = ExecutorUtil.start(this::flushPtySize, PTY_RESIZE_DEBOUNCE_MILLIS);
    }

    private synchronized void flushPtySize() {
        this.pendingPtyResizeFuture = null;
        ShellSSHShell currentShell = this.getShell();
        if (currentShell != null && currentShell.isConnected()) {
            currentShell.setPtySize(this.pendingPtyColumns, this.pendingPtyRows,
                    this.pendingPtyWidth, this.pendingPtyHeight);
        }
    }

    private synchronized void cancelPendingPtyResize() {
        if (this.pendingPtyResizeFuture != null) {
            this.pendingPtyResizeFuture.cancel(false);
            this.pendingPtyResizeFuture = null;
        }
    }

    //    /**
    //     * 通过shell通道执行命令
    //     *
    //     * @param command 命令
    //     * @return 结果
    //     */
    //    public String execShell(String command) throws Exception {
    //        ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
    //        // 客户端转发
    //        if (this.shellConnect.isJumpForward()) {
    //            channel.setAgentForwarding(true);
    //        }
    //        // x11转发
    //        if (this.shellConnect.isX11forwarding()) {
    //            channel.setXForwarding(true);
    //        }
    //        // 设置终端类型
    //        channel.setPty(true);
    //        channel.connect(this.connectTimeout());
    //        if (channel.isConnected()) {
    //            channel.setPtyType(this.shellConnect.getTermType());
    //            InputStream in = channel.getInputStream();
    //            OutputStream out = channel.getOutputStream();
    //            out.write(command.getBytes());
    //            out.flush();
    //            StringBuilder result = new StringBuilder();
    //            byte[] buffer = new byte[1024];
    //            boolean first = true;
    //            while (first || in.available() > 0) {
    //                if (first) {
    //                    ThreadUtil.sleep(50);
    //                    first = false;
    //                }
    //                int len = in.read(buffer);
    //                result.append(new String(buffer, 0, len, this.getCharset()));
    //                ThreadUtil.sleep(10);
    //            }
    //            IOUtil.close(in);
    //            IOUtil.close(out);
    //            channel.disconnect();
    //            return result.toString();
    //        }
    //        return null;
    //    }

    private ShellDockerExec dockerExec;

    public synchronized ShellDockerExec dockerExec() {
        if (this.dockerExec == null) {
            this.dockerExec = new ShellDockerExec(this);
            try {
                if (this.isWindows()) {
                    String output = this.exec("where docker.exe");
                    String environmentPath = this.resolveCommandDirectory(output, "docker.exe");
                    if (environmentPath == null) {
                        JulLog.warn("docker is not available");
                    } else {
                        this.addEnvironmentPath(environmentPath);
                    }
                } else {
                    String output = this.exec("which docker");
                    String environmentPath = this.resolveCommandDirectory(output, "docker");
                    if (environmentPath != null) {
                        this.addEnvironmentPath(environmentPath);
                    } else if (this.isMacos()
                            && this.sftpClient().exist("/Applications/Docker.app/Contents/Resources/bin/docker")) {
                        this.addEnvironmentPath("/Applications/Docker.app/Contents/Resources/bin/");
                    } else {
                        JulLog.warn("docker is not available");
                    }
                }
            } catch (Exception ex) {
                JulLog.warn("detect docker environment failed", ex);
            }
        }
        return this.dockerExec;
    }

    /**
     * 从 which/where 输出中提取可执行文件目录。兼容多行输出和 Windows 本地化失败提示。
     */
    private String resolveCommandDirectory(String output, String command) {
        if (StringUtil.isBlank(output)
                || ShellUtil.isCommandNotFound(output)
                || ShellUtil.isWindowsCommandNotFound(output, command)) {
            return null;
        }
        for (String line : output.lines().toList()) {
            String candidate = line.trim().replace("\"", "");
            int separator = Math.max(candidate.lastIndexOf('\\'), candidate.lastIndexOf('/'));
            if (separator > 0) {
                return candidate.substring(0, separator);
            }
        }
        return null;
    }

    private void addEnvironmentPath(String path) {
        this.initEnvironment();
        if (StringUtil.isNotBlank(path) && !this.environment.contains(path)) {
            this.environment.add(path);
        }
    }

    private ShellSSHExec sshExec;

    public synchronized ShellSSHExec sshExec() {
        if (this.sshExec == null) {
            this.sshExec = new ShellSSHExec(this);
        }
        return this.sshExec;
    }

    private ShellProcessExec processExec;

    public synchronized ShellProcessExec processExec() {
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
}
