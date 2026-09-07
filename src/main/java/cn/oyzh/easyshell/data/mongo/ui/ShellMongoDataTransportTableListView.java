//package cn.oyzh.easyshell.data.mongo.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.mongo.collection.MongoCollection;
//import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
//import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
//
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellMongoDataTransportTableListView extends DBDataTransportObjectListView {
//
//    public void of(List<MongoCollection> tables) {
//        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
//        for (MongoCollection table : tables) {
//            DBDataTransportObject obj = new DBDataTransportObject();
//            obj.setName(table.getName());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//}
