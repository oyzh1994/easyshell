//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellPostgresqlDataTransportViewListView extends DBDataTransportObjectListView {
//
//    public void of(List<PostgresqlView> views) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (PostgresqlView view : views) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(view.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
