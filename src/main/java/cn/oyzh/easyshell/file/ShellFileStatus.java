package cn.oyzh.easyshell.file;

/**
 * 文件任务状态
 *
 * @author oyzh
 * @since 2025-03-15
 */
public enum ShellFileStatus {

    // 预处理
    IN_PREPARATION,
    // 执行中
    EXECUTE_ING,
    // 已结束
    FINISHED,
    // 已失败
    FAILED,
    // 已取消
    CANCELED
}
