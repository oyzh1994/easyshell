package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.server.ShellServerExec;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.ssh.util.JschUtil;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchAlgoNegoFailException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSchProxyException;
import com.jcraft.jsch.JSchSessionDisconnectException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * shell客户端
 *
 * @author oyzh
 * @since 2025/04/25
 */
public abstract class ShellBaseSSHClient implements ShellBaseClient {

    /**
     * 单条 SSH 连接上可同时占用的动态通道数。交互式 Shell 独占一个服务端 session，
     * 动态通道统一预留给 Exec 和 SFTP，并保留服务端 MaxSessions 余量。
     */
    protected static final int MAX_DYNAMIC_CHANNELS = 8;

    /**
     * 系统类型
     */
    protected volatile String osType;

    /**
     * 会话
     */
    protected volatile Session session;

    /**
     * 用户目录
     */
    protected volatile String userHome;

    /**
     * 远程字符集
     */
    protected volatile String remoteCharset;

    /**
     * 环境变量
     */
    protected volatile List<String> environment;

    /**
     * jsch实例
     */
    protected JSch jsch;

    /**
     * shell信息
     */
    protected ShellConnect shellConnect;

    /**
     * shell密钥存储
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    /**
     * 认证失败回调
     */
    protected Function<ShellConnect, ShellConnect> verifyFailureCallback = ShellSSHUtil::onVerifyFailure;

    /**
     * 键盘交互认证验证码供应器，为空时由界面提示输入。
     */
    private Supplier<String> verificationCodeSupplier;

    /**
     * 当前连接是否实际使用过 keyboard-interactive 验证码认证。
     * 2FA 主机需要复用已认证会话，避免 SFTP 再次触发验证码。
     */
    private volatile boolean twoFactorAuthenticationUsed;

    /**
     * 主机密钥确认回调，为空时由界面提示。
     */
    private Function<String, Boolean> hostKeyConfirmCallback;

    /**
     * Agent 身份仓库。密码或私钥认证开启 Agent 转发时，也需要把本地 Agent 仓库绑定到会话，
     * 否则 JSch 会转发默认本地身份仓库而不是 SSH_AUTH_SOCK。
     */
    private volatile IdentityRepository agentIdentityRepository;

    /**
     * Exec 与 SFTP 共用的动态通道许可。同一 SSH 会话派生的 SFTP 客户端必须复用该许可。
     */
    protected final Semaphore dynamicChannelPermits;

    /**
     * 从当前 SSH 连接派生的 SFTP 客户端。
     */
    private ShellSFTPClient sftpClient;

    /**
     * SFTP 独立连接。大文件传输会占满单条 SSH 连接的数据窗口，
     * 独立连接可避免终端、监控等控制命令被拖到通道打开超时。
     */
    private ShellBaseSSHClient sftpSessionClient;

    /**
     * 当前客户端是否已降级到旧服务端算法。
     */
    private volatile boolean legacyAlgorithmFallback;

    public ShellBaseSSHClient(ShellConnect connect) {
        this(connect, new Semaphore(MAX_DYNAMIC_CHANNELS, true));
    }

    protected ShellBaseSSHClient(ShellConnect connect, Semaphore dynamicChannelPermits) {
        this.shellConnect = connect;
        this.dynamicChannelPermits = dynamicChannelPermits;
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    public Session getSession() {
        return session;
    }

    /**
     * 占用一个动态通道名额。Exec 与共享同一会话的 SFTP 必须使用同一个许可池。
     *
     * @return 是否占用成功
     * @throws InterruptedException 线程被中断
     */
    protected boolean acquireDynamicChannel() throws InterruptedException {
        return this.acquireDynamicChannel(this.connectTimeout());
    }

    protected boolean acquireDynamicChannel(long timeout) throws InterruptedException {
        return this.dynamicChannelPermits.tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }

    protected void releaseDynamicChannel() {
        this.dynamicChannelPermits.release();
    }

    public void setVerifyFailureCallback(Function<ShellConnect, ShellConnect> verifyFailureCallback) {
        this.verifyFailureCallback = verifyFailureCallback;
    }

    public Function<ShellConnect, ShellConnect> getVerifyFailureCallback() {
        return this.verifyFailureCallback;
    }

    public void setVerificationCodeSupplier(Supplier<String> verificationCodeSupplier) {
        this.verificationCodeSupplier = verificationCodeSupplier;
    }

    public Supplier<String> getVerificationCodeSupplier() {
        return this.verificationCodeSupplier;
    }

    /**
     * 获取 SFTP 客户端。2FA 主机复用已完成认证的主会话，避免再次请求验证码；
     * 普通主机使用独立会话，避免大文件传输影响终端和控制命令。
     *
     * @return SFTP 客户端
     */
    public synchronized ShellSFTPClient sftpClient() {
        try {
            if (this.sftpClient == null) {
                if (this.isTwoFactorAuthenticationUsed()) {
                    this.sftpClient = new ShellSFTPClient(this.shellConnect, this.session, false, this.dynamicChannelPermits);
                    this.sftpClient.setSessionSupplier(this::ensurePrimarySession);
                } else {
                    this.sftpClient = new ShellSFTPClient(this.shellConnect, null, false, new Semaphore(MAX_DYNAMIC_CHANNELS, true));
                    this.sftpClient.setSessionSupplier(this::ensureSftpSession);
                }
                this.copyMetadataTo(this.sftpClient);
            }
            return this.sftpClient;
        } catch (Throwable ex) {
            JulLog.warn("create sftp client failed", ex);
        }
        return null;
    }

    /**
     * 复制元信息
     *
     * @param client 客户端
     */
    private void copyMetadataTo(ShellSFTPClient client) {
        client.osType = this.osType;
        client.userHome = this.userHome;
        client.shellType = this.shellType;
        client.serverExec = this.serverExec;
        client.environment = this.environment;
        client.remoteCharset = this.remoteCharset;
    }

    /**
     * 获取主会话，供需要复用认证结果的 2FA SFTP 使用。
     */
    private Session ensurePrimarySession() {
        try {
            this.ensureSessionConnected(this.connectTimeout());
        } catch (Exception ex) {
            throw ex instanceof ShellException ? (ShellException) ex : new ShellException(ex);
        }
        Session activeSession = this.session;
        if (activeSession == null || !activeSession.isConnected()) {
            throw new ShellException("ssh session is disconnected");
        }
        return activeSession;
    }

    /**
     * 获取或建立 SFTP 专用会话。该会话只承载 SFTP 通道，不初始化终端、X11 和隧道。
     */
    private Session ensureSftpSession() {
        ShellBaseSSHClient connection;
        synchronized (this) {
            if (this.sftpSessionClient == null) {
                this.sftpSessionClient = this.createSftpSessionClient(this.newSftpConnect());
                this.sftpSessionClient.setVerifyFailureCallback(this.getVerifyFailureCallback());
                this.sftpSessionClient.setHostKeyConfirmCallback(this.getHostKeyConfirmCallback());
                this.sftpSessionClient.setVerificationCodeSupplier(this.getVerificationCodeSupplier());
            }
            connection = this.sftpSessionClient;
        }
        try {
            connection.ensureSessionConnected(this.connectTimeout());
        } catch (Exception ex) {
            throw ex instanceof ShellException ? (ShellException) ex : new ShellException(ex);
        }
        Session activeSession = connection.getSession();
        if (activeSession == null || !activeSession.isConnected()) {
            throw new ShellException("sftp ssh session is disconnected");
        }
        return activeSession;
    }

    /**
     * 创建 SFTP 专用会话客户端。由具体 SSH 子类提供，以便复用自身的代理、跳板等连接能力。
     *
     * @param connect 已移除终端专属能力的连接配置
     * @return 会话客户端
     */
    protected ShellBaseSSHClient createSftpSessionClient(ShellConnect connect) {
        throw new UnsupportedOperationException("sftp session isolation is not supported");
    }

    /**
     * 为 SFTP 专用连接复制配置。X11 和隧道只属于终端主连接，
     * 关闭后可避免 SFTP 连接重复启动本地 X11 服务或占用隧道端口。
     */
    private ShellConnect newSftpConnect() {
        ShellConnect connect = new ShellConnect();
        connect.copy(this.shellConnect);
        connect.setX11forwarding(false);
        connect.setTunnelingConfigs(Collections.emptyList());
        return connect;
    }

    /**
     * 释放父类持有的 SFTP 客户端及独立会话。
     */
    protected synchronized void closeSftpResources() {
        if (this.sftpClient != null) {
            try {
                this.sftpClient.close();
            } catch (Exception ex) {
                JulLog.warn("close sftp client error", ex);
            }
            this.sftpClient = null;
        }
        if (this.sftpSessionClient != null) {
            try {
                this.sftpSessionClient.close();
            } catch (Exception ex) {
                JulLog.warn("close sftp session client error", ex);
            }
            this.sftpSessionClient = null;
        }
    }

    /**
     * 当前连接认证过程中是否请求过验证码。用于让 SFTP 对 2FA 主机复用已认证会话。
     *
     * @return 是否使用过 2FA
     */
    public boolean isTwoFactorAuthenticationUsed() {
        return this.twoFactorAuthenticationUsed;
    }

    public void setHostKeyConfirmCallback(Function<String, Boolean> hostKeyConfirmCallback) {
        this.hostKeyConfirmCallback = hostKeyConfirmCallback;
    }

    public Function<String, Boolean> getHostKeyConfirmCallback() {
        return this.hostKeyConfirmCallback;
    }

    /**
     * 获取系统类型
     *
     * @return 系统类型
     */
    protected String getOsType() {
        if (this.osType == null) {
            this.initMetadata();
        }
        return this.osType;
    }

    /**
     * 初始化元数据
     */
    private synchronized void initMetadata() {
        // 已初始化，直接返回
        if (this.osType != null) {
            return;
        }

        // 系统
        this.osType = this.serverExec().getOsType();
        JulLog.info("os type:{}", this.osType);

        // shell
        this.shellType = this.serverExec().getShellType();
        JulLog.info("shell type:{}", this.shellType);

        // 字符集
        this.remoteCharset = this.serverExec().getCharset();
        JulLog.info("remote charset: {}", this.remoteCharset);

        // 用户目录
        this.userHome = this.serverExec().getUserHome();
        JulLog.info("user home: {}", this.userHome);

        // 环境
        this.initEnvironment();
        JulLog.info("environment: {}", this.environment);
    }

    /**
     * 执行命令
     *
     * @param command 命令
     * @return 结果
     */
    public String exec(String command) {
        return this.exec(command, this.connectTimeout());
    }

    public String exec(String command, int timeout) {
        return this.execInternal(command, timeout, false, true);
    }

    /**
     * 执行命令并优先按 UTF-8 解码输出。Docker CLI 在 Windows GBK 会话中仍输出 UTF-8，
     * 不能直接使用远端控制台字符集解码；遇到本地化命令错误等非 UTF-8 输出时回退到远端字符集。
     *
     * @param command 命令
     * @return 结果
     */
    public String execUtf8(String command) {
        return this.execUtf8(command, this.connectTimeout());
    }

    public String execUtf8(String command, int timeout) {
        return this.execInternal(command, timeout, true, true);
    }

    /**
     * 执行元数据探测命令。探测命令不注入 PATH，也不触发元数据初始化，避免递归占用通道。
     */
    public String execMetadata(String command) {
        return this.execInternal(command, this.connectTimeout(), false, false);
    }

    /**
     * 执行元数据探测命令。探测命令不注入 PATH，也不触发元数据初始化，避免递归占用通道。
     */
    public String execMetadataUtf8(String command) {
        return this.execInternal(command, this.connectTimeout(), true, false);
    }

    protected String execInternal(String command, int timeout, boolean preferUtf8, boolean configureEnvironment) {
        // 获取通道
        ChannelExec channel = null;
        ByteArrayOutputStream stream = null;
        boolean permitAcquired = false;
        String resolvedCommand = this.resolveExecCommand(command);
        Session activeSession;
        try {
            // 命令超时只约束命令执行，建连和通道打开使用独立的连接超时。
            this.ensureSessionConnected(this.connectTimeout());
            activeSession = this.session;
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            JulLog.warn("ssh session prepare failed, command: " + command, ex);
            return null;
        }
        try {
            long waitTimeout = Math.max(this.connectTimeout(), timeout);
            permitAcquired = this.dynamicChannelPermits.tryAcquire(waitTimeout, TimeUnit.MILLISECONDS);
            if (!permitAcquired) {
                JulLog.warn("ssh exec channel acquire timeout, command:{} ", command);
                return null;
            }
            if (activeSession == null || !activeSession.isConnected()) {
                JulLog.warn("ssh session is disconnected before exec, command: {} ", command);
                return null;
            }
            channel = this.newExecChannel(activeSession, resolvedCommand, configureEnvironment);
            if (channel == null) {
                return null;
            }
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), command);
            stream = new ByteArrayOutputStream();
            channel.setErrStream(stream);
            channel.setInputStream(null);
            channel.setOutputStream(stream);
            channel.connect(this.connectTimeout());
            long start = System.currentTimeMillis();
            while (channel.isConnected() && !channel.isClosed()) {
                if (timeout > 0 && System.currentTimeMillis() - start > timeout) {
                    break;
                }
                ThreadUtil.sleep(5);
            }
            String result = this.decodeExecOutput(stream, preferUtf8);
            if (StringUtil.endsWith(result, "\r\n")) {
                result = result.substring(0, result.length() - 2);
            } else if (StringUtil.endWithAny(result, "\n", "\r")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            JulLog.warn("ssh exec failed, command: {}", command, ex);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(stream);
            if (channel != null) {
                channel.disconnect();
            }
            if (permitAcquired) {
                this.dynamicChannelPermits.release();
            }
        }
    }

    private String decodeExecOutput(ByteArrayOutputStream stream, boolean preferUtf8) {
        byte[] bytes = stream.toByteArray();
        String bomDecoded = this.decodeBomOutput(bytes);
        if (bomDecoded != null) {
            return bomDecoded;
        }
        if (preferUtf8) {
            try {
                return stripBom(StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes))
                        .toString());
            } catch (CharacterCodingException ex) {
                JulLog.debug("ssh exec output is not utf-8, fallback to remote charset: {}", this.remoteCharset);
            }
        }
        if (StringUtil.isNotBlank(this.remoteCharset)) {
            return stripBom(new String(bytes, Charset.forName(this.remoteCharset)));
        }
        return stripBom(new String(bytes, this.getCharset()));
    }

    /**
     * 按输出 BOM 解码。Windows 下部分 UTF-8 文件会被 type 原样输出 BOM，而控制台代码页仍为
     * GBK；若按控制台字符集解码，BOM 会显示为乱码。只对带 BOM 的输出切换编码，普通
     * Windows 本地化错误信息继续使用控制台字符集。
     */
    private String decodeBomOutput(byte[] bytes) {
        if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        if (startsWith(bytes, 0xFF, 0xFE)) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }
        if (startsWith(bytes, 0xFE, 0xFF)) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        return null;
    }

    private static boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((bytes[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String stripBom(String value) {
        if (!value.isEmpty() && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }

    /**
     * 获取配置的 PATH 列表，使用当前系统默认的路径分隔符。
     *
     * @return 结果
     */
    public String getExportPath() {
        if (this.osType == null) {
            return "";
        }
        return this.getExportPath(this.isWindows() ? ";" : ":");
    }

    /**
     * 获取配置的可执行文件搜索路径。
     *
     * @param separator 路径分隔符
     * @return 路径列表，不包含远端 PATH 变量表达式
     */
    protected String getExportPath(String separator) {
        if (this.osType == null) {
            return "";
        }
        this.initEnvironment();
        return String.join(separator, this.environment.stream()
                .filter(StringUtil::isNotBlank)
                .map(String::strip)
                .toList());
    }

    /**
     * 初始化环境
     */
    protected synchronized void initEnvironment() {
        if (this.environment == null) {
            List<String> paths = new CopyOnWriteArrayList<>();
            if (this.isWindows()) {
                paths.add("C:/Windows/System");
                paths.add("C:/Windows/System32");
                paths.add("C:/Windows/SysWOW64");
                paths.add("C:/Program Files");
                paths.add("C:/Program Files (x86)");
            } else {
                paths.add("/bin");
                paths.add("/sbin");
                paths.add("/usr/bin");
                paths.add("/usr/sbin");
                paths.add("/usr/games");
                paths.add("/usr/local/bin");
                paths.add("/usr/local/sbin");
                paths.add("/usr/local/games");
            }
            if (this.isMacos()) {
                paths.add("/opt/homebrew/bin");
                paths.add("/Applications/Docker.app/Contents/Resources/bin/");
            }
            this.environment = paths;
        }
    }

    /**
     * 是否macos系统
     *
     * @return 结果
     */
    public boolean isMacos() {
        return isMacosType(this.getOsType());
    }

    /**
     * 是否linux系统
     *
     * @return 结果
     */
    public boolean isLinux() {
        return isLinuxType(this.getOsType());
    }

    /**
     * 是否unix系统
     *
     * @return 结果
     */
    public boolean isUnix() {
        return isUnixType(this.getOsType());
    }

    /**
     * 是否freebsd系统
     *
     * @return 结果
     */
    public boolean isFreeBSD() {
        return StringUtil.containsIgnoreCase(this.getOsType(), "FreeBSD");
    }

    /**
     * 是否openbsd系统
     *
     * @return 结果
     */
    public boolean isOpenBSD() {
        return StringUtil.containsIgnoreCase(this.getOsType(), "OpenBSD");
    }

    /**
     * 是否windows系统
     *
     * @return 结果
     */
    public boolean isWindows() {
        return isWindowsType(this.getOsType());
    }

    private static boolean isMacosType(String value) {
        return StringUtil.containsAnyIgnoreCase(value, "Darwin", "Macos", "OS X");
    }

    private static boolean isLinuxType(String value) {
        return StringUtil.containsIgnoreCase(value, "Linux");
    }

    private static boolean isUnixType(String value) {
        return StringUtil.containsAnyIgnoreCase(value, "FreeBSD", "OpenBSD", "Aix", "HP-UX", "SunOS");
    }

    private static boolean isWindowsType(String value) {
        return StringUtil.containsAnyIgnoreCase(value, "Windows", "MINGW64_NT", "MSYS_NT", "CYGWIN_NT");
    }

    /**
     * 获取远程字符集
     *
     * @return 远程字符集
     */
    public String getRemoteCharset() {
        if (this.remoteCharset == null) {
            this.initMetadata();
        }
        return this.remoteCharset;
    }

    /**
     * 获取文件分割符
     *
     * @return 文件分割符
     */
    public String getFileSeparator() {
        if (this.isWindows()) {
            return "\\";
        }
        return "/";
    }

    /**
     * 获取用户目录
     *
     * @return 用户目录
     */
    public String getUserHome() {
        if (this.userHome == null) {
            this.initMetadata();
        }
        return this.userHome;
    }

    /**
     * 初始化回话
     */
    protected void initSession() throws JSchException {
        if (this.session != null) {
            if (this.legacyAlgorithmFallback) {
                JschUtil.applyLegacyAlgorithms(this.session);
            }
            // 认证方式与页面选择保持一致，避免密钥认证失败后意外回退到密码或交互认证。
            if (this.shellConnect.isPasswordAuth()) {
                this.session.setConfig("PreferredAuthentications", "password,keyboard-interactive");
            } else {
                this.session.setConfig("PreferredAuthentications", "publickey");
            }
            // 设置守护线程
            this.session.setDaemonThread(true);
            // 连续3次失败后断开连接
            this.session.setServerAliveCountMax(3);
            // 心跳处理
            this.session.setServerAliveInterval(ShellSSHUtil.getHeartbeatInterval(this.shellConnect) * 1000);
            // 可选：设置TCP层面的keep-alive
            this.session.setConfig("TCPKeepAlive", "yes");
            // 与旧实现保持一致：跳板转发使用临时本地端口，不参与 known_hosts 校验。
            this.session.setConfig("StrictHostKeyChecking", this.shellConnect.isEnableJump() ? "no" : "ask");
            // JSch 的会话读线程承担整条连接的多路复用，长时间高并发下使用平台线程，
            // 避免虚拟线程 socket 轮询在通道频繁启停时导致整条会话停止响应。
            this.session.setThreadFactory(ThreadUtil::newThread);
        }
    }

    /**
     * 初始化压缩
     */
    protected void initCompression() {
        if (this.session != null) {
            // 启用压缩
            if (this.shellConnect.isEnableCompress()) {
                this.session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
                this.session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
                this.session.setConfig("compression_level", "9");
            } else {
                this.session.setConfig("compression.s2c", "none");
                this.session.setConfig("compression.c2s", "none");
            }
        }
    }

    /**
     * 创建exec通道
     *
     * @param command 命令
     * @return exec通道
     */
    protected ChannelExec newExecChannel(Session activeSession, String command, boolean configureEnvironment) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) activeSession.openChannel("exec");
            if (channel != null) {
                channel.setInputStream(null);
                channel.setOutputStream(null);
                channel.setCommand(command);
                if (configureEnvironment) {
                    this.initEnvironments(channel);
                }
            }
        } catch (Exception ex) {
            if (channel != null) {
                channel.disconnect();
            }
            JulLog.warn("create ssh exec channel failed", ex);
        }
        return channel;
    }

    /**
     * 在远端 shell 中显式注入 PATH。部分 SSH 服务端会忽略 setEnv，因此不能只依赖通道环境变量。
     *
     * @param command 原命令
     * @return 修正后的命令
     */
    protected String resolveExecCommand(String command) {
        if (command == null || this.osType == null) {
            return command;
        }
        try {
            ShellExecCommandResolver.Family family = this.resolveExecShellFamily();
            if (family == ShellExecCommandResolver.Family.UNSUPPORTED) {
                return command;
            }
            String separator = family == ShellExecCommandResolver.Family.CMD
                    || family == ShellExecCommandResolver.Family.POWERSHELL ? ";" : ":";
            return ShellExecCommandResolver.resolve(command, this.getExportPath(separator), family);
        } catch (RuntimeException ex) {
            // PATH 注入是可选增强，任何解析异常都不能阻止原命令执行。
            JulLog.warn("resolve exec command environment failed, use original command", ex);
            return command;
        }
    }

    /**
     * 判断当前远端 shell 能否被安全包装。未知 shell 不注入 PATH，直接执行原命令。
     *
     * @return shell 家族
     */
    private ShellExecCommandResolver.Family resolveExecShellFamily() {
        if (this.isWindows()) {
            if (this.isCmdType()) {
                return ShellExecCommandResolver.Family.CMD;
            }
            if (this.isPowershellType()) {
                return ShellExecCommandResolver.Family.POWERSHELL;
            }
            if (this.isWindowsPosixShellType()) {
                return ShellExecCommandResolver.Family.POSIX;
            }
            return ShellExecCommandResolver.Family.UNSUPPORTED;
        }
        if (this.isMacos() || this.isUnix() || this.isLinux()) {
            if (this.isCshType() || this.isTcshType()) {
                return ShellExecCommandResolver.Family.CSH;
            }
            if (this.isPosixShellType()) {
                return ShellExecCommandResolver.Family.POSIX;
            }
        }
        return ShellExecCommandResolver.Family.UNSUPPORTED;
    }

    /**
     * 是否 POSIX 风格的 shell
     *
     * @return 结果
     */
    public boolean isPosixShellType() {
        return ShellSSHUtil.isPosixShellType(this.getShellName());
    }

    /**
     * Windows 上只有探测到明确的 Unix 风格 shell 路径时才允许 POSIX 语法。
     * shell 探测失败会回退为裸名称 bash，这种结果不足以判断实际解释器，必须放弃注入。
     *
     * @return 结果
     */
    private boolean isWindowsPosixShellType() {
        String shellType = this.getShellType();
        return StringUtil.isNotBlank(shellType)
                && shellType.strip().startsWith("/")
                && this.isPosixShellType();
    }

    protected synchronized void ensureSessionConnected(int timeout) throws Exception {
        if (this.session != null && this.session.isConnected()) {
            if (this.getState() != ShellConnState.CONNECTED) {
                this.onSessionConnected();
            }
            return;
        }
        if (this.session != null) {
            this.session.disconnect();
            this.session = null;
        }
        this.connectSession(timeout);
    }

    /**
     * 会话连接事件
     */
    protected void onSessionConnected() {
    }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    protected String initHost() {
        String host = this.shellConnect.getHost();
        if (StringUtil.isNotBlank(host)) {
            return host.trim();
        }
        return this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
    }

    /**
     * 初始化客户端
     */
    protected void initClient() throws JSchException, AgentProxyException {
        String endpoint = this.initHost();
        String hostIp;
        int port;
        if (endpoint != null && endpoint.startsWith("[")) {
            int bracket = endpoint.indexOf(']');
            int colon = endpoint.indexOf(':', bracket + 1);
            if (bracket < 1 || colon < 0) {
                throw new JSchException("invalid ssh endpoint: " + endpoint);
            }
            hostIp = endpoint.substring(1, bracket);
            port = Integer.parseInt(endpoint.substring(colon + 1));
        } else {
            int colon = endpoint == null ? -1 : endpoint.lastIndexOf(':');
            if (colon <= 0 || colon == endpoint.length() - 1) {
                throw new JSchException("invalid ssh endpoint: " + endpoint);
            }
            hostIp = endpoint.substring(0, colon);
            port = Integer.parseInt(endpoint.substring(colon + 1));
        }
        if (port < 1 || port > 65535) {
            throw new JSchException("invalid ssh port: " + port);
        }
        this.jsch = SSHHolder.newJsch();
        Path knownHosts = ShellSSHUtil.getKnownHostsPath();
        try {
            Path parent = knownHosts.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try {
                Files.createFile(knownHosts);
            } catch (java.nio.file.FileAlreadyExistsException ex) {
                if (!Files.isRegularFile(knownHosts)) {
                    throw ex;
                }
            }
        } catch (Exception ex) {
            throw new JSchException("initialize known_hosts failed: " + knownHosts, ex);
        }
        this.jsch.setKnownHosts(knownHosts.toString());
        // 认证处理
        if (this.shellConnect.isPasswordAuth()) {
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(this.newAuthInteractive(this.shellConnect.getPassword(), null));
        } else if (this.shellConnect.isCertificateAuth()) {
            String priKeyFile = this.shellConnect.getCertificate();
            if (StringUtil.isBlank(priKeyFile) || !Files.isRegularFile(Path.of(priKeyFile))) {
                throw new JSchException("certificate file not found: " + priKeyFile);
            }
            this.jsch.addIdentity(priKeyFile, StringUtil.toBytes(this.shellConnect.getCertificatePwd()));
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(this.newAuthInteractive(null, this.shellConnect.getCertificatePwd()));
        } else if (this.shellConnect.isSSHAgentAuth()) {
            IdentityRepository repository = this.getAgentIdentityRepository();
            if (CollectionUtil.isEmpty(repository.getIdentities())) {
                throw new AgentProxyException("identities is empty");
            }
            this.jsch.setIdentityRepository(repository);
            for (Identity identity : repository.getIdentities()) {
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("Identity: {}", identity.getName());
                }
            }
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(this.newAuthInteractive(null, null));
        } else if (this.shellConnect.isManagerAuth()) {
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
            if (key == null) {
                throw new JSchException("key not found: " + this.shellConnect.getKeyId());
            }
            String keyName = "key_" + key.getId();
            this.jsch.addIdentity(keyName, key.getPrivateKeyBytes(), key.getPublicKeyBytes(), key.getPasswordBytes());
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(this.newAuthInteractive(null, key.getPassword()));
        } else {
            throw new JSchException("unsupported ssh authentication type");
        }
        // 初始化会话
        this.initSession();
        // 初始化压缩
        this.initCompression();
    }

    private ShellSSHAuthInteractive newAuthInteractive(String password, String passphrase) {
        ShellSSHAuthInteractive interactive = new ShellSSHAuthInteractive(password, passphrase);
        interactive.setVerificationCodeSupplier(this.verificationCodeSupplier);
        interactive.setHostKeyConfirmCallback(this.hostKeyConfirmCallback);
        interactive.setVerificationCodeRequestedCallback(() -> this.twoFactorAuthenticationUsed = true);
        return interactive;
    }

    protected synchronized void connectSession(int timeout) throws JSchException, AgentProxyException {
        int retryCount = 0;
        while (true) {
            try {
                this.initClient();
                this.session.setTimeout(timeout);
                this.session.connect(timeout);
                // 初始化agent转发
                this.initAgentForwarding();
                // 会话已连接事件
                this.onSessionConnected();
                return;
            } catch (JSchException ex) {
                if (!this.legacyAlgorithmFallback && this.isAlgorithmNegotiationFailure(ex)) {
                    this.legacyAlgorithmFallback = true;
                    JulLog.warn("ssh algorithm negotiation failed, retrying with legacy algorithms");
                    if (this.session != null) {
                        this.session.disconnect();
                        this.session = null;
                    }
                    continue;
                }
                if (retryCount++ >= 3 || !this.isAuthenticationFailure(ex) || this.verifyFailureCallback == null) {
                    throw ex;
                }
                JulLog.warn("ssh authentication failed, waiting for interactive authentication");
                ShellConnect replacement = this.verifyFailureCallback.apply(this.shellConnect);
                if (replacement == null) {
                    throw ex;
                }
                if (this.session != null) {
                    this.session.disconnect();
                    this.session = null;
                }
                this.shellConnect = replacement;
            }
        }
    }

    /**
     * 为 Agent 转发绑定本地 ssh-agent 身份仓库。必须在认证完成后设置，避免密钥认证
     * 使用 Agent 仓库替代本次连接实际使用的私钥身份。
     *
     * @throws AgentProxyException Agent 不可用
     */
    private void initAgentForwarding() throws AgentProxyException {
        if (!this.shellConnect.isForwardAgent() || this.session == null) {
            return;
        }
        this.session.setIdentityRepository(this.getAgentIdentityRepository());
    }

    /**
     * 获取IdentityRepository
     *
     * @return 结果
     * @throws AgentProxyException
     */
    private synchronized IdentityRepository getAgentIdentityRepository() throws AgentProxyException {
        if (this.agentIdentityRepository == null) {
            this.agentIdentityRepository = JschUtil.initAgentIdentityRepository();
        }
        return this.agentIdentityRepository;
    }

    private boolean isAuthenticationFailure(JSchException ex) {
        // 代理 407/认证握手失败的消息也可能包含 authentication failed，但这不是 SSH
        // 账户认证失败，不能触发桌面认证页或重复提交 SSH 凭证。
        if (this.isProxyFailure(ex)) {
            return false;
        }
        if (ex instanceof JSchSessionDisconnectException disconnectException
                && this.isAuthenticationFailureMessage(disconnectException.getDescription())) {
            return true;
        }
        return this.isAuthenticationFailureMessage(ex.getMessage());
    }

    private boolean isAlgorithmNegotiationFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof JSchAlgoNegoFailException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("algorithm negotiation fail")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isProxyFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof JSchProxyException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isAuthenticationFailureMessage(String message) {
        if (message == null) {
            return false;
        }
        String value = message.toLowerCase(Locale.ROOT);
        return value.contains("auth fail")
                || value.contains("auth cancel")
                || value.contains("userauth fail")
                || value.contains("authentication fail")
                || value.contains("no more auth methods")
                || value.contains("too many authentication failures");
    }

    /**
     * 初始化 Exec 通道环境。X11 只对交互式 shell 有意义，非交互通道不能请求。
     *
     * @param channel 通道
     */
    public void initEnvironments(ChannelExec channel) {
        this.initEnvironments(channel::setEnv, channel::setAgentForwarding);
    }

    /**
     * 初始化 Shell 通道环境
     *
     * @param channel 通道
     */
    protected void initEnvironments(ChannelShell channel) {
        this.initEnvironments(channel::setEnv, channel::setAgentForwarding);
    }

    /**
     * 初始化 SFTP 通道环境。SFTP 不是交互式 X11 会话，不能请求 X11 转发。
     *
     * @param channel 通道
     */
    public void initEnvironments(ChannelSftp channel) {
        this.initEnvironments(channel::setEnv, channel::setAgentForwarding);
    }

    /**
     * 初始化通道公共环境。不能直接引用 ChannelSession，当前 JSch 版本未公开该类。
     *
     * @param envSetter             环境变量设置器
     * @param agentForwardingSetter agent 转发设置器
     */
    private void initEnvironments(BiConsumer<String, String> envSetter, Consumer<Boolean> agentForwardingSetter) {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                envSetter.accept(entry.getKey(), entry.getValue());
            }
        }
        // 初始化环境变量
        envSetter.accept("PATH", this.getExportPath());
        // 初始化字符集
        envSetter.accept("LANG", "en_US." + this.getCharset().displayName());
        // 客户端转发
        if (this.shellConnect.isForwardAgent()) {
            agentForwardingSetter.accept(true);
        }
    }

    protected volatile ShellServerExec serverExec;

    public synchronized ShellServerExec serverExec() {
        if (this.serverExec == null) {
            this.serverExec = new ShellServerExec(this);
        }
        return this.serverExec;
    }

    /**
     * shell类型
     */
    protected volatile String shellType;

    /**
     * 获取终端类型
     *
     * @return 终端类型
     */
    public synchronized String getShellType() {
        if (this.shellType == null) {
            this.initMetadata();
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
        if (StringUtil.isNotBlank(shellType)) {
            if (shellType.contains("/")) {
                shellType = shellType.substring(shellType.lastIndexOf("/") + 1);
            } else if (shellType.contains("\\")) {
                shellType = shellType.substring(shellType.lastIndexOf("\\") + 1);
            }
        }
        return shellType;
    }

    /**
     * 是否zsh的shell
     *
     * @return 结果
     */
    public boolean isShType() {
        String shellName = this.getShellName();
        return StringUtil.endWithAnyIgnoreCase(shellName, "sh", "sh.exe");
    }

    /**
     * 是否bash的shell
     *
     * @return 结果
     */
    public boolean isBashType() {
        String shellName = this.getShellName();
        return StringUtil.endWithAnyIgnoreCase(shellName, "bash", "bash.exe");
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
     * 是否csh的shell
     *
     * @return 结果
     */
    public boolean isCshType() {
        String shellName = this.getShellName();
        return StringUtil.endWithIgnoreCase(shellName, "csh");
    }

    /**
     * 是否tcsh的shell
     *
     * @return 结果
     */
    public boolean isTcshType() {
        String shellName = this.getShellName();
        return StringUtil.endWithIgnoreCase(shellName, "tcsh");
    }

    /**
     * 是否cmd的shell
     *
     * @return 结果
     */
    public boolean isCmdType() {
        String shellName = this.getShellName();
        return StringUtil.equalsAnyIgnoreCase(shellName, "cmd", "cmd.exe");
    }

    /**
     * 是否powershell的shell
     *
     * @return 结果
     */
    public boolean isPowershellType() {
        String shellName = this.getShellName();
        return StringUtil.equalsAnyIgnoreCase(shellName,
                "pwsh", "pwsh.exe", "powershell", "powershell.exe");
    }

    @Override
    public void close() throws Exception {
        IOUtil.closeQuietly(this.serverExec);
        this.serverExec = null;
    }
}
