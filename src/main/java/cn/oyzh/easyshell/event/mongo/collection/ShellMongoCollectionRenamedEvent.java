package cn.oyzh.easyshell.event.mongo.collection;

import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoCollectionRenamedEvent extends Event<String> implements EventFormatter {

    private MongoDatabaseTreeItem dbItem;

    private String newCollectionName;

    public String getNewCollectionName() {
        return newCollectionName;
    }

    public void setNewCollectionName(String newCollectionName) {
        this.newCollectionName = newCollectionName;
    }

    public String tableName() {
        return this.data();
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
        return String.format("[%s:%s] renamed, new name:%s", I18nHelper.collection(), this.tableName(), this.getNewCollectionName());
    }

}
