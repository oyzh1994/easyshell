package cn.oyzh.easyshell.event.mongo.collection;

import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb集合已删除事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoCollectionDroppedEvent extends Event<ShellMongoCollectionTreeItem> implements EventFormatter {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String collectionName() {
        return this.data().collectionName();
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

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] dropped", I18nHelper.collection(), this.collectionName());
    }
}
