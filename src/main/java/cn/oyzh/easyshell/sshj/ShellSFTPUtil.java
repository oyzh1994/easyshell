// package cn.oyzh.easyshell.sshj;
//
// import cn.oyzh.common.exception.ExceptionUtil;
// import cn.oyzh.common.log.JulLog;
// import cn.oyzh.easyshell.ssh.sftp.ShellSFTPAttr;
// import cn.oyzh.easyshell.ssh.sftp.ShellSFTPChannel;
// import cn.oyzh.easyshell.ssh.sftp.ShellSFTPClient;
// import cn.oyzh.easyshell.ssh.sftp.ShellSFTPFile;
// import com.jcraft.jsch.SftpException;
// import net.schmizz.sshj.sftp.FileAttributes;
// import net.schmizz.sshj.sftp.FileMode;
// import net.schmizz.sshj.xfer.FilePermission;
//
// import java.util.Set;
//
// /**
//  * @author oyzh
//  * @since 2025-03-06
//  */
//
// public class ShellSFTPUtil {
//
//     public static String getOwner(int uid, cn.oyzh.easyshell.ssh.sftp.ShellSFTPClient client) {
//         if (client.isWindows()) {
//             return "-";
//         }
//         cn.oyzh.easyshell.ssh.sftp.ShellSFTPAttr attr = client.getAttr();
//         String ownerName = attr.getOwner(uid);
//         if (ownerName == null) {
//             ownerName = client.exec_id_un(uid);
//             attr.putOwner(uid, ownerName);
//         }
//         return ownerName;
//     }
//
//     public static String getGroup(int gid, cn.oyzh.easyshell.ssh.sftp.ShellSFTPClient client) {
//         if (client.isWindows()) {
//             return "-";
//         }
//         ShellSFTPAttr attr = client.getAttr();
//         String groupName = attr.getGroup(gid);
//         if (groupName == null) {
//             groupName = client.exec_id_gn(gid);
//             attr.putGroup(gid, groupName);
//         }
//         return groupName;
//     }
//
//     /**
//      * 读取链接路径
//      *
//      * @param file   文件
//      * @param client 客户端
//      * @return 链接路径
//      * @throws SftpException 异常
//      */
//     public static String realpath(ShellSFTPFile file, ShellSFTPClient client) throws Exception {
//         // 读取链接文件
//         if (file != null && file.isLink() && file.isNormal()) {
//             try {
//                 String linkPath = client.realpath(file.getFilePath());
//                 if (linkPath != null) {
// //                    file.setLinkPath(linkPath);
//                     file.setLinkAttrs(client.stat(linkPath));
//                 }
//                 return linkPath;
//             } catch (SftpException e) {
//                 if (ExceptionUtil.hasMessage(e, "No such file")) {
//                     JulLog.warn("realpath:{} fail", file.getFilePath());
//                 } else {
//                     throw e;
//                 }
//             }
//         }
//         return null;
//     }
//
//     /**
//      * 读取链接路径
//      *
//      * @param file    文件
//      * @param channel sftp通道
//      * @return 链接路径
//      * @throws SftpException 异常
//      */
//     public static String realpath(ShellSFTPFile file, ShellSFTPChannel channel) throws SftpException {
//         // 读取链接文件
//         if (file != null && file.isLink() && file.isNormal()) {
//             try {
//                 String linkPath = channel.realpath(file.getFilePath());
//                 if (linkPath != null) {
// //                    file.setLinkPath(linkPath);
//                     file.setLinkAttrs(channel.stat(linkPath));
//                 }
//                 return linkPath;
//             } catch (SftpException ex) {
//                 if (ExceptionUtil.hasMessage(ex, "No such file")) {
//                     JulLog.warn("realpath:{} fail", file.getFilePath());
//                 } else {
//                     throw ex;
//                 }
//             }
//         }
//         return null;
//     }
//
//     /**
//      * 将 SSHJ 的 FileAttributes 权限转换为类似 "drwx------" 的字符串
//      */
//     public static String formatPermissions(FileAttributes attrs) {
//         StringBuilder sb = new StringBuilder(10);
//         // 1. 文件类型
//         FileMode.Type type = attrs.getMode().getType();
//         switch (type) {
//             case DIRECTORY:
//                 sb.append('d');
//                 break;
//             case SYMLINK:
//                 sb.append('l');
//                 break;
//             case CHAR_SPECIAL:
//                 sb.append('c');
//                 break;
//             case BLOCK_SPECIAL:
//                 sb.append('b');
//                 break;
//             case FIFO_SPECIAL:
//                 sb.append('p');
//                 break;
//             case SOCKET_SPECIAL:
//                 sb.append('s');
//                 break;
//             default:
//                 sb.append('-'); // 普通文件
//         }
//
//         // 2. 权限位：所有者、组、其他用户
//         Set<FilePermission> permissions = attrs.getMode().getPermissions();
//         // 所有者权限
//         sb.append(permissions.contains(FilePermission.USR_R) ? 'r' : '-');
//         sb.append(permissions.contains(FilePermission.USR_W) ? 'w' : '-');
//         sb.append(permissions.contains(FilePermission.USR_X) ? 'x' : '-');
//         // 组权限
//         sb.append(permissions.contains(FilePermission.GRP_R) ? 'r' : '-');
//         sb.append(permissions.contains(FilePermission.GRP_W) ? 'w' : '-');
//         sb.append(permissions.contains(FilePermission.GRP_X) ? 'x' : '-');
//         // 其他用户权限
//         sb.append(permissions.contains(FilePermission.OTH_R) ? 'r' : '-');
//         sb.append(permissions.contains(FilePermission.OTH_W) ? 'w' : '-');
//         sb.append(permissions.contains(FilePermission.OTH_X) ? 'x' : '-');
//         return sb.toString();
//     }
// }
