package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellProxyConfigStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.util.SSHAgentConnectorFactory;
import cn.oyzh.ssh.util.SSHKeyUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshException;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * shell客户端
 *
 * @author oyzh
 * @since 2025/04/25
 */
public abstract class ShellBaseSSHClient implements ShellBaseClient {

    /**
     * 会话
     */
    protected ClientSession session;

    /**
     * ssh客户端
     */
    protected ShellSSHJGitClient sshClient;

    /**
     * 系统类型
     */
    protected String osType;

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
    protected synchronized String osType() {
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
     * @param timeout 超时时间
     * @return 结果
     */
    public String exec(String command, int timeout) {
        // DownLatch latch = DownLatch.of();
        // AtomicReference<String> result = new AtomicReference<>();
        // ThreadUtil.start(() -> {
        //     try {
        //         String res = this.exec(command);
        //         result.set(res);
        //     } finally {
        //         latch.countDown();
        //     }
        // });
        // latch.await(timeout);
        // return result.get();
        // 通道
        ChannelExec channel = null;
        try {
            // JulLog.info("exec command:{}", command);
            // 获取通道
            channel = this.newExecChannel(command);
            // 操作
            ShellClientActionUtil.forAction(this.connectName(), command);
            if (channel != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                channel.setIn(null);
                channel.setOut(stream);
                channel.setErr(stream);
                // 开始时间
                long start = 0L;
                if (timeout > 0) {
                    start = System.currentTimeMillis();
                }
                while (channel.isOpen()) {
                    ThreadUtil.sleep(5);
                    // 检查超时
                    if (timeout > 0) {
                        // 当前时间
                        long now = System.currentTimeMillis();
                        if (now - start > timeout) {
                            break;
                        }
                    }
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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            IOUtil.close(channel);
        }
        return null;
    }

    /**
     * 执行命令
     *
     * @param command 命令
     * @return 结果
     */
    public String exec(String command) {
        // // 通道
        // ChannelExec channel = null;
        // try {
        //     // JulLog.info("exec command:{}", command);
        //     // 获取通道
        //     channel = this.newExecChannel(command);
        //     // 操作
        //     ShellClientActionUtil.forAction(this.connectName(), command);
        //     if (channel != null) {
        //         ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //         channel.setIn(null);
        //         channel.setOut(stream);
        //         channel.setErr(stream);
        //         // 开始时间
        //         long start = System.currentTimeMillis();
        //         while (channel.isOpen()) {
        //             ThreadUtil.sleep(5);
        //             // 当前时间
        //             long now = System.currentTimeMillis();
        //             // 超时
        //             if (now - start > this.connectTimeout()) {
        //                 break;
        //             }
        //         }
        //         String result;
        //         // 如果远程是windows，则要检查下字符集是否要指定
        //         if (StringUtil.isNotBlank(this.remoteCharset)) {
        //             result = stream.toString(this.remoteCharset);
        //         } else {
        //             result = stream.toString();
        //         }
        //         IOUtil.close(stream);
        //         if (StringUtil.endsWith(result, "\r\n")) {
        //             result = result.substring(0, result.length() - 2);
        //         } else if (StringUtil.endWithAny(result, "\n", "\r")) {
        //             result = result.substring(0, result.length() - 1);
        //         }
        //         return result;
        //     }
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        // } finally {
        //     IOUtil.close(channel);
        // }
        return this.exec(command, -1);
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
    protected synchronized void initEnvironment() {
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
     * 创建exec通道
     *
     * @return exec通道
     */
    protected ChannelExec newExecChannel(String command) throws Exception {
        // 获取会话
        ClientSession session = this.takeSession(this.connectTimeout());
        // 针对macos、linux、unix，修正命令
        if (this.osType != null && (this.isMacos() || this.isLinux() || this.isUnix())) {
            command = "export PATH=" + this.getExportPath() + "; " + command;
        }
        // 创建shell
        ChannelExec channel = session.createExecChannel(command, null, this.initEnvironments());
        // 执行验证
        if (channel != null) {
            channel.open().verify(this.connectTimeout());
        }
        return channel;
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
     *
     * @param timeout 超时时间
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
        if (this.shellConnect.isEnableCompress()) {
            List<NamedFactory<Compression>> compressionFactories = ClientBuilder.setUpDefaultCompressionFactories(true);
            compressionFactories.add(BuiltinCompressions.none);
            compressionFactories.add(BuiltinCompressions.zlib);
            compressionFactories.add(BuiltinCompressions.delayedZlib);
            builder.compressionFactories(compressionFactories);
        }

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
        if (this.shellConnect.isSSHAgentAuth() || this.shellConnect.isForwardAgent()) {
            this.sshClient.setAgentFactory(new SSHAgentConnectorFactory());
        }
        // 代理处理
        if (this.shellConnect.isEnableProxy()) {
            this.sshClient.setClientProxyConnector(this.initProxy());
            // 设置代理参数
            this.sshClient.setProxyHost(this.shellConnect.getProxyConfig().getHost());
            this.sshClient.setProxyPort(this.shellConnect.getProxyConfig().getPort());
            // ShellProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
            // Proxy proxy = ShellProxyUtil.initProxy1(proxyConfig);
            // String proxyUser = StringUtil.isBlank(proxyConfig.getUser()) ? null : proxyConfig.getUser();
            // char[] proxyPassword = StringUtil.isBlank(proxyConfig.getPassword()) ? null : proxyConfig.getPassword().toCharArray();
            // ProxyData proxyData = new ProxyData(proxy, proxyUser, proxyPassword);
            // this.sshClient.setProxyDataFactory(new DefaultProxyDataFactory() {
            //    @Override
            //    public ProxyData get(InetSocketAddress remoteAddress) {
            //        return proxyData;
            //    }
            //});
        }
        // 优先的认证方式
        String methods;
//        // 密码
//        if (this.shellConnect.isPasswordAuth()) {
//            methods = ArrayUtil.join(new String[]{UserAuthPasswordFactory.KB_INTERACTIVE, UserAuthPasswordFactory.PASSWORD}, ",");
//            // this.sshClient.addPasswordIdentity(this.shellConnect.getPassword());
//        } else if (this.shellConnect.isSSHAgentAuth()) {// ssh agent
//            methods = ArrayUtil.join(new String[]{UserAuthPasswordFactory.PUBLIC_KEY, UserAuthPasswordFactory.PASSWORD, UserAuthPasswordFactory.KB_INTERACTIVE}, ",");
//        } else if (this.shellConnect.isCertificateAuth()) {// 证书
//            methods = UserAuthPasswordFactory.PUBLIC_KEY;
//            // String priKeyFile = this.shellConnect.getCertificate();
//            // // 检查私钥是否存在
//            // if (!FileUtil.exist(priKeyFile)) {
//            //     MessageBox.warn("certificate file not exist");
//            //     throw new IOException("certificate file not exist");
//            // }
//            // // 加载证书
//            // Iterable<KeyPair> keyPairs = SSHKeyUtil.loadKeysFromFile(priKeyFile, this.shellConnect.getCertificatePwd());
//            // //  设置证书认证
//            // for (KeyPair keyPair : keyPairs) {
//            //     this.sshClient.addPublicKeyIdentity(keyPair);
//            // }
//        } else if (this.shellConnect.isManagerAuth()) {// 密钥
//            methods = UserAuthPasswordFactory.PUBLIC_KEY;
//            // ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
//            // // 检查私钥是否存在
//            // if (key == null) {
//            //     MessageBox.warn("key not found");
//            //     throw new IOException("key not found");
//            // }
//            // // 加载证书
//            // Iterable<KeyPair> keyPairs = SSHKeyUtil.loadKeysForStr(key.getPrivateKey(), key.getPassword());
//            // //  设置证书认证
//            // for (KeyPair keyPair : keyPairs) {
//            //     this.sshClient.addPublicKeyIdentity(keyPair);
//            // }
//        }
        // 公钥、ssh agent
        if (this.shellConnect.isCertificateAuth() || this.shellConnect.isSSHAgentAuth()) {
            methods = ArrayUtil.join(new String[]{UserAuthPasswordFactory.KB_INTERACTIVE, UserAuthPasswordFactory.PUBLIC_KEY, UserAuthPasswordFactory.PASSWORD}, ",");
        } else {// 密码
            methods = ArrayUtil.join(new String[]{UserAuthPasswordFactory.KB_INTERACTIVE, UserAuthPasswordFactory.PASSWORD, UserAuthPasswordFactory.PUBLIC_KEY}, ",");
        }
        // 设置优先认证方式
        CoreModuleProperties.PREFERRED_AUTHS.set(this.sshClient, methods);
        // 设置认证工厂
        this.sshClient.setUserAuthFactories(List.of(
                UserAuthPasswordFactory.INSTANCE,
                UserAuthPublicKeyFactory.INSTANCE,
                UserAuthKeyboardInteractiveFactory.INSTANCE
        ));
        // 交互式认证
        this.sshClient.setUserInteraction(new ShellSSHAuthInteractive(this.shellConnect.getPassword()));
        // 测试环境使用，生产环境需替换
        this.sshClient.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        // 设置密钥工厂
        this.sshClient.setKeyPasswordProviderFactory(() -> (KeyPasswordProvider) CredentialsProvider.getDefault());
        // 心跳
        this.sshClient.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, Duration.ofSeconds(60));
        // 其他参数
        CoreModuleProperties.SOCKET_KEEPALIVE.set(this.sshClient, true);
        CoreModuleProperties.ALLOW_DHG1_KEX_FALLBACK.set(this.sshClient, true);
        CoreModuleProperties.HEARTBEAT_INTERVAL.set(this.sshClient, Duration.ofSeconds(60));
        CoreModuleProperties.IO_CONNECT_TIMEOUT.set(this.sshClient, Duration.ofMillis(timeout));
        CoreModuleProperties.FORWARD_REQUEST_TIMEOUT.set(this.sshClient, Duration.ofMillis(timeout));
//        // 3秒认证超时
//        CoreModuleProperties.AUTH_TIMEOUT.set(this.sshClient, Duration.ofMillis(3000));
        // 启动客户端
        this.sshClient.start();
    }

    /**
     * 认证失败回调
     */
    protected Function<ShellConnect, ShellConnect> verifyFailureCallback = ShellSSHUtil::onVerifyFailure;

    public void setVerifyFailureCallback(Function<ShellConnect, ShellConnect> verifyFailureCallback) {
        this.verifyFailureCallback = verifyFailureCallback;
    }

    public Function<ShellConnect, ShellConnect> getVerifyFailureCallback() {
        return verifyFailureCallback;
    }

    /**
     * 获取会话
     *
     * @param timeout 超时时间
     */
    protected synchronized ClientSession takeSession(int timeout) throws Exception {
        try {
            // 返回已有会话
            if (this.session != null && this.session.isOpen()) {
                return this.session;
            }

            // 设置为null
            this.session = null;

            // // 由于二次验证会要求更多时间，优化下此处的验证时间
            // if (this.shellConnect.isPasswordAuth() && timeout < 15000) {
            //     timeout = 15000;
            // }

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
            // 创建会话
            ClientSession session = future.verify(timeout).getClientSession();
            // 密码
            if (this.shellConnect.isPasswordAuth()) {
                session.addPasswordIdentity(this.shellConnect.getPassword());
            } else if (this.shellConnect.isCertificateAuth()) {// 证书
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
                    session.addPublicKeyIdentity(keyPair);
                }
            } else if (this.shellConnect.isManagerAuth()) {// 密钥
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
                    session.addPublicKeyIdentity(keyPair);
                }
            }

            // 认证
            session.auth().verify(timeout);
            this.session = session;
        } catch (SshException ex) {
            if (this.verifyFailureCallback == null || ex.getDisconnectCode() != SshConstants.SSH2_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE) {
                throw ex;
            }
            // 处理认证失败业务
            ShellConnect connect = this.verifyFailureCallback.apply(this.shellConnect);
            if (connect != null) {
                this.shellConnect = connect;
                return this.takeSession(timeout);
            }
        }
        // 返回会话
        return this.session;
    }

    /**
     * 初始化环境
     */
    public Map<String, String> initEnvironments() {
        // 用户环境
        Map<String, String> environments = this.shellConnect.environments();
        // 初始化环境变量
        if (this.osType != null) {
            environments.put("PATH", this.getExportPath());
        }
        // 初始化字符集
        environments.put("LANG", "en_US." + this.getCharset().displayName());
        return environments;
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

    @Override
    public boolean isConnected() {
        // if (this.session != null) {
        //     return this.session.isOpen();
        // }
        // return this.sshClient != null && this.sshClient.isOpen();
        // ShellConnState state = this.getState();
        // return state != null && state.isConnected();
        return this.session != null && this.session.isOpen();
    }

    /**
     * 连接状态
     */
    protected final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 当前状态监听器
     */
    protected final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

}
