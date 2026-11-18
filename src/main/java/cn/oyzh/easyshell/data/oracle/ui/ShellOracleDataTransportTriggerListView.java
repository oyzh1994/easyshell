//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellOracleDataTransportTriggerListView extends DBDataTransportObjectListView {
//
//    public void of(List<OracleTrigger> triggers) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (OracleTrigger trigger : triggers) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(trigger.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
