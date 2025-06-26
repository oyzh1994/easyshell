package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import com.jcraft.jsch.SftpException;

/**
 * sftp工具类
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSFTPUtil {

    /**
     * 获取拥有者
     *
     * @param uid    用户id
     * @param client 客户端
     * @return 拥有者名称
     */
    public static String getOwner(int uid, ShellSFTPClient client) {
        if (client.isWindows()) {
            return "-";
        }
        ShellSFTPAttr attr = client.getAttr();
        String ownerName = attr.getOwner(uid);
        if (ownerName == null) {
            ownerName = client.exec_id_un(uid);
            attr.putOwner(uid, ownerName);
        }
        return ownerName;
    }

    /**
     * 获取分租
     *
     * @param gid    分组id
     * @param client 客户端
     * @return 分组名称
     */
    public static String getGroup(int gid, ShellSFTPClient client) {
        if (client.isWindows()) {
            return "-";
        }
        ShellSFTPAttr attr = client.getAttr();
        String groupName = attr.getGroup(gid);
        if (groupName == null) {
            groupName = client.exec_id_gn(gid);
            attr.putGroup(gid, groupName);
        }
        return groupName;
    }

    /**
     * 读取链接路径
     *
     * @param file   文件
     * @param client 客户端
     * @return 链接路径
     * @throws SftpException 异常
     */
    public static String realpath(ShellSFTPFile file, ShellSFTPClient client) throws Exception {
        // 读取链接文件
        if (file != null && file.isLink() && file.isNormal()) {
            try {
                String linkPath = client.realpath(file.getFilePath());
                if (linkPath != null) {
//                    file.setLinkPath(linkPath);
                    file.setLinkAttrs(client.stat(linkPath));
                }
                return linkPath;
            } catch (SftpException e) {
                if (ExceptionUtil.hasMessage(e, "No such file")) {
                    JulLog.warn("realpath:{} fail", file.getFilePath());
                } else {
                    throw e;
                }
            }
        }
        return null;
    }

    /**
     * 读取链接路径
     *
     * @param file    文件
     * @param channel sftp通道
     * @return 链接路径
     * @throws SftpException 异常
     */
    public static String realpath(ShellSFTPFile file, ShellSFTPChannel channel) throws SftpException {
        // 读取链接文件
        if (file != null && file.isLink() && file.isNormal()) {
            try {
                String linkPath = channel.realpath(file.getFilePath());
                if (linkPath != null) {
//                    file.setLinkPath(linkPath);
                    file.setLinkAttrs(channel.stat(linkPath));
                }
                return linkPath;
            } catch (SftpException ex) {
                if (ExceptionUtil.hasMessage(ex, "No such file")) {
                    JulLog.warn("realpath:{} fail", file.getFilePath());
                } else {
                    throw ex;
                }
            }
        }
        return null;
    }
}
