//package cn.oyzh.easyshell.event.sqlserver.view;
//
//import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
//import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
//import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
//import cn.oyzh.event.Event;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class ShellSqlServerViewFilteredEvent extends Event<ShellSqlServerViewTreeItem> {
//
//    private List<SqlServerRecordFilter> filters;
//
//    private ShellSqlServerSchemaTreeItem dbItem;
//
//    public String viewName() {
//        return this.data().viewName();
//    }
//
//    public List<SqlServerRecordFilter> getFilters() {
//        return filters;
//    }
//
//    public void setFilters(List<SqlServerRecordFilter> filters) {
//        this.filters = filters;
//    }
//
//    public ShellSqlServerSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    public void setDbItem(ShellSqlServerSchemaTreeItem dbItem) {
//        this.dbItem = dbItem;
//    }
//}
