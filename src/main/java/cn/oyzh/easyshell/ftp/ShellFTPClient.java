package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.sftp.ShellSftpUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * shell终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellFTPClient extends FTPClient implements BaseClient {

    private ShellConnect shellConnect;

    public ShellFTPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
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
        // 配置参数
        Properties config = new Properties();
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
    }

    @Override
    public void close() {
        try {
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
            long endTime = System.currentTimeMillis();
            JulLog.info("shellSftpClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            JulLog.warn("shellSftpClient start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return !this.isConnected();
    }


    public void delete(ShellFTPFile file) {
        try {
            super.deleteFile(file.getFilePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void delete(String filePath) {
        try {
            super.deleteFile(filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void upload(File localFile, String remoteFile) throws SftpException {
    }

    public void download(File localFile, ShellSftpFile remoteFile) throws SftpException {
    }

    public void transport(ShellSftpFile localFile, String remoteFile, ShellFTPClient remoteClient) {
    }


    public List<ShellFTPFile> lsFile(String filePath) {

        List<ShellFTPFile> list = new ArrayList<>();
        try {
            FTPFile[] files = this.listFiles(filePath);
            if (files != null) {
                for (FTPFile file : files) {
                    list.add(new ShellFTPFile(filePath, file));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public String pwdDir() {
        try {
            return super.printWorkingDirectory();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void mkdir(String filePath) {
        try {
            super.makeDirectory(filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ShellFTPFile finfo(String filePath) {
        try {
            FTPFile file = super.mlistFile(filePath);
            String pPath = ShellSftpUtil.parent(filePath);
            return new ShellFTPFile(pPath, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void touch(String filePath) {
        try {
            // 文件不存在，创建文件
            InputStream inputStream = new ByteArrayInputStream(new byte[0]);
            boolean created = super.storeFile(filePath, inputStream);
            inputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean exist(String filePath) {
        try {
            long lastModifiedTime = this.mdtm(filePath);
            if (lastModifiedTime != -1) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
