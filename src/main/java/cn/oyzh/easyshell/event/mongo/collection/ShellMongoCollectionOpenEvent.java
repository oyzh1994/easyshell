package cn.oyzh.easyshell.event.mongo.collection;

import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoCollectionOpenEvent extends Event<ShellMongoCollectionTreeItem> {

    private ShellMongoDatabaseTreeItem dbItem;

    public String collectionName() {
        return this.data().collectionName();
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
