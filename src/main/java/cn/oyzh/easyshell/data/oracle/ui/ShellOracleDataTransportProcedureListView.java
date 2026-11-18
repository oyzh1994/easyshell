//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellOracleDataTransportProcedureListView extends DBDataTransportObjectListView {
//
//    public void of(List<OracleProcedure> procedures) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (OracleProcedure procedure : procedures) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(procedure.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
