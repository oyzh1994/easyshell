package cn.oyzh.easyshell.file;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 文件客户端
 *
 * @author oyzh
 * @since 2025-04-27
 */
public interface ShellFileClient<E extends ShellFile> extends BaseClient {

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
    boolean createDir(String filePath) throws Exception;

    /**
     * 递归删除文件夹
     *
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    void createDirRecursive(String filePath) throws Exception;

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
        ShellFileDeleteTask deleteTask = new ShellFileDeleteTask(file, this);
        this.deleteTasks().add(deleteTask);
        Thread worker = ThreadLocalUtil.getVal("delete:thread");
        if (!ThreadUtil.isAlive(worker)) {
            worker = ThreadUtil.startVirtual(() -> {
                while (!this.isDeleteTaskEmpty()) {
                    ShellFileDeleteTask task = this.deleteTasks().getFirst();
                    try {
                        task.doDelete();
                    } catch (InterruptedException | InterruptedIOException ex) {
                        JulLog.warn("delete interrupted");
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    } finally {
                        this.deleteTasks().remove(task);
                    }
                }
            });
            ThreadLocalUtil.setVal("delete:thread", worker);
        }
    }

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
        ShellFileUploadTask uploadTask = new ShellFileUploadTask(localFile, remotePath, this);
        uploadTask.setCancelCallback(t -> {
            this.uploadTasks().remove(t);
            this.closeDelayResources();
        });
        this.uploadTasks().add(uploadTask);
        Thread worker = ThreadLocalUtil.getVal("upload:thread");
        if (!ThreadUtil.isAlive(worker)) {
            worker = ThreadUtil.startVirtual(() -> {
                while (!this.isUploadTaskEmpty()) {
                    ShellFileUploadTask task = this.uploadTasks().getFirst();
                    try {
                        task.doUpload();
                    } catch (InterruptedException | InterruptedIOException ex) {
                        JulLog.warn("upload interrupted");
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    } finally {
                        this.uploadTasks().remove(task);
                    }
                }
            });
            ThreadLocalUtil.setVal("upload:thread", worker);
        }
    }

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
        ShellFileDownloadTask downloadTask = new ShellFileDownloadTask(remoteFile, localPath, this);
        downloadTask.setCancelCallback(t -> {
            this.downloadTasks().remove(t);
            this.closeDelayResources();
        });
        this.downloadTasks().add(downloadTask);
        Thread worker = ThreadLocalUtil.getVal("download:thread");
        if (!ThreadUtil.isAlive(worker)) {
            worker = ThreadUtil.startVirtual(() -> {
                while (!this.isDownloadTaskEmpty()) {
                    ShellFileDownloadTask task = this.downloadTasks().getFirst();
                    try {
                        task.doDownload();
                    } catch (InterruptedException | InterruptedIOException ex) {
                        JulLog.warn("download interrupted");
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    } finally {
                        this.downloadTasks().remove(task);
                    }
                }
            });
            ThreadLocalUtil.setVal("download:thread", worker);
        }
    }

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
    default void doTransport(String remotePath, E localFile, ShellFileClient<E> remoteClient) throws Exception {
        ShellFileTransportTask transportTask = new ShellFileTransportTask(remotePath, localFile, remoteClient, this);
        transportTask.setCancelCallback(t -> {
            this.transportTasks().remove(t);
            this.closeDelayResources();
        });
        this.transportTasks().add(transportTask);
        Thread worker = ThreadLocalUtil.getVal("transport:thread");
        if (!ThreadUtil.isAlive(worker)) {
            worker = ThreadUtil.startVirtual(() -> {
                while (!this.isTransportTaskEmpty()) {
                    ShellFileTransportTask task = this.transportTasks().getFirst();
                    try {
                        task.doTransport();
                    } catch (InterruptedException | InterruptedIOException ex) {
                        JulLog.warn("transport interrupted");
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    } finally {
                        this.transportTasks().remove(task);
                    }
                }
            });
            ThreadLocalUtil.setVal("transport:thread", worker);
        }
    }

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
     * @return 结果
     */
    default boolean isTaskEmpty() {
        return this.uploadTasks().isEmpty() && this.downloadTasks().isEmpty();
    }

    /**
     * 添加任务数量监听
     *
     * @param callback 回调
     */
    default void addTaskSizeListener(Runnable callback) {
        this.uploadTasks().addListener((ListChangeListener<ShellFileUploadTask>) change -> {
            callback.run();
        });
        this.downloadTasks().addListener((ListChangeListener<ShellFileDownloadTask>) change -> {
            callback.run();
        });
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
}