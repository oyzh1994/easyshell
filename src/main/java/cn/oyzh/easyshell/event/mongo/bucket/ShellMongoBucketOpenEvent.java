package cn.oyzh.easyshell.event.mongo.bucket;

import cn.oyzh.easyshell.mongo.trees.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.mongo.trees.bucket.MongoBucketTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoBucketOpenEvent extends Event<MongoBucketTreeItem> {

    private MongoDatabaseTreeItem dbItem;

    public String bucketName() {
        return this.data().bucketName();
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public MongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
