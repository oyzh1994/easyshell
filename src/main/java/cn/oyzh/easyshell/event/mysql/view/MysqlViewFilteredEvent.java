// package cn.oyzh.easyshell.event.mysql.view;
//
// import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
// import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
// import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
// import cn.oyzh.event.Event;
//
// import java.util.List;
//
// /**
//  * @author oyzh
//  * @since 2024/06/26
//  */
// public class MysqlViewFilteredEvent extends Event<ShellMysqlViewTreeItem> {
//
//     private List<MysqlRecordFilter> filters;
//
//     private ShellMysqlDatabaseTreeItem dbItem;
//
//     public String viewName() {
//         return this.data().viewName();
//     }
//
//     public List<MysqlRecordFilter> getFilters() {
//         return filters;
//     }
//
//     public void setFilters(List<MysqlRecordFilter> filters) {
//         this.filters = filters;
//     }
//
//     public ShellMysqlDatabaseTreeItem getDbItem() {
//         return dbItem;
//     }
//
//     public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
//         this.dbItem = dbItem;
//     }
// }
