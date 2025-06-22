package cn.oyzh.easyshell.ssh.sftp;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sftp链接文件管理器
 *
 * @author oyzh
 * @since 2025-06-07
 */
public class ShellSFTPRealpathCache implements AutoCloseable {

    /**
     * 路径缓存
     * key: 路径 value: 链接路径
     */
    private final Map<String, String> pathCache = new ConcurrentHashMap<>();

    /**
     * 属性缓存
     * key: 路径:修改时间 value: 文件属性
     */
    private final Map<String, SftpATTRS> attrsCache = new ConcurrentHashMap<>();

    /**
     * 读取链接
     *
     * @param file    文件
     * @param channel 通道
     */
    public void realpath(ShellSFTPFile file, ShellSFTPChannel channel) throws SftpException {
        String filePath = file.getFilePath();
        String linkPath = this.pathCache.get(filePath);
        String attrKey = filePath + ":" + file.getMTime();
        SftpATTRS attrs = this.attrsCache.get(attrKey);
        // 缓存处理
        if (linkPath != null) {
            if (attrs == null) {
                attrs = channel.stat(linkPath);
                file.setLinkAttrs(attrs);
                if (attrs != null) {
                    this.attrsCache.put(attrKey, attrs);
                }
            } else {
                file.setLinkAttrs(attrs);
            }
        } else {// 正常处理
            linkPath = ShellSFTPUtil.realpath(file, channel);
            attrs = file.getLinkAttrs();
            if (linkPath != null) {
                this.pathCache.put(filePath, linkPath);
            }
            if (attrs != null) {
                this.attrsCache.put(attrKey, attrs);
            }
        }
    }

    /**
     * 读取链接
     *
     * @param file   文件
     * @param client 客户端
     */
    public void realpath(ShellSFTPFile file, ShellSFTPClient client) throws Exception {
        this.realpath(file, client.takeSFTPChannel());
    }

    @Override
    public void close() throws Exception {
        this.pathCache.clear();
        this.attrsCache.clear();
    }
}
