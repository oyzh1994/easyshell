//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellPostgresqlDataTransportFunctionListView extends DBDataTransportObjectListView {
//
//    public void of(List<PostgresqlFunction> functions) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (PostgresqlFunction function : functions) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(function.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
