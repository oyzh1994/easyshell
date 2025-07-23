package cn.oyzh.easyshell.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.function.ExceptionConsumer;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 文件客户端
 *
 * @author oyzh
 * @since 2025-04-27
 */
public interface ShellFileClient<E extends ShellFile> extends ShellBaseClient {

    /**
     * 列举文件
     *
     * @param filePath 文件路径
     * @return 文件列表
     * @throws Exception 异常
     */
    default List<E> lsFile(String filePath) throws Exception {
        List<E> files = new ArrayList<>();
        this.lsFileDynamic(filePath, files::add);
        return files;
    }

    /**
     * 列举文件，动态加载
     *
     * @param filePath     文件路径
     * @param fileCallback 文件回调
     * @throws Exception 异常
     */
    void lsFileDynamic(String filePath, Consumer<E> fileCallback) throws Exception;

    /**
     * 列举文件，批量加载
     *
     * @param filePath     文件路径
     * @param fileCallback 文件回调
     * @param batchSize    批量大小
     * @throws Exception 异常
     */
    default void lsFileBatch(String filePath, Consumer<List<E>> fileCallback, int batchSize) throws Exception {
        List<E> files = new ArrayList<>();
        this.lsFileDynamic(filePath, e -> {
            files.add(e);
            if (files.size() >= batchSize) {
                fileCallback.accept(files);
                files.clear();
            }
        });
        if (!files.isEmpty()) {
            fileCallback.accept(files);
            files.clear();
        }
    }

    /**
     * 递归列举文件
     *
     * @param file     文件路径
     * @param callback 文件回调
     * @throws Exception 异常
     */
    default void lsFileRecursive(E file, ExceptionConsumer<E> callback) throws Exception {
        if (!file.isNormal()) {
            return;
        }
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
     * 重命名文件
     *
     * @param file    文件
     * @param newName 新名称
     * @return 结果
     * @throws Exception 异常
     */
    boolean rename(E file, String newName) throws Exception;

    /**
     * 文件是否存在
     *
     * @param filePath 文件路径
     * @return 结果
     * @throws Exception 异常
     */
    boolean exist(String filePath) throws Exception;

    /**
     * 获取真实路径，对于部分文件系统适用
     *
     * @param filePath 文件路径
     * @return 真实路径
     * @throws Exception 异常
     */
    String realpath(String filePath) throws Exception;

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
    boolean createDir(String filePath) throws Exception;

    /**
     * 递归删除文件夹
     *
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    default void createDirRecursive(String filePath) throws Exception {
        filePath = ShellFileUtil.fixFilePath(filePath);
        String[] dirs = filePath.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            currentPath.append("/").append(dir);
            try {
                // 创建缺失目录
                if (!this.exist(currentPath.toString())) {
                    this.createDir(currentPath.toString());
                }
            } catch (Exception ex) {
                // 创建缺失目录
                if (ExceptionUtil.hasMessage(ex, "No such file")) {
                    this.createDir(currentPath.toString());
                } else {
                    throw ex;
                }
            }
        }
    }

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
     * 下载文件
     *
     * @param remoteFile 远程文件
     * @param callback   回调
     * @throws IOException 异常
     */
    InputStream getStream(E remoteFile, Function<Long, Boolean> callback) throws Exception;

    /**
     * 上传文件
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws IOException 异常
     */
    default void put(String localFile, String remoteFile) throws Exception {
        this.put(new File(localFile), remoteFile, null);
    }

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
     * @param callback   回调
     * @throws IOException 异常
     */
    default void put(File localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        this.put(new FileInputStream(localFile), remoteFile, callback);
    }

    /**
     * 上传文件
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @param callback   回调
     * @throws IOException 异常
     */
    void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception;

    /**
     * 上传文件
     *
     * @param remoteFile 远程文件
     * @param callback   回调
     * @throws IOException 异常
     */
    OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception;

    /**
     * 创建删除任务
     *
     * @param file 文件
     */
    default void doDelete(E file) {
        ShellFileDeleteTask deleteTask = new ShellFileDeleteTask(this.deleteCompetitor(), file, this);
        this.deleteTasks().add(deleteTask);
        deleteTask.doDelete(() -> {
            synchronized (this.deleteTasks()) {
                this.deleteTasks().remove(deleteTask);
            }
        }, ex -> {
            if (!ExceptionUtil.isInterrupt(ex)) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 获取删除竞争器
     *
     * @return 删除竞争器
     */
    Competitor deleteCompetitor();

    /**
     * 获取删除任务列表
     *
     * @return 删除任务列表
     */
    ObservableList<ShellFileDeleteTask> deleteTasks();

    /**
     * 创建上传任务
     *
     * @param localFile  本地文件
     * @param remotePath 远程路径
     */
    default void doUpload(File localFile, String remotePath) {
        ShellFileUploadTask uploadTask = new ShellFileUploadTask(this.uploadCompetitor(), localFile, remotePath, this);
        this.uploadTasks().add(uploadTask);
        uploadTask.doUpload(() -> {
            synchronized (this.uploadTasks()) {
                this.uploadTasks().remove(uploadTask);
            }
        }, ex -> {
            ex.printStackTrace();
            MessageBox.exception(ex);
        });
    }

    /**
     * 获取上传竞争器
     *
     * @return 上传竞争器
     */
    Competitor uploadCompetitor();

    /**
     * 获取上传任务列表
     *
     * @return 上传任务列表
     */
    ObservableList<ShellFileUploadTask> uploadTasks();

    /**
     * 创建下载任务
     *
     * @param remoteFile 远程文件
     * @param localPath  本地路径
     */
    default void doDownload(E remoteFile, File localPath) {
        this.doDownload(remoteFile, localPath.getPath());
    }

    /**
     * 创建下载任务
     *
     * @param remoteFile 远程文件
     * @param localPath  本地路径
     */
    default void doDownload(E remoteFile, String localPath) {
        ShellFileDownloadTask downloadTask = new ShellFileDownloadTask(this.downloadCompetitor(), remoteFile, localPath, this);
        this.downloadTasks().add(downloadTask);
        downloadTask.doDownload(() -> {
            synchronized (this.downloadTasks()) {
                this.downloadTasks().remove(downloadTask);
            }
        }, ex -> {
            ex.printStackTrace();
            MessageBox.exception(ex);
        });
    }

    /**
     * 获取下载竞争器
     *
     * @return 下载竞争器
     */
    Competitor downloadCompetitor();

    /**
     * 获取下载任务列表
     *
     * @return 下载任务列表
     */
    ObservableList<ShellFileDownloadTask> downloadTasks();

    /**
     * 创建传输任务
     *
     * @param remotePath   远程路径
     * @param localFile    本地文件
     * @param remoteClient 远程客户端
     */
    default void doTransport(String remotePath, E localFile, ShellFileClient<E> remoteClient) {
        ShellFileTransportTask transportTask = new ShellFileTransportTask(this.transportCompetitor(), remotePath, localFile, remoteClient, this);
        this.transportTasks().add(transportTask);
        transportTask.doTransport(() -> {
            synchronized (this.transportTasks()) {
                this.transportTasks().remove(transportTask);
            }
        }, ex -> {
            ex.printStackTrace();
            MessageBox.exception(ex);
        });
    }

    /**
     * 获取传输竞争器
     *
     * @return 传输竞争器
     */
    Competitor transportCompetitor();

    /**
     * 获取传输任务列表
     *
     * @return 传输任务列表
     */
    ObservableList<ShellFileTransportTask> transportTasks();

    /**
     * 获取任务数量
     *
     * @return 任务数量
     */
    default int getTaskSize() {
        return this.uploadTasks().size() + this.downloadTasks().size();
    }

    /**
     * 下载任务是否为空
     *
     * @return 结果
     */
    default boolean isDownloadTaskEmpty() {
        return this.downloadTasks().isEmpty();
    }

    /**
     * 上传任务是否为空
     *
     * @return 结果
     */
    default boolean isUploadTaskEmpty() {
        return this.uploadTasks().isEmpty();
    }

    /**
     * 删除任务是否为空
     *
     * @return 结果
     */
    default boolean isDeleteTaskEmpty() {
        return this.deleteTasks().isEmpty();
    }

    /**
     * 传输任务是否为空
     *
     * @return 结果
     */
    default boolean isTransportTaskEmpty() {
        return this.transportTasks().isEmpty();
    }

    /**
     * 任务是否为空
     *
     * @param type 类型
     * @return 结果
     */
    default boolean isTaskEmpty(String type) {
        if (type.contains("upload") && !this.isUploadTaskEmpty()) {
            return false;
        }
        if (type.contains("download") && !this.isDownloadTaskEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 添加任务数量监听
     *
     * @param callback 回调
     * @param type     类型
     */
    default void addTaskSizeListener(Runnable callback, String type) {
        if (type.contains("upload")) {
            this.uploadTasks().addListener((ListChangeListener<ShellFileUploadTask>) change -> {
                callback.run();
            });
        }
        if (type.contains("download")) {
            this.downloadTasks().addListener((ListChangeListener<ShellFileDownloadTask>) change -> {
                callback.run();
            });
        }
    }

    /**
     * 关闭延迟资源，例如文件流
     */
    void closeDelayResources();

    /**
     * 设置文件权限
     *
     * @param permissions 权限
     * @param filePath    文件路径
     * @return 结果
     * @throws Exception 异常
     */
    boolean chmod(int permissions, String filePath) throws Exception;

    /**
     * 文件信息
     *
     * @param filePath 文件路径
     * @return 文件
     * @throws Exception 异常
     */
    E fileInfo(String filePath) throws Exception;

    /**
     * 部分场景下，例如上传、下载、删除、传输会占用客户端，可能需要fork一个子客户端去操作
     * 如果不需要fork，则直接返回自己即可
     * 如果fork失败，则建议返回自己
     *
     * @return fork出来的子客户端
     * @see #isForked() 配合这个方法这是个子客户端
     */
    default ShellFileClient<E> forkClient() {
        return this;
    }

    /**
     * 是否子客户端
     *
     * @return 结果
     */
    default boolean isForked() {
        return false;
    }

    /**
     * 判断是否支持cd操作
     *
     * @return 结果
     */
    default boolean isCdSupport() {
        return true;
    }

    /**
     * 判断是否支持chmod操作
     *
     * @return 结果
     */
    default boolean isChmodSupport() {
        return true;
    }

    /**
     * 判断是否支持realpath操作
     *
     * @return 结果
     */
    default boolean isRealpathSupport() {
        return true;
    }

    /**
     * 判断是否支持workDir操作
     *
     * @return 结果
     */
    default boolean isWorkDirSupport() {
        return true;
    }

    /**
     * 判断是否支持putStream操作
     *
     * @return 结果
     */
    default boolean isPutStreamSupport() {
        return true;
    }

    /**
     * 判断是否支持createDir操作
     *
     * @return 结果
     */
    default boolean isCreateDirSupport() {
        return true;
    }

    /**
     * 判断是否支持createDirRecursive操作
     *
     * @return 结果
     */
    default boolean isCreateDirRecursiveSupport() {
        return true;
    }

    // String ACTION_cd = "cd";
    //
    // String ACTION_chmod = "chmod";
    //
    // String ACTION_workdir = "workdir";
    //
    // String ACTION_realpath = "realpath";
    //
    // String ACTION_putStream = "putStream";
    //
    // String ACTION_createDir = "createDir";
    //
    // String ACTION_createDirRecursive = "createDirRecursive";

}
