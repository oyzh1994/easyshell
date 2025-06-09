// package cn.oyzh.easyshell.sshj;
//
// import cn.oyzh.common.exception.ExceptionUtil;
// import cn.oyzh.common.util.IOUtil;
// import cn.oyzh.easyshell.file.ShellFileUtil;
// import com.jcraft.jsch.SftpException;
// import net.schmizz.sshj.sftp.FileAttributes;
// import net.schmizz.sshj.sftp.RemoteFile;
// import net.schmizz.sshj.sftp.RemoteResourceInfo;
// import net.schmizz.sshj.sftp.SFTPClient;
// import net.schmizz.sshj.xfer.InMemoryDestFile;
// import net.schmizz.sshj.xfer.InMemorySourceFile;
// import net.schmizz.sshj.xfer.LocalDestFile;
// import net.schmizz.sshj.xfer.LocalSourceFile;
//
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.stream.Collectors;
//
// /**
//  * @author oyzh
//  * @since 2025-03-05
//  */
// public class ShellSFTPChannel implements AutoCloseable{
//
//     private SFTPClient channel;
//
//     /**
//      * 链接管理器
//      */
//     private final ShellSFTPRealpathManager realpathManager;
//
//     public ShellSFTPChannel(SFTPClient channel, ShellSFTPRealpathManager realpathManager) {
//         this.channel = channel;
//         this.realpathManager = realpathManager;
//     }
//
//     public List<RemoteResourceInfo> ls(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         return this.channel.ls(path);
//     }
//
//     /**
//      * 获取链接路径
//      *
//      * @param path 路径
//      * @return 链接路径
//      * @throws SftpException 异常
//      */
//     public String realpath(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         return this.channel.readlink(path);
//     }
//
//     /**
//      * 列举文件
//      *
//      * @param path 文件路径
//      * @return 文件列表
//      * @throws SftpException 异常
//      */
//     public List<ShellSFTPFile> lsFile(String path) throws SftpException, IOException {
//         String filePath = ShellFileUtil.fixFilePath(path);
//         // 文件列表
//         List<ShellSFTPFile> files = new ArrayList<>();
//         // 总列表
//         List<RemoteResourceInfo> vector = this.ls(path);
//         // 遍历列表
//         for (RemoteResourceInfo lsEntry : vector) {
//             files.add(new ShellSFTPFile(filePath, lsEntry));
//         }
//         // 过滤链接文件
//         List<ShellSFTPFile> linkFiles = files.stream().filter(ShellSFTPFile::isLink).toList();
//         // 处理链接文件
//         this.realpathManager.put(linkFiles);
//         // 等待完成
//         this.realpathManager.waitComplete();
//         return files;
//     }
//
//     public List<ShellSFTPFile> lsFileNormal(String path) throws SftpException, IOException {
//         List<ShellSFTPFile> files = this.lsFile(path);
//         return files.stream().filter(ShellSFTPFile::isNormal).collect(Collectors.toList());
//     }
//
//     public void rm(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         this.channel.rm(path);
//     }
//
//     public void rmdir(String path) throws IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         this.channel.rmdir(path);
//     }
//
//     public void mkdir(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         this.channel.mkdir(path);
//     }
//
//     public boolean exist(String path) throws SftpException, IOException {
//         try {
//             path = ShellFileUtil.fixFilePath(path);
//             return this.stat(path) != null;
//         } catch (IOException ex) {
//             if (ExceptionUtil.hasMessage(ex, "No such file")) {
//                 return false;
//             }
//             throw ex;
//         }
//     }
//
//     public void touch(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         RemoteFile remoteFile = this.channel.open(path);
//         remoteFile.write(0, new byte[]{}, 0, 0);
//         remoteFile.close();
//     }
//
//     public void rename(String path, String newPath) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         this.channel.rename(path, newPath);
//     }
//
//     /**
//      * 获取文件属性
//      *
//      * @param path 路径
//      * @return 文件属性
//      * @throws SftpException 异常
//      */
//     public FileAttributes stat(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         return this.channel.stat(path);
//     }
//
//     public RemoteFile open(String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//        return this.channel.open(path);
//     }
//
//     public void put(String src, String dest) throws SftpException, IOException {
//         src = ShellFileUtil.fixFilePath(src);
//         dest = ShellFileUtil.fixFilePath(dest);
//         this.channel.put(src, dest);
//     }
//
//     public void put(InputStream src, String dest) throws SftpException, IOException {
//         dest = ShellFileUtil.fixFilePath(dest);
//         long length = src.available();
//         LocalSourceFile sourceFile = new InMemorySourceFile() {
//             @Override
//             public String getName() {
//                 return "";
//             }
//
//             @Override
//             public long getLength() {
//                 return length;
//             }
//
//             @Override
//             public InputStream getInputStream() {
//                 return src;
//             }
//         };
//         this.channel.put(sourceFile, dest);
//     }
//
//     public void get(String src, String dest) throws SftpException, IOException {
//         src = ShellFileUtil.fixFilePath(src);
//         dest = ShellFileUtil.fixFilePath(dest);
//         this.channel.get(src, dest);
//     }
//
//     public OutputStream getStream(String src) throws SftpException, IOException {
//         src = ShellFileUtil.fixFilePath(src);
//
//         ByteArrayOutputStream out = new ByteArrayOutputStream();
//         LocalDestFile destFile = new InMemoryDestFile() {
//             @Override
//             public long getLength() {
//                 return out.size();
//             }
//
//             @Override
//             public OutputStream getOutputStream() throws IOException {
//                 return out;
//             }
//
//             @Override
//             public OutputStream getOutputStream(boolean append) throws IOException {
//                 return out;
//             }
//         };
//         this.channel.get(src, destFile);
//         return out;
//     }
//
//     /**
//      * 修改权限，这个方法windows执行无效果
//      *
//      * @param permission 权限
//      * @param path       文件路径
//      */
//     public void chmod(int permission, String path) throws SftpException, IOException {
//         path = ShellFileUtil.fixFilePath(path);
//         this.channel.chmod(path, permission);
//     }
//
//     public boolean isConnected() {
//         return this.channel != null;
//     }
//
//     @Override
//     public void close() throws IOException {
//         IOUtil.close(this.channel);
//         this.channel = null;
//     }
// }
