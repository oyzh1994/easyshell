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
import cn.oyzh.easyshell.ssh.server.ShellServerExec;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.ssh.util.JschUtil;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSession;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
    protected static final int MAX_DYNAMIC_CHANNELS = 6;

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
        if (this.osType != null) {
            return;
        }
        String type = this.execMetadata("uname -s");
        if (StringUtil.isNotBlank(type) && ShellUtil.isWindowsCommandNotFound(type, "uname")) {
            type = this.execMetadataUtf8("cmd.exe /c ver");
        }
        this.osType = type;
        JulLog.info("os type:{}", type);
        String charset = null;
        if (this.isWindows()) {
            charset = ShellUtil.getCharsetFromChcp(this.execMetadata("chcp"));
        } else if (this.isLinux() || this.isMacos() || this.isUnix()) {
            charset = ShellUtil.getCharsetFromLang(this.execMetadata("echo $LANG"));
        }
        this.remoteCharset = charset;
        if (JulLog.isInfoEnabled()) {
            JulLog.info("remote charset: {}", charset);
        }
        String userHome = this.execMetadata(this.isWindows() ? "echo %HOME%" : "echo $HOME");
        if (StringUtil.isNotBlank(userHome)) {
            this.userHome = userHome + (this.isWindows() ? "\\" : "/");
        }
        if (JulLog.isInfoEnabled()) {
            JulLog.info("user home: {}", this.userHome);
        }
        this.initEnvironment();
        if (JulLog.isInfoEnabled()) {
            JulLog.info("environment: {}", this.environment);
        }
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
    protected String execMetadata(String command) {
        return this.execInternal(command, this.connectTimeout(), false, false);
    }

    /**
     * 执行元数据探测命令。探测命令不注入 PATH，也不触发元数据初始化，避免递归占用通道。
     */
    protected String execMetadataUtf8(String command) {
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
     * 获取path变量
     *
     * @return 结果
     */
    public String getExportPath() {
        if (this.osType == null) {
            return "";
        }
        this.initEnvironment();
        StringBuilder builder = new StringBuilder();
        if (this.isWindows()) {
            for (String string : this.environment) {
                builder.append(";").append(string);
            }
            if (this.shellType != null) {
                if (this.isCmdType()) {
                    builder.append(";%PATH%");
                } else if (this.isPowershellType()) {
                    builder.append(";$env:PATH");
                }
            }
        } else {
            for (String string : this.environment) {
                builder.append(":").append(string);
            }
            builder.append(":$PATH");
        }
        return builder.substring(1);
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
        return StringUtil.containsAnyIgnoreCase(value, "FreeBSD", "Aix", "HP-UX", "SunOS");
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
     * 启用压缩
     */
    protected void useCompression() {
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
        if (this.isMacos() || this.isUnix() || this.isLinux()) {
            return "env PATH=\"" + this.getExportPath() + "\" " + command;
        }
        if (this.isWindows()) {
            if (this.isCmdType()) {
                return "set \"PATH=" + this.getExportPath() + ";\" & " + command;
            }
            if (this.isPowershellType()) {
                return "$env:PATH=\"" + this.getExportPath() + ";\"; " + command;
            }
            if (this.isShType() || this.isBashType()) {
                return "$env:PATH=\"" + this.getExportPath() + ";\"; " + command;
            }
        }
        return command;
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

        this.initSession();
        this.useCompression();
    }

    private ShellSSHAuthInteractive newAuthInteractive(String password, String passphrase) {
        ShellSSHAuthInteractive interactive = new ShellSSHAuthInteractive(password, passphrase);
        interactive.setVerificationCodeSupplier(this.verificationCodeSupplier);
        interactive.setHostKeyConfirmCallback(this.hostKeyConfirmCallback);
        return interactive;
    }

    protected synchronized void connectSession(int timeout) throws JSchException, AgentProxyException {
        int retryCount = 0;
        while (true) {
            try {
                this.initClient();
                this.session.setTimeout(timeout);
                this.session.connect(timeout);
                this.configureAgentForwarding();
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
    private void configureAgentForwarding() throws AgentProxyException {
        if (!this.shellConnect.isForwardAgent() || this.session == null) {
            return;
        }
        this.session.setIdentityRepository(this.getAgentIdentityRepository());
    }

    private synchronized IdentityRepository getAgentIdentityRepository() throws AgentProxyException {
        IdentityRepository repository = this.agentIdentityRepository;
        if (repository == null) {
            repository = this.agentIdentityRepository;
            if (repository == null) {
                repository = JschUtil.initAgentIdentityRepository();
                this.agentIdentityRepository = repository;
            }
        }
        return repository;
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
     * 初始化环境
     *
     * @param channel 通道
     */
    public void initEnvironments(ChannelSession channel) {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                channel.setEnv(entry.getKey(), entry.getValue());
            }
        }
        // 初始化环境变量
        channel.setEnv("PATH", this.getExportPath());
        // 初始化字符集
        channel.setEnv("LANG", "en_US." + this.getCharset().displayName());
        // 客户端转发
        if (this.shellConnect.isForwardAgent()) {
            channel.setAgentForwarding(true);
        }
        // x11转发
        if (this.shellConnect.isX11forwarding()) {
            channel.setXForwarding(true);
        }
    }

    protected ShellServerExec serverExec;

    public synchronized ShellServerExec serverExec() {
        if (this.serverExec == null) {
            this.serverExec = new ShellServerExec(this);
        }
        return this.serverExec;
    }

    /**
     * shell类型
     */
    protected String shellType;

    /**
     * 获取终端类型
     *
     * @return 终端类型
     */
    public synchronized String getShellType() {
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
        return StringUtil.containsAnyIgnoreCase(shellName, "cmd", "cmd.exe");
    }

    /**
     * 是否powershell的shell
     *
     * @return 结果
     */
    public boolean isPowershellType() {
        String shellName = this.getShellName();
        return StringUtil.containsAnyIgnoreCase(shellName, "powershell", "powershell.exe");
    }

    @Override
    public void close() throws Exception {
        IOUtil.closeQuietly(this.serverExec);
        this.serverExec = null;
    }
}
