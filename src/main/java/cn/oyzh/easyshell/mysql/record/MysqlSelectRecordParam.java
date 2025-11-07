package cn.oyzh.easyshell.mysql.record;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-13
 */
public class MysqlSelectRecordParam {

    private Long start;

    private Long limit;

    private String dbName;

    // private String schema;

    private String tableName;

    private boolean readonly;

    private List<MysqlColumn> columns;

    private List<MysqlRecordFilter> filters;

    private MysqlRecordPrimaryKey primaryKey;

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

    // public String getSchema() {
    //     return schema;
    // }
    //
    // public void setSchema(String schema) {
    //     this.schema = schema;
    // }

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

    public List<MysqlColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<MysqlColumn> columns) {
        this.columns = columns;
    }

    public List<MysqlRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MysqlRecordFilter> filters) {
        this.filters = filters;
    }

    public MysqlRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(MysqlRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
