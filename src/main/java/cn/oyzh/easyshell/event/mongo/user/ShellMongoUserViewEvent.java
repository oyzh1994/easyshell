package cn.oyzh.easyshell.event.mongo.user;

import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoUserViewEvent extends Event<MongoUser> {

    private ShellMongoDatabaseTreeItem dbItem;

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
