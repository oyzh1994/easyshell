package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.sftp.delete.ShellSftpDeleteManager;
import cn.oyzh.easyshell.sftp.download.ShellSftpDownloadManager;
import cn.oyzh.easyshell.sftp.transport.ShellSftpTransportManager;
import cn.oyzh.easyshell.sftp.upload.ShellSftpUploadManager;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHClientActionUtil;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellSftpClient implements BaseClient {

    /**
     * shell信息
     */
    private final ShellConnect shellConnect;

    @Override
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

    public ShellSftpClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    public ShellSftpClient(ShellConnect shellConnect,Session session) {
        this.shellConnect = shellConnect;
        this.session = session;
    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.shellConnect.getUser(), this.shellConnect.getPassword(), this.shellConnect.getHost());
        }
        // 连接信息
        String hostIp = this.shellConnect.hostIp();
        int port = this.shellConnect.hostPort();
        // 创建会话
        this.session = SSHHolder.getJsch().getSession(this.shellConnect.getUser(), hostIp, port);
        // 设置密码
        this.session.setPassword(this.shellConnect.getPassword());
        // 配置参数
        Properties config = new Properties();
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
        // 设置配置
        this.session.setConfig(config);
        // 超时连接
        this.session.setTimeout(this.shellConnect.connectTimeOutMs());
    }

    @Override
    public void close() {
        try {
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
            // 销毁回话
            if (this.session != null && this.session.isConnected()) {
                this.session.disconnect();
                this.session = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(int timeout) {
        if (this.isConnected()) {
            return;
        }
        try {
            // 初始化客户端
            this.initClient();
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 执行连接
            if (this.session != null) {
                this.session.connect(timeout);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            JulLog.warn("shellClient start error", ex);
            throw new ShellException(ex);
        }
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.session == null || !this.session.isConnected();
    }

    public String connectName() {
        return this.shellConnect.getName();
    }

    private ShellSftpChannelManager sftpManager;

    public ShellSftpChannelManager getSftpManager() {
        if (this.sftpManager == null) {
            this.sftpManager = new ShellSftpChannelManager();
        }
        return this.sftpManager;
    }

    private ShellSftpUploadManager uploadManager;

    public ShellSftpUploadManager getUploadManager() {
        if (this.uploadManager == null) {
            this.uploadManager = new ShellSftpUploadManager();
        }
        return uploadManager;
    }

    private ShellSftpDeleteManager deleteManager;

    public ShellSftpDeleteManager getDeleteManager() {
        if (this.deleteManager == null) {
            this.deleteManager = new ShellSftpDeleteManager(this::newSftp);
        }
        return deleteManager;
    }

    private ShellSftpDownloadManager downloadManager;

    public ShellSftpDownloadManager getDownloadManager() {
        if (this.downloadManager == null) {
            this.downloadManager = new ShellSftpDownloadManager();
        }
        return downloadManager;
    }

    private ShellSftpTransportManager transportManager;

    public ShellSftpTransportManager getTransportManager() {
        if (this.transportManager == null) {
            this.transportManager = new ShellSftpTransportManager();
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
            ShellSftp sftp = new ShellSftp(channel, this.osType);
            sftp.connect(this.connectTimeout());
            return sftp;
        } catch (Exception ex) {
            ex.printStackTrace();
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
        ChannelExec channel = null;
        try {
            String extCommand = null;
            if (StringUtil.startWithAnyIgnoreCase(command, "source", "which", "where")) {
                extCommand = command;
            } else if (StringUtil.startWithAnyIgnoreCase(command, "uname")) {
                extCommand = "/usr/bin/" + command;
            } else if (this.isWindows()) {
                // 初始化环境
                if (this.environment.isEmpty()) {
                    this.initEnvironment();
                }
                extCommand = command;
            } else if (this.isLinux() || this.isMacos()) {
                // 初始化环境
                if (this.environment.isEmpty()) {
                    this.initEnvironment();
                }
                String exportPath = this.getExportPath();
                extCommand = "export PATH=$PATH" + exportPath + " && " + command;
            } else if (this.isUnix()) {
                extCommand = command;
            }
            channel = (ChannelExec) this.session.openChannel("exec");
            // 客户端转发
            if (this.shellConnect.isJumpForward()) {
                channel.setAgentForwarding(true);
            }
            // x11转发
            if (this.shellConnect.isX11forwarding()) {
                channel.setXForwarding(true);
            }
            // 操作
            ShellSSHClientActionUtil.forAction(this.connectName(), command);
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

    public String exec_id_un(int uid) {
        return this.exec("id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("id -gn " + gid);
    }

    public int connectTimeout() {
        return this.shellConnect.connectTimeOutMs();
    }

    private ShellSftpAttr attr;

    public ShellSftpAttr getAttr() {
        if (this.attr == null) {
            this.attr = new ShellSftpAttr();
        }
        return this.attr;
    }

    public void delete(ShellSftpFile file) {
        this.getDeleteManager().fileDelete(file);
    }

    public void upload(File localFile, String remoteFile) throws SftpException {
        this.getUploadManager().fileUpload(localFile, remoteFile, this);
    }

    public void download(File localFile, ShellSftpFile remoteFile) throws SftpException {
        this.getDownloadManager().fileDownload(localFile, remoteFile, this);
    }

    public void transport(ShellSftpFile localFile, String remoteFile, ShellSftpClient remoteClient) {
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
            this.remoteCharset = ShellUtil.getCharsetFromChcp(output);
        }
        return this.remoteCharset;
    }

    public String getFileSeparator() {
        if (this.isWindows()) {
            return "\\";
        }
        return "/";
    }
}
