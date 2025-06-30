package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import org.apache.sshd.sftp.client.SftpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPChannel implements AutoCloseable {

    private SftpClient channel;

    /**
     * 链接管理器
     */
    private ShellSFTPCache realpathCache;

    public ShellSFTPChannel(SftpClient sftpClient, ShellSFTPCache realpathCache) {
        this.channel = sftpClient;
        this.realpathCache = realpathCache;
    }

    public Iterable<SftpClient.DirEntry> ls(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        return this.channel.readEntries(path);
    }

    /**
     * 获取链接路径
     *
     * @param path 路径
     * @return 链接路径
     * @throws IOException 异常
     */
    public String realpath(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        return this.channel.canonicalPath(path);
    }

    /**
     * 列举文件
     *
     * @param path 文件路径
     * @return 文件列表
     * @throws Exception 异常
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
     * @throws Exception 异常
     */
    public void lsFile(String path, Consumer<ShellSFTPFile> fileCallback) throws Exception {
        String filePath = ShellFileUtil.fixFilePath(path);
        // 总列表
        Iterable<SftpClient.DirEntry> vector = this.ls(path);
        // 遍历列表
        for (SftpClient.DirEntry entry : vector) {
            // 非文件，跳过
            if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
                continue;
            }
            ShellSFTPFile file = new ShellSFTPFile(filePath, entry);
            // 处理链接文件
            if (file.isLink()) {
                this.realpathCache.realpath(file, this);
            }
            fileCallback.accept(file);
        }
    }

    public void rm(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.remove(path);
    }

    public void rmdir(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.rmdir(path);
    }

    public String pwd() throws Exception {
        return this.channel.canonicalPath(".");
    }

    public void mkdir(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.mkdir(path);
    }

    public boolean exist(String path) throws IOException {
        try {
            path = ShellFileUtil.fixFilePath(path);
            return this.stat(path) != null;
        } catch (Exception ex) {
            if (ExceptionUtil.hasMessage(ex, "No such file")) {
                return false;
            }
            throw ex;
        }
    }

    /**
     * 创建文件
     *
     * @param path 路径
     * @throws IOException 异常
     */
    public void touch(String path) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("".getBytes());
        this.put(stream, path);
    }

    /**
     * 重命名
     *
     * @param path    路径
     * @param newPath 新路径
     * @throws IOException 异常
     */
    public void rename(String path, String newPath) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.rename(path, newPath);
    }

    /**
     * 获取文件属性
     *
     * @param path 路径
     * @return 文件属性
     * @throws IOException 异常
     */
    public SftpClient.Attributes stat(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        return this.channel.stat(path);
    }

    /**
     * 上传
     *
     * @param src  源
     * @param dest 目标
     * @throws IOException 异常
     */
    public void put(String src, String dest) throws IOException {
        this.channel.put(Path.of(src), dest);
    }

    /**
     * 写入
     *
     * @param dest 目标
     * @throws IOException 异常
     */
    public OutputStream write(String dest) throws IOException {
        return this.channel.write(dest);
    }

    /**
     * 上传
     *
     * @param src  源
     * @param dest 目标
     * @throws IOException 异常
     */
    public void put(InputStream src, String dest) throws IOException {
        this.channel.put(src, dest);
        IOUtil.close(src);
    }


    /**
     * 下载
     *
     * @param src 源
     * @return 文件
     * @throws IOException 异常
     */
    public InputStream get(String src) throws IOException {
        src = ShellFileUtil.fixFilePath(src);
        return this.channel.read(src);
    }

    /**
     * 下载
     *
     * @param src  源
     * @param dest 目标
     * @throws IOException 异常
     */
    public void get(String src, String dest) throws IOException {
        src = ShellFileUtil.fixFilePath(src);
        dest = ShellFileUtil.fixFilePath(dest);
        InputStream stream = this.channel.read(src);
        IOUtil.saveToFile(stream, dest);
        IOUtil.close(stream);
    }

    /**
     * 下载
     *
     * @param src  源
     * @param dest 目标
     * @throws IOException 异常
     */
    public void get(String src, OutputStream dest) throws IOException {
        src = ShellFileUtil.fixFilePath(src);
        InputStream stream = this.channel.read(src);
        IOUtil.saveToStream(stream, dest);
        IOUtil.close(stream);
        IOUtil.close(dest);
    }

    /**
     * 修改权限，这个方法windows执行无效果
     *
     * @param permission 权限
     * @param path       文件路径
     */
    public void chmod(int permission, String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        // 获取现有属性
        SftpClient.Attributes attrs = this.channel.stat(path);
        // 修改权限位
        attrs.setPermissions(permission);
        // 使用 setStat 方法提交修改
        this.channel.setStat(path, attrs);
    }

    @Override
    public void close() {
        IOUtil.close(this.channel);
        this.realpathCache = null;
    }

    public boolean isClosed() {
        return !this.channel.isOpen();
    }
}
