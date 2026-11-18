//package cn.oyzh.easyshell.event.oracle.view;
//
//import cn.oyzh.easyshell.oracle.record.OracleRecordFilter;
//import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
//import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewTreeItem;
//import cn.oyzh.event.Event;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class ShellOracleViewFilteredEvent extends Event<ShellOracleViewTreeItem> {
//
//    private List<OracleRecordFilter> filters;
//
//    private ShellOracleSchemaTreeItem dbItem;
//
//    public String viewName() {
//        return this.data().viewName();
//    }
//
//    public List<OracleRecordFilter> getFilters() {
//        return filters;
//    }
//
//    public void setFilters(List<OracleRecordFilter> filters) {
//        this.filters = filters;
//    }
//
//    public ShellOracleSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
//        this.dbItem = dbItem;
//    }
//}
