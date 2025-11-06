package cn.oyzh.easyshell.event.mysql;//package cn.oyzh.easymysql.mysql;
//
//import lombok.experimental.UtilityClass;
//
///**
// * DB事件类型
// *
// * @author oyzh
// * @since 2023/11/20
// */
//@UtilityClass
//public class DBEventTypes {
//
//    /**
//     * 应用退出事件
//     */
//    public static final String APP_EXIT = "APP_EXIT";
//
//    /**
//     * DB节点已修改事件(来源于消息)
//     */
//    public static final String DB_NODE_UPDATED = "DB_NODE_UPDATED";
//
//    /**
//     * DB节点删除事件(来源于操作)
//     */
//    public static final String DB_NODE_DELETE = "DB_NODE_DELETE";
//
//    /**
//     * DB连接成功事件
//     */
//    public static final String DB_CONNECTION_CONNECTED = "DB_CONNECTION_CONNECTED";
//
//    /**
//     * DB连接关闭事件
//     */
//    public static final String DB_CONNECTION_CLOSED = "DB_CONNECTION_CLOSED";
//
//    /**
//     * DB数据库关闭事件
//     */
//    public static final String DB_DATABASE_CLOSED = "DB_DATABASE_CLOSED";
//
//    /**
//     * DB数据库新增事件
//     */
//    public static final String DB_DATABASE_ADDED = "DB_DATABASE_ADDED";
//
//    /**
//     * DB数据库编辑事件
//     */
//    public static final String DB_DATABASE_UPDATED = "DB_DATABASE_UPDATED";
//
//    /**
//     * DB数据库删除事件
//     */
//    public static final String DB_DATABASE_DROPPED = "DB_DATABASE_DROPPED";
//
//    /**
//     * DB树子节点变化事件
//     */
//    public static final String TREE_CHILD_CHANGED = "TREE_CHILD_CHANGED";
//
//    /**
//     * DB树过子节点滤事件
//     */
//    public static final String TREE_CHILD_FILTER = "TREE_CHILD_FILTER";
//
//    /**
//     * DB树图标变化事件
//     */
//    public static final String TREE_GRAPHIC_CHANGED = "TREE_GRAPHIC_CHANGED";
//
//    /**
//     * DB树图标颜色变化事件
//     */
//    public static final String TREE_GRAPHIC_COLOR_CHANGED = "TREE_GRAPHIC_COLOR_CHANGED";
//
//    /**
//     * DB信息新增
//     */
//    public static final String DB_INFO_ADDED = "DB_INFO_ADDED";
//
//    /**
//     * DB信息修改
//     */
//    public static final String DB_INFO_UPDATED = "DB_INFO_UPDATED";
//
//    /**
//     * DB删除修改
//     */
//    public static final String DB_INFO_DELETED = "DB_INFO_DELETED";
//
//    /**
//     * 展开左侧
//     */
//    public static final String LEFT_EXTEND = "LEFT_EXTEND";
//
//    /**
//     * 收缩左侧
//     */
//    public static final String LEFT_COLLAPSE = "LEFT_COLLAPSE";
//
//    /**
//     * DB认证
//     */
//    public static final String DB_AUTH = "DB_AUTH";
//
//    /**
//     * DB认证列表
//     */
//    public static final String DB_AUTH_MAIN = "DB_AUTH_MAIN";
//
//    /**
//     * DB过滤列表
//     */
//    public static final String DB_FILTER_MAIN = "DB_FILTER_MAIN";
//
//    /**
//     * 连接变更事件
//     */
//    public static final String CONNECTION_CHANGED = "CONNECTION_CHANGED";
//
//    /**
//     * DB信息新增
//     */
//    public static final String DB_INFO_ADD = "DB_INFO_ADD";
//
//    /**
//     * DB键过滤
//     */
//    public static final String DB_KEY_FILTER = "DB_KEY_FILTER";
//
//    /**
//     * DBTTL更新
//     */
//    public static final String DB_TTL_UPDATED = "DB_TTL_UPDATED";
//
//    /**
//     * DB键添加
//     */
//    public static final String DB_KEY_ADDED = "DB_KEY_ADDED";
//
//    /**
//     * DB键移动
//     */
//    public static final String DB_KEY_MOVED = "DB_KEY_MOVED";
//
//    /**
//     * DB键更名
//     */
//    public static final String DB_KEY_RENAMED = "DB_KEY_RENAMED";
//
//    /**
//     * DB键删除
//     */
//    public static final String DB_KEY_DELETED = "DB_KEY_DELETED";
//
//    /**
//     * DB键刷新
//     */
//    public static final String DB_KEY_FLUSHED = "DB_KEY_FLUSHED";
//
//    /**
//     * DB键复制
//     */
//    public static final String DB_KEY_COPY = "DB_KEY_COPY";
//
//    /**
//     * DB新增list行
//     */
//    public static final String DB_LIST_ROW_ADDED = "DB_LIST_ROW_ADDED";
//
//    /**
//     * DB新增set成员
//     */
//    public static final String DB_SET_MEMBER_ADDED = "DB_SET_MEMBER_ADDED";
//
//    // /**
//    //  * DB变更zset的显示方式
//    //  */
//    // public static final String DB_CHANGE_ZSET_SHOW_TYPE = "DB_CHANGE_ZSET_SHOW_TYPE";
//
//    /**
//     * DB新增zset成员
//     */
//    public static final String DB_ZSET_MEMBER_ADDED = "DB_ZSET_MEMBER_ADDED";
//
//    /**
//     * DB新增zset地理坐标
//     */
//    public static final String DB_ZSET_COORDINATE_ADDED = "DB_ZSET_COORDINATE_ADDED";
//
//    /**
//     * DB新增stream消息
//     */
//    public static final String DB_STREAM_MESSAGE_ADDED = "DB_STREAM_MESSAGE_ADDED";
//
//    /**
//     * DB新增hash字段
//     */
//    public static final String DB_HASH_FIELD_ADDED = "DB_HASH_FIELD_ADDED";
//
//    /**
//     * DB新增hyperLogLog元素
//     */
//    public static final String DB_HYLOG_ELEMENT_ADDED = "DB_HYLOG_ELEMENT_ADDED";
//
//    /**
//     * DB过滤配置新增
//     */
//    public static final String DB_FILTER_ADDED = "DB_FILTER_ADDED";
//
//    /**
//     * DB导入开始事件
//     */
//    public static final String DB_IMPORT_START = "DB_IMPORT_START";
//
//    /**
//     * DB导入结束事件
//     */
//    public static final String DB_IMPORT_FINISH = "DB_IMPORT_FINISH";
//
//    /**
//     * DB搜索开始事件
//     */
//    public static final String DB_SEARCH_START = "DB_SEARCH_START";
//
//    /**
//     * DB搜索结束事件
//     */
//    public static final String DB_SEARCH_FINISH = "DB_SEARCH_FINISH";
//
//    /**
//     * DB搜索历史选择事件
//     */
//    public static final String DB_SEARCH_HISTORY_SELECTED = "DB_SEARCH_HISTORY_SELECTED";
//
//    /**
//     * DB替换历史选择事件
//     */
//    public static final String DB_REPLACE_HISTORY_SELECTED = "DB_REPLACE_HISTORY_SELECTED";
//
//    /**
//     * DB过滤历史选择事件
//     */
//    public static final String DB_FILTER_HISTORY_SELECTED = "DB_REPLACE_HISTORY_SELECTED";
//
//    /**
//     * 打开终端事件
//     */
//    public static final String DB_OPEN_TERMINAL = "DB_OPEN_TERMINAL";
//
//    /**
//     * 打开表
//     */
//    public static final String DB_TABLE_OPEN = "DB_TABLE_OPEN";
//
//    /**
//     * 新建查询事件
//     */
//    public static final String DB_QUERY_ADD = "DB_QUERY_ADD";
//
//    /**
//     * 查询新增事件
//     */
//    public static final String DB_QUERY_ADDED = "DB_QUERY_ADDED";
//
//    /**
//     * 查询删除事件
//     */
//    public static final String DB_QUERY_DELETED = "DB_QUERY_DELETED";
//
//    /**
//     * 打开查询
//     */
//    public static final String DB_QUERY_OPEN = "DB_QUERY_OPEN";
//
//    /**
//     * DB终端关闭事件
//     */
//    public static final String DB_CLOSE_TERMINAL = "DB_CLOSE_TERMINAL";
//
//    /**
//     * 表重命名事件
//     */
//    public static final String TABLE_REAMED = "TABLE_REAMED";
//
//    /**
//     * 表清空事件
//     */
//    public static final String TABLE_CLEARED = "TABLE_CLEARED";
//
//    /**
//     * 表截断事件
//     */
//    public static final String TABLE_TRUNCATED = "TABLE_TRUNCATED";
//
//    /**
//     * 表删除事件
//     */
//    public static final String TABLE_DROPPED = "TABLE_DROPPED";
//
//    /**
//     * 表新增事件
//     */
//    public static final String TABLE_ADDED = "TABLE_ADDED";
//
//    /**
//     * 表修改事件
//     */
//    public static final String TABLE_UPDATED = "TABLE_UPDATED";
//
//    /**
//     * 字段新增事件
//     */
//    public static final String COLUMN_ADDED = "COLUMN_ADDED";
//
//    /**
//     * 列修改事件
//     */
//    public static final String COLUMN_UPDATED = "COLUMN_UPDATED";
//
//    /**
//     * 索引新增事件
//     */
//    public static final String INDEX_ADDED = "INDEX_ADDED";
//
//    /**
//     * 索引修改事件
//     */
//    public static final String INDEX_UPDATED = "INDEX_UPDATED";
//
//    /**
//     * 外键新增事件
//     */
//    public static final String FOREIGN_KEY_ADDED = "FOREIGN_KEY_ADDED";
//
//    /**
//     * 外键修改事件
//     */
//    public static final String FOREIGN_KEY_UPDATED = "FOREIGN_KEY_UPDATED";
//
//    /**
//     * 行新增事件
//     */
//    public static final String ROW_ADDED = "ROW_ADDED";
//
//    /**
//     * 行修改事件
//     */
//    public static final String ROW_UPDATED = "ROW_UPDATED";
//
//    /**
//     * 行删除事件
//     */
//    public static final String ROW_DELETED = "ROW_DELETED";
//
//    /**
//     * 服务信息事件
//     */
//    public static final String DB_SERVER_INFO = "DB_SERVER_INFO";
//
//    /**
//     * DB客户端关闭事件
//     */
//    public static final String DB_CLINE_CLOSED = "DB_CLINE_CLOSED";
//
//    /**
//     * 添加连接事件
//     */
//    public static final String DB_ADD_CONNECT = "DB_ADD_CONNECT";
//
//    /**
//     * 添加分组事件
//     */
//    public static final String DB_ADD_GROUP = "DB_ADD_GROUP";
//}
