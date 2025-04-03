package cn.oyzh.easyshell.shell;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CharsetUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.process.ProcessExec;
import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.sftp.SftpAttr;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.ShellSftpManager;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteManager;
import cn.oyzh.easyshell.sftp.download.SftpDownloadManager;
import cn.oyzh.easyshell.sftp.transport.SftpTransportManager;
import cn.oyzh.easyshell.sftp.upload.SftpUploadManager;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellSSHConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.util.ShellKeyUtil;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.easyshell.x11.X11Manager;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.ssh.SSHException;
import cn.oyzh.ssh.SSHForwardConfig;
import cn.oyzh.ssh.SSHForwarder;
import cn.oyzh.ssh.SSHHolder;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellClient {

    /**
     * shell信息
     */
    private final ShellConnect shellConnect;

    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    /**
     * shell会话
     */
    private Session session;

    public Session getSession() {
        return session;
    }

    /**
     * ssh端口转发器
     */
    private SSHForwarder sshForwarder;

    /**
     * shell密钥存储
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    /**
     * x11配置存储
     */
    private final ShellX11ConfigStore x11ConfigStore = ShellX11ConfigStore.INSTANCE;

    /**
     * shell配置存储
     */
    private final ShellSSHConfigStore sshConfigStore = ShellSSHConfigStore.INSTANCE;

//    /**
//     * 静默关闭标志位
//     */
//    private boolean closeQuietly;

    public ShellClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        // 监听连接状态
        this.stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
//                case CLOSED -> {
//                    if (!this.closeQuietly) {
//                        ShellEventUtil.connectionClosed(this);
//                    }
//                }
                case CONNECTED -> ShellEventUtil.connectionConnected(this);
                default -> {

                }
            }
        });
    }

    /**
     * 连接状态
     */
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 获取状态
     *
     * @return 状态
     */
    public ShellConnState getState() {
        return this.state.get();
    }

    /**
     * 更新状态
     */
    public void updateState() {
        ShellConnState state = this.getState();
        if (state == ShellConnState.CONNECTED) {
            if (this.session == null || !this.session.isConnected()) {
                this.state.set(ShellConnState.INTERRUPT);
            }
        }
    }

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    public ShellConnState state() {
        return this.stateProperty().get();
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    public ReadOnlyObjectProperty<ShellConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    /**
     * 添加连接状态监听器
     *
     * @param stateListener 监听器
     */
    public void addStateListener(ChangeListener<ShellConnState> stateListener) {
        if (stateListener != null) {
            this.stateProperty().addListener(stateListener);
        }
    }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    private String initHost() {
        // 连接地址
        String host;
        // 初始化ssh端口转发
        if (this.shellConnect.isSSHForward()) {
            // 初始化ssh转发器
            ShellSSHConfig sshConfig = this.shellConnect.getSshConfig();
            // 从数据库获取
            if (sshConfig == null) {
                sshConfig = this.sshConfigStore.getByIid(this.shellConnect.getId());
            }
            if (sshConfig != null) {
                if (this.sshForwarder == null) {
                    this.sshForwarder = new SSHForwarder(sshConfig);
                }
                // ssh配置
                SSHForwardConfig forwardConfig = new SSHForwardConfig();
                forwardConfig.setHost(this.shellConnect.hostIp());
                forwardConfig.setPort(this.shellConnect.hostPort());
                // 执行连接
                int localPort = this.sshForwarder.forward(forwardConfig);
                // 连接信息
                host = "127.0.0.1:" + localPort;
            } else {
                JulLog.warn("ssh forward is enable but ssh config is null");
                throw new SSHException("ssh forward is enable but ssh config is null");
            }
        } else {// 直连
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.shellConnect.getUser(), this.shellConnect.getPassword(), this.shellConnect.getHost());
        }
        // 连接信息
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        // 密码
        if (this.shellConnect.isPasswordAuth()) {
            // 创建会话
            this.session = SSHHolder.JSCH.getSession(this.shellConnect.getUser(), hostIp, port);
            this.session.setPassword(this.shellConnect.getPassword());
        } else if (this.shellConnect.isCertificateAuth()) {// 证书
            String priKeyFile = this.shellConnect.getCertificate();
            // 检查私钥是否存在
            if (!FileUtil.exist(priKeyFile)) {
                MessageBox.warn("certificate file not exist");
                return;
            }

//            priKeyFile = """
//                    -----BEGIN ENCRYPTED PRIVATE KEY-----
//                    MC4CAQAwBQYDK2VwBCIEIKjt/RWhKtueLFAv/XsqBSlUd1jmNQSUqjNwz4OIFKv7
//                    -----END ENCRYPTED PRIVATE KEY-----
//                    """;
//            priKeyFile = """
//                    -----BEGIN ENCRYPTED PRIVATE KEY-----
//                    MC4CAQAwBQYDK2VwBCIEIGBdSVs7fOdMZ1Q6zQx0TaxraNOWHfXCzpJ6iblK1gXj
//                    -----END ENCRYPTED PRIVATE KEY-----
//                    """;
//            priKeyFile = """
//                    -----BEGIN ENCRYPTED PRIVATE KEY-----
//                    Proc-Type: 4,ENCRYPTED
//                    DEK-Info: AES-256-CBC,CF530674348ECCC9451934600FA4C175
//                    IuLofJFk0g0LFfRSZ0iTRLayiMQAgwhR7orPQGTFBy632YXiV8AAfvynxlZ3Bsb6
//                    ov9AZQW6tkUnO8yDcwagfw==
//                    -----END ENCRYPTED PRIVATE KEY-----
//                    """;
            priKeyFile = """
                    -----BEGIN ENCRYPTED PRIVATE KEY-----
                    MC4CAQAwBQYDK2VwBCIEIJi2Pp4/d/OE8/cTNdM2US09ZuBFqvyY3iYayVuXHTy7
                    -----END ENCRYPTED PRIVATE KEY-----
                    """;
            String password = "your_secure_passphrase!123"; // 在此设置密码
//            String password = "your_strong_password_123!"; // 在此设置密码
//            String password = "your_strong_password_123!";
//            String password = "your_password";

//            // 配置支持Ed25519算法
//            SSHHolder.JSCH.setConfig("server_host_key", "ssh-ed25519,ssh-rsa");
//
//            // 添加更多兼容算法配置
//
//            SSHHolder.JSCH.setConfig("PubkeyAcceptedAlgorithms", "+ssh-ed25519,ssh-rsa");

            // 添加身份认证
            SSHHolder.JSCH.addIdentity("ed25519_key",
                    priKeyFile.getBytes(),
                    null,
                    password.getBytes()
            );


//            String pubkey = "C:\\Users\\Administrator\\Desktop\\k9.pub";
//            String pubkey = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIINNtsDClAFKIO9iL/zcOUXos/q1CRhZwZELE3eZq6/e generated-by-java";
            // 添加认证
//            SSHHolder.JSCH.addIdentity("ke1", priKeyFile.getBytes(), null, password.getBytes());
//            SSHHolder.JSCH.addIdentity(priKeyFile, password.getBytes());
//            SSHHolder.JSCH.addIdentity( priKeyFile, pubkey, password.getBytes());
            // 创建会话
            this.session = SSHHolder.JSCH.getSession(this.shellConnect.getUser(), hostIp, port);
//            session.setConfig("server_host_key", "ssh-ed25519,ssh-rsa");
        } else if (this.shellConnect.isManagerAuth()) {// 密钥
            ShellKey key = this.keyStore.selectOne(this.shellConnect.getCertificate());
            // 检查私钥是否存在
            if (key == null) {
                MessageBox.warn("certificate file not exist");
                return;
            }
            // 生成私钥文件
            File priKeyFile = ShellKeyUtil.generateKeyFile(key);
            // 添加认证
            SSHHolder.JSCH.addIdentity(priKeyFile.getPath(), "");
            // 创建会话
            this.session = SSHHolder.JSCH.getSession(this.shellConnect.getUser(), hostIp, port);
        }
        // 配置参数
        Properties config = new Properties();
        // 设置终端类型
        config.put("term", "xterm-256color");
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
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
                config.put("ForwardX11", "yes");
                config.put("ForwardX11Trusted", "yes");
                this.session.setX11Host(x11Config.getHost());
                this.session.setX11Port(x11Config.getPort());
                // 本地转发，启动x11服务
                if (x11Config.isLocal()) {
                    X11Manager.startXServer();
                }
            } else {
                throw new RuntimeException("X11forwarding is enable but x11config is null");
            }
        }
        // 设置配置
        this.session.setConfig(config);
//        session.setConfig("PubkeyAcceptedAlgorithms", "+ssh-ed25519");
//        session.setConfig("userauth.gssapi-with-mic", "no");

        // 超时连接
        this.session.setTimeout(this.shellConnect.connectTimeOutMs());
    }

//    /**
//     * 关闭客户端，静默模式
//     */
//    public void closeQuiet() {
//        this.closeQuietly = true;
//        this.close();
//    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            if (this.shell != null) {
                this.shell.close();
                this.shell = null;
            }
            if (this.shellExec != null) {
                this.shellExec.close();
                this.shellExec = null;
            }
            if (this.serverExec != null) {
                this.serverExec.close();
                this.serverExec = null;
            }
            if (this.dockerExec != null) {
                this.dockerExec.close();
                this.dockerExec = null;
            }
            if (this.sftpManager != null) {
                this.sftpManager.close();
                this.sftpManager = null;
            }
            if (this.deleteManager != null) {
                this.deleteManager.close();
                this.deleteManager = null;
            }
            if (this.uploadManager != null) {
                this.uploadManager.close();
                this.uploadManager = null;
            }
            if (this.transportManager != null) {
                this.transportManager.close();
                this.transportManager = null;
            }
            if (this.downloadManager != null) {
                this.downloadManager.close();
                this.downloadManager = null;
            }
            if (this.session != null) {
                this.session.disconnect();
                this.session = null;
                this.state.set(ShellConnState.CLOSED);
            }
            // 从监听器队列移除
            ShellClientChecker.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 开始连接客户端
     */
    public void start() {
        this.start(this.connectTimeout());
    }

    /**
     * 开始连接客户端
     *
     * @param timeout 超时时间
     */
    public void start(int timeout) {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            // 初始化客户端
            this.initClient();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 初始化连接池
            this.state.set(ShellConnState.CONNECTING);
            // 执行连接
            this.session.connect(timeout);
            // 判断连接结果
            if (this.session.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
//                // 初始化环境
//                this.initEnvironment();
            } else if (this.state.get() == ShellConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("shellClient start error", ex);
            throw new ShellException(ex);
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

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        if (!this.isClosed()) {
            return this.state.get().isConnected();
        }
        return false;
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.session == null || !this.session.isConnected() || !this.state.get().isConnected();
    }

    public String connectName() {
        return this.shellConnect.getName();
    }

    private ShellShell shell;

    public ShellShell getShell() {
        return shell;
    }

    public ShellShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
                // 客户端转发
                if (this.shellConnect.isSSHForward()) {
                    channel.setAgentForwarding(true);
                }
                // x11转发
                if (this.shellConnect.isX11forwarding()) {
                    channel.setXForwarding(true);
                }
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                // todo: 必须设置为这个，不然htop鼠标交互不了
                channel.setPtyType("xterm-color");
                this.shell = new ShellShell(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

    private ShellSftpManager sftpManager;

    public ShellSftpManager getSftpManager() {
        if (this.sftpManager == null) {
            this.sftpManager = new ShellSftpManager();
        }
        return this.sftpManager;
    }

    private SftpUploadManager uploadManager;

    public SftpUploadManager getUploadManager() {
        if (this.uploadManager == null) {
            this.uploadManager = new SftpUploadManager();
        }
        return uploadManager;
    }

    private SftpDeleteManager deleteManager;

    public SftpDeleteManager getDeleteManager() {
        if (this.deleteManager == null) {
            this.deleteManager = new SftpDeleteManager(this);
        }
        return deleteManager;
    }

    private SftpDownloadManager downloadManager;

    public SftpDownloadManager getDownloadManager() {
        if (this.downloadManager == null) {
            this.downloadManager = new SftpDownloadManager();
        }
        return downloadManager;
    }

    private SftpTransportManager transportManager;

    public SftpTransportManager getTransportManager() {
        if (this.transportManager == null) {
            this.transportManager = new SftpTransportManager();
        }
        return transportManager;
    }

    public ShellSftp openSftp() {
        if (!this.getSftpManager().hasAvailable()) {
            ShellSftp sftp = this.newSftp();
            if (sftp != null) {
                this.getSftpManager().push(sftp);
                return sftp;
            }
        }
        return this.getSftpManager().take();
    }

    public ShellSftp newSftp() {
        try {
            ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
            ShellSftp sftp = new ShellSftp(channel, this);
            sftp.connect(this.connectTimeout());
            return sftp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String exec(String command) {
        ChannelExec channel = null;
        try {
            String extCommand = null;
            if (StringUtil.startWithAnyIgnoreCase(command, "source", "which", "where")) {
                extCommand = command;
            } else if (StringUtil.startWithAnyIgnoreCase(command, "uname")) {
                extCommand = "/usr/bin/" + command;
            } else if (this.isWindows()) {
                extCommand = command;
            } else if (this.isLinux() || this.isMacos()) {
                String exportPath = this.getExportPath();
                extCommand = "export PATH=$PATH" + exportPath + " && " + command;
            } else if (this.isUnix()) {
                extCommand = command;
            }
            channel = (ChannelExec) this.session.openChannel("exec");
            // 客户端转发
            if (this.shellConnect.isSSHForward()) {
                channel.setAgentForwarding(true);
            }
            // x11转发
            if (this.shellConnect.isX11forwarding()) {
                channel.setXForwarding(true);
            }
            channel.setCommand(extCommand);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);
            channel.setErrStream(stream);
            channel.connect();
            while (channel.isConnected()) {
                Thread.sleep(5);
            }
            String result;
            // 如果远程是windows，则要检查下字符集是否要指定
            if (this.remoteCharset != null) {
                result = stream.toString(this.remoteCharset);
            } else {
                result = stream.toString();
            }
            stream.close();
            if (StringUtil.endsWith(result, "\n")) {
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
        StringBuilder builder = new StringBuilder();
        // 初始化环境
        if (this.environment.isEmpty()) {
            this.initEnvironment();
        }
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

    public String exec_id_un(int uid) {
        return this.exec("id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("id -gn " + gid);
    }

    public int connectTimeout() {
        return this.shellConnect.connectTimeOutMs();
    }

    private SftpAttr attr;

    public SftpAttr getAttr() {
        if (this.attr == null) {
            this.attr = new SftpAttr();
        }
        return this.attr;
    }

    public void delete(SftpFile file) {
        this.getDeleteManager().fileDelete(file);
    }

    public void upload(File localFile, String remoteFile) throws SftpException {
        this.getUploadManager().fileUpload(localFile, remoteFile, this);
    }

    public void download(File localFile, SftpFile remoteFile) throws SftpException {
        this.getDownloadManager().fileDownload(localFile, remoteFile, this);
    }

    public void transport(SftpFile localFile, String remoteFile, ShellClient remoteClient) {
        this.getTransportManager().fileTransport(localFile, remoteFile, this, remoteClient);
    }

    /**
     * 环境变量
     */
    private final List<String> environment = new ArrayList<>();

    /**
     * 初始化环境
     */
    private void initEnvironment() {
        if (this.isWindows()) {
            this.environment.add("C:/Windows/System");
            this.environment.add("C:/Windows/System32");
            this.environment.add("C:/Windows/SysWOW64");
            this.environment.add("C:/Program Files");
            this.environment.add("C:/Program Files (x86)");
            JulLog.info("remote charset: {}", this.getRemoteCharset());
        } else {
            this.environment.add("/bin");
            this.environment.add("/sbin");
            this.environment.add("/usr/bin");
            this.environment.add("/usr/sbin");
            this.environment.add("/usr/local/bin");
            this.environment.add("/usr/local/sbin");
        }
    }

    private DockerExec dockerExec;

    public DockerExec dockerExec() {
        if (this.dockerExec == null) {
            this.dockerExec = new DockerExec(this);
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
                    } else if (this.isMacos() && this.openSftp().exist("/Applications/Docker.app/Contents/Resources/bin/docker")) {
                        this.environment.add("/Applications/Docker.app/Contents/Resources/bin/");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.dockerExec;
    }

    private ServerExec serverExec;

    public ServerExec serverExec() {
        if (this.serverExec == null) {
            this.serverExec = new ServerExec(this);
        }
        return this.serverExec;
    }

    private ShellExec shellExec;

    public ShellExec shellExec() {
        if (this.shellExec == null) {
            this.shellExec = new ShellExec(this);
        }
        return this.shellExec;
    }

    private ProcessExec processExec;

    public ProcessExec processExec() {
        if (this.processExec == null) {
            this.processExec = new ProcessExec(this);
        }
        return this.processExec;
    }

    public Charset getCharset() {
        return CharsetUtil.fromName(this.shellConnect.getCharset());
    }

    private String osType;

    private String osType() {
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

    public boolean isMacos() {
        return StringUtil.containsIgnoreCase(this.osType(), "Darwin");
    }

    public boolean isLinux() {
        return StringUtil.containsIgnoreCase(this.osType(), "Linux");
    }

    public boolean isUnix() {
        return StringUtil.containsAnyIgnoreCase(this.osType(), "FreeBSD", "Aix");
    }

    public boolean isFreeBSD() {
        return StringUtil.containsIgnoreCase(this.osType(), "FreeBSD");
    }

    public boolean isWindows() {
        return StringUtil.equals(this.osType(), "Windows");
    }

    private String whoami;

    public String whoami() {
        if (this.whoami == null) {
            this.whoami = this.exec("whoami");
            if (this.isWindows() && this.whoami.contains("\\")) {
                this.whoami = this.whoami.substring(this.whoami.lastIndexOf("\\") + 1).trim();
            }
        }
        return this.whoami;
    }

//    @Deprecated
//    public String getUserBase() {
//        if (this.isMacos()) {
//            return "/Users/" + this.whoami() + "/";
//        }
//        return "/" + this.whoami() + "/";
//    }

    private String userHome;

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

    private String remoteCharset;

    public String getRemoteCharset() {
        if (this.remoteCharset == null) {
            String output = this.exec("chcp");
            if (output.contains("437")) {
                this.remoteCharset = "iso-8859-1";
            } else if (output.contains("936")) {
                this.remoteCharset = "gbk";
            } else if (output.contains("65001")) {
                this.remoteCharset = "utf-8";
            }
        }
        return this.remoteCharset;
    }
}
