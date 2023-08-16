package cn.oyzh.easyssh.ssh;

import lombok.experimental.UtilityClass;

/**
 * ssh事件
 *
 * @author oyzh
 * @since 2023/06/16
 */
@UtilityClass
public class SSHEvents {

    /**
     * 应用退出事件
     */
    public static final String APP_EXIT = "APP_EXIT";

    /**
     * 连接变更事件
     */
    public static final String CONNECTION_CHANGED = "CONNECTION_CHANGED";

    /**
     * ssh信息新增
     */
    public static final String SSH_INFO_ADD = "SSH_INFO_ADD";

    /**
     * ssh信息修改
     */
    public static final String SSH_INFO_UPDATED = "SSH_INFO_UPDATED";

    /**
     * 展开左侧
     */
    public static final String LEFT_EXTEND = "LEFT_EXTEND";

    /**
     * 收缩左侧
     */
    public static final String LEFT_COLLAPSE = "LEFT_COLLAPSE";

    /**
     * ssh导入开始事件
     */
    public static final String SSH_IMPORT_START = "SSH_IMPORT_START";

    /**
     * ssh导入结束事件
     */
    public static final String SSH_IMPORT_FINISH = "SSH_IMPORT_FINISH";

    /**
     * ssh搜索开始事件
     */
    public static final String SSH_SEARCH_START = "SSH_SEARCH_START";

    /**
     * ssh搜索结束事件
     */
    public static final String SSH_SEARCH_FINISH = "SSH_SEARCH_FINISH";

    /**
     * ssh搜索历史选择事件
     */
    public static final String SSH_SEARCH_HISTORY_SELECTED = "SSH_SEARCH_HISTORY_SELECTED";

    /**
     * ssh替换历史选择事件
     */
    public static final String SSH_REPLACE_HISTORY_SELECTED = "SSH_REPLACE_HISTORY_SELECTED";

    /**
     * 打开终端事件
     */
    public static final String SSH_OPEN_TERMINAL = "SSH_OPEN_TERMINAL";

    /**
     * 服务信息事件
     */
    public static final String SSH_SERVER_INFO = "SSH_SERVER_INFO";

    /**
     * 连接已关闭事件
     */
    public static final String SSH_CONNECT_CLOSED = "SSH_CONNECT_CLOSED";

    /**
     * 关闭连接事件
     */
    public static final String SSH_CLOSE_CONNECT = "SSH_CLOSE_CONNECT";

    /**
     * 添加连接事件
     */
    public static final String SSH_ADD_CONNECT = "SSH_ADD_CONNECT";

    /**
     * 添加分组事件
     */
    public static final String SSH_ADD_GROUP = "SSH_ADD_GROUP";
}
