package cn.oyzh.easyshell.event.mongo.bucket;

import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.bucket.MongoBucketTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMongoBucketDroppedEvent extends Event<MongoBucketTreeItem> implements EventFormatter {

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

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] dropped", I18nHelper.bucket(), this.bucketName());
    }
}
