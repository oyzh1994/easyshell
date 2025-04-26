package cn.oyzh.easyshell.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShellFTPUtil {

    public static void uploadFolder(FTPClient ftpClient, File localFolder, String remoteBaseDir) throws IOException {
        if (!localFolder.exists() ||!localFolder.isDirectory()) {
            return;
        }
        // 遍历本地文件夹中的所有文件和子文件夹
        File[] files = localFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 处理子文件夹
                    String remoteSubDir = remoteBaseDir + "/" + file.getName();
                    // 在服务器上创建对应的子文件夹
                    if (!ftpClient.changeWorkingDirectory(remoteSubDir)) {
                        if (!ftpClient.makeDirectory(remoteSubDir)) {
                            System.err.println("无法创建远程文件夹: " + remoteSubDir);
                            continue;
                        }
                        ftpClient.changeWorkingDirectory(remoteSubDir);
                    }
                    // 递归上传子文件夹
                    uploadFolder(ftpClient, file, remoteSubDir);
                    // 返回上级目录
                    ftpClient.changeToParentDirectory();
                } else {
                    // 处理文件
                    String remoteFilePath = remoteBaseDir + "/" + file.getName();
                    try (InputStream inputStream = new FileInputStream(file)) {
                        // 上传文件
                        boolean uploaded = ftpClient.storeFile(remoteFilePath, inputStream);
                        if (uploaded) {
                            System.out.println("文件上传成功: " + remoteFilePath);
                        } else {
                            System.err.println("文件上传失败: " + remoteFilePath);
                        }
                    }
                }
            }
        }
    }

    public static void deleteDirectory(FTPClient ftpClient, String directory) throws IOException {
        FTPFile[] files = ftpClient.listFiles(directory);
        if (files != null) {
            for (FTPFile file : files) {
                String filePath = directory + "/" + file.getName();
                if (file.isDirectory()) {
                    // 递归删除子文件夹
                    deleteDirectory(ftpClient, filePath);
                } else {
                    // 删除文件
                    ftpClient.deleteFile(filePath);
                }
            }
        }
        // 删除空文件夹
        ftpClient.removeDirectory(directory);
    }


    public static String getPermissionsString(FTPFile file) {
        StringBuilder permissions = new StringBuilder();

        // 根据文件类型添加前缀
        permissions.append(getFileTypePrefix(file));

        // 用户权限
        permissions.append(getPermissionChar(file, FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        permissions.append(getPermissionChar(file, FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        permissions.append(getPermissionChar(file, FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));

        // 组权限
        permissions.append(getPermissionChar(file, FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        permissions.append(getPermissionChar(file, FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        permissions.append(getPermissionChar(file, FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));

        // 其他用户权限
        permissions.append(getPermissionChar(file, FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        permissions.append(getPermissionChar(file, FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        permissions.append(getPermissionChar(file, FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        return permissions.toString();
    }

    private static String getFileTypePrefix(FTPFile file) {
        if (file.isDirectory()) {
            return "d";
        } else if (file.isSymbolicLink()) {
            return "l";
        } else {
            return "-";
        }
    }

    private static char getPermissionChar(FTPFile file, int who, int permission) {
        return file.hasPermission(who, permission) ? getPermissionSymbol(permission) : '-';
    }

    private static char getPermissionSymbol(int permission) {
        switch (permission) {
            case FTPFile.READ_PERMISSION:
                return 'r';
            case FTPFile.WRITE_PERMISSION:
                return 'w';
            case FTPFile.EXECUTE_PERMISSION:
                return 'x';
            default:
                return '-';
        }
    }

}
