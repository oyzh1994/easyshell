package cn.oyzh.easyshell.ssh.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.ssh.ShellSSHChannel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPChannel extends ShellSSHChannel {

    /**
     * 链接管理器
     */
    private final ShellSFTPRealpathManager realpathManager;

    public ShellSFTPChannel(ChannelSftp channel, ShellSFTPRealpathManager realpathManager) {
        super(channel);
        this.realpathManager = realpathManager;
    }

    @Override
    public ChannelSftp getChannel() {
        return (ChannelSftp) super.getChannel();
    }

    public Vector<ChannelSftp.LsEntry> ls(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        return this.getChannel().ls(path);
    }

    public void cd(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        this.getChannel().cd(path);
    }

    /**
     * 获取链接路径
     *
     * @param path 路径
     * @return 链接路径
     * @throws SftpException 异常
     */
    public String realpath(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        return this.getChannel().realpath(path);
    }

    /**
     * 列举文件
     *
     * @param path 文件路径
     * @return 文件列表
     * @throws SftpException 异常
     */
    public List<ShellSFTPFile> lsFile(String path) throws SftpException {
        String filePath = ShellFileUtil.fixFilePath(path);
        // 文件列表
        List<ShellSFTPFile> files = new ArrayList<>();
        // 总列表
        Vector<ChannelSftp.LsEntry> vector = this.ls(path);
        // 遍历列表
        for (ChannelSftp.LsEntry lsEntry : vector) {
            files.add(new ShellSFTPFile(filePath, lsEntry));
        }
        // 过滤链接文件
        List<ShellSFTPFile> linkFiles = files.stream().filter(ShellSFTPFile::isLink).toList();
        // 处理链接文件
        this.realpathManager.put(linkFiles);
        // 等待完成
        this.realpathManager.waitComplete();
        return files;
    }

    public List<ShellSFTPFile> lsFileNormal(String path) throws SftpException {
        List<ShellSFTPFile> files = this.lsFile(path);
        return files.stream().filter(ShellSFTPFile::isNormal).collect(Collectors.toList());
    }

    public void rm(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        this.getChannel().rm(path);
    }

    public void rmdir(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        this.getChannel().rmdir(path);
    }

    public String pwd() throws SftpException {
        return this.getChannel().pwd();
    }

    public void mkdir(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        this.getChannel().mkdir(path);
    }

    public boolean exist(String path) throws SftpException {
        try {
            path = ShellFileUtil.fixFilePath(path);
            return this.stat(path) != null;
        } catch (SftpException ex) {
            if (ExceptionUtil.hasMessage(ex, "No such file")) {
                return false;
            }
            throw ex;
        }
    }

    public void touch(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
        this.getChannel().put(inputStream, path);
    }

    public void rename(String path, String newPath) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        this.getChannel().rename(path, newPath);
    }

    /**
     * 获取文件属性
     *
     * @param path 路径
     * @return 文件属性
     * @throws SftpException 异常
     */
    public SftpATTRS stat(String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        return this.getChannel().stat(path);
    }

    public void put(String src, String dest, SftpProgressMonitor monitor) throws SftpException {
        this.put(src, dest, monitor, ChannelSftp.OVERWRITE);
    }

    public void put(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        src = ShellFileUtil.fixFilePath(src);
        dest = ShellFileUtil.fixFilePath(dest);
        this.getChannel().put(src, dest, monitor, mode);
    }

    public void put(InputStream src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        dest = ShellFileUtil.fixFilePath(dest);
        this.getChannel().put(src, dest, monitor, mode);
    }

    public void put(String src, String dest) throws SftpException {
        this.put(src, dest, null, ChannelSftp.OVERWRITE);
    }

    public void put(InputStream src, String dest) throws SftpException {
        this.put(src, dest, null, ChannelSftp.OVERWRITE);
    }

    public OutputStream put(String dest, SftpProgressMonitor monitor) throws SftpException {
        return this.put(dest, monitor, ChannelSftp.OVERWRITE);
    }

    public void get(String src, String dest, SftpProgressMonitor monitor) throws SftpException {
        this.get(src, dest, monitor, ChannelSftp.OVERWRITE);
    }

    public void get(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        src = ShellFileUtil.fixFilePath(src);
        dest = ShellFileUtil.fixFilePath(dest);
        this.getChannel().get(src, dest, monitor, mode);
    }

    public InputStream get(String src) throws SftpException {
        return this.get(src, (SftpProgressMonitor) null);
    }

    public InputStream get(String src, SftpProgressMonitor monitor) throws SftpException {
        src = ShellFileUtil.fixFilePath(src);
        return this.getChannel().get(src, monitor);
    }

    public void get(String src, String dest) throws SftpException {
        this.get(src, dest, null, ChannelSftp.OVERWRITE);
    }

    public OutputStream put(String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        dest = ShellFileUtil.fixFilePath(dest);
        return this.getChannel().put(dest, monitor, mode);
    }

    /**
     * 修改权限，这个方法windows执行无效果
     *
     * @param permission 权限
     * @param path       文件路径
     */
    public void chmod(int permission, String path) throws SftpException {
        path = ShellFileUtil.fixFilePath(path);
        this.getChannel().chmod(permission, path);
    }
}
