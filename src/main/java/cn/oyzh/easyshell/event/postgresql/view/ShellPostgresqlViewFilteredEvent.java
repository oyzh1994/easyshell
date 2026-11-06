//package cn.oyzh.easyshell.event.postgresql.view;
//
//import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
//import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
//import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
//import cn.oyzh.event.Event;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class ShellPostgresqlViewFilteredEvent extends Event<ShellPostgresqlViewTreeItem> {
//
//    private List<PostgresqlRecordFilter> filters;
//
//    private ShellPostgresqlSchemaTreeItem dbItem;
//
//    public String viewName() {
//        return this.data().viewName();
//    }
//
//    public List<PostgresqlRecordFilter> getFilters() {
//        return filters;
//    }
//
//    public void setFilters(List<PostgresqlRecordFilter> filters) {
//        this.filters = filters;
//    }
//
//    public ShellPostgresqlSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
//        this.dbItem = dbItem;
//    }
//}
