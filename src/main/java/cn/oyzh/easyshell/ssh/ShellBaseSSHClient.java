package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.internal.ShellBaseClient;
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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    private final Semaphore execChannelPermits = new Semaphore(4);

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
            ShellSSHClientActionUtil.forAction(this.connectName(), command);
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
            String result;
            // 如果远程是windows，则要检查下字符集是否要指定
            if (StringUtil.isNotBlank(this.remoteCharset)) {
                result = stream.toString(this.remoteCharset);
            } else {
                result = stream.toString(this.getCharset());
            }
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
            Files.createFile(knownHosts);
        } catch (java.nio.file.FileAlreadyExistsException ignored) {
        } catch (Exception ex) {
            throw new JSchException("initialize known_hosts failed: " + knownHosts, ex);
        }
        this.jsch.setKnownHosts(knownHosts.toString());

        if (this.shellConnect.isPasswordAuth()) {
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(new ShellSSHAuthInteractive(this.shellConnect.getPassword()));
        } else if (this.shellConnect.isCertificateAuth()) {
            String priKeyFile = this.shellConnect.getCertificate();
            if (StringUtil.isBlank(priKeyFile) || !Files.isRegularFile(Path.of(priKeyFile))) {
                throw new JSchException("certificate file not found: " + priKeyFile);
            }
            this.jsch.addIdentity(priKeyFile, this.shellConnect.getCertificatePwd());
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(new ShellSSHAuthInteractive(null, this.shellConnect.getCertificatePwd()));
        } else if (this.shellConnect.isSSHAgentAuth()) {
            var repository = JschUtil.initAgentIdentityRepository();
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
            this.session.setUserInfo(new ShellSSHAuthInteractive());
        } else if (this.shellConnect.isManagerAuth()) {
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
            if (key == null) {
                throw new JSchException("key not found: " + this.shellConnect.getKeyId());
            }
            String keyName = "key_" + key.getId();
            this.jsch.addIdentity(keyName, key.getPrivateKeyBytes(), key.getPublicKeyBytes(), key.getPasswordBytes());
            this.session = this.jsch.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(new ShellSSHAuthInteractive(null, key.getPassword()));
        } else {
            throw new JSchException("unsupported ssh authentication type");
        }

        this.initSession();
        this.useCompression();
    }

    protected synchronized void connectSession(int timeout) throws JSchException, AgentProxyException {
        int retryCount = 0;
        while (true) {
            try {
                this.initClient();
                this.session.setTimeout(timeout);
                this.session.connect(timeout);
                this.onSessionConnected();
                return;
            } catch (JSchException ex) {
                if (retryCount++ >= 3 || !this.isAuthenticationFailure(ex) || this.verifyFailureCallback == null) {
                    throw ex;
                }
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
            }
        }
    }

    private boolean isAuthenticationFailure(JSchException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        String value = message.toLowerCase();
        return value.contains("auth fail")
                || value.contains("userauth fail")
                || value.contains("authentication failed")
                || value.contains("no more auth methods available");
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
