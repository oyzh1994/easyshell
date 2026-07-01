package cn.oyzh.easyshell.mongo.record;


import cn.oyzh.easyshell.mongo.column.MongoColumn;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-13
 */
public class MongoSelectRecordParam {

    private Long start;

    private Long limit;

    private String dbName;

    private String collectionName;

    private boolean readonly;

    private List<MongoColumn> columns;

    private List<MongoRecordFilter> filters;

    public MongoSelectRecordParam() {
    }

    public MongoSelectRecordParam(String dbName, String collectionName) {
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

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

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public List<MongoColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<MongoColumn> columns) {
        this.columns = columns;
    }

    public List<MongoRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MongoRecordFilter> filters) {
        this.filters = filters;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

}
