package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.function.ExceptionSupplier;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * sftp工具类
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSFTPUtil {

    ///**
    // * 获取拥有者
    // *
    // * @param uid    用户id
    // * @param client 客户端
    // * @return 拥有者名称
    // */
    //public static String getOwner(int uid, ShellSFTPClient client) {
    //    if (client.isWindows()) {
    //        return "-";
    //    }
    //    ShellSFTPAttr attr = client.getAttr();
    //    String ownerName = attr.getOwner(uid);
    //    if (ownerName == null) {
    //        ownerName = client.exec_id_un(uid);
    //        attr.putOwner(uid, ownerName);
    //    }
    //    return ownerName;
    //}
    //
    ///**
    // * 获取分租
    // *
    // * @param gid    分组id
    // * @param client 客户端
    // * @return 分组名称
    // */
    //public static String getGroup(int gid, ShellSFTPClient client) {
    //    if (client.isWindows()) {
    //        return "-";
    //    }
    //    ShellSFTPAttr attr = client.getAttr();
    //    String groupName = attr.getGroup(gid);
    //    if (groupName == null) {
    //        groupName = client.exec_id_gn(gid);
    //        attr.putGroup(gid, groupName);
    //    }
    //    return groupName;
    //}

    /**
     * 将文件属性转换为 10 位权限字符串（含文件类型）
     *
     * @param attrs 文件属性
     * @return 10位权限字符串
     */
    public static String formatPermissions(SftpATTRS attrs) {
        StringBuilder result = new StringBuilder(10);
        result.append(getFileType(attrs));
        int permissions = attrs.getPermissions();
        result.append((permissions & 0400) != 0 ? 'r' : '-');
        result.append((permissions & 0200) != 0 ? 'w' : '-');
        if ((permissions & 0100) != 0) {
            result.append((permissions & 04000) != 0 ? 's' : 'x');
        } else {
            result.append((permissions & 04000) != 0 ? 'S' : '-');
        }
        result.append((permissions & 0040) != 0 ? 'r' : '-');
        result.append((permissions & 0020) != 0 ? 'w' : '-');
        if ((permissions & 0010) != 0) {
            result.append((permissions & 02000) != 0 ? 's' : 'x');
        } else {
            result.append((permissions & 02000) != 0 ? 'S' : '-');
        }
        result.append((permissions & 0004) != 0 ? 'r' : '-');
        result.append((permissions & 0002) != 0 ? 'w' : '-');
        if ((permissions & 0001) != 0) {
            result.append((permissions & 01000) != 0 ? 't' : 'x');
        } else {
            result.append((permissions & 01000) != 0 ? 'T' : '-');
        }
        return result.toString();
    }

    /**
     * 将9位权限字符串解析为整数权限位
     *
     * @param permissionString 9位权限字符串，如 "rwxr-xr-x"
     * @return 整数权限位
     */
    public static int parsePermissions1(String permissionString) {
        if (permissionString == null || permissionString.length() != 9) {
            throw new IllegalArgumentException("permission string must contain 9 characters");
        }
        int permissions = 0;
        permissions |= (permissionString.charAt(0) == 'r') ? 0400 : 0;
        permissions |= (permissionString.charAt(1) == 'w') ? 0200 : 0;
        permissions |= (permissionString.charAt(2) == 'x') ? 0100 : 0;
        permissions |= (permissionString.charAt(2) == 's' || permissionString.charAt(2) == 'S') ? 04000 : 0;
        permissions |= (permissionString.charAt(3) == 'r') ? 0040 : 0;
        permissions |= (permissionString.charAt(4) == 'w') ? 0020 : 0;
        permissions |= (permissionString.charAt(5) == 'x') ? 0010 : 0;
        permissions |= (permissionString.charAt(5) == 's' || permissionString.charAt(5) == 'S') ? 02000 : 0;
        permissions |= (permissionString.charAt(6) == 'r') ? 0004 : 0;
        permissions |= (permissionString.charAt(7) == 'w') ? 0002 : 0;
        permissions |= (permissionString.charAt(8) == 'x') ? 0001 : 0;
        permissions |= (permissionString.charAt(8) == 't' || permissionString.charAt(8) == 'T') ? 01000 : 0;
        return permissions;
    }

    /**
     * 获取文件类型
     *
     * @param attrs 属性
     * @return 文件类型
     */
    public static String getFileType(SftpATTRS attrs) {
        if (attrs.isDir()) {
            return "d";
        }
        if (attrs.isLink()) {
            return "l";
        }
        if (attrs.isReg()) {
            return "-";
        }
        return "?";
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
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
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
                if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    JulLog.warn("realpath:{} fail", file.getFilePath());
                } else {
                    throw ex;
                }
            }
        }
        return null;
    }

    /**
     * 解析路径，将~展开为用户home目录
     * SFTP协议不支持~，这里在客户端展开
     *
     * @param filePath 文件路径
     * @param homeFunc home目录获取函数
     * @return 解析后的路径
     */
    public static String resolvePath(String filePath, ExceptionSupplier<String> homeFunc) {
        filePath = ShellFileUtil.fixFilePath(filePath);
        if (StringUtil.isBlank(filePath) || !StringUtil.startWith(filePath, "~")) {
            return filePath;
        }
        if (homeFunc == null) {
            return filePath;
        }
        try {
            String home = homeFunc.get();
            if (StringUtil.isBlank(home)) {
                return filePath;
            }
            if ("~".equals(filePath)) {
                return home;
            }
            if (StringUtil.startWith(filePath, "~/") || StringUtil.startWith(filePath, "~\\")) {
                return home + filePath.substring(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // ~user形式无法确定目标用户的目录，保持原样
        return filePath;
    }
}
