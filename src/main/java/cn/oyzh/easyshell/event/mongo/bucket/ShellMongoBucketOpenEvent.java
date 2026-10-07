package cn.oyzh.easyshell.event.mongo.bucket;

import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mongodb桶打开事件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoBucketOpenEvent extends Event<ShellMongoBucketTreeItem> {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 获取桶名称
     *
     * @return 桶名称
     */
    public String bucketName() {
        return this.data().bucketName();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
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
