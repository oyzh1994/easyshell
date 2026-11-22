//package cn.oyzh.easyshell.data.sqlserver.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellSqlServerDataTransportViewListView extends DBDataTransportObjectListView {
//
//    public void of(List<SqlServerView> views) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (SqlServerView view : views) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(view.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
