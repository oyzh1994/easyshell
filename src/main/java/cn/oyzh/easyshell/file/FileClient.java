package cn.oyzh.easyshell.file;

import cn.oyzh.easyshell.util.ShellFile;

import java.util.List;

/**
 * 文件客户端
 *
 * @author oyzh
 * @since 2025-04-27
 */
public interface FileClient<E extends ShellFile> {

    /**
     * 列举文件
     *
     * @param filePath 文件路径
     * @return 文件列表
     * @throws Exception 异常
     */
    List<E> lsFile(String filePath) throws Exception;

    /**
     * 删除文件
     *
     * @param file 文件
     * @throws Exception 异常
     */
    void delete(E file) throws Exception;

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
    String pwdDir() throws Exception;

    /**
     * 进入位置
     *
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    void cd(String filePath) throws Exception;
}
