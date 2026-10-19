package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
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
 * sftp通道，封装sftp客户端的文件操作
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPChannel implements AutoCloseable {

    /**
     * 通道
     */
    private SftpClient channel;

    // /**
    //  * 缓存
    //  */
    // private ShellSFTPCache cache;

    /**
     * 构造sftp通道
     *
     * @param sftpClient sftp客户端
     */
    public ShellSFTPChannel(SftpClient sftpClient) {
        this.channel = sftpClient;
        // this.cache = cache;
    }

    /**
     * 列举指定路径下的文件项
     *
     * @param path 路径
     * @return 文件项列表
     * @throws IOException 异常
     */
    public Iterable<SftpClient.DirEntry> ls(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        JulLog.info("ls {}", path);
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
            // // 处理链接文件
            // if (file.isLink()) {
            //     this.cache.realpath(file, this);
            // }
            fileCallback.accept(file);
        }
    }

    /**
     * 删除文件
     *
     * @param path 路径
     * @throws IOException 异常
     */
    public void rm(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.remove(path);
    }

    /**
     * 删除目录
     *
     * @param path 路径
     * @throws IOException 异常
     */
    public void rmdir(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.rmdir(path);
    }

    /**
     * 当前工作目录
     */
    private String pwd;

    /**
     * 获取工作目录
     *
     * @return 工作目录
     * @throws Exception 异常
     */
    public String pwd() throws Exception {
        if (StringUtil.isEmpty(this.pwd)) {
            this.pwd = this.channel.canonicalPath(".");
        }
        return this.pwd;
    }

    /**
     * 创建目录
     *
     * @param path 路径
     * @throws IOException 异常
     */
    public void mkdir(String path) throws IOException {
        path = ShellFileUtil.fixFilePath(path);
        this.channel.mkdir(path);
    }

    /**
     * 判断文件或目录是否存在
     *
     * @param path 路径
     * @return 是否存在
     * @throws IOException 异常
     */
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
        try {
            path = ShellFileUtil.fixFilePath(path);
            return this.channel.stat(path);
        } catch (IOException ex) {
            if (ExceptionUtil.hasMessage(ex, "No such file")) {
                return null;
            }
            throw ex;
        }
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
        // 转为rwx格式
        String str = ShellFileUtil.octalToRwx(permission + "");
        // 转为权限
        int perms = ShellSFTPUtil.parsePermissions1(str);
        // 权限信息
        SftpClient.Attributes attrs = new SftpClient.Attributes();
        // 设置权限
        attrs.setPermissions(perms);
        // 使用 setStat 方法提交修改
        this.channel.setStat(path, attrs);
    }

    @Override
    public void close() {
        IOUtil.close(this.channel);
        // this.cache = null;
        this.channel = null;
    }

    /**
     * 是否已关闭
     *
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return !this.channel.isOpen();
    }
}
