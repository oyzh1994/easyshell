//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.oracle.view.OracleView;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellOracleDataTransportViewListView extends DBDataTransportObjectListView {
//
//    public void of(List<OracleView> views) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (OracleView view : views) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(view.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
