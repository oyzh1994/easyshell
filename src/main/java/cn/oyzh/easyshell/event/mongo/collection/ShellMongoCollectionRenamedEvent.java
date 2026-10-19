package cn.oyzh.easyshell.event.mongo.collection;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb集合已重命名事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoCollectionRenamedEvent extends Event<String> implements EventFormatter {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 新集合名称
     */
    private String newCollectionName;

    public String getNewCollectionName() {
        return newCollectionName;
    }

    public void setNewCollectionName(String newCollectionName) {
        this.newCollectionName = newCollectionName;
    }

    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String tableName() {
        return this.data();
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
        return String.format("[%s:%s] renamed, new name:%s", I18nHelper.collection(), this.tableName(), this.getNewCollectionName());
    }

}
