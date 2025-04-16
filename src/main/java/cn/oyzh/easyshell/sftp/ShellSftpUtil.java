package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.shell.ShellClient;
import com.jcraft.jsch.SftpException;

/**
 * @author oyzh
 * @since 2025-03-06
 */

public class ShellSftpUtil {

    public static String concat(String src, String name) {
        if (src.endsWith("/") && name.startsWith("/")) {
            return src + name.substring(1);
        }
        if (!src.endsWith("/") && !name.startsWith("/")) {
            return src + "/" + name;
        }
        return src + name;
    }

    public static String getOwner(int uid, ShellClient client) {
        ShellSftpAttr attr = client.getAttr();
        String ownerName = attr.getOwner(uid);
        if (ownerName == null) {
            ownerName = client.exec_id_un(uid);
            attr.putOwner(uid, ownerName);
        }
        return ownerName;
    }

    public static String getGroup(int gid, ShellClient client) {
        ShellSftpAttr attr = client.getAttr();
        String groupName = attr.getGroup(gid);
        if (groupName == null) {
            groupName = client.exec_id_gn(gid);
            attr.putGroup(gid, groupName);
        }
        return groupName;
    }

    public static String parent(String dest) {
        if (StringUtil.isEmpty(dest)) {
            return dest;
        }
        return dest.substring(0, dest.lastIndexOf("/"));
    }

    /**
     * 读取链接路径
     *
     * @param file 文件
     * @param sftp sftp操作器
     * @throws SftpException 异常
     */
    public static void realpath(ShellSftpFile file, ShellSftp sftp) throws SftpException {
        // 读取链接文件
        if (file != null && file.isLink()) {
            String linkPath = sftp.realpath(file.getPath());
            if (linkPath != null) {
                file.setLinkPath(linkPath);
                file.setLinkAttrs(sftp.stat(linkPath));
            }
        }
    }
}
