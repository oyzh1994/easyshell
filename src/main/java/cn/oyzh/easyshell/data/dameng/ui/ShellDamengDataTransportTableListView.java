//package cn.oyzh.easyshell.data.dameng.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.dameng.table.DamengTable;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellDamengDataTransportTableListView extends DBDataTransportObjectListView {
//
//    public void of(List<DamengTable> tables) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (DamengTable table : tables) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(table.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//
//}
