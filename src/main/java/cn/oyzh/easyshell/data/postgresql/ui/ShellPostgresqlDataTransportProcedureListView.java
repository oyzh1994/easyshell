//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellPostgresqlDataTransportProcedureListView extends DBDataTransportObjectListView {
//
//    public void of(List<PostgresqlProcedure> procedures) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (PostgresqlProcedure procedure : procedures) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(procedure.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
