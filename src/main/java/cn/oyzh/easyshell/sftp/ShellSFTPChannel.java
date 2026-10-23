package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.util.IOUtil;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPChannel extends ShellSSHChannel {

    public ShellSFTPChannel(ChannelSftp channel) {
        super(channel);
    }

    @Override
    public ChannelSftp getChannel() {
        return (ChannelSftp) super.getChannel();
    }

    private String resolvePath(String file) throws SftpException {
        return ShellSFTPUtil.resolvePath(file, this.pwd());
    }

    public Vector<ChannelSftp.LsEntry> ls(String path) throws SftpException {
        path = this.resolvePath(path);
        return this.getChannel().ls(path);
    }

    public void cd(String path) throws SftpException {
        path = this.resolvePath(path);
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
        path = this.resolvePath(path);
        return this.getChannel().realpath(path);
    }

    /**
     * 列举文件
     *
     * @param path 文件路径
     * @return 文件列表
     * @throws SftpException 异常
     */
    public List<ShellSFTPFile> lsFile(String path) throws Exception {
        // 文件列表
        List<ShellSFTPFile> files = new ArrayList<>();
        this.lsFile(path, files::add);
        return files;
    }

    /**
     * 列举文件
     *
     * @param path         文件路径
     * @param fileCallback 文件回调
     * @throws SftpException 异常
     */
    public void lsFile(String path, Consumer<ShellSFTPFile> fileCallback) throws Exception {
        String filePath = this.resolvePath(path);
        // 总列表
        Vector<ChannelSftp.LsEntry> vector = this.ls(path);
        // 遍历列表
        for (ChannelSftp.LsEntry entry : vector) {
            // 非文件，跳过
            if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
                continue;
            }
            ShellSFTPFile file = new ShellSFTPFile(filePath, entry);
            // 目录列举只判断链接类型，链接目标在用户点击时按需解析。
            // 提前 realpath + stat 会对每个链接增加两次串行 SFTP 往返。
            fileCallback.accept(file);
        }
    }

    @Deprecated
    public List<ShellSFTPFile> lsFileNormal(String path) throws Exception {
        List<ShellSFTPFile> files = this.lsFile(path);
        return files.stream().filter(ShellSFTPFile::isNormal).collect(Collectors.toList());
    }

    public void rm(String path) throws SftpException {
        path = this.resolvePath(path);
        this.getChannel().rm(path);
    }

    public void rmdir(String path) throws SftpException {
        path = this.resolvePath(path);
        this.getChannel().rmdir(path);
    }

    public String pwd() throws SftpException {
        return this.getChannel().pwd();
    }

    public void mkdir(String path) throws SftpException {
        path = this.resolvePath(path);
        this.getChannel().mkdir(path);
    }

    public boolean exist(String path) throws SftpException {
        try {
            path = this.resolvePath(path);
            return this.stat(path) != null;
        } catch (SftpException ex) {
            if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw ex;
        }
    }

    public void touch(String path) throws SftpException {
        path = this.resolvePath(path);
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        try {
            this.getChannel().put(stream, path);
        } finally {
            IOUtil.close(stream);
        }
    }

    public void rename(String path, String newPath) throws SftpException {
        path = this.resolvePath(path);
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
        path = this.resolvePath(path);
        return this.getChannel().stat(path);
    }

    /**
     * 上传
     *
     * @param src  源
     * @param dest 目标
     * @throws SftpException 异常
     */
    public void put(String src, String dest) throws SftpException {
        this.getChannel().put(src, dest);
    }

    /**
     * 上传
     *
     * @param src  源
     * @param dest 目标
     * @throws SftpException 异常
     */
    public void put(InputStream src, String dest) throws SftpException {
        try {
            this.getChannel().put(src, dest);
        } finally {
            IOUtil.close(src);
        }
    }

    /**
     * 上传
     *
     * @param dest 目标
     * @return 目标
     * @throws SftpException 异常
     */
    public OutputStream put(String dest) throws SftpException {
        dest = this.resolvePath(dest);
        return this.getChannel().put(dest);
    }

    /**
     * 下载
     *
     * @param src 源
     * @return 文件
     * @throws SftpException 异常
     */
    public InputStream get(String src) throws SftpException {
        src = this.resolvePath(src);
        return this.getChannel().get(src);
    }

    /**
     * 下载
     *
     * @param src  源
     * @param dest 目标
     * @throws SftpException 异常
     */
    public void get(String src, String dest) throws SftpException {
        src = this.resolvePath(src);
        dest = this.resolvePath(dest);
        this.getChannel().get(src, dest);
    }

    /**
     * 下载
     *
     * @param src  源
     * @param dest 目标
     * @throws SftpException 异常
     */
    public void get(String src, OutputStream dest) throws SftpException {
        src = this.resolvePath(src);
        try {
            this.getChannel().get(src, dest);
        } finally {
            IOUtil.close(dest);
        }
    }

    @Deprecated
    public void put(String src, String dest, SftpProgressMonitor monitor) throws SftpException {
        this.put(src, dest, monitor, ChannelSftp.OVERWRITE);
    }

    @Deprecated
    public void put(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        src = this.resolvePath(src);
        dest = this.resolvePath(dest);
        this.getChannel().put(src, dest, monitor, mode);
    }

    @Deprecated
    public void put(InputStream src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        dest = this.resolvePath(dest);
        try {
            this.getChannel().put(src, dest, monitor, mode);
        } finally {
            IOUtil.close(src);
        }
    }

    @Deprecated
    public OutputStream put(String dest, SftpProgressMonitor monitor) throws SftpException {
        return this.put(dest, monitor, ChannelSftp.OVERWRITE);
    }

    @Deprecated
    public OutputStream put(String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        dest = this.resolvePath(dest);
        return this.getChannel().put(dest, monitor, mode);
    }

    @Deprecated
    public void get(String src, String dest, SftpProgressMonitor monitor) throws SftpException {
        this.get(src, dest, monitor, ChannelSftp.OVERWRITE);
    }

    @Deprecated
    public void get(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        src = this.resolvePath(src);
        dest = this.resolvePath(dest);
        this.getChannel().get(src, dest, monitor, mode);
    }

    @Deprecated
    public InputStream get(String src, SftpProgressMonitor monitor) throws SftpException {
        src = this.resolvePath(src);
        return this.getChannel().get(src, monitor);
    }

    /**
     * 修改权限，这个方法windows执行无效果
     *
     * @param permission 权限
     * @param path       文件路径
     */
    public void chmod(int permission, String path) throws SftpException {
        path = this.resolvePath(path);
        this.getChannel().chmod(permission, path);
    }
}
