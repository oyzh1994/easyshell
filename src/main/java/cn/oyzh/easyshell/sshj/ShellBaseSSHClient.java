package cn.oyzh.easyshell.sshj;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.ssh.ShellSSHClientActionUtil;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;

import java.io.InputStream;
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
    protected SSHClient sshClient;

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

    public ShellBaseSSHClient(ShellConnect connect) {
        this.shellConnect = connect;
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    /**
     * 创建新会话
     *
     * @return Session
     * @throws Exception 异常
     */
    public Session newSession() throws Exception {
        return this.sshClient.startSession();
    }

    /**
     * 初始化环境
     *
     * @param session 会话
     * @throws Exception 异常
     */
    protected void initEnvironments(Session session) throws Exception {
        // 用户环境
        Map<String, String> userEnvs = this.shellConnect.environments();
        if (CollectionUtil.isNotEmpty(userEnvs)) {
            for (Map.Entry<String, String> entry : userEnvs.entrySet()) {
                session.setEnvVar(entry.getKey(), entry.getValue());
            }
        }
        // 初始化环境变量
        if (this.osType != null) {
            session.setEnvVar("PATH", this.getExportPath());
        }
        // 初始化字符集
        session.setEnvVar("LANG", "en_US." + this.getCharset().displayName());
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
        Session session = null;
        Session.Command channel = null;
        try {
            // 获取会话
            session = this.newSession();
            // 获取通道
            channel = session.exec(command);
            // 操作
            ShellSSHClientActionUtil.forAction(this.connectName(), command);
            InputStream in = channel.getInputStream();
            byte[] bytes = in.readAllBytes();
            String result;
            // 如果远程是windows，则要检查下字符集是否要指定
            if (StringUtil.isNotBlank(this.remoteCharset)) {
                result = new String(bytes, this.remoteCharset);
            } else {
                result = new String(bytes);
            }
            // IOUtil.close(in);
            if (StringUtil.endsWith(result, "\r\n")) {
                result = result.substring(0, result.length() - 2);
            } else if (StringUtil.endWithAny(result, "\n", "\r")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            IOUtil.close(session);
            IOUtil.close(channel);
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
        JulLog.info("remote charset: {}", this.getRemoteCharset());
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

    ///**
    // * 初始化回话
    // */
    //protected void initSession() throws JSchException {
    //    if (this.session != null) {
    //        // // 设置守护线程
    //        // this.session.setDaemonThread(true);
    //        // // 连续3次失败后断开连接
    //        // this.session.setServerAliveCountMax(3);
    //        // // 每60秒发送一次TCP keep-alive包
    //        // this.session.setServerAliveInterval(60_000);
    //        // // 可选：设置TCP层面的keep-alive
    //        // this.session.setConfig("TCPKeepAlive", "yes");
    //        // // 去掉首次连接确认
    //        // this.session.setConfig("StrictHostKeyChecking", "no");
    //
    //    }
    //}

    /**
     * 使用压缩
     *
     * @param session 会话
     */
    protected void useCompression(Session session) throws TransportException {
        if (session != null && this.shellConnect.isEnableCompress()) {
            // 启用压缩
            this.sshClient.useCompression();
        }
    }

    // /**
    //  * 创建exec通道
    //  *
    //  * @return exec通道
    //  */
    // protected Session.Shell newExecChannel() {
    //     try {
    //         return this.getSession().startShell();
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    //     return null;
    // }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    protected String initHost() {
        return this.shellConnect.getHost();
    }

    /**
     * 初始化客户端
     *
     * @param timeout 超时时间
     */
    protected void initClient(int timeout) throws Exception {
        // 连接信息
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        this.sshClient = new SSHClient();
        // 证书检验
        this.sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        // 设置超时时间
        this.sshClient.setConnectTimeout(timeout);
        // 连接
        this.sshClient.connect(hostIp, port);
        // 密码认证
        if (this.shellConnect.isPasswordAuth()) {
            List<AuthMethod> authMethods = new ArrayList<>();
            // 认证方法，密码&验证码
            if (StringUtil.isNotEmpty(this.shellConnect.getPassword())) {
                AuthPassword authPassword = new AuthPassword(new PasswordFinder() {
                    @Override
                    public char[] reqPassword(Resource<?> resource) {
                        return shellConnect.getPassword().toCharArray();
                    }

                    @Override
                    public boolean shouldRetry(Resource<?> resource) {
                        return false;
                    }
                });
                ShellSSHAuthInteractive authInteractive = new ShellSSHAuthInteractive(this.shellConnect.getPassword());
                authMethods.add(authPassword);
                authMethods.add(authInteractive);
            }
            this.sshClient.auth(this.shellConnect.getUser(), authMethods);
        } else if (this.shellConnect.isCertificateAuth()) {// 证书认证
            String priKeyFile = this.shellConnect.getCertificate();
            // 检查私钥是否存在
            if (!FileUtil.exist(priKeyFile)) {
                MessageBox.warn("priKeyFile file not exist");
                return;
            }
            // 加载
            KeyProvider provider = this.sshClient.loadKeys(priKeyFile, "123456".toCharArray());
            // 认证
            this.sshClient.authPublickey(this.shellConnect.getUser(), provider);
        } else if (this.shellConnect.isSSHAgentAuth()) {// ssh agent
            // IdentityRepository repository = SSHHolder.getAgentJsch().getIdentityRepository();
            // if (!(repository instanceof AgentIdentityRepository)) {
            //     repository = SSHUtil.initAgentIdentityRepository();
            //     if (CollectionUtil.isEmpty(repository.getIdentities())) {
            //         throw new AgentProxyException("identities is empty");
            //     }
            //     SSHHolder.getAgentJsch().setIdentityRepository(repository);
            // }
            // for (Identity identity : repository.getIdentities()) {
            //     JulLog.info("Identity: {}", identity.getName());
            // }
            // // 创建会话
            // this.session = SSHHolder.getAgentJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        } else if (this.shellConnect.isManagerAuth()) {// 密钥
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
            // 检查私钥是否存在
            if (key == null) {
                MessageBox.warn("key not found");
                return;
            }
            PasswordFinder passwordFinder = null;
            if (StringUtil.isNotBlank(key.getPassword())) {
                passwordFinder = PasswordUtils.createOneOff(key.getPassword().toCharArray());
            }
            // 加载
            KeyProvider provider = this.sshClient.loadKeys(key.getPrivateKey(), key.getPublicKey(), passwordFinder);
            // 认证
            this.sshClient.authPublickey(this.shellConnect.getUser(), provider);
        }
    }
}
