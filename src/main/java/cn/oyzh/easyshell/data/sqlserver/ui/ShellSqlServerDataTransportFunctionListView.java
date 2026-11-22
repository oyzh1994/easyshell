//package cn.oyzh.easyshell.data.sqlserver.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellSqlServerDataTransportFunctionListView extends DBDataTransportObjectListView {
//
//    public void of(List<SqlServerFunction> functions) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (SqlServerFunction function : functions) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(function.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
