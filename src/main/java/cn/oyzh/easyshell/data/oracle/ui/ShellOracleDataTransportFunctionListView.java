//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.oracle.function.OracleFunction;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellOracleDataTransportFunctionListView extends DBDataTransportObjectListView {
//
//    public void of(List<OracleFunction> functions) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (OracleFunction function : functions) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(function.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
