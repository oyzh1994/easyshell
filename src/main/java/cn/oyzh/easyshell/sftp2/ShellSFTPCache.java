package cn.oyzh.easyshell.sftp2;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * sftp链接文件管理器
 *
 * @author oyzh
 * @since 2025-06-07
 */
public class ShellSFTPCache implements AutoCloseable {

    /**
     * 路径缓存
     * key: 路径 value: 链接路径
     */
    private final Map<String, String> pathCache = new ConcurrentHashMap<>(64);

    /**
     * 属性缓存
     * key: 路径:修改时间 value: 文件属性
     */
    private final Map<String, SftpATTRS> attrsCache = new ConcurrentHashMap<>(64);

    /**
     * 拥有者缓存
     * key: 拥有者id value: 拥有者名称
     */
    private final Map<Integer, String> ownerCache = new ConcurrentHashMap<>(4);

    /**
     * 分组缓存
     * key: 分组id value: 分组名称
     */
    private final Map<Integer, String> groupCache = new ConcurrentHashMap<>(4);

    /**
     * 读取链接
     *
     * @param file     文件
     * @param supplier 通道提供者
     */
    public void realpath(ShellSFTPFile file, Supplier<ShellSFTPChannel> supplier) throws SftpException {
        try (ShellSFTPChannel channel = supplier.get()) {
            this.realpath(file, channel);
        }
    }

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

    // /**
    //  * 读取链接
    //  *
    //  * @param file   文件
    //  * @param client 客户端
    //  */
    // public void realpath(ShellSFTPFile file, ShellSFTPClient client) throws Exception {
    //     this.realpath(file, client.takeSFTPChannel());
    // }

    /**
     * 获取拥有者
     *
     * @param uid    拥有者id
     * @param client 客户端
     * @return 拥有者名称
     */
    public String getOwner(int uid, ShellSFTPClient client) {
        String owner = this.ownerCache.get(uid);
        if (owner == null) {
            owner = client.exec_id_un(uid);
            this.ownerCache.put(uid, owner);
        }
        return owner;
    }

    /**
     * 获取分组
     *
     * @param gid    分组id
     * @param client 客户端
     * @return 分组名称
     */
    public String getGroup(int gid, ShellSFTPClient client) {
        String group = this.groupCache.get(gid);
        if (group == null) {
            group = client.exec_id_gn(gid);
            this.groupCache.put(gid, group);
        }
        return group;
    }

    @Override
    public void close() throws Exception {
        this.pathCache.clear();
        this.attrsCache.clear();
        this.ownerCache.clear();
        this.groupCache.clear();
    }
}
