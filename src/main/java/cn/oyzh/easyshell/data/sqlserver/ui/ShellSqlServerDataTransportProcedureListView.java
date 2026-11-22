//package cn.oyzh.easyshell.data.sqlserver.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellSqlServerDataTransportProcedureListView extends DBDataTransportObjectListView {
//
//    public void of(List<SqlServerProcedure> procedures) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (SqlServerProcedure procedure : procedures) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(procedure.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
