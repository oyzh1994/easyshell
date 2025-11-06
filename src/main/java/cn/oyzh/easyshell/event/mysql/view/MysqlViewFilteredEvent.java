// package cn.oyzh.easyshell.event.mysql.view;
//
// import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
// import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
// import cn.oyzh.easyshell.trees.mysql.view.MysqlViewTreeItem;
// import cn.oyzh.event.Event;
//
// import java.util.List;
//
// /**
//  * @author oyzh
//  * @since 2024/06/26
//  */
// public class MysqlViewFilteredEvent extends Event<MysqlViewTreeItem> {
//
//     private List<MysqlRecordFilter> filters;
//
//     private MysqlDatabaseTreeItem dbItem;
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
//     public MysqlDatabaseTreeItem getDbItem() {
//         return dbItem;
//     }
//
//     public void setDbItem(MysqlDatabaseTreeItem dbItem) {
//         this.dbItem = dbItem;
//     }
// }
