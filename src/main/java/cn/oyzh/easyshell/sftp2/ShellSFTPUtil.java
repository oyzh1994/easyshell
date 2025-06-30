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
     * 将 SSH 文件权限转换为 10 位字符串表示
     * @param attrs 文件属性
     * @return 类似 "drwxr-xr-x" 的权限字符串
     */
    public static String permissionsToString(SftpClient.Attributes attrs) {
        StringBuilder sb = new StringBuilder(10);

        // 第一位：文件类型
        int permissions = attrs.getPermissions();
        sb.append(getTypeChar(permissions));

        // 权限位：用户、组、其他
        appendPermissions(sb, permissions, 0x1C0); // 用户权限 (700)
        appendPermissions(sb, permissions, 0x38);  // 组权限 (070)
        appendPermissions(sb, permissions, 0x7);   // 其他权限 (007)

        // 处理特殊权限
        appendSpecialPermissions(sb, permissions);

        return sb.toString();
    }

    private static char getTypeChar(int permissions) {
        int typeBits = (permissions >>> 12) & 0xF;
        switch (typeBits) {
            case 0x4: return 'd'; // 目录
            case 0xA: return 'l'; // 符号链接
            case 0xC: return 's'; // 套接字
            case 0x2: return 'c'; // 字符设备
            case 0x6: return 'b'; // 块设备
            case 0x1: return 'p'; // 命名管道
            default: return '-';  // 普通文件
        }
    }

    private static void appendPermissions(StringBuilder sb, int permissions, int mask) {
        // 读权限
        sb.append((permissions & (mask >> 2)) != 0 ? 'r' : '-');
        // 写权限
        sb.append((permissions & (mask >> 1)) != 0 ? 'w' : '-');
        // 执行权限
        sb.append((permissions & mask) != 0 ? 'x' : '-');
    }

    private static void appendSpecialPermissions(StringBuilder sb, int permissions) {
        // SUID (4000)
        if ((permissions & 04000) != 0) {
            char c = sb.charAt(3);
            sb.setCharAt(3, c == 'x' ? 's' : 'S');
        }
        // SGID (2000)
        if ((permissions & 02000) != 0) {
            char c = sb.charAt(6);
            sb.setCharAt(6, c == 'x' ? 's' : 'S');
        }
        // Sticky (1000)
        if ((permissions & 01000) != 0) {
            char c = sb.charAt(9);
            sb.setCharAt(9, c == 'x' ? 't' : 'T');
        }
    }
}
