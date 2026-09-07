//package cn.oyzh.easyshell.data.dameng.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellDamengDataTransportTriggerListView extends DBDataTransportObjectListView {
//
//    public void of(List<DamengTrigger> triggers) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (DamengTrigger trigger : triggers) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(trigger.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
