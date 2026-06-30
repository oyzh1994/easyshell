package cn.oyzh.easyshell.event.mongo.bucket;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMongoBucketDroppedEvent extends Event<ShellMongoBucketTreeItem> implements EventFormatter {

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

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] dropped", I18nHelper.bucket(), this.bucketName());
    }
}
