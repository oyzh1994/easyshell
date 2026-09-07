//package cn.oyzh.easyshell.data.mysql.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.mysql.event.MysqlEvent;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellMysqlDataTransportEventListView extends DBDataTransportObjectListView {
//
//    public void of(List<MysqlEvent> events) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (MysqlEvent event : events) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(event.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
