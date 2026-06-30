package cn.oyzh.easyshell.event.mongo.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.event.Event;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoQueryOpenEvent extends Event<ShellQuery> {

    private ShellMongoDatabaseTreeItem dbItem;

    public String queryId() {
        return this.data().getUid();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
