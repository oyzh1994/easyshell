// package cn.oyzh.easyshell.event.mysql.table;
//
// import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
// import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
// import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTableTreeItem;
// import cn.oyzh.event.Event;
//
// import java.util.List;
//
// /**
//  * @author oyzh
//  * @since 2024/06/26
//  */
// public class MysqlTableFilteredEvent extends Event<ShellMysqlTableTreeItem> {
//
//     private List<MysqlRecordFilter> filters;
//
//     private ShellMysqlDatabaseTreeItem dbItem;
//
//     public String tableName() {
//         return this.data().tableName();
//     }
//
//     public ShellMysqlDatabaseTreeItem getDbItem() {
//         return dbItem;
//     }
//
//     public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
//         this.dbItem = dbItem;
//     }
//
//     public List<MysqlRecordFilter> getFilters() {
//         return filters;
//     }
//
//     public void setFilters(List<MysqlRecordFilter> filters) {
//         this.filters = filters;
//     }
// }
