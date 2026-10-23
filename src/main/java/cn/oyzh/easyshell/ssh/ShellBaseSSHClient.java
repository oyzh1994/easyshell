package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.ssh.util.JschUtil;
import cn.oyzh.ssh.util.SSHUtil;
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
import java.util.LinkedHashMap;
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
     * 系统类型
     */
    protected String osType;

    /**
     * 会话
     */
    protected volatile Session session;

    protected JSch jsch;

    /**
     * 用户目录
     */
    protected String userHome;

    /**
     * 远程字符集
     */
    protected String remoteCharset;

    /**
     * shell信息
     */
    protected ShellConnect shellConnect;

    /**
     * 环境变量
     */
    protected List<String> environment;

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

    private final Semaphore execChannelPermits = new Semaphore(4);

    /**
     * 当前客户端是否已降级到旧服务端算法。
     */
    private volatile boolean legacyAlgorithmFallback;

    public ShellBaseSSHClient(ShellConnect connect) {
        this.shellConnect = connect;
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    public Session getSession() {
        return session;
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

    @Override
    public abstract void close() throws Exception;

    @Override
    public abstract boolean isConnected();

    /**
     * 获取系统类型
     *
     * @return 系统类型
     */
    protected synchronized String osType() {
        if (this.osType == null) {
            String output = this.exec("uname -s");
            if (StringUtil.isNotBlank(output) && ShellUtil.isWindowsCommandNotFound(output, "uname")) {
                this.osType = this.exec("cmd.exe /c ver");
            } else {
                this.osType = output;
            }
        }
        return this.osType;
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
        return this.exec(command, timeout, false);
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
        return this.exec(command, timeout, true);
    }

    private String exec(String command, int timeout, boolean preferUtf8) {
        // 获取通道
        ChannelExec channel = null;
        ByteArrayOutputStream stream = null;
        boolean permitAcquired = false;
        try {
            long waitTimeout = timeout > 0 ? timeout : this.connectTimeout();
            permitAcquired = this.execChannelPermits.tryAcquire(waitTimeout, TimeUnit.MILLISECONDS);
            if (!permitAcquired) {
                JulLog.warn("ssh exec channel acquire timeout, command: " + command);
                return null;
            }
            channel = this.newExecChannel(command);
            if (channel == null) {
                return null;
            }
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), command);
            stream = new ByteArrayOutputStream();
            channel.setErrStream(stream);
            channel.setInputStream(null);
            channel.setOutputStream(stream);
            channel.connect(timeout > 0 ? Math.min(this.connectTimeout(), timeout) : this.connectTimeout());
            long start = System.currentTimeMillis();
            while (channel.isConnected() && !channel.isClosed()) {
                if (timeout > 0 && System.currentTimeMillis() - start > timeout) {
                    break;
                }
                Thread.sleep(10);
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
            JulLog.warn("ssh exec failed, command: " + command, ex);
        } finally {
            IOUtil.close(stream);
            if (channel != null) {
                channel.disconnect();
            }
            if (permitAcquired) {
                this.execChannelPermits.release();
            }
        }
        return null;
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
    public synchronized String getExportPath() {
        // 初始化环境
        if (this.environment == null) {
            this.initEnvironment();
        }
        StringBuilder builder = new StringBuilder();
        if (this.isWindows()) {
            for (String string : this.environment) {
                builder.append(";").append(string);
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
    protected void initEnvironment() {
        this.environment = new CopyOnWriteArrayList<>();
        if (this.isWindows()) {
            this.environment.add("C:/Windows/System");
            this.environment.add("C:/Windows/System32");
            this.environment.add("C:/Windows/SysWOW64");
            this.environment.add("C:/Program Files");
            this.environment.add("C:/Program Files (x86)");
        } else {
            this.environment.add("/bin");
            this.environment.add("/sbin");
            this.environment.add("/usr/bin");
            this.environment.add("/usr/sbin");
            this.environment.add("/usr/games");
            this.environment.add("/usr/local/bin");
            this.environment.add("/usr/local/sbin");
            this.environment.add("/usr/local/games");
        }
        if (this.isMacos()) {
            this.environment.add("/Applications/Docker.app/Contents/Resources/bin/");
        }
        String remoteCharset = this.getRemoteCharset();
        if (JulLog.isInfoEnabled()) {
            JulLog.info("remote charset: {}", remoteCharset);
        }
    }

    /**
     * 是否macos系统
     *
     * @return 结果
     */
    public boolean isMacos() {
        return StringUtil.containsAnyIgnoreCase(this.osType(), "Darwin", "Macos", "OS X");
    }

    /**
     * 是否linux系统
     *
     * @return 结果
     */
    public boolean isLinux() {
        return StringUtil.containsIgnoreCase(this.osType(), "Linux");
    }

    /**
     * 是否unix系统
     *
     * @return 结果
     */
    public boolean isUnix() {
        return StringUtil.containsAnyIgnoreCase(this.osType(), "FreeBSD", "Aix", "HP-UX", "SunOS");
    }

    /**
     * 是否freebsd系统
     *
     * @return 结果
     */
    public boolean isFreeBSD() {
        return StringUtil.containsIgnoreCase(this.osType(), "FreeBSD");
    }

    /**
     * 是否windows系统
     *
     * @return 结果
     */
    public boolean isWindows() {
        return StringUtil.containsAnyIgnoreCase(this.osType(), "Windows", "MINGW64_NT", "MSYS_NT", "CYGWIN_NT");
    }

    /**
     * 获取远程字符集
     *
     * @return 远程字符集
     */
    public synchronized String getRemoteCharset() {
        if (this.remoteCharset == null) {
            if (this.isWindows()) {
                String output = this.exec("chcp");
                this.remoteCharset = ShellUtil.getCharsetFromChcp(output);
            } else if (this.isUnix() || this.isMacos() || this.isLinux()) {
                String output = this.exec("echo $LANG");
                this.remoteCharset = ShellUtil.getCharsetFromLang(output);
            }
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
    public synchronized String getUserHome() {
        if (this.userHome == null) {
            if (this.isWindows()) {
                this.userHome = this.exec("echo %HOME%");
                this.userHome += "\\";
            } else {
                this.userHome = this.exec("echo $HOME");
                this.userHome += "/";
            }
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
            // 每60秒发送一次TCP keep-alive包
            this.session.setServerAliveInterval(60_000);
            // 可选：设置TCP层面的keep-alive
            this.session.setConfig("TCPKeepAlive", "yes");
            // 与旧实现保持一致：跳板转发使用临时本地端口，不参与 known_hosts 校验。
            this.session.setConfig("StrictHostKeyChecking", this.shellConnect.isEnableJump() ? "no" : "ask");
            // 设置线程工厂
            this.session.setThreadFactory(ThreadUtil::newThreadVirtual);
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
    protected synchronized ChannelExec newExecChannel(String command) {
        ChannelExec channel = null;
        try {
            this.ensureSessionConnected(this.connectTimeout());
            channel = (ChannelExec) this.session.openChannel("exec");
            if (channel == null) {
                throw new JSchException("create ssh exec channel returned null");
            }
            channel.setInputStream(null);
            channel.setOutputStream(null);
            channel.setCommand(this.resolveExecCommand(command));
            //// 获取连接
            // ShellConnect shellConnect = this.getShellConnect();
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
            //// 客户端转发
            // if (shellConnect.isJumpForward()) {
            //    channel.setAgentForwarding(true);
            //}
            //// x11转发
            // if (shellConnect.isX11forwarding()) {
            //    channel.setXForwarding(true);
            //}
            this.initEnvironments(channel);
            return channel;
        } catch (Exception ex) {
            if (channel != null) {
                channel.disconnect();
            }
            JulLog.warn("create ssh exec channel failed", ex);
        }
        return null;
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
        if (this.isMacos() || this.isLinux() || this.isUnix()) {
            return "export PATH=" + this.getExportPath() + "; " + command;
        }
        return command;
    }

    protected synchronized void ensureSessionConnected(int timeout) throws JSchException, AgentProxyException {
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

        if (this.shellConnect.isPasswordAuth()) {
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(this.newAuthInteractive(this.shellConnect.getPassword(), null));
        } else if (this.shellConnect.isCertificateAuth()) {
            String priKeyFile = this.shellConnect.getCertificate();
            if (StringUtil.isBlank(priKeyFile) || !Files.isRegularFile(Path.of(priKeyFile))) {
                throw new JSchException("certificate file not found: " + priKeyFile);
            }
            this.jsch.addIdentity(priKeyFile, this.shellConnect.getCertificatePwd());
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
                this.osType = null;
                this.userHome = null;
                this.remoteCharset = null;
                this.environment = null;
                this.agentIdentityRepository = null;
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

    private IdentityRepository getAgentIdentityRepository() throws AgentProxyException {
        IdentityRepository repository = this.agentIdentityRepository;
        if (repository == null) {
            synchronized (this) {
                repository = this.agentIdentityRepository;
                if (repository == null) {
                    repository = JschUtil.initAgentIdentityRepository();
                    this.agentIdentityRepository = repository;
                }
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
     */
    public Map<String, String> initEnvironments() {
        Map<String, String> environments = new LinkedHashMap<>();
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            environments.putAll(userEnvs);
        }
        if (this.osType != null) {
            environments.put("PATH", this.getExportPath());
        }
        environments.put("LANG", "en_US." + this.getCharset().displayName());
        return environments;
    }

    /**
     * 初始化环境
     *
     * @param channel 通道
     */
    public void initEnvironments(ChannelExec channel) {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                channel.setEnv(entry.getKey(), entry.getValue());
            }
        }
        // 初始化环境变量
        if (this.osType != null) {
            channel.setEnv("PATH", this.getExportPath());
        }
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

    /**
     * 初始化环境
     *
     * @param channel 通道
     */
    public void initEnvironments(ChannelShell channel) {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                channel.setEnv(entry.getKey(), entry.getValue());
            }
        }
        // 初始化环境变量
        if (this.osType != null) {
            channel.setEnv("PATH", this.getExportPath());
        }
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

    /**
     * 初始化环境
     *
     * @param channel 通道
     */
    public void initEnvironments(ChannelSftp channel) {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                channel.setEnv(entry.getKey(), entry.getValue());
            }
        }
        // 初始化环境变量
        if (this.osType != null) {
            channel.setEnv("PATH", this.getExportPath());
        }
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
}
