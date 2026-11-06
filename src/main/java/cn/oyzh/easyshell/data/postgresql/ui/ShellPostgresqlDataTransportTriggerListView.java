//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellPostgresqlDataTransportTriggerListView extends DBDataTransportObjectListView {
//
//    public void of(List<PostgresqlTrigger> triggers) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (PostgresqlTrigger trigger : triggers) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(trigger.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
