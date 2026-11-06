//package cn.oyzh.easyshell.event.postgresql.table;
//
//import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
//import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
//import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
//import cn.oyzh.event.Event;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class ShellPostgresqlTableFilteredEvent extends Event<ShellPostgresqlTableTreeItem> {
//
//    private List<PostgresqlRecordFilter> filters;
//
//    private ShellPostgresqlSchemaTreeItem dbItem;
//
//    public String tableName() {
//        return this.data().tableName();
//    }
//
//    public ShellPostgresqlSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
//        this.dbItem = dbItem;
//    }
//
//    public List<PostgresqlRecordFilter> getFilters() {
//        return filters;
//    }
//
//    public void setFilters(List<PostgresqlRecordFilter> filters) {
//        this.filters = filters;
//    }
//}
