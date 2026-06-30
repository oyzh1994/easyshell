package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * db数据库选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class ShellMongoCollectionComboBox extends FXComboBox<String> {

    public void init(String dbName, ShellMongoClient client) {
        this.init(dbName, null, client);
    }

    public void init(String dbName, String tableName, ShellMongoClient client) {
        List<MongoCollection> list = client.listCollections(dbName);
        this.setItem(list.parallelStream().map(MongoCollection::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        } else {
            this.clearChild();
        }
    }
}
