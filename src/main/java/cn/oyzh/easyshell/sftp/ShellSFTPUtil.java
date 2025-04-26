package cn.oyzh.easyshell.sftp;

import com.jcraft.jsch.SftpException;

/**
 * @author oyzh
 * @since 2025-03-06
 */

public class ShellSFTPUtil {

    public static String getOwner(int uid, ShellSFTPClient client) {
        ShellSFTPAttr attr = client.getAttr();
        String ownerName = attr.getOwner(uid);
        if (ownerName == null) {
            ownerName = client.exec_id_un(uid);
            attr.putOwner(uid, ownerName);
        }
        return ownerName;
    }

    public static String getGroup(int gid, ShellSFTPClient client) {
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
     * @param file 文件
     * @param sftp sftp操作器
     * @throws SftpException 异常
     */
    public static void realpath(ShellSFTPFile file, ShellSFTPChannel sftp) throws SftpException {
        // 读取链接文件
        if (file != null && file.isLink()) {
            String linkPath = sftp.realpath(file.getFilePath());
            if (linkPath != null) {
                file.setLinkPath(linkPath);
                file.setLinkAttrs(sftp.stat(linkPath));
            }
        }
    }
}
