package cn.oyzh.easyshell.sqlite.record;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;

import java.util.List;

/**
 * SQLite查询记录参数
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteSelectRecordParam {

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
    private String dbName = "main";

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
    private List<SQLiteColumn> columns;

    /**
     * 过滤条件列表
     */
    private List<SQLiteRecordFilter> filters;

    /**
     * 主键
     */
    private SQLiteRecordPrimaryKey primaryKey;

    public boolean hasPageControl() {
        return this.start != null && this.limit != null;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public List<SQLiteColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<SQLiteColumn> columns) {
        this.columns = columns;
    }

    public List<SQLiteRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SQLiteRecordFilter> filters) {
        this.filters = filters;
    }

    public SQLiteRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(SQLiteRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
