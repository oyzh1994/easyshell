package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import com.jcraft.jsch.SftpException;

/**
 * @author oyzh
 * @since 2025-03-06
 */

public class ShellSFTPUtil {

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
     * @throws SftpException 异常
     */
    public static void realpath(ShellSFTPFile file, ShellSFTPClient client) throws Exception {
        // 读取链接文件
        if (file != null && file.isLink()) {
            String linkPath = client.realpath(file.getFilePath());
            if (linkPath != null) {
//                file.setLinkPath(linkPath);
                file.setLinkAttrs(client.stat(linkPath));
            }
        }
    }

    /**
     * 读取链接路径
     *
     * @param file    文件
     * @param channel sftp通道
     * @throws SftpException 异常
     */
    public static void realpath(ShellSFTPFile file, ShellSFTPChannel channel) throws SftpException {
        // 读取链接文件
        if (file != null && file.isLink()) {
            try {
                String linkPath = channel.realpath(file.getFilePath());
                if (linkPath != null) {
//                    file.setLinkPath(linkPath);
                    file.setLinkAttrs(channel.stat(linkPath));
                }
            } catch (SftpException e) {
                if (ExceptionUtil.hasMessage(e, "No such file")) {
                    JulLog.warn("realpath:{} fail", file.getFilePath());
                } else {
                    throw e;
                }
            }
        }
    }
}
