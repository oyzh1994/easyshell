package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellProxyConfigStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.util.SSHAgentConnectorFactory;
import cn.oyzh.ssh.util.SSHKeyUtil;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractiveFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.kex.DHGClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientProxyConnector;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.x11.X11ChannelFactory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.channel.ChannelFactory;
import org.apache.sshd.common.compression.BuiltinCompressions;
import org.apache.sshd.common.compression.Compression;
import org.apache.sshd.common.global.KeepAliveHandler;
import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.common.kex.KeyExchangeFactory;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.core.CoreModuleProperties;
import org.eclipse.jgit.internal.transport.sshd.agent.JGitSshAgentFactory;
import org.eclipse.jgit.internal.transport.sshd.proxy.HttpClientConnector;
import org.eclipse.jgit.internal.transport.sshd.proxy.Socks5ClientConnector;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.sshd.KeyPasswordProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * shell客户端
 *
 * @author oyzh
 * @since 2025/04/25
 */
public abstract class ShellBaseSSHClient implements BaseClient {

    /**
     * 系统类型
     */
    protected String osType;

    /**
     * ssh客户端
     */
    protected ShellSSHJGitClient sshClient;

    /**
     * 会话
     */
    protected ClientSession session;

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
     * 代理配置存储
     */
    private final ShellProxyConfigStore proxyConfigStore = ShellProxyConfigStore.INSTANCE;

    public ShellBaseSSHClient(ShellConnect connect) {
        this.shellConnect = connect;
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    public ClientSession getSession() {
        return session;
    }

    /**
     * 获取系统类型
     *
     * @return 系统类型
     */
    protected String osType() {
        if (this.osType == null) {
            String output = this.exec("which");
            if (StringUtil.isNotBlank(output) && ShellUtil.isWindowsCommandNotFound(output, "which")) {
                this.osType = "Windows";
            } else {
                this.osType = this.exec("uname");
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
        // 获取通道
        ChannelExec channel = this.newExecChannel(command);
        try {
            // 操作
            ShellSSHClientActionUtil.forAction(this.connectName(), command);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setIn(null);
            channel.setErr(stream);
            channel.setOut(stream);
            while (!channel.isClosed()) {
                Thread.sleep(1);
            }
            String result;
            // 如果远程是windows，则要检查下字符集是否要指定
            if (StringUtil.isNotBlank(this.remoteCharset)) {
                result = stream.toString(this.remoteCharset);
            } else {
                result = stream.toString();
            }
            IOUtil.close(stream);
            if (StringUtil.endsWith(result, "\r\n")) {
                result = result.substring(0, result.length() - 2);
            } else if (StringUtil.endWithAny(result, "\n", "\r")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channel != null) {
                IOUtil.close(channel);
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
        // 初始化环境
        if (this.environment == null) {
            this.initEnvironment();
        }
        StringBuilder builder = new StringBuilder();
        if (this.isWindows()) {
            for (String string : this.environment) {
                builder.append(string).append(";");
            }
            builder.deleteCharAt(builder.length() - 1);
        } else {
            for (String string : this.environment) {
                builder.append(":").append(string);
            }
        }
        return builder.toString();
    }

    /**
     * 初始化环境
     */
    protected void initEnvironment() {
        this.environment = new ArrayList<>();
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
        return StringUtil.containsIgnoreCase(this.osType(), "Darwin");
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
        return StringUtil.containsAnyIgnoreCase(this.osType(), "FreeBSD", "Aix");
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
        return StringUtil.equals(this.osType(), "Windows");
    }

    /**
     * 获取远程字符集
     *
     * @return 远程字符集
     */
    public String getRemoteCharset() {
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
    public String getUserHome() {
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
    protected void initSession() {
        if (this.session != null) {
            // // 设置守护线程
            // this.session.setDaemonThread(true);
            // // 连续3次失败后断开连接
            // this.session.setServerAliveCountMax(3);
            // // 每60秒发送一次TCP keep-alive包
            // this.session.setServerAliveInterval(60_000);
            // // 可选：设置TCP层面的keep-alive
            // this.session.setConfig("TCPKeepAlive", "yes");
            // // 去掉首次连接确认
            // this.session.setConfig("StrictHostKeyChecking", "no");
            // // 设置线程工厂
            // this.session.setThreadFactory(ThreadUtil::newThreadVirtual);
        }
    }

    /**
     * 启用压缩
     */
    protected void useCompression() {
        if (this.session != null) {
            // // 启用压缩
            // if (this.shellConnect.isEnableCompress()) {
            //     this.session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
            //     this.session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
            //     // 设置压缩级别（可选，范围 1-9，默认 6）
            //     this.session.setConfig("Compression", "yes");
            //     this.session.setConfig("CompressionLevel", "9");
            //     this.session.setConfig("compression.level", "9");
            // } else {
            //     this.session.setConfig("Compression", "no");
            //     this.session.setConfig("compression.s2c", "none");
            //     this.session.setConfig("compression.c2s", "none");
            // }
        }
    }

    /**
     * 创建exec通道
     *
     * @return exec通道
     */
    protected ChannelExec newExecChannel(String command) {
        try {
            // 获取会话
            ClientSession session = this.takeSession(this.connectTimeout());
            // 创建shell
            ChannelExec channel = session.createExecChannel(command, null, this.initEnvironments());
            channel.setIn(null);
            channel.setOut(null);
            channel.setErr(null);
            channel.open().verify(this.connectTimeout());
            return channel;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    protected String initHost() {
        return this.shellConnect.getHost();
    }

    /**
     * 初始化代理
     *
     * @return 客户端代理连接器
     */
    private ClientProxyConnector initProxy() {
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
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        int proxyPort = proxyConfig.getPort();
        String proxyHost = proxyConfig.getHost();
        String proxyUser = StringUtil.isBlank(proxyConfig.getUser()) ? null : proxyConfig.getUser();
        char[] proxyPassword = StringUtil.isBlank(proxyConfig.getPassword()) ? null : proxyConfig.getPassword().toCharArray();
        ClientProxyConnector connector = null;
        if (proxyConfig.isHttpProxy()) {
            connector = new HttpClientConnector(
                    new InetSocketAddress(proxyHost, proxyPort),
                    new InetSocketAddress(hostIp, port),
                    proxyUser,
                    proxyPassword
            );
        } else if (proxyConfig.isSocksProxy()) {
            connector = new Socks5ClientConnector(
                    new InetSocketAddress(proxyHost, proxyPort),
                    new InetSocketAddress(hostIp, port),
                    proxyUser,
                    proxyPassword
            );
        }
        return connector;
    }

    /**
     * 初始化客户端
     */
    protected void initClient(int timeout) throws Exception {
        // 客户端构建器
        ClientBuilder builder = new ClientBuilder();
        // 保持连接
        builder.globalRequestHandlers(List.of(KeepAliveHandler.INSTANCE));
        // 客户端
        builder.factory(ShellSSHJGitClient::new);

        // key交换工厂
        List<KeyExchangeFactory> keyExchangeFactories = ClientBuilder.setUpDefaultKeyExchanges(true);
        keyExchangeFactories.add(DHGClient.newFactory(BuiltinDHFactories.dhg1));
        keyExchangeFactories.add(DHGClient.newFactory(BuiltinDHFactories.dhg14));
        keyExchangeFactories.add(DHGClient.newFactory(BuiltinDHFactories.dhgex));
        builder.keyExchangeFactories(keyExchangeFactories);

        // 压缩工厂
        List<NamedFactory<Compression>> compressionFactories = ClientBuilder.setUpDefaultCompressionFactories(true);
        compressionFactories.add(BuiltinCompressions.none);
        compressionFactories.add(BuiltinCompressions.zlib);
        compressionFactories.add(BuiltinCompressions.delayedZlib);
        builder.compressionFactories(compressionFactories);

        // 签名工厂
        List<NamedFactory<Signature>> signatureFactories = ClientBuilder.setUpDefaultSignatureFactories(true);
        for (BuiltinSignatures signature : BuiltinSignatures.values()) {
            if (!signatureFactories.contains(signature)) {
                signatureFactories.add(signature);
            }
        }
        builder.signatureFactories(signatureFactories);

        // 通道工厂
        List<ChannelFactory> channelFactories = new ArrayList<>(ClientBuilder.DEFAULT_CHANNEL_FACTORIES);
        // x11处理
        if (this.shellConnect.isX11forwarding()) {
            channelFactories.add(X11ChannelFactory.INSTANCE);
        }
        builder.channelFactories(channelFactories);

        // 创建客户端
        this.sshClient = (ShellSSHJGitClient) builder.build();
        // ssh agent处理
        if (this.shellConnect.isSSHAgentAuth()) {
            this.sshClient.setAgentFactory(new JGitSshAgentFactory(SSHAgentConnectorFactory.INSTANCE, null));
        }
        // 代理处理
        if (this.shellConnect.isEnableProxy()) {
            this.sshClient.setClientProxyConnector(this.initProxy());
            // 设置代理参数
            this.sshClient.setProxyHost(this.shellConnect.getProxyConfig().getHost());
            this.sshClient.setProxyPort(this.shellConnect.getProxyConfig().getPort());
        }
        // 优先的认证方式
        String methods = UserAuthPasswordFactory.PASSWORD;
        // 密码
        if (this.shellConnect.isPasswordAuth()) {
            methods = ArrayUtil.join(new String[]{UserAuthPasswordFactory.KB_INTERACTIVE, UserAuthPasswordFactory.PASSWORD}, ",");
            this.sshClient.addPasswordIdentity(this.shellConnect.getPassword());
        } else if (this.shellConnect.isSSHAgentAuth()) {// ssh agent
            methods = ArrayUtil.join(new String[]{UserAuthPasswordFactory.PUBLIC_KEY, UserAuthPasswordFactory.PASSWORD, UserAuthPasswordFactory.KB_INTERACTIVE}, ",");
        } else if (this.shellConnect.isCertificateAuth()) {// 证书、密钥
            methods = UserAuthPasswordFactory.PUBLIC_KEY;
            String priKeyFile = this.shellConnect.getCertificate();
            // 检查私钥是否存在
            if (!FileUtil.exist(priKeyFile)) {
                MessageBox.warn("certificate file not exist");
                throw new IOException("certificate file not exist");
            }
            // 加载证书
            Iterable<KeyPair> keyPairs = SSHKeyUtil.loadKeysFromFile(priKeyFile, this.shellConnect.getCertificatePwd());
            //  设置证书认证
            for (KeyPair keyPair : keyPairs) {
                this.sshClient.addPublicKeyIdentity(keyPair);
            }
        } else if (this.shellConnect.isManagerAuth()) {
            methods = UserAuthPasswordFactory.PUBLIC_KEY;
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
            // 检查私钥是否存在
            if (key == null) {
                MessageBox.warn("key not found");
                throw new IOException("key not found");
            }
            // 加载证书
            Iterable<KeyPair> keyPairs = SSHKeyUtil.loadKeysForStr(key.getPrivateKey(), key.getPassword());
            //  设置证书认证
            for (KeyPair keyPair : keyPairs) {
                this.sshClient.addPublicKeyIdentity(keyPair);
            }
        }
        // 设置优先认证方式
        CoreModuleProperties.PREFERRED_AUTHS.set(this.sshClient, methods);
        // 设置认证工厂
        this.sshClient.setUserAuthFactories(List.of(
                UserAuthKeyboardInteractiveFactory.INSTANCE,
                UserAuthPasswordFactory.INSTANCE,
                UserAuthPublicKeyFactory.INSTANCE
        ));
        // 交互式认证
        this.sshClient.setUserInteraction(new ShellSSHAuthInteractive(this.shellConnect.getPassword()));
        // 启动客户端
        this.sshClient.start();
        // 测试环境使用，生产环境需替换
        this.sshClient.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        // 设置密码工厂
        this.sshClient.setKeyPasswordProviderFactory(() -> (KeyPasswordProvider) CredentialsProvider.getDefault());
        //  获取会话
        this.session = this.takeSession(timeout);
    }

    /**
     * 获取会话
     *
     * @param timeout 超时时间
     */
    protected synchronized ClientSession takeSession(int timeout) throws Exception {
        // 返回已有会话
        if (this.session != null && this.session.isOpen()) {
            return this.session;
        }

        // 会话连接信息
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);

        // 会话连接参数
        HostConfigEntry entry = new HostConfigEntry();
        entry.setPort(port);
        entry.setHostName(hostIp);
        entry.setUsername(this.shellConnect.getUser());

        // 创建会话连接
        ConnectFuture future = this.sshClient.connect(entry);
        // ConnectFuture future = this.sshClient.connect(this.shellConnect.getUser(), hostIp, port);
        // 创建会话
        ClientSession session = future.verify(timeout).getClientSession();
        // 心跳
        session.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, Duration.ofSeconds(60));
        // // 密码
        // if (this.shellConnect.isPasswordAuth()) {
        //     // 设置地址和端口
        //     // session.setUserInteraction(new ShellSSHAuthInteractive(this.shellConnect.getPassword()));
        // } else if (this.shellConnect.isCertificateAuth()) {// 证书
        //     String priKeyFile = this.shellConnect.getCertificate();
        //     // 检查私钥是否存在
        //     if (!FileUtil.exist(priKeyFile)) {
        //         MessageBox.warn("certificate file not exist");
        //         throw new IOException("certificate file not exist");
        //     }
        //     // 加载证书
        //     Iterable<KeyPair> keyPairs = SSHKeyUtil.loadKeysFromFile(priKeyFile, this.shellConnect.getCertificatePwd());
        //     //  设置证书认证
        //     for (KeyPair keyPair : keyPairs) {
        //         session.addPublicKeyIdentity(keyPair);
        //     }
        // } else if (this.shellConnect.isManagerAuth()) {// 密钥
        //     ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
        //     // 检查私钥是否存在
        //     if (key == null) {
        //         MessageBox.warn("key not found");
        //         throw new IOException("key not found");
        //     }
        //     // 加载证书
        //     Iterable<KeyPair> keyPairs = SSHKeyUtil.loadKeysForStr(key.getPrivateKey(), key.getPassword());
        //     //  设置证书认证
        //     for (KeyPair keyPair : keyPairs) {
        //         session.addPublicKeyIdentity(keyPair);
        //     }
        // }
        // 认证
        session.auth().verify(timeout);
        // 初始化会话
        this.initSession();
        // 启用压缩
        this.useCompression();
        // 设置会话
        this.session = session;
        // 返回会话
        return session;
    }

    /**
     * 初始化环境
     */
    public Map<String, String> initEnvironments() {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        // 初始化环境变量
        if (this.osType != null) {
            userEnvs.put("PATH", this.getExportPath());
        }
        // 初始化字符集
        userEnvs.put("LANG", "en_US." + this.getCharset().displayName());
        return userEnvs;
    }

    @Override
    public void close() throws Exception {
        // 销毁会话
        if (this.session != null) {
            IOUtil.close(this.session);
            this.session = null;
        }
        // 销毁客户端
        if (this.sshClient != null) {
            IOUtil.close(this.sshClient);
            this.sshClient = null;
        }
    }
}
