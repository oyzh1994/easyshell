//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellPostgresqlDataTransportTableListView extends DBDataTransportObjectListView {
//
//    public void of(List<PostgresqlTable> tables) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (PostgresqlTable table : tables) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(table.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//
//}
