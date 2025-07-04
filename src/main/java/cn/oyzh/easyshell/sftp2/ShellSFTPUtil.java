package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import org.apache.sshd.sftp.client.SftpClient;


/**
 * sftp工具类
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class ShellSFTPUtil {

    /**
     * 读取链接路径
     *
     * @param file   文件
     * @param client 客户端
     * @return 链接路径
     * @throws Exception 异常
     */
    public static String realpath(ShellSFTPFile file, ShellSFTPClient client) throws Exception {
        // 读取链接文件
        if (file != null && file.isLink() && file.isNormal()) {
            try {
                String linkPath = client.realpath(file.getFilePath());
                if (linkPath != null) {
                    file.setLinkAttrs(client.stat(linkPath));
                }
                return linkPath;
            } catch (Exception e) {
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
     * @throws Exception 异常
     */
    public static String realpath(ShellSFTPFile file, ShellSFTPChannel channel) throws Exception {
        // 读取链接文件
        if (file != null && file.isLink() && file.isNormal()) {
            try {
                String linkPath = channel.realpath(file.getFilePath());
                if (linkPath != null) {
                    file.setLinkAttrs(channel.stat(linkPath));
                }
                return linkPath;
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "No such file")) {
                    JulLog.warn("realpath:{} fail", file.getFilePath());
                } else {
                    throw ex;
                }
            }
        }
        return null;
    }

    /**
     * 将文件属性转换为 10 位权限字符串（含文件类型）
     */
    public static String formatPermissions(SftpClient.Attributes attrs) {
        StringBuilder sb = new StringBuilder(10);

        // 第一位：文件类型
        sb.append(getFileType(attrs));

        int permissions = attrs.getPermissions();

        // 所有者权限（含特殊位）
        sb.append((permissions & 0400) != 0 ? 'r' : '-');
        sb.append((permissions & 0200) != 0 ? 'w' : '-');
        if ((permissions & 0100) != 0) {
            sb.append((permissions & 04000) != 0 ? 's' : 'x'); // SUID
        } else {
            sb.append((permissions & 04000) != 0 ? 'S' : '-');
        }

        // 组权限（含特殊位）
        sb.append((permissions & 0040) != 0 ? 'r' : '-');
        sb.append((permissions & 0020) != 0 ? 'w' : '-');
        if ((permissions & 0010) != 0) {
            sb.append((permissions & 02000) != 0 ? 's' : 'x'); // SGID
        } else {
            sb.append((permissions & 02000) != 0 ? 'S' : '-');
        }

        // 其他用户权限（含特殊位）
        sb.append((permissions & 0004) != 0 ? 'r' : '-');
        sb.append((permissions & 0002) != 0 ? 'w' : '-');
        if ((permissions & 0001) != 0) {
            sb.append((permissions & 01000) != 0 ? 't' : 'x'); // Sticky
        } else {
            sb.append((permissions & 01000) != 0 ? 'T' : '-');
        }

        return sb.toString();
    }

    /**
     * 将9位权限字符串解析为整数权限位
     *
     * @param permissionString 9位权限字符串，如 "rwxr-xr-x"
     */
    public static int parsePermissions1(String permissionString) {

        int permissions = 0;

        // 解析所有者权限
        permissions |= (permissionString.charAt(0) == 'r') ? 0400 : 0;
        permissions |= (permissionString.charAt(1) == 'w') ? 0200 : 0;
        permissions |= (permissionString.charAt(2) == 'x') ? 0100 : 0;
        permissions |= (permissionString.charAt(2) == 's' || permissionString.charAt(2) == 'S') ? 04000 : 0;

        // 解析组权限
        permissions |= (permissionString.charAt(3) == 'r') ? 0040 : 0;
        permissions |= (permissionString.charAt(4) == 'w') ? 0020 : 0;
        permissions |= (permissionString.charAt(5) == 'x') ? 0010 : 0;
        permissions |= (permissionString.charAt(5) == 's' || permissionString.charAt(5) == 'S') ? 02000 : 0;

        // 解析其他用户权限
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
    public static String getFileType(SftpClient.Attributes attrs) {
        // 第一位：文件类型
        if (attrs.isDirectory()) {
            return "d";
        }
        if (attrs.isSymbolicLink()) {
            return "l";
        }
        if (attrs.isRegularFile()) {
            return "-";
        }
        return "?";
    }
}
