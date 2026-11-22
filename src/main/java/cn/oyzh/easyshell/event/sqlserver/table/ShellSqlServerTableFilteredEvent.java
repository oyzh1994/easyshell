//package cn.oyzh.easyshell.event.sqlserver.table;
//
//import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
//import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
//import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTableTreeItem;
//import cn.oyzh.event.Event;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class ShellSqlServerTableFilteredEvent extends Event<ShellSqlServerTableTreeItem> {
//
//    private List<SqlServerRecordFilter> filters;
//
//    private ShellSqlServerSchemaTreeItem dbItem;
//
//    public String tableName() {
//        return this.data().tableName();
//    }
//
//    public ShellSqlServerSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    public void setDbItem(ShellSqlServerSchemaTreeItem dbItem) {
//        this.dbItem = dbItem;
//    }
//
//    public List<SqlServerRecordFilter> getFilters() {
//        return filters;
//    }
//
//    public void setFilters(List<SqlServerRecordFilter> filters) {
//        this.filters = filters;
//    }
//}
