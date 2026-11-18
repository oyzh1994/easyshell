//package cn.oyzh.easyshell.event.oracle.table;
//
//import cn.oyzh.easyshell.oracle.record.OracleRecordFilter;
//import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
//import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
//import cn.oyzh.event.Event;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class ShellOracleTableFilteredEvent extends Event<ShellOracleTableTreeItem> {
//
//    private List<OracleRecordFilter> filters;
//
//    private ShellOracleSchemaTreeItem dbItem;
//
//    public String tableName() {
//        return this.data().tableName();
//    }
//
//    public ShellOracleSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
//        this.dbItem = dbItem;
//    }
//
//    public List<OracleRecordFilter> getFilters() {
//        return filters;
//    }
//
//    public void setFilters(List<OracleRecordFilter> filters) {
//        this.filters = filters;
//    }
//}
