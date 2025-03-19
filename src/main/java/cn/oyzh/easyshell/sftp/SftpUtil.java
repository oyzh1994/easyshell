package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.shell.ShellClient;

/**
 * @author oyzh
 * @since 2025-03-06
 */

public class SftpUtil {

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
        SftpAttr attr = client.getAttr();
        String ownerName = attr.getOwner(uid);
        if (ownerName == null) {
            ownerName = client.exec_id_un(uid);
            attr.putOwner(uid, ownerName);
        }
        return ownerName;
    }

    public static String getGroup(int gid, ShellClient client) {
        SftpAttr attr = client.getAttr();
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
}
