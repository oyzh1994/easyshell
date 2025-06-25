package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.ssh.util.SSHHolder;
import cn.oyzh.ssh.util.SSHUtil;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
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
     * 会话
     */
    protected Session session;

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

    public Session getSession() {
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
        ChannelExec channel = this.newExecChannel();
        try {
            // 操作
            ShellSSHClientActionUtil.forAction(this.connectName(), command);
            channel.setCommand(command);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setErrStream(stream);
            channel.setInputStream(null);
            channel.setOutputStream(stream);
            channel.connect(this.connectTimeout());
            while (channel.isConnected()) {
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
                channel.disconnect();
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
            // 去掉首次连接确认
            this.session.setConfig("StrictHostKeyChecking", "no");
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
                // 设置压缩级别（可选，范围 1-9，默认 6）
                this.session.setConfig("Compression", "yes");
                this.session.setConfig("CompressionLevel", "9");
                this.session.setConfig("compression.level", "9");
            } else {
                this.session.setConfig("Compression", "no");
                this.session.setConfig("compression.s2c", "none");
                this.session.setConfig("compression.c2s", "none");
            }
        }
    }

    /**
     * 创建exec通道
     *
     * @return exec通道
     */
    protected ChannelExec newExecChannel() {
        try {
            // 如果会话关了，则先启动会话
            if (this.isClosed()) {
                this.session.connect(this.connectTimeout());
            }
            ChannelExec channel = (ChannelExec) this.session.openChannel("exec");
            channel.setInputStream(null);
            channel.setOutputStream(null);
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
     * 初始化客户端
     */
    protected void initClient() throws JSchException, AgentProxyException {
        // 连接信息
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        // 密码
        if (this.shellConnect.isPasswordAuth()) {
            // 创建会话
            this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setUserInfo(new ShellSSHAuthInteractive(this.shellConnect.getPassword()));
        } else if (this.shellConnect.isCertificateAuth()) {// 证书
            String priKeyFile = this.shellConnect.getCertificate();
            // 检查私钥是否存在
            if (!FileUtil.exist(priKeyFile)) {
                MessageBox.warn("certificate file not exist");
                return;
            }
            // 添加认证，并指定密码
            // SSHHolder.getJsch().addIdentity(priKeyFile);
            SSHHolder.getJsch().addIdentity(priKeyFile, this.shellConnect.getCertificatePwd());
            // 创建会话
            this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        } else if (this.shellConnect.isSSHAgentAuth()) {// ssh agent
            IdentityRepository repository = SSHHolder.getAgentJsch().getIdentityRepository();
            if (!(repository instanceof AgentIdentityRepository)) {
                repository = SSHUtil.initAgentIdentityRepository();
                if (CollectionUtil.isEmpty(repository.getIdentities())) {
                    throw new AgentProxyException("identities is empty");
                }
                SSHHolder.getAgentJsch().setIdentityRepository(repository);
            }
            for (Identity identity : repository.getIdentities()) {
                JulLog.info("Identity: {}", identity.getName());
            }
            // 创建会话
            this.session = SSHHolder.getAgentJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        } else if (this.shellConnect.isManagerAuth()) {// 密钥
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getKeyId());
            // 检查私钥是否存在
            if (key == null) {
                MessageBox.warn("key not found");
                return;
            }
            String keyName = "key_" + key.getId();
            // 添加认证，并指定密码
            // SSHHolder.getJsch().addIdentity(keyName, key.getPrivateKeyBytes(), key.getPublicKeyBytes(),null);
            SSHHolder.getJsch().addIdentity(keyName, key.getPrivateKeyBytes(), key.getPublicKeyBytes(), key.getPasswordBytes());
            // 创建会话
            this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        }
        // 初始化会话
        this.initSession();
        // 启用压缩
        this.useCompression();
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
        if (this.shellConnect.isJumpForward()) {
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
        if (this.shellConnect.isJumpForward()) {
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
        if (this.shellConnect.isJumpForward()) {
            channel.setAgentForwarding(true);
        }
        // x11转发
        if (this.shellConnect.isX11forwarding()) {
            channel.setXForwarding(true);
        }
    }
}
