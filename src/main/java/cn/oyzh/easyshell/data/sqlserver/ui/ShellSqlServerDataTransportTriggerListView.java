//package cn.oyzh.easyshell.data.sqlserver.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellSqlServerDataTransportTriggerListView extends DBDataTransportObjectListView {
//
//    public void of(List<SqlServerTrigger> triggers) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (SqlServerTrigger trigger : triggers) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(trigger.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
