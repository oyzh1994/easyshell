package cn.oyzh.easyshell.mysql.record;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;

import java.util.List;

/**
 * MySQL查询记录参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlSelectRecordParam {

    /**
     * 起始位置
     */
    private Long start;

    /**
     * 查询条数
     */
    private Long limit;

    /**
     * 库名称
     */
    private String dbName;

    // private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 是否只读
     */
    private boolean readonly;

    /**
     * 字段列表
     */
    private List<MysqlColumn> columns;

    /**
     * 过滤条件列表
     */
    private List<MysqlRecordFilter> filters;

    /**
     * 主键
     */
    private MysqlRecordPrimaryKey primaryKey;

    /**
     * 是否包含分页条件
     *
     * @return 结果
     */
    public boolean hasPageControl() {
        return this.start != null && this.limit != null;
    }

    /**
     * 获取起始位置
     *
     * @return 起始位置
     */
    public Long getStart() {
        return start;
    }

    /**
     * 设置起始位置
     *
     * @param start 起始位置
     */
    public void setStart(Long start) {
        this.start = start;
    }

    /**
     * 获取查询条数
     *
     * @return 查询条数
     */
    public Long getLimit() {
        return limit;
    }

    /**
     * 设置查询条数
     *
     * @param limit 查询条数
     */
    public void setLimit(Long limit) {
        this.limit = limit;
    }

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置库名称
     *
     * @param dbName 库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    // public String getSchema() {
    //     return schema;
    // }
    //
    // public void setSchema(String schema) {
    //     this.schema = schema;
    // }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名称
     *
     * @param tableName 表名称
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 是否只读
     *
     * @return 结果
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * 设置是否只读
     *
     * @param readonly 是否只读
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<MysqlColumn> getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(List<MysqlColumn> columns) {
        this.columns = columns;
    }

    /**
     * 获取过滤条件列表
     *
     * @return 过滤条件列表
     */
    public List<MysqlRecordFilter> getFilters() {
        return filters;
    }

    /**
     * 设置过滤条件列表
     *
     * @param filters 过滤条件列表
     */
    public void setFilters(List<MysqlRecordFilter> filters) {
        this.filters = filters;
    }

    /**
     * 获取主键
     *
     * @return 主键
     */
    public MysqlRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * 设置主键
     *
     * @param primaryKey 主键
     */
    public void setPrimaryKey(MysqlRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
