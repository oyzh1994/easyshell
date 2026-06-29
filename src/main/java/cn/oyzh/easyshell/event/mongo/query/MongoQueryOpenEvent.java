package cn.oyzh.easyshell.event.mongo.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.event.Event;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class MongoQueryOpenEvent extends Event<ShellQuery> {

    private MongoDatabaseTreeItem dbItem;

    public String queryId() {
        return this.data().getUid();
    }

    public MongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
