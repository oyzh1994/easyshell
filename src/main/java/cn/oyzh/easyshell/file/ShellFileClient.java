package cn.oyzh.easyshell.file;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 文件客户端
 *
 * @author oyzh
 * @since 2025-04-27
 */
public interface ShellFileClient<E extends ShellFile> {

    /**
     * 列举文件
     *
     * @param filePath 文件路径
     * @return 文件列表
     * @throws Exception 异常
     */
    List<E> lsFile(String filePath) throws Exception;

    /**
     * 递归列举文件
     *
     * @param file     文件路径
     * @param callback 文件回调
     * @throws Exception 异常
     */
    default void lsFileRecursive(E file, Consumer<E> callback) throws Exception {
        if (file.isFile()) {
            callback.accept(file);
        } else {
            List<E> files = this.lsFile(file.getFilePath());
            for (E f : files) {
                this.lsFileRecursive(f, callback);
            }
        }
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @throws Exception 异常
     */
    default void delete(E file) throws Exception {
        this.delete(file.getFilePath());
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @throws Exception 异常
     */
    void delete(String file) throws Exception;

    /**
     * 删除文件夹
     *
     * @param dir 文件夹
     * @throws Exception 异常
     */
    default void deleteDir(E dir) throws Exception {
        this.deleteDir(dir.getFilePath());
    }

    /**
     * 删除文件夹
     *
     * @param dir 文件夹
     * @throws Exception 异常
     */
    void deleteDir(String dir) throws Exception;

    /**
     * 递归删除文件夹
     *
     * @param dir 文件夹
     * @throws Exception 异常
     */
    default void deleteDirRecursive(E dir) throws Exception {
        this.deleteDirRecursive(dir.getFilePath());
    }

    /**
     * 递归删除文件夹
     *
     * @param dir 文件夹
     * @throws Exception 异常
     */
    void deleteDirRecursive(String dir) throws Exception;

    /**
     * 执行删除文件/文件夹
     *
     * @param file 文件
     */
    void doDelete(E file);

    /**
     * 重命名文件
     *
     * @param filePath 文件路径
     * @param newPath  新路径
     * @return 结果
     * @throws Exception 异常
     */
    boolean rename(String filePath, String newPath) throws Exception;

    /**
     * 文件是否存在
     *
     * @param filePath 文件路径
     * @return 结果
     * @throws Exception 异常
     */
    boolean exist(String filePath) throws Exception;

    /**
     * 创建文件
     *
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    void touch(String filePath) throws Exception;

    /**
     * 创建文件夹
     *
     * @param filePath 文件路径
     * @return 结果
     * @throws Exception 异常
     */
    boolean mkdir(String filePath) throws Exception;

    /**
     * 获取当前位置
     *
     * @return 结果
     * @throws Exception 异常
     */
    String workDir() throws Exception;

    /**
     * 进入位置
     *
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    void cd(String filePath) throws Exception;

    /**
     * 执行上传
     *
     * @param localFile  本地文件
     * @param remotePath 远程路径
     */
    void doUpload(File localFile, String remotePath);

    /**
     * 执行下载
     *
     * @param remoteFile 远程文件
     * @param localPath  本地路径
     */
    default void doDownload(E remoteFile, File localPath) {
        this.doDownload(remoteFile, localPath.getPath());
    }

    /**
     * 执行下载
     *
     * @param remoteFile 远程文件
     * @param localPath  本地路径
     */
    void doDownload(E remoteFile, String localPath);

    /**
     * 下载文件
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    default void get(E remoteFile, String localFile) throws Exception {
        this.get(remoteFile, localFile, null);
    }

    /**
     * 下载文件
     *
     * @param remoteFile 远程文件
     * @param localFile  本地文件
     * @param callback   下载变化回调
     * @throws IOException 异常
     */
    void get(E remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception;

    /**
     * 上传文件
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    default void put(File localFile, String remoteFile) throws Exception {
        this.put(localFile, remoteFile, null);
    }

    /**
     * 上传文件
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
     void put(File localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception ;
}