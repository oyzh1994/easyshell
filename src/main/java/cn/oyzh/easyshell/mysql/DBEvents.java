package cn.oyzh.easyshell.mysql;// package cn.oyzh.easyshell.mysql;
//
// import lombok.experimental.UtilityClass;
//
// /**
//  * db事件
//  *
//  * @author oyzh
//  * @since 2023/06/16
//  */
// @UtilityClass
// public class DBEvents {
//
//     /**
//      * 应用退出事件
//      */
//     public static final String APP_EXIT = "APP_EXIT";
//
//     /**
//      * 连接变更事件
//      */
//     public static final String CONNECTION_CHANGED = "CONNECTION_CHANGED";
//
//     /**
//      * db信息新增
//      */
//     public static final String DB_INFO_ADD = "DB_INFO_ADD";
//
//     /**
//      * db信息修改
//      */
//     public static final String DB_INFO_UPDATED = "DB_INFO_UPDATED";
//
//     /**
//      * db键过滤
//      */
//     public static final String DB_KEY_FILTER = "DB_KEY_FILTER";
//
//     /**
//      * dbTTL更新
//      */
//     public static final String DB_TTL_UPDATED = "DB_TTL_UPDATED";
//
//     /**
//      * db键添加
//      */
//     public static final String DB_KEY_ADDED = "DB_KEY_ADDED";
//
//     /**
//      * db键移动
//      */
//     public static final String DB_KEY_MOVED = "DB_KEY_MOVED";
//
//     /**
//      * db键更名
//      */
//     public static final String DB_KEY_RENAMED = "DB_KEY_RENAMED";
//
//     /**
//      * db键删除
//      */
//     public static final String DB_KEY_DELETED = "DB_KEY_DELETED";
//
//     /**
//      * db键刷新
//      */
//     public static final String DB_KEY_FLUSH = "DB_KEY_FLUSH";
//
//     /**
//      * db键复制
//      */
//     public static final String DB_KEY_COPY = "DB_KEY_COPY";
//
//     /**
//      * db新增list行
//      */
//     public static final String DB_LIST_ROW_ADDED = "DB_LIST_ROW_ADDED";
//
//     /**
//      * db新增set成员
//      */
//     public static final String DB_SET_MEMBER_ADDED = "DB_SET_MEMBER_ADDED";
//
//     /**
//      * db变更zset的显示方式
//      */
//     public static final String DB_CHANGE_ZSET_SHOW_TYPE = "DB_CHANGE_ZSET_SHOW_TYPE";
//
//     /**
//      * db新增zset成员
//      */
//     public static final String DB_ZSET_MEMBER_ADDED = "DB_ZSET_MEMBER_ADDED";
//
//     /**
//      * db新增地理坐标
//      */
//     public static final String DB_GEO_COORDINATE_ADDED = "DB_GEO_COORDINATE_ADDED";
//
//     /**
//      * db新增stream消息
//      */
//     public static final String DB_STREAM_MESSAGE_ADDED = "DB_STREAM_MESSAGE_ADDED";
//
//     /**
//      * db新增hash字段
//      */
//     public static final String DB_HASH_FIELD_ADDED = "DB_HASH_FIELD_ADDED";
//
//     /**
//      * db新增hyperLogLog元素
//      */
//     public static final String DB_HYPER_LOG_LOG_ELEMENT_ADDED = "DB_HYPER_LOG_LOG_ELEMENT_ADDED";
//
//     /**
//      * db过滤配置新增
//      */
//     public static final String DB_FILTER_ADDED = "DB_FILTER_ADDED";
//
//     /**
//      * 展开左侧
//      */
//     public static final String LEFT_EXTEND = "LEFT_EXTEND";
//
//     /**
//      * 收缩左侧
//      */
//     public static final String LEFT_COLLAPSE = "LEFT_COLLAPSE";
//
//     /**
//      * db导入开始事件
//      */
//     public static final String DB_IMPORT_START = "DB_IMPORT_START";
//
//     /**
//      * db导入结束事件
//      */
//     public static final String DB_IMPORT_FINISH = "DB_IMPORT_FINISH";
//
//     /**
//      * db搜索开始事件
//      */
//     public static final String DB_SEARCH_START = "DB_SEARCH_START";
//
//     /**
//      * db搜索结束事件
//      */
//     public static final String DB_SEARCH_FINISH = "DB_SEARCH_FINISH";
//
//     /**
//      * db搜索历史选择事件
//      */
//     public static final String DB_SEARCH_HISTORY_SELECTED = "DB_SEARCH_HISTORY_SELECTED";
//
//     /**
//      * db替换历史选择事件
//      */
//     public static final String DB_REPLACE_HISTORY_SELECTED = "DB_REPLACE_HISTORY_SELECTED";
//
//     /**
//      * db过滤历史选择事件
//      */
//     public static final String DB_FILTER_HISTORY_SELECTED = "DB_REPLACE_HISTORY_SELECTED";
//
//     /**
//      * 打开终端事件
//      */
//     public static final String DB_OPEN_TERMINAL = "DB_OPEN_TERMINAL";
//
//     /**
//      * 打开订阅事件
//      */
//     public static final String DB_OPEN_PUBSUB = "DB_OPEN_PUBSUB";
//
//     /**
//      * 服务信息事件
//      */
//     public static final String DB_SERVER_INFO = "DB_SERVER_INFO";
//
//     /**
//      * db客户端关闭事件
//      */
//     public static final String DB_CLINE_CLOSED = "DB_CLINE_CLOSED";
//
//     /**
//      * 添加连接事件
//      */
//     public static final String DB_ADD_CONNECT = "DB_ADD_CONNECT";
//
//     /**
//      * 添加分组事件
//      */
//     public static final String DB_ADD_GROUP = "DB_ADD_GROUP";
// }
