//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.oracle.table.OracleTable;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellOracleDataTransportTableListView extends DBDataTransportObjectListView {
//
//    public void of(List<OracleTable> tables) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (OracleTable table : tables) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(table.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//
//}
