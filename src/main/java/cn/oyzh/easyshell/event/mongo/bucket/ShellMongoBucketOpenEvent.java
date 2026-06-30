package cn.oyzh.easyshell.event.mongo.bucket;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoBucketOpenEvent extends Event<ShellMongoBucketTreeItem> {

    private ShellMongoDatabaseTreeItem dbItem;

    public String bucketName() {
        return this.data().bucketName();
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
