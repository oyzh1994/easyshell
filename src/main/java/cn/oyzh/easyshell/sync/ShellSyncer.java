package cn.oyzh.easyshell.sync;

/**
 * 同步器
 *
 * @author oyzh
 * @since 2025-10-11
 */
public interface ShellSyncer {

    /**
     * 同步
     *
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    void sync(String snippetName) throws Exception;

    /**
     * 清除
     *
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    void clear(String snippetName) throws Exception;
}
