package cn.oyzh.easyshell.sshj.sftp;

import net.schmizz.sshj.sftp.FileAttributes;

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
    private final Map<String, FileAttributes> attrsCache = new ConcurrentHashMap<>();

    /**
     * 读取链接
     */
    public void realpath(ShellSFTPFile file, ShellSFTPClient client) {
        try {
            String filePath = file.getFilePath();
            String linkPath = this.pathCache.get(filePath);
            String attrKey = filePath + ":" + file.getMTime();
            FileAttributes attrs = this.attrsCache.get(attrKey);
            // 缓存处理
            if (linkPath != null) {
                if (attrs == null) {
                    attrs = client.stat(linkPath);
                    file.setLinkAttrs(attrs);
                    if (attrs != null) {
                        this.attrsCache.put(attrKey, attrs);
                    }
                } else {
                    file.setLinkAttrs(attrs);
                }
            } else {// 正常处理
                linkPath = ShellSFTPUtil.realpath(file, client);
                attrs = file.getLinkAttrs();
                if (linkPath != null) {
                    this.pathCache.put(filePath, linkPath);
                }
                if (attrs != null) {
                    this.attrsCache.put(attrKey, attrs);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        this.pathCache.clear();
        this.attrsCache.clear();
    }
}
