package cn.oyzh.easyssh.sftp;

import cn.oyzh.easyssh.ssh.SSHClient;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2025-03-06
 */
@UtilityClass
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

    public static String getOwner(int uid, SSHClient client) {
        SftpAttr attr = client.getAttr();
        String ownerName = attr.getOwner(uid);
        if (ownerName == null) {
            ownerName = client.exec_id_un(uid);
            attr.putOwner(uid, ownerName);
        }
        return ownerName;
    }

    public static String getGroup(int gid, SSHClient client) {
        SftpAttr attr = client.getAttr();
        String groupName = SftpUtil.getOwner(gid, client);
        if (groupName == null) {
            groupName = client.exec_id_gn(gid);
            attr.putGroup(gid, groupName);
        }
        return groupName;
    }
}
