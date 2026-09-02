package cn.oyzh.easyshell.dameng.record;

import cn.oyzh.easyshell.dameng.column.DamengColumn;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-13
 */
public class DamengSelectRecordParam {

    private Long start;

    private Long limit;

    private String schema;

    private String tableName;

    private boolean readonly;

    private List<DamengColumn> columns;

    private List<DamengRecordFilter> filters;

    private DamengRecordPrimaryKey primaryKey;

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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
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

    public List<DamengColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DamengColumn> columns) {
        this.columns = columns;
    }

    public List<DamengRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<DamengRecordFilter> filters) {
        this.filters = filters;
    }

    public DamengRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(DamengRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
