package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.sftp.delete.ShellSFTPDeleteManager;
import cn.oyzh.easyshell.sftp.download.ShellSFTPDownloadManager;
import cn.oyzh.easyshell.sftp.transport.ShellSFTPTransportManager;
import cn.oyzh.easyshell.sftp.upload.ShellSFTPUploadManager;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.ssh.util.SSHHolder;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.Properties;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellSFTPClient extends ShellClient {

    public ShellSFTPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    public ShellSFTPClient(ShellConnect shellConnect, Session session) {
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
        int port = this.shellConnect.hostPort();
        String hostIp = this.shellConnect.hostIp();
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
                // 连接超时
                this.session.setTimeout(timeout);
                // 连接
                this.session.connect(timeout);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("shellSftpClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            JulLog.warn("shellSftpClient start error", ex);
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

    private ShellSFTPChannelManager sftpManager;

    public ShellSFTPChannelManager getSftpManager() {
        if (this.sftpManager == null) {
            this.sftpManager = new ShellSFTPChannelManager();
        }
        return this.sftpManager;
    }

    private ShellSFTPUploadManager uploadManager;

    public ShellSFTPUploadManager getUploadManager() {
        if (this.uploadManager == null) {
            this.uploadManager = new ShellSFTPUploadManager();
        }
        return uploadManager;
    }

    private ShellSFTPDeleteManager deleteManager;

    public ShellSFTPDeleteManager getDeleteManager() {
        if (this.deleteManager == null) {
            this.deleteManager = new ShellSFTPDeleteManager(this::newSFTP);
        }
        return deleteManager;
    }

    private ShellSFTPDownloadManager downloadManager;

    public ShellSFTPDownloadManager getDownloadManager() {
        if (this.downloadManager == null) {
            this.downloadManager = new ShellSFTPDownloadManager();
        }
        return downloadManager;
    }

    private ShellSFTPTransportManager transportManager;

    public ShellSFTPTransportManager getTransportManager() {
        if (this.transportManager == null) {
            this.transportManager = new ShellSFTPTransportManager();
        }
        return transportManager;
    }

    public ShellSFTPChannel openSFTP() {
        if (!this.getSftpManager().hasAvailable()) {
            ShellSFTPChannel sftp = this.newSFTP();
            if (sftp != null) {
                this.getSftpManager().push(sftp);
                return sftp;
            }
        }
        return this.getSftpManager().take();
    }

    public ShellSFTPChannel newSFTP() {
        try {
            ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
            ShellSFTPChannel sftp = new ShellSFTPChannel(channel, this.osType);
            sftp.connect(this.connectTimeout());
            return sftp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String exec_id_un(int uid) {
        return this.exec("id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("id -gn " + gid);
    }

    private ShellSFTPAttr attr;

    public ShellSFTPAttr getAttr() {
        if (this.attr == null) {
            this.attr = new ShellSFTPAttr();
        }
        return this.attr;
    }

    public void delete(ShellSFTPFile file) {
        this.getDeleteManager().fileDelete(file);
    }

    public void upload(File localFile, String remoteFile) throws SftpException {
        this.getUploadManager().fileUpload(localFile, remoteFile, this);
    }

    public void download(File localFile, ShellSFTPFile remoteFile) throws SftpException {
        this.getDownloadManager().fileDownload(localFile, remoteFile, this);
    }

    public void transport(ShellSFTPFile localFile, String remoteFile, ShellSFTPClient remoteClient) {
        this.getTransportManager().fileTransport(localFile, remoteFile, this, remoteClient);
    }
}
