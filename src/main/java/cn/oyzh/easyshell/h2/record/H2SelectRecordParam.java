package cn.oyzh.easyshell.h2.record;

import cn.oyzh.easyshell.h2.column.H2Column;

import java.util.List;

/**
 * H2查询记录参数
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2SelectRecordParam {

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
    private List<H2Column> columns;

    /**
     * 过滤条件列表
     */
    private List<H2RecordFilter> filters;

    /**
     * 主键
     */
    private H2RecordPrimaryKey primaryKey;

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

    public List<H2Column> getColumns() {
        return columns;
    }

    public void setColumns(List<H2Column> columns) {
        this.columns = columns;
    }

    public List<H2RecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<H2RecordFilter> filters) {
        this.filters = filters;
    }

    public H2RecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(H2RecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
